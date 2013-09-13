package com.minecade.rfb.engine;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.minecade.engine.MinecadeWorld;
import com.minecade.engine.enums.PlayerTagEnum;
import com.minecade.engine.task.FireworksTask;
import com.minecade.engine.utils.EngineUtils;
import com.minecade.rfb.enums.RFBInventoryEnum;
import com.minecade.rfb.enums.RFBStatusEnum;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;
import com.minecade.rfb.task.TimerTask;
import com.minecade.rfb.worlds.RFBBaseWorld;

public class RFBMatch {

    private final int time;
    private final int startCountdown;
    private final int readyCountdown;
    private final int requiredPlayers;
    private final int beastFreedomCountdown;

    private RunFromTheBeastPlugin plugin;
    private Location lobbyLocation;
    private volatile RFBStatusEnum status = RFBStatusEnum.WAITING_FOR_PLAYERS;
    private static final String LOBBY = "lobby";
    private Map<String, RFBPlayer> players;
    private int timeLeft;
    private TimerTask timerTask;
    private String winners;
    private MinecadeWorld arena;
    private RFBScoreBoard rfbScoreboard;
    private Map<String, RFBPlayer> spectators;
    private RFBPlayer beast;

    public RFBMatch(RunFromTheBeastPlugin plugin) {
        this.plugin = plugin;
        this.rfbScoreboard = new RFBScoreBoard(this.plugin);

        this.time = plugin.getConfig().getInt("match.time");
        this.startCountdown = plugin.getConfig().getInt("match.start-countdown");
        this.readyCountdown = plugin.getConfig().getInt("match.ready-countdown");
        this.requiredPlayers = plugin.getConfig().getInt("match.required-players");
        this.beastFreedomCountdown = plugin.getConfig().getInt("match.beast-countdown");

        this.players = new ConcurrentHashMap<String, RFBPlayer>(this.requiredPlayers);
        this.spectators = new ConcurrentHashMap<String, RFBPlayer>();
    }

    public void initWorld(WorldInitEvent event) {
        World world = event.getWorld();

        // World can't be empty
        if (world == null) {
            this.plugin.getServer().getLogger().severe("RFBMatch initWorld: world parameter is null");
            return;
        }

        // if the map is the lobby
        if (plugin.getConfig().getString("match.lobby-world-name").equalsIgnoreCase(world.getName())) {
            this.lobbyLocation = EngineUtils.locationFromConfig(this.plugin.getConfig(), world, "lobby.spawn");
            world.setSpawnLocation(this.lobbyLocation.getBlockX(), this.lobbyLocation.getBlockY(), this.lobbyLocation.getBlockZ());
        }
        
        this.rfbScoreboard.init();
    }

