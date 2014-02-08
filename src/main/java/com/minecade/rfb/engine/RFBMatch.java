package com.minecade.rfb.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.minecade.engine.enums.PlayerTagEnum;
import com.minecade.engine.settings.SettingsEnum;
import com.minecade.engine.settings.SettingsManager;
import com.minecade.engine.task.FireworksTask;
import com.minecade.engine.utils.DragonBarUtils;
import com.minecade.engine.utils.EngineUtils;
import com.minecade.rfb.enums.RFBInventoryEnum;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBMatch {

    private RunFromTheBeastPlugin plugin;
    private RFBWorld world;
    private final int requiredPlayerCount;
    private Map<String, RFBPlayer> spectators;
    private Map<String, RFBPlayer> players;
    private RFBPlayer beast;
    private final int matchCountdown;
    private final int beastWaitingTime;
    private final int readyCountdown;
    private int countdown;
    private final MatchScoreboard rfbScoreboard;
    private final MatchTimerTask timerTask;
    private List<String> broadcastMessages;
    private final String LOBBY = "lobby1";
    
    private Status status = Status.WAITING_FOR_PLAYERS;

    public RFBMatch(RunFromTheBeastPlugin plugin, RFBWorld rfbWorld){
        this.plugin = plugin;
        this.world = rfbWorld;
        this.requiredPlayerCount = plugin.getConfig().getInt("match.required-players");
        this.matchCountdown = plugin.getConfig().getInt("match.time");
        this.beastWaitingTime = plugin.getConfig().getInt("match.beast-countdown");
        this.readyCountdown = plugin.getConfig().getInt("match.ready-countdown");
        this.players = new ConcurrentHashMap<String, RFBPlayer>();
        this.spectators = new ConcurrentHashMap<String, RFBPlayer>();
        // broadcast messages list
        this.broadcastMessages = Collections.synchronizedList(new ArrayList<String>());
        // init scoreboard
        this.rfbScoreboard = new MatchScoreboard();
        // init timer - a single timer which never stops controls all the
        // match's logic
        this.timerTask = new MatchTimerTask();
        this.timerTask.runTaskTimer(plugin, 20, 20);
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent event){
        final Player bukkitPlayer = event.getPlayer();
        EngineUtils.clearBukkitPlayer(bukkitPlayer);
        switch(this.status){
        case ALL_WAITING:
        case BEAST_WAITING:
        case IN_PROGRESS:
            event.setRespawnLocation(this.world.getSpectatorSpawnLocation());
            RFBPlayer player = new RFBPlayer(this.plugin, bukkitPlayer);
            //add player death as a espectator
            this.addSpectatorToMatch(player, null);
            break;
        case STOPPED:
        case WAITING_FOR_PLAYERS:
            event.setRespawnLocation(plugin.getGame().getLobbySpawnLocation());
            break;
        default:
            break;
        }
        
    }
    
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player bukkitPlayer = event.getPlayer();
        ItemStack itemInHand = bukkitPlayer.getItemInHand();
        if (RFBInventoryEnum.LEAVE_COMPASS.getMaterial().equals(itemInHand.getType())) {
            EngineUtils.disconnect(bukkitPlayer, LOBBY, null);
        }
        
        if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR)){
            if(this.players.containsKey(bukkitPlayer.getName())){
                final RFBPlayer player = this.players.get(bukkitPlayer.getName());
                if(player.getBukkitPlayer().getPassenger() != null){
                    if(player.getBukkitPlayer().getPassenger() instanceof Player){
                        Player passenger = (Player)player.getBukkitPlayer().getPassenger();
                        player.addUnridePassengerCount();
                        int leftHits = 30 - player.getUnridePassengerCount();
                        if(leftHits > 0){
                            if(leftHits % 5 == 0){
                                player.getBukkitPlayer().sendMessage(String.format("%sLeft hits to eject the coward[%s%s%s] above you: %s%s", 
                                        ChatColor.DARK_GRAY, ChatColor.YELLOW, passenger.getName(), ChatColor.DARK_GRAY, ChatColor.GREEN, leftHits));
                            }
                        } else {
                            this.unridePassenger(player);
                        }
                    }
                }else if(player.getBukkitPlayer().getVehicle() != null) {
                    this.unrideVehicle(player);
                }
            }
        }
