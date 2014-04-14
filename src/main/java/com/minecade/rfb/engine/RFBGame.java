package com.minecade.rfb.engine;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.minecade.engine.MapLocation;
import com.minecade.engine.enums.PlayerTagEnum;
import com.minecade.engine.utils.DragonBarUtils;
import com.minecade.engine.utils.EngineUtils;
import com.minecade.engine.utils.GhostManager;
import com.minecade.engine.utils.MinecadePortal;
import com.minecade.rfb.enums.RFBInventoryEnum;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;
import com.minecade.rfb.util.Callback;

public class RFBGame {
    
    public enum Status {
        WAITING_FOR_PLAYERS, FULL, OFFLINE;
    }
    
    private String LOBBY = "lobby1";
    private RunFromTheBeastPlugin plugin;
    private final World world;
    private final LobbyTimerTask timerTask;
    private final int lobbyCountdown;
    
    private Location lobbySpawnLocation;
    private LobbyScoreboard rfbScoreboard;
    private int maxPlayers;
    private int maxVipPlayers;
    private int countdown = -1;
    private Map<String, RFBMatch> matches;
    private Map<String, RFBPlayer> respawnInLobby;
    private RFBMatch nextMatch;
    private Queue<RFBPlayer> nextMatchPlayersQueue;
    private List<String> broadcastMessages;
    private Map<String, RFBPlayer> gamePlayers;
    private Status status = Status.OFFLINE;
    private boolean forceMatch = false;
    private GhostManager ghostManager;

    public RFBGame(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
        // Load properties from config
        this.LOBBY = (plugin.getConfig().getString("server.lobby-teleport")) != null ? plugin.getConfig().getString("server.lobby-teleport") : LOBBY;
        this.maxPlayers = plugin.getConfig().getInt("server.max-players");
        this.maxVipPlayers = plugin.getConfig().getInt("server.max-vip-players");
        this.lobbyCountdown = plugin.getConfig().getInt("match.start-countdown");
        this.countdown = this.lobbyCountdown;
        // create lobby world
        WorldCreator worldCreator = new WorldCreator(plugin.getConfig().getString("lobby.world-name"));
        worldCreator.generator(plugin.getEmptyGenerator());
        this.world = worldCreator.createWorld();
        initWorld(this.world);
        this.matches = new HashMap<String, RFBMatch>();
        // first match
        RFBMatch match = new RFBMatch(this.plugin, new RFBWorld(RFBWorldName.HaloRaceWorld));
        plugin.getPortalManager().addPortalToMatch(new MinecadePortal(match.getName(), new MapLocation(2, 28, 15), 
                new MapLocation(4, 31, 15), Material.PORTAL, this.world));
        this.matches.put(match.getName(), match);
        //second match
        RFBMatch match1 = new RFBMatch(this.plugin, new RFBWorld(RFBWorldName.DamnedTunnelsWorld));
        plugin.getPortalManager().addPortalToMatch(new MinecadePortal(match1.getName(), new MapLocation(-1, 28, 15), 
                new MapLocation(1, 31, 15), Material.PORTAL, this.world));
        this.matches.put(match1.getName(), match1);
        //third match
        RFBMatch match2 = new RFBMatch(this.plugin, new RFBWorld(RFBWorldName.LevelsWorld));
        plugin.getPortalManager().addPortalToMatch(new MinecadePortal(match2.getName(), new MapLocation(-4, 28, 15), 
                new MapLocation(-2, 31, 15), Material.PORTAL, this.world));
        this.matches.put(match2.getName(), match2);
        //four match
        RFBMatch match3 = new RFBMatch(this.plugin, new RFBWorld(RFBWorldName.IslandWorld));
        plugin.getPortalManager().addPortalToMatch(new MinecadePortal(match3.getName(), new MapLocation(-7, 28, 15), 
                new MapLocation(-5, 31, 15), Material.PORTAL, this.world));
        this.matches.put(match3.getName(), match3);
        RFBMatch match4 = new RFBMatch(this.plugin, new RFBWorld(RFBWorldName.IslandCopyWorld));
        plugin.getPortalManager().addPortalToMatch(new MinecadePortal(match4.getName(), new MapLocation(-10, 28, 15), 
                new MapLocation(-8, 31, 15), Material.PORTAL, this.world));
        this.matches.put(match4.getName(), match4);
        this.nextMatch = selectNextMatch();
        // init scoreboard
        this.rfbScoreboard = new LobbyScoreboard(this.plugin);
        // Initialize ghost manager
        this.ghostManager = new GhostManager(this.plugin, this.rfbScoreboard.getScoreboard());
        // broadcast messages list
        this.broadcastMessages = Collections.synchronizedList(new ArrayList<String>());
        // Initialize properties
        this.gamePlayers = new ConcurrentHashMap<String, RFBPlayer>(this.maxVipPlayers);
        this.respawnInLobby = new ConcurrentHashMap<String, RFBPlayer>(this.maxVipPlayers);
        
        this.nextMatchPlayersQueue = new ConcurrentLinkedQueue<RFBPlayer>();
        this.status = Status.WAITING_FOR_PLAYERS;
        // Initialize game timer
        this.timerTask = new LobbyTimerTask();
        // We have a single timer that runs forever - it will just ignore/no-op in most game statuses
        this.timerTask.runTaskTimer(plugin, 20, 20);
    }
    