    /**
     * Call when player join the match
     * 
     * @param PlayerJoinEvent
     * @author jdgil
     */
    public synchronized void playerJoin(PlayerJoinEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        bukkitPlayer.setAllowFlight(false);

        // Player banned
        if (this.plugin.getPersistence().isPlayerBanned(bukkitPlayer.getName())) {
            bukkitPlayer.kickPlayer(plugin.getConfig().getString("match.ban-message"));
            return;
        }

        final RFBPlayer player = new RFBPlayer(this.plugin, bukkitPlayer);

        switch (this.status) {

        case RESTARTING:
            this.teleportToLobby(bukkitPlayer, null);
            return;
        case WAITING_FOR_PLAYERS:
            EngineUtils.clearBukkitPlayer(bukkitPlayer);
            this.setupInventoryToMatch(player);

            // Setup scoreboard
            this.rfbScoreboard.assignTeam(player);
            bukkitPlayer.setScoreboard(this.rfbScoreboard.getScoreboard());
            // add player
            this.players.put(bukkitPlayer.getName(), player);
            this.rfbScoreboard.setMatchPlayers(this.requiredPlayers - this.players.size(), false);

            int playersRemaining = requiredPlayers - this.players.size();

            if (playersRemaining <= 0) {
                // Update server status
                this.status = this.changeRFBStatus(RFBStatusEnum.STARTING_MATCH);
                plugin.getPersistence().updateServerStatus(this.status);

                // Begin match start timer
                this.timeLeft = this.timeLeft == 0 ? this.startCountdown : timeLeft;
                if (null != this.timerTask) {
                    this.timerTask.cancel();
                }
                this.timerTask = new TimerTask(this, this.timeLeft, true, false, false, false);
                this.timerTask.runTaskTimer(this.plugin, 1l, 20l);
                this.broadcastMessage(String.format("%s[%s] %splayers reached, the match will begin soon", ChatColor.RED, this.players.size(),
                        ChatColor.DARK_GRAY));

                this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Select optional beasts from VIP list.
                        for (RFBPlayer optionalBeast : RFBMatch.this.players.values()) {
                            if (RFBMatch.this.isBeastVipDialyPassEnable(optionalBeast)) {
                                optionalBeast.getBukkitPlayer().sendMessage(
                                        String.format("%sYou have enabled your Daily Beast %sVIP %sPass", ChatColor.DARK_GRAY, ChatColor.RED,
                                                ChatColor.DARK_GRAY));
                                optionalBeast.getBukkitPlayer().sendMessage(
                                        String.format("%sYou have a higher chance of becoming the beast!", ChatColor.DARK_GRAY));
                            }
                        }
                    }
                });
                
            }
            break;
        case STARTING_MATCH:
          //add spectators
            if(player.getPlayerModel().isVip() || plugin.getPersistence().isPlayerStaff(bukkitPlayer)){
                this.addSpectatorToMatch(player, this.lobbyLocation);
            } else {
                this.teleportToLobby(bukkitPlayer, plugin.getConfig().getString("server.full-message"));
            }
            return;
        case ALL_WAITING:
          //add spectators
            this.plugin.getServer().getLogger().severe(String.format("spectator case: %s", player.getPlayerModel().isVip()));
            if(player.getPlayerModel().isVip() || plugin.getPersistence().isPlayerStaff(bukkitPlayer)){
                this.addSpectatorToMatch(player, this.arena.getRandomSpawn());
            } else {
                this.teleportToLobby(bukkitPlayer, plugin.getConfig().getString("server.full-message"));
            }
            return;
        case BEAST_WAITING:
        case IN_PROGRESS:
            //add spectators
            if(player.getPlayerModel().isVip() || plugin.getPersistence().isPlayerStaff(bukkitPlayer)){
                this.addSpectatorToMatch(player, ((RFBBaseWorld) this.arena).getSpectatorSpawnLocation());
            } else {
                this.teleportToLobby(bukkitPlayer, plugin.getConfig().getString("server.full-message"));
            }
            return;
        }
        
        // Teleport to match location
        bukkitPlayer.teleport(this.arena == null ? this.lobbyLocation : this.arena.getRandomSpawn());
    }
    
    private void addSpectatorToMatch(RFBPlayer player, Location respawnLocation){
        this.spectators.put(player.getBukkitPlayer().getName(), player);
        player.getBukkitPlayer().setAllowFlight(true);
        player.getBukkitPlayer().teleport(respawnLocation);
        EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
        this.hidePlayer(player.getBukkitPlayer());
        player.getBukkitPlayer().setCanPickupItems(false);
        player.getBukkitPlayer().getInventory().addItem(RFBInventoryEnum.LEAVE_COMPASS.getItemStack());
        this.broadcastMessageToSpectators(String.format("%s%sYou are a spectating the match now!", ChatColor.DARK_PURPLE, ChatColor.BOLD));
    }
    
    private RFBStatusEnum changeRFBStatus(RFBStatusEnum status){
        RFBStatusEnum newStatus = status;
        switch(status){
        case RESTARTING:
            if(this.status == RFBStatusEnum.IN_PROGRESS){
                newStatus = RFBStatusEnum.RESTARTING;
            }
            break;
        case WAITING_FOR_PLAYERS:
            //initial status, set when create the RFBMatch
            if(this.status == RFBStatusEnum.STARTING_MATCH){
                newStatus = RFBStatusEnum.WAITING_FOR_PLAYERS;
            }
            break;
        case STARTING_MATCH:
            if(this.status == RFBStatusEnum.WAITING_FOR_PLAYERS){
                newStatus = RFBStatusEnum.STARTING_MATCH;
            }
            break;
        case ALL_WAITING:
            if(this.status == RFBStatusEnum.STARTING_MATCH){
                newStatus = RFBStatusEnum.ALL_WAITING;
            }
            break;
        case BEAST_WAITING:
            if(this.status == RFBStatusEnum.ALL_WAITING){
                newStatus = RFBStatusEnum.BEAST_WAITING;
            }
            break;
        case IN_PROGRESS:
            if(this.status == RFBStatusEnum.BEAST_WAITING){
                newStatus = RFBStatusEnum.IN_PROGRESS;
            }
            break;
        }
        return newStatus;
    }

    /**
     * Initiate match
     * 
     * @author jdgil
     */
    public synchronized void initMatch() {
        this.arena = plugin.getRandomWorld();
        this.status = this.changeRFBStatus(RFBStatusEnum.ALL_WAITING);

        synchronized (this.players) {

            // Set beast.
            beast = this.selectBeast(this.players.values());
            beast.getBukkitPlayer().sendMessage(
                    String.format("%sYou are the %sBEAST%s, pickup the items in the chest!", ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.DARK_GRAY));
            beast.getBukkitPlayer().teleport(((RFBBaseWorld) this.arena).getBeastSpawnLocation());

            //teleport players to jail
            for (RFBPlayer player : this.players.values()) {
                player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
                EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
                if (!player.getBukkitPlayer().getName().equals(beast.getBukkitPlayer().getName())) {
                    player.getBukkitPlayer().teleport(this.arena.getRandomSpawn());
                }
            }
            //teleport spectators to jail
            for (RFBPlayer spectator : this.spectators.values()) {
                spectator.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
                if (!spectator.getBukkitPlayer().getName().equals(beast.getBukkitPlayer().getName())) {
                    spectator.getBukkitPlayer().teleport(this.arena.getRandomSpawn());
                }
            }
        }
        this.broadcastMessageToRunners(String.format("%sRunners, get ready!", ChatColor.DARK_GRAY));
        this.broadcastMessageToRunners(String.format("%sYou will be free in %s[%s]", ChatColor.DARK_GRAY, ChatColor.RED, this.readyCountdown));
        
        // Create the task for the count down, freedom for runners
        if (null != this.timerTask) {
            this.timerTask.cancel();
        }
        this.timerTask = new TimerTask(this, this.readyCountdown, false, true, false, false);
        this.timerTask.runTaskTimer(this.plugin, 1l, 20l);
        
        // Update scoreboard
        this.rfbScoreboard.init();
        this.rfbScoreboard.setMatchPlayers(this.players.size(), true);
    }

    private RFBPlayer selectBeast(Collection<RFBPlayer> players){
        final RFBPlayer beastSelected;
        Collection<RFBPlayer> vipPlayers = new ArrayList<RFBPlayer>();
        for (RFBPlayer player : players) {
            if (player.getPlayerModel().isVip() && this.isBeastVipDialyPassEnable(player))
                vipPlayers.add(player);
        }

        if(vipPlayers.size() > 0){
            beastSelected =  (RFBPlayer) vipPlayers.toArray()[plugin.getRandom().nextInt(vipPlayers.size())];
            beastSelected.getPlayerModel().setBeastPass(DateUtils.truncate(new Date(), Calendar.DATE));
            this.plugin.getPersistence().updatePlayer(beastSelected.getPlayerModel());
            
            beastSelected.getBukkitPlayer().sendMessage(String.format("%sSystem has used your Dialy Beast %sVIP %sPass", ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.DARK_GRAY));
        } else {
            beastSelected = (RFBPlayer) players.toArray()[plugin.getRandom().nextInt(players.size())];
        }

        // non-critical scoreboard code, put it inside a task so if it fails, it won't stop critical code.
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                //set up scoreboard for the beast
                RFBMatch.this.rfbScoreboard.assignBeast(beastSelected);
                beastSelected.getBukkitPlayer().setScoreboard(RFBMatch.this.rfbScoreboard.getScoreboard());
            }
        });
        return beastSelected;
    }

    private boolean isBeastVipDialyPassEnable(RFBPlayer player) {
        if (player.getPlayerModel().isVip()) {

            if (player.getPlayerModel().getBeastPass() == null) {
                player.getPlayerModel().setBeastPass(new Date(0l));
                this.plugin.getPersistence().updatePlayer(player.getPlayerModel());
            }

            DateFormat dateFormatPass = new SimpleDateFormat("yyyy/MM/dd");
            DateFormat dateFormatToday = new SimpleDateFormat("yyyy/MM/dd");
            Date lastTimeBeast = player.getPlayerModel().getBeastPass();
            dateFormatPass.format(lastTimeBeast);
            dateFormatToday.format(new Date());
            if (!((dateFormatPass.format(lastTimeBeast)).compareTo(dateFormatToday.format(new Date())) == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start match
     * 
     * @author kvnamo
     */
    public synchronized void startMatch() {
        // Update server status
        this.status = this.changeRFBStatus(RFBStatusEnum.BEAST_WAITING);
        plugin.getPersistence().updateServerStatus(this.status);

        this.timeLeft = this.time;
        if (null != this.timerTask) {
            this.timerTask.cancel();
        }
        this.timerTask = new TimerTask(this, this.timeLeft, false, false, true, false);
        this.timerTask.runTaskTimer(plugin, 1l, 20l);

        this.verifyGameOver();
        // Free the runners now
        this.broadcastMessageToRunners(String.format("%sBegin to run now, Beast will be free in %s%s %sseconds", ChatColor.DARK_GRAY, ChatColor.RED,
                this.beastFreedomCountdown, ChatColor.DARK_GRAY));
        for (RFBPlayer player : this.players.values()) {
            if (!player.getBukkitPlayer().getName().equals(beast.getBukkitPlayer().getName())) {
                player.getBukkitPlayer().teleport(((RFBBaseWorld) this.arena).getFreeRunnersRandomSpawn());
            }
        }
        this.broadcastMessageToSpectators(String.format("%sYou are specting now, spectate with runners!!", ChatColor.GREEN));
        // Free the spectators now
        for (RFBPlayer spectator : this.spectators.values()) {
            if (!spectator.getBukkitPlayer().getName().equals(beast.getBukkitPlayer().getName())) {
                spectator.getBukkitPlayer().teleport(((RFBBaseWorld) this.arena).getSpectatorSpawnLocation());
            }
        }
        this.broadcastMessageToBeast(String.format("%sBe prepared, get your weapon and armor!", ChatColor.DARK_GRAY));
        this.broadcastMessageToBeast(String.format("%sYou will be free in %s[%s] %sseconds", ChatColor.DARK_GRAY, ChatColor.RED, this.beastFreedomCountdown,
                ChatColor.DARK_GRAY));
        
        //set speed2 to the beast
        this.beast.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (this.beastFreedomCountdown + 15) * 20, 2));
        // Free the beast task
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                beast.getBukkitPlayer().teleport(((RFBBaseWorld) RFBMatch.this.arena).getFreeRunnersRandomSpawn());
                RFBMatch.this.status = RFBMatch.this.changeRFBStatus(RFBStatusEnum.IN_PROGRESS);
            }
        }, this.beastFreedomCountdown * 20);
    }
    
    /**
     *  Force match start
     * @return error
     * @author kvnamo 
     */
    public String forceStartMatch() {
        // Check match status
        if(this.status.equals(RFBStatusEnum.WAITING_FOR_PLAYERS)){
            // Init match
            this.initMatch();
            // Cancel init match timer
            if(this.timerTask != null){
                this.timerTask.cancel();
            }
            // Start match immediatly
            this.startMatch();
            
            return null;
        }
        
        return "Server must be in Waiting For Players status to execute this command.";
    }

    /**
     * Stop the game
     * 
     * @author kvnamo
     */
    public void stopGame() {
        // Update server status
        this.status = this.changeRFBStatus(RFBStatusEnum.RESTARTING);
        plugin.getPersistence().updateServerStatus(this.status);

        // Send players and spectators to the lobby
        final Player[] onlinePlayers = this.plugin.getServer().getOnlinePlayers();

        for (Player player : onlinePlayers) {
            EngineUtils.disconnect(player.getPlayer(), LOBBY,
                    String.format("Thanks for playing! Winners: %s%s%s", ChatColor.BOLD, ChatColor.YELLOW, this.winners == null ? "None" : this.winners));
        }

        // Stop the game
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                RFBMatch.this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
            }
        }, 5 * 20);
    }

    /**
     * Game over
     * 
     * @author kvnamo
     */
    public synchronized void verifyGameOver() {
        // Finish the game if there is only one player or the match timer is
        // cero
        this.plugin.getServer().getLogger().severe(String.format("verifyGameOver.timeleft: %s", this.timeLeft));
        if (this.players.size() <= 1 || this.timeLeft <= 0) {
            // Save winners stats in database
            synchronized (this.players) {
                for (RFBPlayer player : this.players.values()) {
                    if (this.timeLeft != 0) {
                        // Save player stats
                        player.getPlayerModel().setWins(player.getPlayerModel().getWins() + 1);
                        player.getPlayerModel().setButterCoins(player.getPlayerModel().getButterCoins() + 1);
                        player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                        this.plugin.getPersistence().updatePlayer(player.getPlayerModel());

                        // Get winners
                        this.winners = StringUtils.isBlank(this.winners) ? player.getBukkitPlayer().getName() : this.winners + ", "
                                + player.getBukkitPlayer().getName();

                        // Throw fireworks for winner
                        new FireworksTask(player.getBukkitPlayer(), 10).runTaskTimer(this.plugin, 1l, 20l);
                        
                    } else {
                        player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                        player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                        this.plugin.getPersistence().updatePlayer(player.getPlayerModel());
                        player.getBukkitPlayer().sendMessage(String.format("%sTime is out, you lost the game!", ChatColor.RED));
                    }
                }
                
                this.broadcastMessage(String.format("Thanks for playing! Winners: %s%s%s", ChatColor.BOLD, 
                        ChatColor.YELLOW, this.winners == null ? "None" : this.winners));
                // Start finish timer
                this.timerTask.cancel();
                this.timeLeft = 10;
                new TimerTask(this, this.timeLeft, false, false, false, true).runTaskTimer(this.plugin, 11, 20l);
            }
        }
    }

    /**
     * Hide spectator from other players
     * 
     * @param spectator
     * @author jdgil
     */
    private void hidePlayer(Player bukkitPlayer) {
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().hidePlayer(bukkitPlayer);
        }

