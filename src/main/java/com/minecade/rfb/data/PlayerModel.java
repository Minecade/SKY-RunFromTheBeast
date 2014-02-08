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

public class PlayerModel {

    private long id;
    
    private String username;
    
    private long kills;
    
    private long deaths;
    
    private long suicides;
    
    private int wins;
    
    private int butterCoins;
    
    private int losses;
    
    private Date lastSeen;
       
    private int timePlayed;
    
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
