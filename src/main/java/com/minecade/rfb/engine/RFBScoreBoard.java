package com.minecade.rfb.engine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.minecade.engine.enums.PlayerTagEnum;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBScoreBoard {
    /**
     * Scoreboard title
     */
    private final String TITLE = "Run From The Beast";
    
    /**
     * Scoreboard objective
     */
    private final String OBJECTIVE = "Players Left";
    
    /**
     * Scoreboard players to start
     */
    private final String PLAYERS_TO_START = "Players to Start";
    
    /**
     * Scoreboard players to start
     */
    private final String PLAYERS = "Players";     
    
    /**
     * Scoreboard players left
     */
    private final String TIME_LEFT = "Time Left";
    
    private Scoreboard scoreboard;
    
    /**
     * Gets the scoreboard
     * @author kvnamo
     */
    public Scoreboard getScoreboard(){
        return this.scoreboard;
    }
    
    /**
     * Scoreboard objective
     * @return The scoreboard objective.
     * @author kvnamo
     */
    private Objective getScoreboardObjective(){
        return this.scoreboard.getObjective(OBJECTIVE);
    }
    
    /**
     * GRScoreboard constructor
     * @param plugin
     * @author jdgil
     */
    public RFBScoreBoard(RunFromTheBeastPlugin plugin){
     // Creates new scoreboard
        this.scoreboard =  plugin.getServer().getScoreboardManager().getNewScoreboard();
        
        // Create teams
        if(this.scoreboard.getTeams().isEmpty()){
            for(PlayerTagEnum tag: PlayerTagEnum.values()){
                this.scoreboard.registerNewTeam(tag.name()).setPrefix(tag.getPrefix());
            }
        }
    }
    
    /**
     * Init scoreboard
     * @author kvnamo
     */
    public void init(){
        // Unregister previous scoreboard
        if (this.getScoreboardObjective() != null) {
            this.getScoreboardObjective().unregister();
        }
        
        // Setup scoreboard
        this.scoreboard.registerNewObjective(OBJECTIVE, "Run From The Beast")
            .setDisplayName(String.format("%s%s", ChatColor.YELLOW, TITLE)); 
        this.getScoreboardObjective().setDisplaySlot(DisplaySlot.SIDEBAR);   
    }
    
    /**
     * Assign team to player
     * @param player
     * @author kvnamo
     */
    public void assignTeam(RFBPlayer player){
        PlayerTagEnum playerTag = PlayerTagEnum.getTag(player.getBukkitPlayer(), player.getMinecadeAccount());
        
        Team team = this.scoreboard.getTeam(playerTag.name());
        team.addPlayer(Bukkit.getOfflinePlayer(player.getBukkitPlayer().getName()));
        team.setPrefix(playerTag.getPrefix());
    }
    
    /**
     * Sets the number of players necessaries to star the game
     * @param playersToStart
     * @author kvnamo
     */
    public void setMatchPlayers(int matchPlayers, boolean matchStarted){
        this.getScoreboardObjective().getScore(Bukkit.getOfflinePlayer(
                matchStarted ? PLAYERS : PLAYERS_TO_START)).setScore(matchPlayers);
    }
    
    /**
     * Sets the time left
     * @param timeLeft
     * @author kvnamo
     */
    public void setTimeLeft(int timeLeft) {
        this.getScoreboardObjective().getScore(Bukkit.getOfflinePlayer(TIME_LEFT)).setScore(timeLeft);
    }
}

