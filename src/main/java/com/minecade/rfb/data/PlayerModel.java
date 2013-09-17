package com.minecade.rfb.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

// this class is an entity that should be persisted
@Entity
// name of the table in the database/file
@Table(name = "players")
public class PlayerModel {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id", unique = true, insertable = false)
    private long id;
    
    @Column(name = "username", length = 16, unique = true, nullable = false)
    private String username;
    
    @Column(name = "kills", nullable = false)
    private long kills;
    
    @Column(name = "deaths", nullable = false)
    private long deaths;
    
    @Column(name = "suicides", nullable = false)
    private long suicides;
    
    @Column(name = "win", nullable = false)
    private int wins;
    
    @Column(name = "butter_coins", nullable = false)
    private int butterCoins;
    
    @Column(name = "losses", nullable = false)
    private int losses;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_seen", nullable = false)
    private Date lastSeen;
       
    @Column(name = "time_played", nullable = false)
    private int timePlayed;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "beast_pass", nullable = true)
    private Date beastPass;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the kills
     */
    public long getKills() {
        return kills;
    }

    /**
     * @param kills the kills to set
     */
    public void setKills(long kills) {
        this.kills = kills;
    }

    /**
     * @return the suicides
     */
    public long getSuicides() {
        return suicides;
    }

    /**
     * @param suicides the suicides to set
     */
    public void setSuicides(long suicides) {
        this.suicides = suicides;
    }

    /**
     * @return the lastSeen
     */
    public Date getLastSeen() {
        return lastSeen;
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * @return the win
     */
    public int getWins() {
        return wins;
    }

    /**
     * @param win the win to set
     */
    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * @return the losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * @param losses the losses to set
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }

    /**
     * 
     * @return total time played
     */
    public int getTimePlayed() {
        return this.timePlayed;
    }

    /**
     * 
     * @param total time played
     */
    public void setTimePlayed(int timePlayed) {
        this.timePlayed = timePlayed;
    }

    /**
     * @return the deaths
     */
    public long getDeaths() {
        return deaths;
    }

    /**
     * @param deaths the deaths to set
     */
    public void setDeaths(long deaths) {
        this.deaths = deaths;
    }

    /**
     * @return the beast_Pass
     * @author jdgil
     */
    public Date getBeastPass() {
        return beastPass;
    }

    /**
     * @param beast_Pass the beast_Pass to set
     * @author jdgil
     */
    public void setBeastPass(Date beast_Pass) {
        this.beastPass = beast_Pass;
    }

    /**
     * @return the butterCoins
     */
    public int getButterCoins() {
        return butterCoins;
    }

    /**
     * @param butterCoins the butterCoins to set
     */
    public void setButterCoins(int butterCoins) {
        this.butterCoins = butterCoins;
    }
}
