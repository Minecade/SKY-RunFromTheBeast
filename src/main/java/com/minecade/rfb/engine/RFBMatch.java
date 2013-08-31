package com.minecade.rfb.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;

import com.minecade.engine.MinecadeWorld;
import com.minecade.engine.task.FireworksTask;
import com.minecade.engine.utils.EngineUtils;
import com.minecade.rfb.task.TimerTask;
import com.minecade.rfb.enums.RFBStatus;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBMatch {
    
    private final int time;
    private final int maxPlayers;
    private final int startCountdown;
    private final int readyCountdown;
    private final int requiredPlayers;
    
    private RunFromTheBeastPlugin plugin;
    private Location lobbyLocation;
    private RFBStatus status = RFBStatus.WAITING_FOR_PLAYERS;
    private static final String LOBBY = "lobby";
    private Map<String, RFBPlayer> players;
    private int timeLeft;
    private TimerTask timerTask;
    private String winners;
    private MinecadeWorld arena;
    private RFBScoreBoard rfbScoreboard;
    private Map<String, RFBPlayer> spectators; 

    public RFBMatch(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
        this.rfbScoreboard = new RFBScoreBoard(this.plugin);
        
        this.maxPlayers = plugin.getConfig().getInt("server.max-players");
        this.time = plugin.getConfig().getInt("match.time");
        this.startCountdown = plugin.getConfig().getInt("match.start-countdown");
        this.readyCountdown = plugin.getConfig().getInt("match.ready-countdown");
        this.requiredPlayers = plugin.getConfig().getInt("match.required-players");
        
        this.players =  new ConcurrentHashMap<String, RFBPlayer>(this.requiredPlayers);
        this.spectators =  new ConcurrentHashMap<String, RFBPlayer>();
    }
    
    public void initWorld(WorldInitEvent event){
        World world = event.getWorld();

        // World can't be empty
        if (world == null){
            this.plugin.getServer().getLogger().severe("RFBMatch initWorld: world parameter is null");
            return;
        }
        this.plugin.getServer().getLogger().info(String.format("initworld.this.lobbyLocation %s", this.lobbyLocation));
        // if the map is the lobby
        if (plugin.getConfig().getString("match.lobby-world-name").equalsIgnoreCase(world.getName())) {
            this.lobbyLocation = EngineUtils.locationFromConfig(this.plugin.getConfig(), world, "lobby.spawn");
            this.plugin.getServer().getLogger().info(String.format("initworld.this.lobbyLocation %s", this.lobbyLocation));
            world.setSpawnLocation(this.lobbyLocation.getBlockX(), this.lobbyLocation.getBlockY(), this.lobbyLocation.getBlockZ());
        }
    }
    
    /**
     * Call when player join the match
     * @param PlayerJoinEvent
     * @author jdgil 
     */
    public void playerJoin(PlayerJoinEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        
        // Player banned
        if(this.plugin.getPersistence().isPlayerBanned(bukkitPlayer.getName())){
            bukkitPlayer.kickPlayer(plugin.getConfig().getString("match.ban-message"));
            return;
        }

        // Server Stopped
        if (RFBStatus.RESTARTING.equals(this.status)){
            EngineUtils.disconnect(bukkitPlayer, LOBBY, null);
            return;
        }

        final RFBPlayer player = new RFBPlayer(this.plugin, bukkitPlayer);
        EngineUtils.clearBukkitPlayer(bukkitPlayer);
        
        // Setup scoreboard
        this.rfbScoreboard.assignTeam(player);
        bukkitPlayer.setScoreboard(this.rfbScoreboard.getScoreboard());
        
        if(RFBStatus.WAITING_FOR_PLAYERS.equals(this.status)) {
            // If player is spectator or OP
            if(bukkitPlayer.isOp()) {
                bukkitPlayer.setAllowFlight(true);
            }

            this.players.put(bukkitPlayer.getName(), player);
            this.rfbScoreboard.setMatchPlayers(this.players.size());
            
            int playersRemaining = requiredPlayers - this.players.size();
            
            if(playersRemaining == 0){
                // Update server status
                this.status = RFBStatus.STARTING_MATCH;
                plugin.getPersistence().updateServerStatus(this.status);
                // Begin match start timer
                this.timeLeft = this.timeLeft == 0 ? this.startCountdown : timeLeft;
                this.plugin.getServer().getLogger().severe(String.format("Begin Match Soon, time left: %s", this.timeLeft));
                this.timerTask = new TimerTask(this, this.timeLeft, true, false, false);
                this.timerTask.runTaskTimer(this.plugin, 1l, 20l);
            }
            else {
                this.broadcastMessage(String.format("%sWe need %s[%s] %splayer(s) to start.", 
                    ChatColor.DARK_GRAY, ChatColor.RED, playersRemaining, ChatColor.DARK_GRAY));
            }
        } else if(this.players.size() < this.maxPlayers){
            
            if(player.getPlayerModel().isVip()){
                //player.loadInventoy();
                this.players.put(bukkitPlayer.getName(), player);                
                this.rfbScoreboard.setMatchPlayers(this.players.size());
            }
            else if (plugin.getPersistence().isPlayerStaff(bukkitPlayer)) {
                //player.loadInventoy();
                this.hidePlayer(bukkitPlayer);
                bukkitPlayer.setAllowFlight(true);
                this.spectators.put(bukkitPlayer.getName(), player);
            } 
            else{
                bukkitPlayer.kickPlayer(plugin.getConfig().getString("match.server-full-message"));
                return;
            }
        }
        
        //Teleport to match location
        this.plugin.getServer().getLogger().info(String.format("this.arena %s", this.arena));
        this.plugin.getServer().getLogger().info(String.format("this.lobbyLocation %s", this.lobbyLocation));
        bukkitPlayer.teleport(this.arena == null ? this.lobbyLocation : this.arena.getRandomSpawn()); 
    }
    
    /**
     * Initiate match
     * @author jdgil
     */
    public void initMatch() {
        this.arena = plugin.getRandomWorld(); 
        this.plugin.getServer().getLogger().info(String.format("RFBMatch.initMatch, arena %s", arena.getName()));
        synchronized (this.players) {
            for (RFBPlayer player : this.players.values()) {
                player.getBukkitPlayer().setAllowFlight(true);
                EngineUtils.clearBukkitPlayer(player.getBukkitPlayer());
                player.getBukkitPlayer().teleport(this.arena.getRandomSpawn());
            }
        }

        // Create the task for the count down.
        this.timerTask = new TimerTask(this, this.readyCountdown, false, true, false);
        this.timerTask.runTaskTimer(this.plugin, 1l, 20l); 
    }
    
    /**
     * Start match
     * @author kvnamo
     */
    public void startMatch(){
        // Update server status
        this.plugin.getServer().getLogger().info(String.format("RFBMatch.startMatch"));
        this.status = RFBStatus.IN_PROGRESS;
        plugin.getPersistence().updateServerStatus(this.status);
        
        this.rfbScoreboard.setMatchPlayers(this.players.size());

        this.timeLeft = this.time;
        this.timerTask = new TimerTask(this, this.timeLeft, false, false, false);
        this.timerTask.runTaskTimer(plugin, 11, 20l);

        this.gameOver();
        this.broadcastMessage(String.format("%sMatch started!", ChatColor.RED));
    }
    
    /**
     * Stop the game
     * @author kvnamo
     */
    public void stopGame(){
        this.plugin.getServer().getLogger().info(String.format("RFBMatch.stopMatch"));
        // Update server status
        this.status = RFBStatus.RESTARTING;
        plugin.getPersistence().updateServerStatus(this.status);
        
        // Send players and spectators to the lobby
        final Player[] onlinePlayers = this.plugin.getServer().getOnlinePlayers();
        
        for(Player player : onlinePlayers) {
            EngineUtils.disconnect(player.getPlayer(), LOBBY, String.format(
                "Thanks for playing! Winners: %s%s%s", 
                ChatColor.BOLD, ChatColor.YELLOW, 
                this.winners == null ? "None" : this.winners));
        }

        // Stop the game
        RFBMatch.this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
    }
    
    /**
     * Game over
     * @author jdgil
     */
    public void gameOver(){
        this.plugin.getServer().getLogger().info(String.format("RFBMatch.gameOver"));
        // Finish the game if there is only one player or the match timer is cero
        if(this.players.size() <= 1 || this.timeLeft == 0){
            // Start finish timer
            this.timerTask.cancel();
            this.timeLeft = 10;
            new TimerTask(this,  this.timeLeft, false, false, true).runTaskTimer(this.plugin, 11, 20l);
            
            // Save winners stats in database
            synchronized (this.players) {
                for (RFBPlayer player : this.players.values()) {
                    //Save player stats
                    player.getPlayerModel().setWins(player.getPlayerModel().getWins() + 1);
                    player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
                    this.plugin.getPersistence().updatePlayer(player.getPlayerModel());
                    
                    // Get winners
                    this.winners =  StringUtils.isBlank(this.winners) ? 
                        player.getBukkitPlayer().getName() : this.winners + ", " + player.getBukkitPlayer().getName();
                        
                    // Throw fireworks for winner
                    new FireworksTask(player.getBukkitPlayer(), 10).runTaskTimer(this.plugin, 1l, 20l);
                }
            }
        }
    }
    
    public void playerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();

        final RFBPlayer player = this.players.get(playerName);
        this.players.remove(playerName);

        if (RFBStatus.STARTING_MATCH.equals(this.status)) {
            // Check if starting players number is reached
            if(this.startingPlayers()) return;
                    
            // Update server status
            this.status = RFBStatus.WAITING_FOR_PLAYERS;
            plugin.getPersistence().updateServerStatus(this.status);

            // Cancel begin timer task
            this.timerTask.cancel();
        }
        else if (RFBStatus.IN_PROGRESS.equals(this.status) && player != null) {
            //Save player stats
            player.getPlayerModel().setLosses(player.getPlayerModel().getLosses() + 1);
            player.getPlayerModel().setTimePlayed(player.getPlayerModel().getTimePlayed() + this.time - this.timeLeft);
            this.plugin.getPersistence().updatePlayer(player.getPlayerModel());
            
            this.broadcastMessage(String.format("%s[%s] %squit the game", ChatColor.RED, playerName, ChatColor.DARK_GRAY));
            this.gameOver();
        }
        
        // Update scoreboard
        this.rfbScoreboard.setMatchPlayers(this.players.size());
    }
    
    /**
     * Required starting players
     * @author kvnamo
     */
    private boolean startingPlayers() {
        
        if(this.players.size() < this.requiredPlayers){

            for(RFBPlayer player : this.spectators.values()){
                // Add spectator to players list
                this.players.put(player.getBukkitPlayer().getName(), player);
                this.spectators.remove(player.getBukkitPlayer().getName());
                
                // Show player
                this.showPlayer(player.getBukkitPlayer());
                
                // Send player to spawn location
                player.getBukkitPlayer().setFlying(false);
                player.getBukkitPlayer().teleport(this.arena == null ? lobbyLocation : this.arena.getRandomSpawn());
                player.getBukkitPlayer().sendMessage(String.format("%sYou are now playing the game!", ChatColor.YELLOW));
                
                // Check if more spectators are needed
                if(this.players.size() == this.requiredPlayers) return true; 
            }
        }
        
        return false;
    }
    
    /**
     * Show spectator 
     * @param player
     * @author jdgil
     */
    private void showPlayer(Player bukkitPlayer){
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().showPlayer(bukkitPlayer);
        }
        
        for (RFBPlayer player : this.spectators.values()) {
            player.getBukkitPlayer().showPlayer(bukkitPlayer);
        }
    }
    
    /**
     * Hide spectator from other players
     * @param spectator
     * @author jdgil
     */
    private void hidePlayer(Player bukkitPlayer){
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().hidePlayer(bukkitPlayer);
        }
        
        for (RFBPlayer player : this.spectators.values()) {
            player.getBukkitPlayer().hidePlayer(bukkitPlayer);
        }
    }
    
    /**
     * Update the time left to start the match.
     * @param timeLeft  
     * @author jdgil
     */
    public void setRemainingTime(int timeLeft){
        this.timeLeft = timeLeft;
        this.rfbScoreboard.setTimeLeft(timeLeft);
        
        for (RFBPlayer player : this.players.values()) {
            if(RFBStatus.STARTING_MATCH.equals(this.status) && timeLeft < 6){
                player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.CLICK, 3, -3);
            }
        }
    }
    
    /**
     * Broadcast message to every player in match
     * @param message
     * @author jdgil
     */
    private void broadcastMessage(String message){
        for (RFBPlayer player : this.players.values()) {
            player.getBukkitPlayer().sendMessage(message);
        }
    }
    
    /**
     * Call when a block breaks.
     * @param event
     * @author jdgil
     */
    public void blockBreak(BlockBreakEvent event) {
        if (EngineUtils.isOpInCreativeMode(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }
}