//        dont delete this, it is used to set up every new world.
//        if(event.getClickedBlock() != null) {
//            Bukkit.getLogger().info(String.format("Botton: %s", event.getClickedBlock().getLocation()));
//        }
        if(event.getClickedBlock() != null && event.getClickedBlock().hasMetadata("buttonId")){
            List<MetadataValue> values = event.getClickedBlock().getMetadata("buttonId");
            if(values.size() >= 1) {
                MetadataValue value = values.get(0);
                Location location = this.getWorld().getButtonLocationByName(value.asString());
                if(location != null){
                    bukkitPlayer.teleport(location);
                }
            }
        }
        if(bukkitPlayer != null && this.spectators.containsKey(bukkitPlayer.getName())){
            event.setCancelled(true);
        }
    }
    
    private void onPlayerJoin(RFBPlayer rfbPlayer) {
        Player player = rfbPlayer.getBukkitPlayer();
        // remove invisibility
        this.plugin.getGame().getGhostManager().setGhost(rfbPlayer.getBukkitPlayer(), false);
        EngineUtils.clearBukkitPlayer(player);
        player.setAllowFlight(false);
        player.setScoreboard(this.rfbScoreboard.getScoreboard());
        this.rfbScoreboard.assignTeam(rfbPlayer);
        rfbPlayer.sendMessage(String.format("%sWelcome to %s - Match will start soon!", ChatColor.LIGHT_PURPLE,
                getName()));
        rfbPlayer.sendMessage(String.format("%s%sVIP%s %sPlayers can %s%sride%s %sother players and avoid beast's coup", 
                ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.DARK_GRAY, ChatColor.YELLOW, ChatColor.BOLD, ChatColor.RESET, ChatColor.DARK_GRAY));
    }

    public synchronized void playerDeath(PlayerDeathEvent event, RFBPlayer rfbPlayer) {
        final Player bukkitPlayer = event.getEntity();
        event.getDrops().clear();
        //if player is spectator just remove and return, dont do anything else
        if (this.spectators.containsKey(bukkitPlayer.getName())){
            final RFBPlayer spectator = this.spectators.get(bukkitPlayer.getName());
            this.unridePassenger(spectator);
            this.spectators.remove(spectator.getBukkitPlayer().getName());
            this.setPlayerRulesToSpectator(spectator);
            return;
        }
        switch (this.status) {
        case IN_PROGRESS:
        case ALL_WAITING:
        case BEAST_WAITING:
            final RFBPlayer player = this.players.remove(bukkitPlayer.getName());
            if (player != null) {
                this.unridePassenger(player);
                // Check if there is a killer
                final RFBPlayer killer = player.getLastDamageBy() != null ? this.players.get(player.getLastDamageBy()) : null;
                // if death player is the beast
                if (this.beast != null && player.getBukkitPlayer().getName().equalsIgnoreCase(beast.getBukkitPlayer().getName())) {
                    synchronized (this.players) {
                        // Update Butter Coins in central DB - 5 butter coins for every winner and 1 butter coin else for beast's killer
                        if(null != killer) {
                            killer.sendMessage(String.format("%sYou have %skilled%s the %sbeast%s! all the %srunners%s alive are the winners", 
                                    ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.DARK_GRAY, ChatColor.GOLD, 
                                    ChatColor.DARK_GRAY));
                            this.addButterCoinsAsynchronously(killer.getBukkitPlayer(), 5);
                        }
                        // Save stats in database for the loser: Beast.
                        beast.getPlayerModel().setLosses(beast.getPlayerModel().getLosses() + 1);
                        beast.getPlayerModel().setDeaths(beast.getPlayerModel().getDeaths() + 1);
                        beast.getPlayerModel().setTimePlayed(beast.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                        this.plugin.getPersistence().updatePlayerAsynchronously(beast.getPlayerModel());
                        this.beast = null;
                    }
                } else {
                    // Death is a runner
                    // Save stats in database
                    player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                    player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                    //FIXME dont use ebean and PlayerModel, use our own sql queries
                    this.plugin.getPersistence().updatePlayerAsynchronously(player.getPlayerModel());
                    player.getBukkitPlayer().sendMessage(String.format("The beast has killed you, thanks for playing!"));
                    this.rfbScoreboard.setMatchPlayers(this.players.size());
                    
                    //add butter coin for the runner's killer
                    if (killer != null) {
                        player.setLastDamageBy(null);
                        killer.getPlayerModel().setKills(killer.getPlayerModel().getKills() + 1);
                        if(beast != null){
                            this.plugin.getPersistence().updatePlayerAsynchronously(beast.getPlayerModel());
                        }
                        
                        // Update Butter Coins in central DB
                        this.addButterCoinsAsynchronously(killer.getBukkitPlayer(), 1);
                        // Announce kill
                        broadcastMessages.add(String.format("%s[%s] %slost, Killed by %s[%s]", ChatColor.RED, player.getBukkitPlayer().getName(), 
                                ChatColor.DARK_GRAY, ChatColor.YELLOW, killer.getBukkitPlayer().getName()));
                    }
                }
                // send auto respawn packet
                EngineUtils.respawnPlayer(rfbPlayer.getBukkitPlayer());
            }
            break;
        default:
            break;
        }
    }
    
    public synchronized void playerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        

        //if player is spectator just remove and return, dont do anything else
        if (this.spectators.containsKey(playerName)){
            final RFBPlayer spectator = this.spectators.get(playerName);
            this.unridePassenger(spectator);
            this.spectators.remove(spectator.getBukkitPlayer().getName());
            this.setPlayerRulesToSpectator(spectator);
            return;
        }
        
        final RFBPlayer player = this.players.get(playerName);
        if (player!= null) {
            this.unridePassenger(player);
            this.players.remove(playerName);
            switch (this.status) {
            case ALL_WAITING:
            case BEAST_WAITING:
            case IN_PROGRESS:
                // Save player stats
                player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                this.plugin.getPersistence().updatePlayerAsynchronously(player.getPlayerModel());

                this.broadcastMessage(String.format("%s[%s] %squit the game", ChatColor.RED, playerName, ChatColor.DARK_GRAY));
                
                if(this.beast != null && playerName.equalsIgnoreCase(this.beast.getBukkitPlayer().getName())){
                    this.broadcastMessages.add(String.format("%sBEAST%s quit the game, the match finished!", ChatColor.RED, ChatColor.DARK_GRAY));
                    this.beast = null;
                }
                // Update scoreboard
                this.rfbScoreboard.setMatchPlayers(this.players.size());
                break;
            default:
                break;
            }
        }
    }

    public void entityDamage(EntityDamageEvent event) {
        Player bukkitPlayer;
        // the entity damaged was a player
        if (event.getEntity() instanceof Player) {
            bukkitPlayer = (Player) event.getEntity();
            //dont damage to spectators
            if(this.spectators.containsKey(bukkitPlayer.getName())){
                event.setCancelled(true);
            }
            final RFBPlayer player = this.players.get(bukkitPlayer.getName());
            
            if(event.getCause().equals(DamageCause.FALL)) {
                event.setCancelled(true);
                return;
            }
            
            switch (this.status) {
            case WAITING_FOR_PLAYERS:
                if(event.getCause().equals(DamageCause.VOID)){
                    plugin.getGame().backToLobby(player);
                }
            case STOPPED:
            case ALL_WAITING:
            case BEAST_WAITING:
                event.setCancelled(true);
            case IN_PROGRESS:
                if(event.getCause() != null && event.getCause().equals(DamageCause.VOID)){
                    if (player != null) {
                        // Remove dead player from the players list and add him
                        // to spectators list
                        this.unridePassenger(player);
                        this.players.remove(bukkitPlayer.getName());
                        if(this.beast != null && bukkitPlayer.getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName())){
                            this.broadcastMessages.add(String.format("%sBEAST%s quit the game, the match finished!", ChatColor.RED, ChatColor.DARK_GRAY));
                            this.beast = null;
                        }
                        // Save stats in database
                        player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
                        player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                        this.plugin.getPersistence().updatePlayerAsynchronously(player.getPlayerModel());
                        
                        this.broadcastMessage(String.format("%s[%s] %slost.", ChatColor.RED, bukkitPlayer.getName(), ChatColor.GRAY));
                        
                        this.addSpectatorToMatch(player, this.world.getPlayerFreeRandomSpawnLocation());
                        this.rfbScoreboard.setMatchPlayers(this.players.size());
                    }
                } else if(event.getCause().equals(DamageCause.ENTITY_ATTACK)){
                    Player bukkitDamager = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                    RFBPlayer attackVictim = this.players.get(bukkitPlayer.getName());
                    RFBPlayer attackDamager = this.players.get(bukkitDamager.getName());
                                   
                    // damage was caused for other kind of entity.
                    if (attackDamager == null || attackVictim == null) {
                        event.setCancelled(true);
                        return;
                    }
                    //dont hit the passenger
                    if(attackVictim.getBukkitPlayer().getVehicle() != null){
                        Entity vehicle = attackVictim.getBukkitPlayer().getVehicle();
                        if(vehicle instanceof Player){
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (this.beast != null && !(attackVictim.getBukkitPlayer().getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName()))
                            && !(attackDamager.getBukkitPlayer().getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName()))) {
                        event.setCancelled(true);
                        //ride a player
                        if(attackDamager.getMinecadeAccount().isVip() || attackDamager.getMinecadeAccount().isStaff() || attackDamager.getBukkitPlayer().isOp()) {
                            attackVictim.getBukkitPlayer().setPassenger(attackDamager.getBukkitPlayer());
                            attackVictim.getBukkitPlayer().sendMessage(String.format("%sWatch out, you have a %s%sVIP%s%s player[%s%s%s] above you.", 
                                    ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.DARK_GRAY, ChatColor.YELLOW, 
                                    attackDamager.getBukkitPlayer().getName(), ChatColor.DARK_GRAY ));
                            attackVictim.getBukkitPlayer().sendMessage(String.format("%sHe can't be %sdamaged%s for the %sbeast%s while he is above you.",
                                    ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.DARK_GRAY, ChatColor.YELLOW, ChatColor.DARK_GRAY ));
                            attackVictim.getBukkitPlayer().sendMessage(String.format("%sYou have to %shit%s the %sair%s to eject the passenger",
                                    ChatColor.DARK_GRAY, ChatColor.YELLOW, ChatColor.DARK_GRAY, ChatColor.GREEN, ChatColor.DARK_GRAY ));
                            attackDamager.getBukkitPlayer().sendMessage(String.format("%sLeft click %sto air to be %sejected",
                                    ChatColor.YELLOW, ChatColor.DARK_GRAY, ChatColor.RED));
                        }
                    } else {
                        attackVictim.setLastDamageBy(attackDamager.getBukkitPlayer().getName());
                    }
                } else if(event.getCause().equals(DamageCause.SUFFOCATION)) {
                    if(bukkitPlayer.getVehicle() != null){
                        event.setCancelled(true);
                    }
                } else {
                    this.unrideVehicle(player);
                }
                break;
            default:
                break;
            }
        }
    }
    
    private void unridePassenger(RFBPlayer vehicle){
        if(vehicle.getBukkitPlayer().getPassenger() != null){
            if(vehicle.getBukkitPlayer().getPassenger() instanceof Player){
                Player passenger = (Player)vehicle.getBukkitPlayer().getPassenger();
                vehicle.getBukkitPlayer().eject();
                vehicle.setUnridePassengerCount(0);
                vehicle.getBukkitPlayer().sendMessage(String.format("%sCoward[%s%s%s] was ejected", 
                        ChatColor.DARK_GRAY, ChatColor.YELLOW, passenger.getName(), ChatColor.DARK_GRAY));
            }
        }
    }
    
    private void unrideVehicle(RFBPlayer passenger){
        if(passenger != null && passenger.getBukkitPlayer().getVehicle() != null){
            if(passenger.getBukkitPlayer().getVehicle() instanceof Player){
                Player vehicle = (Player)passenger.getBukkitPlayer().getVehicle();
                RFBPlayer rfbVehicle = this.players.get(vehicle.getName());
                vehicle.eject();
                rfbVehicle.setUnridePassengerCount(0);
                passenger.getBukkitPlayer().sendMessage(String.format("%sYou are %swalking%s again!", 
                        ChatColor.DARK_GRAY, ChatColor.YELLOW, ChatColor.DARK_GRAY));
            }
        }
    }
    
    /**
     * This uses the actual queue to extract players to warm up the match
     */
    public synchronized void readyMatch(Queue<RFBPlayer> nextMatchPlayersQueue) {
        players.clear();
        RFBPlayer rfbPlayer = nextMatchPlayersQueue.poll();
        while (null != rfbPlayer) {
            // move players to spawn points and configure them
            //teleport players to jail
            Location spawnPoint = this.world.getNextPlayerSpawn();
            Bukkit.getLogger().info(String.format("Spawn point for player: [%s] - [%s]", rfbPlayer.getBukkitPlayer().getName(), spawnPoint));
            if (rfbPlayer.getBukkitPlayer().teleport(spawnPoint)) {
                DragonBarUtils.handleRelocation(rfbPlayer.getBukkitPlayer(), spawnPoint);
                players.put(rfbPlayer.getBukkitPlayer().getName(), rfbPlayer);
                onPlayerJoin(rfbPlayer);
            } else {
                Bukkit.getLogger().severe(
                        String.format("Unable to teleport player: [%s] to match: [%s] - Stopping match...",
                                rfbPlayer.getBukkitPlayer().getName(), getName()));
                finishMatch();
                return;
            }
            if (players.size() < requiredPlayerCount) {
                rfbPlayer = nextMatchPlayersQueue.poll();
            } else {
                break;
            }
        }
        countdown = readyCountdown;
        status = RFBMatch.Status.ALL_WAITING;
        rfbScoreboard.resetAllScores();
        this.rfbScoreboard.setMatchPlayers(this.players.size());
        synchronized (this.players) {
            // Set beast, if this.beast != null was because some staff people forced to be the beast.
            if(this.beast == null){
                beast = this.selectBeast(this.players.values());
            }
            
            if(this.beast != null){
                beast.getBukkitPlayer().sendMessage(
                        String.format("%sYou are the %sBEAST%s!", ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.DARK_GRAY));
                beast.getBukkitPlayer().teleport(this.world.getBeastSpawnLocation());
                beast.getBukkitPlayer().getInventory().setArmorContents(getBeastArmor());
                beast.getBukkitPlayer().setItemInHand(getBeastSword());
            } else {
                //TODO something was wrong, end the match.
            }
        }
    }
    
    private synchronized void startingMatch() {

        // update countdown and status
        this.countdown = beastWaitingTime;
        this.status = Status.BEAST_WAITING;
        for (RFBPlayer player : players.values()) {
            player.sendMessage(String.format("%sMatch started!", ChatColor.LIGHT_PURPLE));
        }
        //freedom to runners
        for (RFBPlayer player : this.players.values()) {
            if (beast != null && !player.getBukkitPlayer().getName().equals(beast.getBukkitPlayer().getName())) {
                if(player.getBukkitPlayer().teleport(this.world.getPlayerFreeRandomSpawnLocation())){
//                    DragonBarUtils.setMovingMessage(player.getBukkitPlayer(), String.format("Run, run, run far away as you can, beast[%s%s%s] will be in %s seconds", 
//                            ChatColor.YELLOW, this.beast.getBukkitPlayer().getName(), ChatColor.RESET, beastWaitingTime), ((countdown * 1f) / (this.matchCountdown * 1f)) * 100f);
                }
            }
        }

        
    }
    
    private synchronized void initMatch() {
        this.countdown = matchCountdown;
        this.status = Status.IN_PROGRESS;
        this.rfbScoreboard.setMatchPlayers(this.players.size());
        //freedom for the beast
        if (beast != null && beast.getBukkitPlayer().teleport(this.world.getPlayerFreeRandomSpawnLocation())) {
            //TODO message: Beast is free
        }
    }
    
    private synchronized void finishMatch() {

        status = Status.STOPPED;
        String winners = StringUtils.EMPTY;
        //pmsScoreboard.resetAllScores();
        //winners are the runners
        if(this.countdown <= 0 || this.beast == null){
            for(RFBPlayer rfbPlayer : this.players.values()) {
                if(this.beast != null && this.beast.getPlayerModel() != null && 
                        rfbPlayer.getBukkitPlayer().getName().equalsIgnoreCase(this.beast.getBukkitPlayer().getName())){
                    // Beast is alive and he lost for countdown = 0
                    beast.getPlayerModel().setLosses(beast.getPlayerModel().getLosses() + 1);
                    beast.getPlayerModel().setDeaths(beast.getPlayerModel().getDeaths() + 1);
                    beast.getPlayerModel().setTimePlayed(beast.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                    this.plugin.getPersistence().updatePlayerAsynchronously(beast.getPlayerModel());
                    this.beast.getBukkitPlayer().sendMessage(String.format("%sTime is out, there are runners alive, %syou lost!", ChatColor.DARK_GRAY, ChatColor.RED));
                } else if (rfbPlayer.getPlayerModel() != null) {
                    //Save stats for winners: runners
                    rfbPlayer.getPlayerModel().setWins(rfbPlayer.getPlayerModel().getWins() + 1);
                    rfbPlayer.getPlayerModel().setTimePlayed(rfbPlayer.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                    this.plugin.getPersistence().updatePlayerAsynchronously(rfbPlayer.getPlayerModel());
                    // Get winners: All runners alive.
                    winners = StringUtils.isBlank(winners) ? rfbPlayer.getBukkitPlayer().getName() : winners + ", "
                            + rfbPlayer.getBukkitPlayer().getName();
                    rfbPlayer.getBukkitPlayer().sendMessage(String.format("%sCongratulations%s, you %swin%s the match!!", ChatColor.GREEN,
                            ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.DARK_GRAY));
                    // Update Butter Coins in central DB
                    this.addButterCoinsAsynchronously(rfbPlayer.getBukkitPlayer(), 3);
                    new FireworksTask(rfbPlayer.getBukkitPlayer(), 10).runTaskTimer(this.plugin, 1l, 20l);
                }
            }
            broadcastMessages.add(String.format("%sThanks for playing! Winners: %s%s%s", ChatColor.RED, 
                    ChatColor.YELLOW,  ChatColor.BOLD, winners == null ? "None" : winners));
        } else {
            //Beast is the winner
            if(this.beast != null && players.containsKey(beast.getBukkitPlayer().getName()) && players.size() <= 1) {
                // Update Butter Coins in central DB
                this.addButterCoinsAsynchronously(beast.getBukkitPlayer(), 5);
                
                // Save player stats
                beast.getPlayerModel().setWins(beast.getPlayerModel().getWins() + 1);
                beast.getPlayerModel().setTimePlayed(beast.getPlayerModel().getTimePlayed() + this.matchCountdown - this.countdown);
                this.plugin.getPersistence().updatePlayerAsynchronously(beast.getPlayerModel());

                // Throw fireworks for winner
                new FireworksTask(beast.getBukkitPlayer(), 10).runTaskTimer(this.plugin, 1l, 20l);
                broadcastMessages.add(String.format("%sThanks for playing! %s%sBEAST%s is the Winner: %s%s%s",ChatColor.RED, ChatColor.GOLD, 
                        ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.YELLOW, winners == null ? "None" : beast.getBukkitPlayer().getName()));
            }
        }
        countdown = 10;
    }
    
    private void resetMatch(){
        
        for (RFBPlayer rfbPlayer : players.values()) {
            this.unridePassenger(rfbPlayer);
            this.unridePassenger(rfbPlayer);
            plugin.getGame().backToLobby(rfbPlayer);
        }
        for (RFBPlayer rfbPlayer : spectators.values()) {
            this.setPlayerRulesToSpectator(rfbPlayer);
            plugin.getGame().backToLobby(rfbPlayer);
        }
        this.players.clear();
        this.spectators.clear();
        this.beast = null;
        
        rfbScoreboard.resetAllScores();
        // Reload world
        world.reloadWorld();
        status = Status.WAITING_FOR_PLAYERS;
    }
    
    private static ItemStack[] getBeastArmor() {
        final ItemStack[] armor = new ItemStack[4];
        final ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
        armor[3] = helmet;
        final ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        armor[2] = chestplate;
        final ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        armor[1] = leggings;
        final ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        armor[0] = boots;
        return armor;
    }
    
    private static ItemStack getBeastSword() {
        final ItemStack spade = new ItemStack(Material.DIAMOND_SWORD, 1);
        return spade;
    }
    
    public void addSpectatorToMatch(RFBPlayer player, Location respawnLocation){
        this.spectators.put(player.getBukkitPlayer().getName(), player);
        player.getBukkitPlayer().setAllowFlight(true);
        EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
        this.hidePlayer(player.getBukkitPlayer());
        player.getBukkitPlayer().setCanPickupItems(false);
        player.getBukkitPlayer().getInventory().addItem(RFBInventoryEnum.LEAVE_COMPASS.getItemStack());
        player.getBukkitPlayer().sendMessage(String.format("%s%sYou are now spectating the match!", ChatColor.DARK_PURPLE, ChatColor.BOLD));
        if(player.getBukkitPlayer().isValid()){
            player.getBukkitPlayer().setScoreboard(this.rfbScoreboard.getScoreboard());
        }
        //if spectator is added in a respawn location must be null, respawn event make the teleport to respawn point
        if(respawnLocation != null){
            //Location spawnPoint = this.world.getPlayerFreeRandomSpawnLocation().clone();
            player.getBukkitPlayer().teleport(respawnLocation);
        }
        
        this.plugin.getServer().getLogger().severe(String.format("adding spectator to match: %s", player.getBukkitPlayer().getName()));
    }
    
    private void setPlayerRulesToSpectator(RFBPlayer player){
        player.getBukkitPlayer().setAllowFlight(false);
        EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
        this.showPlayer(player.getBukkitPlayer());
    }
    
    private void tickPlayers() {
        for (RFBPlayer rfbPlayer : players.values()) {
            for (String message : broadcastMessages) {
                rfbPlayer.sendMessage(message);
            }
            updateDragonBar(rfbPlayer.getBukkitPlayer());
            //Bukkit.getLogger().info(String.format("status: [%s] - countdown: [%s]", getStatus(), countdown));
            // Play sounds
            if (countdown == 1) {
                rfbPlayer.getBukkitPlayer().playSound(rfbPlayer.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 3f, 1.5f);
            } else if (countdown <= 5) {
                rfbPlayer.getBukkitPlayer().playSound(rfbPlayer.getBukkitPlayer().getLocation(), Sound.CLICK, 3, -3);
            }
        }
        
        for (RFBPlayer rfbSpectator : spectators.values()) {
            for (String message : broadcastMessages) {
                rfbSpectator.sendMessage(message);
            }
        }
        broadcastMessages.clear();
    }
    
    private void addButterCoinsAsynchronously(final Player bukkitPlayer, final int butterCoins){
        
        // Update Butter Coins in central DB
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                int vipButterCoins = RFBMatch.this.plugin.getPersistence().isPlayerStaff(bukkitPlayer) ?
                    SettingsManager.getInstance().getInt(SettingsEnum.VIP_BUTTERCOIN_MULTIPLIER) * butterCoins :
                    SettingsManager.getInstance().getInt(SettingsEnum.BUTTERCOIN_MULTIPLIER) * butterCoins ;
                
                RFBMatch.this.plugin.getPersistence().addButterCoins(bukkitPlayer.getName(), vipButterCoins);
            }
        });
        
        bukkitPlayer.sendMessage(String.format("%s[ButterCoins] %sYou have earned %s ButterCoins!", 
            ChatColor.GOLD, ChatColor.YELLOW, butterCoins));
    }
    
    private void updateDragonBar(Player player) {
        switch (getStatus()) {
        case ALL_WAITING:
            DragonBarUtils.setMessage(player, String.format("Match Starting In: %s", String.valueOf(Math.max(0, countdown))),
                    ((countdown * 1f) / (readyCountdown * 1f)) * 100f);
            break;
        case BEAST_WAITING:
//            Bukkit.getLogger().info(
//                    String.format("matchCountdown: [%s] - countdown: [%s] - percentage: [%s]", matchCountdown, countdown,
//                            ((countdown * 1f) / (matchCountdown * 1f)) * 100f));
            if(this.beast != null && this.beast.getBukkitPlayer().getName().equalsIgnoreCase(player.getName())){
                DragonBarUtils.setMessage(player, String.format("Match Starting In: %s", String.valueOf(Math.max(0, countdown))),
                        ((countdown * 1f) / (beastWaitingTime * 1f)) * 100f);
            } else {
                if(this.beast != null){
                    DragonBarUtils.setMovingMessage(player, String.format("beast[%s%s%s] will be free in %s%s%s seconds, " +
                            "Run, run faster as you can or the beast will kill you", ChatColor.YELLOW, this.beast.getBukkitPlayer().getName(), 
                            ChatColor.RESET, ChatColor.GREEN, countdown, ChatColor.RESET), ((countdown * 1f) / (this.matchCountdown * 1f)) * 100f, 4);
                }
            }
            break;
        case IN_PROGRESS:
//            Bukkit.getLogger().info(
//                    String.format("matchTime: [%s] - countdown: [%s] - percentage: [%s]", matchTime, countdown,
//                            ((countdown * 1f) / (matchTime * 1f)) * 100f));
            if(this.beast != null && this.beast.getBukkitPlayer().getName().equalsIgnoreCase(player.getName())){
                DragonBarUtils.setMessage(player, String.format("Time Left: %s", String.valueOf(Math.max(0, countdown))),
                        ((countdown * 1f) / (matchCountdown * 1f)) * 100f);
            } else {
                if(this.beast != null){
                    Double distance = beast.getBukkitPlayer().getLocation().distance(player.getLocation());
                    DragonBarUtils.setMessageWithHeader(player, String.format("Distance from beast: %s%s%s", 
                            ChatColor.GOLD, distance.intValue(), ChatColor.RESET), ((countdown * 1f) / (matchCountdown * 1f)) * 100f);
                }
            }
            break;
        case STOPPED:
//            Bukkit.getLogger().info(
//                    String.format("resetTime: [%s] - countdown: [%s] - percentage: [%s]", 10, countdown,
//                            ((countdown * 1f) / (matchTime * 1f)) * 100f));
            DragonBarUtils.setMessage(player, String.format("Back To Lobby In: %s", String.valueOf(Math.max(0, countdown))),
                    ((countdown * 1f) / (10 * 1f)) * 100f);
            break;
        default:
            DragonBarUtils.removeBar(player);
            break;
        }
    }
    
    private synchronized void updateScoreboard() {
        switch(this.status){
        case ALL_WAITING:
            break;
        case BEAST_WAITING:
            break;
        case IN_PROGRESS:
            this.rfbScoreboard.setMatchPlayers(this.players.size());
            this.rfbScoreboard.setTimeLeft(this.countdown);
            break;
        case STOPPED:
            break;
        case WAITING_FOR_PLAYERS:
            break;
        default:
            break;
        
        }
    }
    
    private RFBPlayer selectBeast(Collection<RFBPlayer> players){
        RFBPlayer beastSelected = null;
        if (players.size() <= 0){
            return null;
        }
        synchronized(this.players) {
            Player beastPlayer = plugin.getPassManager().selectPlayer(castListToPlayers(players));
            if(beastPlayer != null){
                RFBPlayer beastRFBPlayer = this.players.get(beastPlayer.getName());
                if(beastRFBPlayer != null){
                    beastSelected = beastRFBPlayer;
                }
            }
            if(beastSelected == null){
                Collection<RFBPlayer> tempPlayers = new ArrayList<RFBPlayer>(players);
                beastSelected = (RFBPlayer) tempPlayers.toArray()[plugin.getRandom().nextInt(tempPlayers.size())];
            }
        }

        // non-critical scoreboard code, put it inside a task so if it fails, it won't stop critical code.
        final RFBPlayer finalBeast = beastSelected;
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                //set up scoreboard for the beast
                RFBMatch.this.rfbScoreboard.assignBeast(finalBeast);
                finalBeast.getBukkitPlayer().setScoreboard(RFBMatch.this.rfbScoreboard.getScoreboard());
            }
        });
        return finalBeast;
    }

    private Collection<Player> castListToPlayers(Collection<RFBPlayer> players){
        Collection<Player> bukkitPlayers = new ArrayList<>();
        for(RFBPlayer rfbPlayer : players){
            bukkitPlayers.add(rfbPlayer.getBukkitPlayer());
        }
        return bukkitPlayers;
    }
    
    public void updateLobbyPortal(){
        switch(this.status){
        case ALL_WAITING:
        case BEAST_WAITING:
        case IN_PROGRESS:
            this.plugin.getPortalManager().enablePortal(this.getName());
            break;
        case STOPPED:
        case WAITING_FOR_PLAYERS:
            this.plugin.getPortalManager().disablePortal(this.getName());
            break;
        default:
            break;
        }
    }
    
    private void hidePlayer(Player bukkitPlayer) {
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().hidePlayer(bukkitPlayer);
        }
    }
    
    private void showPlayer(Player bukkitPlayer) {
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().showPlayer(bukkitPlayer);
        }

        for (RFBPlayer player : this.spectators.values()) {
            player.getBukkitPlayer().showPlayer(bukkitPlayer);
        }
    }
    
    private void broadcastMessage(String message) {
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().sendMessage(message);
        }
    }
    
    public void addBroadcastMessage(String message) {
        broadcastMessages.add(message);
    }
    
    public int getRequiredPlayerCount() {
        return requiredPlayerCount;
    }
    
    public String getName() {
        return null != world ? world.getName() : null;
    }

    public Status getStatus() {
        return status;
    }
    
    private class MatchTimerTask extends BukkitRunnable {

        @Override
        public void run() {
            // logic depends on match status

            // statuses with countdown
            switch (getStatus()) {
            case ALL_WAITING:
            case BEAST_WAITING:
            case IN_PROGRESS:
            case STOPPED:
                countdown--;
                break;
            default:
                break;
            }

            // status transitions
            switch (getStatus()) {
            case ALL_WAITING:
                if (beast == null || !players.containsKey(beast.getBukkitPlayer().getName()) || players.size() <= 1 || countdown <= 0) {
                    startingMatch();
                }
                break;
            case BEAST_WAITING:
                if (beast == null || !players.containsKey(beast.getBukkitPlayer().getName()) || players.size() <= 1 || countdown <= 0) {
                    initMatch();
                }
                break;
            case IN_PROGRESS:
                if (beast == null || !players.containsKey(beast.getBukkitPlayer().getName()) || players.size() <= 1 || countdown <= 0) {
                    finishMatch();
                }
                break;
            case STOPPED:
                if (countdown <= 0) {
                    resetMatch();
                }
                break;
            default:
                break;
            }

            tickPlayers();
            updateLobbyPortal();
            updateScoreboard();
        }

    }
    
    public enum Status {
        WAITING_FOR_PLAYERS,
        ALL_WAITING,
        BEAST_WAITING,
        IN_PROGRESS, 
        STOPPED
    }
    
    private class MatchScoreboard {

        private final String OBJECTIVE_TITLE = String.format("%s%s", ChatColor.DARK_RED, "Run From The Beast");
        private final String OBJECTIVE = "RFB";
        private final String PLAYERS = String.format("%s%s", ChatColor.GREEN, "Runners");
        private final String BEAST = "Beast";
        private final String TIME_LEFT = String.format("%s%s", ChatColor.GOLD, "Time Left");

        private final Scoreboard scoreboard;
        private final Objective sideObjective;

        public MatchScoreboard() {
            // Creates new scoreboard
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            sideObjective = this.scoreboard.registerNewObjective(OBJECTIVE, OBJECTIVE);
            sideObjective.setDisplayName(OBJECTIVE_TITLE);
            sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

            // Create teams
            if(this.scoreboard.getTeams().isEmpty()){
                for(PlayerTagEnum tag: PlayerTagEnum.values()){
                    this.scoreboard.registerNewTeam(tag.name()).setPrefix(tag.getPrefix());
                }
            }
        }

        public void assignTeam(RFBPlayer player){
            PlayerTagEnum playerTag = PlayerTagEnum.getTag(player.getBukkitPlayer(), player.getMinecadeAccount());
            
            Team team = this.scoreboard.getTeam(playerTag.name());
            team.addPlayer(Bukkit.getOfflinePlayer(player.getBukkitPlayer().getName()));
            team.setPrefix(playerTag.getPrefix());
        }

        public void assignBeast(RFBPlayer beast){
            Team team = this.scoreboard.getTeam(PlayerTagEnum.getTag(beast.getBukkitPlayer(), beast.getMinecadeAccount()).name());
            team.removePlayer(Bukkit.getOfflinePlayer(beast.getBukkitPlayer().getName()));
            Team beastTeam = this.scoreboard.getTeam(BEAST);
            if (beastTeam == null) {
                beastTeam = this.scoreboard.registerNewTeam(BEAST);
            }
            Iterator<OfflinePlayer> beastPlayers = beastTeam.getPlayers().iterator();
            while(beastPlayers.hasNext()){
                beastTeam.removePlayer(beastPlayers.next());
            }
            beastTeam.addPlayer(Bukkit.getOfflinePlayer(beast.getBukkitPlayer().getName()));
            beastTeam.setPrefix(String.format("[%s%s%s%s] ", ChatColor.RED, ChatColor.BOLD, BEAST, ChatColor.RESET));
        }

        public Scoreboard getScoreboard() {
            return scoreboard;
        }

        public void resetAllScores() {
            for (OfflinePlayer player : scoreboard.getPlayers()) {
                this.scoreboard.resetScores(player);
            }
        }
        
        public void setMatchPlayers(int matchPlayers){
            int runners = matchPlayers - 1 < 0 ? 0 : matchPlayers - 1;
            this.scoreboard.getObjective(OBJECTIVE).getScore(Bukkit.getOfflinePlayer(PLAYERS)).setScore(runners);
        }
        
        public void setTimeLeft(int timeLeft) {
            this.scoreboard.getObjective(OBJECTIVE).getScore(Bukkit.getOfflinePlayer(TIME_LEFT)).setScore(timeLeft);
        }
    }

    /**
     * @return the world
     */
    public RFBWorld getWorld() {
        return world;
    }
}
