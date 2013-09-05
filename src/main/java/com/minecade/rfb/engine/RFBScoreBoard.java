package com.minecade.rfb.engine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.minecade.rfb.enums.RFBPlayerTag;
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
        
        // Unregister previous scoreboard
        if (this.getScoreboardObjective() != null) {
            this.getScoreboardObjective().unregister();
        }
        
        // Create teams
        if(this.scoreboard.getTeams().isEmpty()){
            for(RFBPlayerTag tag: RFBPlayerTag.values()){
                this.scoreboard.registerNewTeam(tag.name()).setPrefix(tag.getPrefix());
            }
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
        Team team = this.scoreboard.getTeam(player.getTag().name());
        team.addPlayer(Bukkit.getOfflinePlayer(player.getBukkitPlayer().getName()));
        team.setPrefix(player.getTag().getPrefix());
    }
    
    /**
     * Sets the number of players necessaries to star the game
     * @param playersToStart
     * @author kvnamo
     */
    public void setMatchPlayers(int matchPlayers){
        this.getScoreboardObjective().getScore(Bukkit.getOfflinePlayer(PLAYERS)).setScore(matchPlayers);
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