    public synchronized void onPlayerQuit(final PlayerQuitEvent event) {

        String playerName = event.getPlayer().getName();
        plugin.getPassManager().removePlayer(playerName);
        // remove from main list
        RFBPlayer rfbPlayer = this.gamePlayers.remove(playerName);
        // remove dragon bar
        DragonBarUtils.removeBar(event.getPlayer());
        if (rfbPlayer != null) {
            // remove from queue if present
            nextMatchPlayersQueue.remove(rfbPlayer);
            ghostManager.removePlayer(rfbPlayer.getBukkitPlayer());

            // Get player match
            RFBMatch match = getPlayerMatch(rfbPlayer.getBukkitPlayer());

            // The player is in the lobby
            if (match == null) {
                addBroadcastMessage(String.format("%s%s %squit the game.", ChatColor.LIGHT_PURPLE, playerName,
                        ChatColor.GRAY));
            } else {
                match.playerQuit(event);
            }
        }
    }
    
    public void onPlayerJoin(Player player) {
        // clear inventory, potion effects and other properties
        EngineUtils.clearBukkitPlayer(player);
        // allow the player to stay in the lobby while we load his data from the database asynchronously
        // just make sure he can't interact with anything/anyone in the meanwhile
        player.teleport(this.getLobbySpawnLocation());
        DragonBarUtils.removeBar(player);
        // load player data asynchronously
        plugin.getPersistence().loadPlayerDataAsynchronously(player, new PlayerJoinCallback(player));
    }
    
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        // server failed to teleport player to lobby at the end of the match
        if (respawnInLobby.containsKey(event.getPlayer().getName())) {
            onPlayerJoinLobby(respawnInLobby.remove(event.getPlayer().getName()));
            event.setRespawnLocation(getLobbySpawnLocation());
            DragonBarUtils.handleRelocation(event.getPlayer(), getLobbySpawnLocation());
            return;
        }

        final RFBMatch match = getPlayerMatch(event.getPlayer());