//        for (RFBPlayer player : this.spectators.values()) {
//            player.getBukkitPlayer().hidePlayer(bukkitPlayer);
//        }
    }

    /**
     * Update the time left to start the match.
     * 
     * @param timeLeft
     * @author jdgil
     */
    public void setRemainingTime(int timeLeft) {
        this.timeLeft = timeLeft;
        this.rfbScoreboard.setTimeLeft(timeLeft);

        for (RFBPlayer player : this.players.values()) {
            if ((RFBStatusEnum.STARTING_MATCH.equals(this.status) || RFBStatusEnum.ALL_WAITING.equals(this.status) 
                    || RFBStatusEnum.BEAST_WAITING.equals(this.status)) && timeLeft < 6) {
                player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.CLICK, 3, -3);
            }
        }
    }

    /**
     * Broadcast message to every player in match
     * 
     * @param message
     * @author jdgil
     */
    private void broadcastMessage(String message) {
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().sendMessage(message);
        }
    }

    /**
     * Broadcast message only to runners in match
     * 
     * @param message
     * @author jdgil
     */
    private void broadcastMessageToRunners(String message) {
        for (RFBPlayer player : this.players.values()) {
            if (!(player.getBukkitPlayer().getName().equalsIgnoreCase(beast.getBukkitPlayer().getName()))) {
                player.getBukkitPlayer().sendMessage(message);
            }
        }
    }

    /**
     * Broadcast message only to the beast in match
     * 
     * @param message
     * @author jdgil
     */
    private void broadcastMessageToBeast(String message) {
        for (RFBPlayer player : this.players.values()) {
            if (player.getBukkitPlayer().getName().equalsIgnoreCase(beast.getBukkitPlayer().getName())) {
                player.getBukkitPlayer().sendMessage(message);
            }
        }
    }
    
    /**
     * Broadcast message only to spectators
     * 
     * @param message
     * @author jdgil
     */
    private void broadcastMessageToSpectators(String message) {
        for (RFBPlayer player : this.spectators.values()) {
                player.getBukkitPlayer().sendMessage(message);
        }
    }

    /**
     * Call when a block breaks.
     * 
     * @param event
     * @author jdgil
     */
    public void blockBreak(BlockBreakEvent event) {
        if (EngineUtils.isOpInCreativeMode(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * Call when a entity damager
     * 
     * @param EntityDamageEvent
     *            .
     * @author jdgil
     */
    public synchronized void playerDeath(PlayerDeathEvent event) {
        final Player bukkitPlayer = (Player) event.getEntity();
        
        if (this.spectators.containsKey(bukkitPlayer)){
            this.spectators.remove(bukkitPlayer.getName());
            return;
        }
        switch (this.status) {
        case IN_PROGRESS:
            final RFBPlayer player = this.players.remove(bukkitPlayer.getName());
            if (player != null) {
                // if death player is the beast
                if (player.getBukkitPlayer().getName().equalsIgnoreCase(beast.getBukkitPlayer().getName())) {
                    synchronized (this.players) {
                        for (RFBPlayer playerMatch : this.players.values()) {
                            // Save Stats for winners: Runners
                            playerMatch.getPlayerModel().setWins(playerMatch.getPlayerModel().getWins() + 1);
                            player.getPlayerModel().setButterCoins(player.getPlayerModel().getButterCoins() + 1);
                            playerMatch.getPlayerModel().setTimePlayed(playerMatch.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                            this.plugin.getPersistence().updatePlayer(playerMatch.getPlayerModel());

                            // Get winners: All runners alive.
                            this.winners = StringUtils.isBlank(this.winners) ? playerMatch.getBukkitPlayer().getName() : this.winners + ", "
                                    + playerMatch.getBukkitPlayer().getName();

                            // Throw fireworks for winner
                            new FireworksTask(playerMatch.getBukkitPlayer(), 10).runTaskTimer(this.plugin, 1l, 20l);
                        }

                        // Save stats in database for the loser: Beast.
                        beast.getPlayerModel().setLosses(beast.getPlayerModel().getLosses() + 1);
                        beast.getPlayerModel().setDeaths(beast.getPlayerModel().getDeaths() + 1);
                        beast.getPlayerModel().setTimePlayed(beast.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                        this.plugin.getPersistence().updatePlayer(beast.getPlayerModel());
                    }

                    this.broadcastMessage(String.format("%sThanks for playing! Winners: %s%s%s", ChatColor.RED, 
                            ChatColor.YELLOW,  ChatColor.BOLD, this.winners == null ? "None" : this.winners));
                    this.timerTask.cancel();
                    this.timeLeft = 10;
                    new TimerTask(this, this.timeLeft, false, false, false, true).runTaskTimer(this.plugin, 11, 20l);
                } else {
                    // Death is a runner
                    // Remove dead player from the players list and add him to
                    // spectators list
                    //this.players.remove(player.getBukkitPlayer().getName());

                    // Save stats in database
                    player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                    player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                    this.plugin.getPersistence().updatePlayer(player.getPlayerModel());
                    player.getBukkitPlayer().sendMessage(String.format("The beast has killed you, thanks for playing!"));
                    //EngineUtils.disconnect(player.getBukkitPlayer(), LOBBY, String.format("The beast has killed you, thanks for playing!"));
                    this.broadcastMessage(String.format("%s[%s] %slost.", ChatColor.RED, player.getBukkitPlayer().getName(), ChatColor.DARK_GRAY));

                    this.rfbScoreboard.setMatchPlayers(this.players.size(), true);
                    //add player death as a espectator
                    this.addSpectatorToMatch(player, ((RFBBaseWorld) this.arena).getSpectatorSpawnLocation());
                    this.verifyGameOver();
                }
                // Check if it is a kill
                RFBPlayer killer = player.getLastDamageBy() != null ? this.players.get(player.getLastDamageBy()) : null;

                if (killer != null) {
                    player.setLastDamageBy(null);
                    killer.getPlayerModel().setKills(killer.getPlayerModel().getKills() + 1);
                    // Announce kill
                    this.broadcastMessage(String.format("%s%s %skilled by %s%s", ChatColor.RED, bukkitPlayer.getName(), ChatColor.DARK_GRAY, ChatColor.RED,
                            killer.getBukkitPlayer().getName()));
                }
            }
            break;
        case RESTARTING:
            bukkitPlayer.kickPlayer("Server stop.");
            break;
        case STARTING_MATCH:
        case WAITING_FOR_PLAYERS:
            bukkitPlayer.teleport(lobbyLocation);
            break;
        default:
            break;
        }
    }

    /**
     * When player exits or gets kicked out of the match
     * 
     * @param PlayerQuitEvent
     *            .
     * @author: jdgil
     */
    public synchronized void playerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();

        //if player is spectator just remove and return, dont do anything else
        if (this.spectators.containsKey(playerName)){
            this.spectators.remove(playerName);
            return;
        }
        
        final RFBPlayer player = this.players.get(playerName);
        if (player!= null) {
            this.players.remove(playerName);
            switch (this.status) {
            case STARTING_MATCH:
                // Check if starting players number is reached
                int playersRemaining = requiredPlayers - this.players.size();
                if (playersRemaining > 0) {
                    // Cancel begin timer task
                    this.timerTask.cancel();

                    // Update server status
                    this.status = this.changeRFBStatus(RFBStatusEnum.WAITING_FOR_PLAYERS);
                    plugin.getPersistence().updateServerStatus(this.status);

                    // Update scoreboard
                    this.rfbScoreboard.setMatchPlayers(this.requiredPlayers - this.players.size(), false);
                }
                break;
            case ALL_WAITING:
            case BEAST_WAITING:
            case IN_PROGRESS:
                // Save player stats
                player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                this.plugin.getPersistence().updatePlayer(player.getPlayerModel());

                this.broadcastMessage(String.format("%s[%s] %squit the game", ChatColor.RED, playerName, ChatColor.DARK_GRAY));
                
                if(this.beast != null && playerName.equalsIgnoreCase(this.beast.getBukkitPlayer().getName())){
                    this.broadcastMessageToRunners(String.format("%sBEAST%s quit the game, the match finished!", ChatColor.RED, ChatColor.DARK_GRAY));
                    
                    //task to stop the match
                    this.timerTask.cancel();
                    this.timeLeft = 10;
                    new TimerTask(this, this.timeLeft, false, false, false, true).runTaskTimer(this.plugin, 11, 20l);
                } else {
                    this.verifyGameOver();
                }

                // Update scoreboard
                this.rfbScoreboard.setMatchPlayers(this.players.size(), true);
                break;
            default:
                break;
            }
        }
    }
    
    public void playerToggleFlight(PlayerToggleFlightEvent  event){
        Player bukkitPlayer = event.getPlayer();
        if(bukkitPlayer != null && this.players.containsKey(bukkitPlayer.getName())){
            event.setCancelled(true);
        }
    }
    
    public void entityShootBowEvent(EntityShootBowEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player)event.getEntity();
            if(player != null && this.spectators.containsKey(player.getName())){
                event.setCancelled(true);
            }
        }
    }
    
    public void inventoryOpenEvent(InventoryOpenEvent event){
       if(event.getPlayer() instanceof Player){
           Player bukkitPlayer =  (Player)event.getPlayer();
           if(bukkitPlayer != null && this.spectators.containsKey(bukkitPlayer.getName())){
               event.setCancelled(true);
           }
       }
    }
    /**
     * Show player.
     * 
     * @param bukkitPlayer
     *            the bukkit player
     */
//    private void showPlayer(Player bukkitPlayer) {
//        for (RFBPlayer player : this.players.values()) {
//            player.getBukkitPlayer().showPlayer(bukkitPlayer);
//        }
//
//        for (RFBPlayer player : this.spectators.values()) {
//            player.getBukkitPlayer().showPlayer(bukkitPlayer);
//        }
//    }

    /**
     * Required starting players
     * 
     * @author kvnamo
     */
//    private boolean startingPlayers() {
//
//        if (this.players.size() < this.requiredPlayers) {
//            synchronized (this.players) {
//                for (RFBPlayer player : this.spectators.values()) {
//                    // Add spectator to players list
//                    this.players.put(player.getBukkitPlayer().getName(), player);
//                    this.spectators.remove(player.getBukkitPlayer().getName());
//
//                    // Show player
//                    this.showPlayer(player.getBukkitPlayer());
//
//                    // Send player to spawn location
//                    player.getBukkitPlayer().setFlying(false);
//                    player.getBukkitPlayer().teleport(this.arena == null ? lobbyLocation : this.arena.getRandomSpawn());
//                    player.getBukkitPlayer().sendMessage(String.format("%sThe game has started!", ChatColor.YELLOW));
//
//                    // Check if more spectators are needed
//                    if (this.players.size() >= this.requiredPlayers)
//                        return true;
//                }
//            }
//        }
//
//        return false;
//    }

    /**
     * Call when a entity damager
     * 
     * @param EntityDamageEvent
     *            .
     * @author kvnamo
     */
    public void entityDamage(EntityDamageEvent event) {
        Player bukkitPlayer;
        // the entity damaged was a player
        if (event.getEntity() instanceof Player) {            
            bukkitPlayer = (Player) event.getEntity();
            //dont damage to spectators
            if(this.spectators.containsKey(bukkitPlayer.getName())){
                event.setCancelled(true);
            }
            switch (event.getCause()) {
            // the cause was a fall to void
            case VOID:
                switch (this.status) {
                case IN_PROGRESS:
                    final RFBPlayer player = this.players.get(bukkitPlayer.getName());
                    if (player != null) {
                        // Remove dead player from the players list and add him
                        // to spectators list
                        this.players.remove(bukkitPlayer.getName());
                        this.hidePlayer(bukkitPlayer);
                        this.spectators.put(bukkitPlayer.getName(), player);

                        // Save stats in database
                        player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                        player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                        this.plugin.getPersistence().updatePlayer(player.getPlayerModel());

                        bukkitPlayer.teleport(this.arena.getRandomSpawn());
                        bukkitPlayer.sendMessage(String.format("%s You are now spectating the game.", ChatColor.YELLOW));
                        this.broadcastMessage(String.format("%s[%s] %slost.", ChatColor.RED, bukkitPlayer.getName(), ChatColor.GRAY));

                        this.rfbScoreboard.setMatchPlayers(this.players.size(), true);
                        this.verifyGameOver();
                    }
                    break;
                case RESTARTING:
                    bukkitPlayer.kickPlayer("Server stop.");
                    break;
                case STARTING_MATCH:
                case WAITING_FOR_PLAYERS:
                    bukkitPlayer.teleport(lobbyLocation);
                    break;
                default:
                    break;
                }
                break;
            case FALL:
                final RFBPlayer player = this.players.get(bukkitPlayer.getName());
                if (player != null) {
                    event.setCancelled(true);
                }
                break;
            case ENTITY_ATTACK:
                Player bukkitDamager = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                RFBPlayer attackVictim = this.players.get(bukkitPlayer.getName());
                RFBPlayer attackDamager = this.players.get(bukkitDamager.getName());

                // damage was caused for other kind of entity.
                if (attackDamager == null || attackVictim == null) {
                    event.setCancelled(true);
                    return;
                }

                if (!(attackVictim.getBukkitPlayer().getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName()))
                        && !(attackDamager.getBukkitPlayer().getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName()))) {
                    event.setCancelled(true);
                } else {
                    attackVictim.setLastDamageBy(attackDamager.getBukkitPlayer().getName());
                }
                break;
            case FIRE_TICK:
                if(this.arena == null || this.status != RFBStatusEnum.IN_PROGRESS){
                    event.setCancelled(true);
                }
                    
                break;
            default:
                break;

            }
        }
    }
    
    public void playerRespawn(PlayerRespawnEvent event){
        final Player bukkitPlayer = event.getPlayer();
        EngineUtils.clearBukkitPlayer(bukkitPlayer);
        if (this.lobbyLocation != null)
            event.setRespawnLocation(this.lobbyLocation);
        
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                EngineUtils.disconnect(bukkitPlayer, LOBBY, null);
            }
        });

    }

    /**
     * Gets inventory player ready for match
     * 
     * @param message
     * @author jdgil
     */
    private void setupInventoryToMatch(RFBPlayer player) {
        EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
        player.getBukkitPlayer().getInventory().addItem(RFBInventoryEnum.INSTRUCTIONS.getItemStack());
        player.getBukkitPlayer().getInventory().addItem(this.getPlayerStats(player));
        player.getBukkitPlayer().getInventory().addItem(RFBInventoryEnum.LEAVE_COMPASS.getItemStack());
    }
    

    private void teleportToLobby(Player player, String message){
        final Player bukkitPlayer = player;
        final String msg = message;
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                EngineUtils.disconnect(bukkitPlayer, LOBBY, msg);
            }
        });
    }

    /**
     * Loads the player stats
     * 
     * @author jdgil
     */
    private ItemStack getPlayerStats(RFBPlayer player) {

        ItemStack stats = RFBInventoryEnum.STATS_BOOK.getItemStack();
        BookMeta statsMeta = (BookMeta) stats.getItemMeta();

        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String kdr = (double) player.getPlayerModel().getLosses() == 0 ? "0" : decimalFormat.format((double) player.getPlayerModel().getKills()
                / (double) player.getPlayerModel().getLosses());
        String timePlayed = decimalFormat.format((double) player.getPlayerModel().getTimePlayed() / (double) 86400);

        statsMeta.setPages(String.format(
                "%s%s%s STATS! \n\n\n%s %sWins: %s%s\n %sKills: %s%s\n %sDeaths: %s%s\n %sLooses: %s%s\n %sButter Coins: %s%s\n %sKDR: %s%s\n %sTime played: %s%s days.",
                ChatColor.BOLD, ChatColor.RED, player.getBukkitPlayer().getName().toUpperCase(), ChatColor.DARK_GRAY, ChatColor.BOLD, ChatColor.DARK_GRAY,
                player.getPlayerModel().getWins(), ChatColor.BOLD, ChatColor.DARK_GRAY, player.getPlayerModel().getKills(), ChatColor.BOLD,
                ChatColor.DARK_GRAY, player.getPlayerModel().getDeaths(), ChatColor.BOLD, ChatColor.DARK_GRAY, player.getPlayerModel().getLosses(),
                ChatColor.BOLD, ChatColor.DARK_GRAY, player.getPlayerModel().getButterCoins(), ChatColor.BOLD, ChatColor.DARK_GRAY, kdr, ChatColor.BOLD, ChatColor.DARK_GRAY, timePlayed));
                stats.setItemMeta(statsMeta);

        return stats;
    }

    /**
     * When player chats
     * 
     * @param player
     * @author kvnamo
     */
    public void chatMessage(AsyncPlayerChatEvent event) {
        final RFBPlayer player = this.players.get(event.getPlayer().getName());

        // Spectators are not allowed to send messages.
        if (player == null) {
            event.getPlayer().sendMessage(String.format("%sOnly living players can send messages.", ChatColor.GRAY));
            event.setCancelled(true);
            return;
        }

        // Last message.
        if (StringUtils.isNotBlank(player.getLastMessage()) && player.getLastMessage().equals(event.getMessage().toLowerCase())) {
            event.getPlayer().sendMessage(String.format("%sPlease don't send the same message multiple times!", ChatColor.GRAY));
            event.setCancelled(true);
        }

        player.setLastMessage(event.getMessage().toLowerCase());
        PlayerTagEnum playerTag = PlayerTagEnum.getTag(player.getBukkitPlayer(), player.getMinecadeAccount());
        event.setFormat(playerTag.getPrefix() + ChatColor.WHITE + "%s" + ChatColor.GRAY + ": %s");
    }

    /**
     * Call when player press right click button
     * 
     * @param bukkitPlayer
     * @author kvnamo
     */
    public void rightClick(PlayerInteractEvent event) {
        Player bukkitPlayer = event.getPlayer();
        ItemStack itemInHand = bukkitPlayer.getItemInHand();
        
        if (RFBInventoryEnum.LEAVE_COMPASS.getMaterial().equals(itemInHand.getType())) {
            EngineUtils.disconnect(bukkitPlayer, LOBBY, null);
        }
        if(bukkitPlayer != null && this.spectators.containsKey(bukkitPlayer.getName())){
            event.setCancelled(true);
        }
    }
}