        if (match != null) {
            match.onPlayerRespawn(event);
        } else {
            event.setRespawnLocation(getLobbySpawnLocation());
            DragonBarUtils.handleRelocation(event.getPlayer(), getLobbySpawnLocation());
        }
    }
    
    public void onPlayerDeath(final PlayerDeathEvent event) {

        final RFBPlayer rfbPlayer = getPlayer(event.getEntity());
        final RFBMatch match = getPlayerMatch(event.getEntity());

        plugin.getPassManager().removePlayer(rfbPlayer.getBukkitPlayer().getName());
        if (rfbPlayer != null && match != null) {
            match.playerDeath(event, rfbPlayer);
            // no-op if the player is in the lobby
        } else {
            // send auto respawn packet
            EngineUtils.respawnPlayer(rfbPlayer.getBukkitPlayer());
        }
    }
    
    public void onEntityDamage(final EntityDamageEvent event) {

        if (event.getEntity() instanceof Player) {
            final RFBPlayer player = getPlayer((Player) event.getEntity());
            if (null != player) {
                final RFBMatch match = getPlayerMatch(player.getBukkitPlayer());
                // delegate event to the match
                if (match != null) {
                    match.entityDamage(event);
                    return;
                }

                // back to lobby spawn if felt to the void
                if (DamageCause.VOID.equals(event.getCause())) {
                    event.getEntity().teleport(this.getLobbySpawnLocation());
                }
            }
        }
        event.setCancelled(true);
    }
    
    public void onPlayerInteract(final PlayerInteractEvent event) {

        final RFBPlayer player = getPlayer(event.getPlayer());
        if (null != player) {
            final RFBMatch match = getPlayerMatch(player.getBukkitPlayer());
            // delegate event to the match
            if (match != null) {
                match.onPlayerInteract(event);
                return;
            } else {
                if(player.getBukkitPlayer().getItemInHand().getType().equals(RFBInventoryEnum.LEAVE_COMPASS.getMaterial())){
                    Bukkit.getLogger().info(String.format("To Lobby[%s]", LOBBY));
                    EngineUtils.disconnect(player.getBukkitPlayer(), LOBBY, null);
                }
            }
        }
        event.setCancelled(true);
    }
    
    public void chatMessage(AsyncPlayerChatEvent event){
        final RFBPlayer rfbPlayer = getPlayer(event.getPlayer());
        if (null != rfbPlayer) {
            // message equals last message - cancel
            if (StringUtils.isNotBlank(rfbPlayer.getLastMessage())
                    && rfbPlayer.getLastMessage().equalsIgnoreCase(event.getMessage())) {
                event.getPlayer().sendMessage(RunFromTheBeastPlugin.getMessage("game.chat.spam"));
                event.setCancelled(true);
            }
            // keep record of the last message
            rfbPlayer.setLastMessage(event.getMessage());
            // format according to tag
            PlayerTagEnum playerTag = PlayerTagEnum.getTag(rfbPlayer.getBukkitPlayer(), rfbPlayer.getMinecadeAccount());
            
            if(!RunFromTheBeastPlugin.getMessage(String.format("rank.%s", playerTag.name().toLowerCase())).equalsIgnoreCase(String.format("rank.%s", playerTag.name().toLowerCase()))){
                event.setFormat(String.format("%s%s%%s%s: %%s", RunFromTheBeastPlugin.getMessage(String.format("rank.%s", playerTag.name().toLowerCase())), ChatColor.WHITE, ChatColor.GRAY));
            } else {
                event.setFormat(String.format("%s%s%%s%s: %%s", playerTag.getPrefix(), ChatColor.WHITE, ChatColor.GRAY));
            }
            
            // select recipient depending on location: lobby/match
            final RFBMatch match = getPlayerMatch(rfbPlayer.getBukkitPlayer());
            event.getRecipients().clear();
            if (match != null) {
                event.getRecipients().addAll(match.getWorld().getWorld().getPlayers());
            } else {
                event.getRecipients().addAll(world.getPlayers());
            }
        } else {
            event.setCancelled(true);
        }
    }
    
    public void blockBreak(BlockBreakEvent event) {
        if (EngineUtils.isOpInCreativeMode(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }
    
    public void portalEvent(EntityPortalEnterEvent event) {

        String playerName = ((Player) event.getEntity()).getName();
        RFBPlayer player = this.gamePlayers.get(playerName);
        // The player is in lobby
        if (player != null) {
            if((player.getMinecadeAccount().isVip() || player.getMinecadeAccount().isTitan() || plugin.getPersistence().isPlayerStaff(player.getBukkitPlayer()))) {
                String matchPortal = plugin.getPortalManager().playerIsInMatchPortal(player.getBukkitPlayer());
                if(matchPortal != null){
                    if(this.matches.containsKey(matchPortal)){
                        RFBMatch match = this.matches.get(matchPortal);
                        match.addSpectatorToMatch(player, match.getWorld().getSpectatorSpawnLocation());
                        this.gamePlayers.remove(playerName);
                        this.nextMatchPlayersQueue.remove(player);
                    }
                }
            } else {
                String message = RunFromTheBeastPlugin.getMessage("game.nonvip.spectator");
                if(!message.equalsIgnoreCase(player.getLastMessage())){
                    player.getBukkitPlayer().sendMessage(message);
                    player.setLastMessage(message);
                }
            }
        }
    }
    
    private void onPlayerJoinLobby(RFBPlayer rfbPlayer) {
        gamePlayers.put(rfbPlayer.getBukkitPlayer().getName(), rfbPlayer);
        nextMatchPlayersQueue.add(rfbPlayer);
        ghostManager.setGhost(rfbPlayer.getBukkitPlayer(), true);
        // clear player inventory and properties
        Player player = rfbPlayer.getBukkitPlayer();
        EngineUtils.clearBukkitPlayer(player);
        player.setAllowFlight(false);
        // add lobby items
        player.getInventory().addItem(
                RFBInventoryEnum.INSTRUCTIONS.getItemStack());
        player.getInventory().addItem(this.getPlayerStats(rfbPlayer));
        player.getInventory().addItem(RFBInventoryEnum.LEAVE_COMPASS.getItemStack());
        plugin.getPassManager().setInventory(rfbPlayer.getBukkitPlayer());
        // Assign player scoreboard
        if(rfbPlayer.getBukkitPlayer().isValid() && !rfbPlayer.getBukkitPlayer().isDead()){
            rfbScoreboard.assignTeam(rfbPlayer);
            rfbPlayer.getBukkitPlayer().setScoreboard(rfbScoreboard.getScoreboard());
        }
    }
    
    private RFBMatch selectNextMatch() {

        SortedMap<String, RFBMatch> allMatchesMap = new ConcurrentSkipListMap<String, RFBMatch>(this.matches);
        SortedMap<String, RFBMatch> subMatchesMap;
        RFBMatch result = null;
        
        subMatchesMap = this.nextMatch == null ? allMatchesMap : allMatchesMap.headMap(this.nextMatch.getName());
        //evaluate match list from nextMatch.
        for (RFBMatch match : subMatchesMap.values()) {
            if (RFBMatch.Status.WAITING_FOR_PLAYERS.equals(match.getStatus())) {
                result = match;
            }
        }
        //evaluate match list from the beginning.
        if(result == null){
            for (RFBMatch match : allMatchesMap.values()) {
                if (RFBMatch.Status.WAITING_FOR_PLAYERS.equals(match.getStatus())) {
                    result = match;
                }
            }
        }
        
        return result;
    }
    
    public String forceStartMatch() {
        forceMatch = true;
        return null;
    }
    
    private void initWorld(World world) {
        // Init lobby world
        EngineUtils.setupWorld(world);
        this.lobbySpawnLocation = EngineUtils.locationFromConfig(plugin.getConfig(), world, "lobby.spawn");
        world.setSpawnLocation(getLobbySpawnLocation().getBlockX(), getLobbySpawnLocation().getBlockY(),
                getLobbySpawnLocation().getBlockZ());
    }
    
    private ItemStack getPlayerStats(RFBPlayer player) {
        
        ItemStack stats = RFBInventoryEnum.STATS_BOOK.getItemStack();
        BookMeta statsMeta = (BookMeta) stats.getItemMeta();
  
        if(player.getPlayerModel() == null){
            return stats;
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String kdr = (double) player.getPlayerModel().getLosses() == 0 ? "0" : decimalFormat.format((double) player.getPlayerModel().getKills()
                / (double) player.getPlayerModel().getLosses());
        String timePlayed = decimalFormat.format((double) player.getPlayerModel().getTimePlayed() / (double) 86400);
        
        statsMeta.setPages(String.format(RunFromTheBeastPlugin.getMessage("game.book.stats"),player.getBukkitPlayer().getName().toUpperCase(), 
                player.getPlayerModel().getWins(), player.getPlayerModel().getKills(), player.getPlayerModel().getDeaths(), 
                player.getPlayerModel().getLosses(), player.getMinecadeAccount().getButterCoins(), kdr, timePlayed));
                stats.setItemMeta(statsMeta);
  
        return stats;
  }
    
    public Location getLobbySpawnLocation() {
        return lobbySpawnLocation.clone();
    }
    
    private RFBPlayer getPlayer(Player player) {
        return gamePlayers.get(player.getName());
    }
    
    private RFBMatch getPlayerMatch(Player player) {
        return matches.get(player.getLocation().getWorld().getName());
    }
    
    public void addBroadcastMessage(String message) {
        broadcastMessages.add(message);
    }
    
    private void tickPlayers() {
        for (Player player : world.getPlayers()) {
            RFBPlayer rfbPlayer = getPlayer(player);
            if (null == rfbPlayer) {
                return;
            }
            // broadcaste pending messages
            for (String message : broadcastMessages) {
                rfbPlayer.sendMessage(message);
            }
            updateDragonBar(rfbPlayer.getBukkitPlayer());
            // Play sounds
            if (null != nextMatch && nextMatchPlayersQueue.size() >= nextMatch.getRequiredPlayerCount()) {
                if (countdown == 1) {
                    rfbPlayer.getBukkitPlayer().playSound(rfbPlayer.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 3f, 1.5f);
                } else if (countdown <= 5) {
                    rfbPlayer.getBukkitPlayer().playSound(rfbPlayer.getBukkitPlayer().getLocation(), Sound.CLICK, 3, -3);
                }
            }
        }
        broadcastMessages.clear();
    }
    
    private void updateDragonBar(Player player) {
        if (null == nextMatch) {
            DragonBarUtils.setMessage(player, RunFromTheBeastPlugin.getMessage("game.dragonbar.server.full"), 100f);
            return;
        }
        int playersToStart = Math.max(0, nextMatch.getRequiredPlayerCount() - nextMatchPlayersQueue.size());
        if (playersToStart == 0) {
            DragonBarUtils.setMessage(player, String.format(RunFromTheBeastPlugin.getMessage("game.dragonbar.starting"), String.valueOf(Math.max(0, countdown))),
                    ((countdown * 1f) / (lobbyCountdown * 1f)) * 100f);
        } else {
            String messageDragonBar = String.format("Waiting For %s%s%s Player(s) To Start Next Match, " +
                    "Run, run, run for your life or the %sbeast %swill come to kill you, " +
                    "Use the %s%sVIP Sky Pass%s to be the Beast, Upgrade your account to " +
                    "%s%sVIP%s and use the %sportals%s to spectate any match", ChatColor.GREEN, String.valueOf(Math.max(0, playersToStart)), 
                    ChatColor.RESET, ChatColor.YELLOW, ChatColor.RESET, ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, 
                    ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.DARK_PURPLE, ChatColor.RESET);
            
            String tempMessage =  String.format(RunFromTheBeastPlugin.getMessage("game.dragonbar.waiting.players"), String.valueOf(Math.max(0, playersToStart)));
            DragonBarUtils.setMovingMessage(player, tempMessage,
                    (nextMatch.getRequiredPlayerCount() - playersToStart) * (100f/nextMatch.getRequiredPlayerCount()), 4);
        }
    }
    
    public void backToLobby(RFBPlayer rfbPlayer) {
        if (rfbPlayer.getBukkitPlayer().teleport(getLobbySpawnLocation())) {
            onPlayerJoinLobby(rfbPlayer);
            DragonBarUtils.handleRelocation(rfbPlayer.getBukkitPlayer(), getLobbySpawnLocation());
        } else {
            respawnInLobby.put(rfbPlayer.getBukkitPlayer().getName(), rfbPlayer);
            //EngineUtils.disconnect(pmsPlayer.getPlayer(), LOBBY, "Unable to join game's lobby at this time...");
        }
    }
    
    private class LobbyScoreboard {

        private final Scoreboard scoreboard;

        public LobbyScoreboard(RunFromTheBeastPlugin plugin) {
            // Creates new scoreboard
            this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            // Create teams
            // if (this.scoreboard.getTeams().isEmpty()) {
            for (PlayerTagEnum tag : PlayerTagEnum.values()) {
                if(!RunFromTheBeastPlugin.getMessage(String.format("rank.%s", tag.name().toLowerCase())).equalsIgnoreCase(String.format("rank.%s", tag.name().toLowerCase()))){
                    this.scoreboard.registerNewTeam(tag.name()).setPrefix(RunFromTheBeastPlugin.getMessage(String.format("rank.%s", tag.name().toLowerCase())));
                } else {
                    this.scoreboard.registerNewTeam(tag.name()).setPrefix(tag.getPrefix());
                }
            }
        }

        public void assignTeam(RFBPlayer rfbPlayer) {
            PlayerTagEnum playerTag = PlayerTagEnum.getTag(rfbPlayer.getBukkitPlayer(), rfbPlayer.getMinecadeAccount());
            Team team = this.scoreboard.getTeam(playerTag.name());
            team.addPlayer(Bukkit.getOfflinePlayer(rfbPlayer.getBukkitPlayer().getName()));
            
            if(!RunFromTheBeastPlugin.getMessage(String.format("rank.%s", playerTag.name().toLowerCase())).equalsIgnoreCase(String.format("rank.%s", playerTag.name().toLowerCase()))){
                team.setPrefix(RunFromTheBeastPlugin.getMessage(String.format("rank.%s", playerTag.name().toLowerCase())));
            } else {
                team.setPrefix(playerTag.getPrefix());
            }
            
        }

        public Scoreboard getScoreboard() {
            return scoreboard;
        }

    }
    
    private class LobbyTimerTask extends BukkitRunnable {

        @Override
        public void run() {
            if (null == nextMatch) {
                nextMatch = selectNextMatch();
            }

            if (null == nextMatch) {
                status = Status.FULL;
            } else {
                status = Status.WAITING_FOR_PLAYERS;
                if (forceMatch && nextMatchPlayersQueue.size() >= 2) {
                    forceMatch = false;
                    nextMatch.readyMatch(nextMatchPlayersQueue);
                    nextMatch = selectNextMatch();
                    countdown = lobbyCountdown;
                } else if (nextMatchPlayersQueue.size() >= nextMatch.getRequiredPlayerCount()) {
                    countdown--;
                    if (countdown <= 0) {
                        nextMatch.readyMatch(nextMatchPlayersQueue);
                        nextMatch = selectNextMatch();
                        countdown = lobbyCountdown;
                    }
                }
            }

            tickPlayers();
            plugin.getPersistence().createOrUpdateServerAsynchronously();
        }

    }
    
    private class PlayerJoinCallback extends Callback<RFBPlayer> {

        final Player player;

        public PlayerJoinCallback(Player player) {
            super();
            this.player = player;
        }

        @Override
        public void run() {
            switch (getState()) {
            case DONE:
                RFBPlayer rfbPlayer = getResult();
                // Check if the server needs more players or if the player is
                // VIP
                if (Bukkit.getOnlinePlayers().length <= RFBGame.this.maxPlayers
                        || ((rfbPlayer.isVip() || rfbPlayer.isTitan()) && Bukkit.getOnlinePlayers().length <= RFBGame.this.maxVipPlayers)) {
                    onPlayerJoinLobby(rfbPlayer);
                }
                // If the server is full disconnect the player.
                else {
                    EngineUtils.disconnect(rfbPlayer.getBukkitPlayer(), LOBBY, "The server is full at this time");
                    // PMSGame.this.plugin.getPersistence().updateServerStatus(ServerStatusEnum.FULL);
                }
                break;
            case ERROR:
                // log errors to console
                Bukkit.getLogger().log(getLevel(), StringUtils.join(getErrorMessages(), " - "));
                // disconnect player
                EngineUtils.disconnect(player, LOBBY, StringUtils.join(getErrorMessages(), " - "));
                // PMSGame.this.plugin.getPersistence().updateServerStatus(ServerStatusEnum.FULL);
            default:
                break;
            }
        }
    }

    /**
     * @return the ghostManager
     */
    public GhostManager getGhostManager() {
        return ghostManager;
    }

    /**
     * @return the nextMatch
     */
    public RFBMatch getNextMatch() {
        return nextMatch;
    }
}
