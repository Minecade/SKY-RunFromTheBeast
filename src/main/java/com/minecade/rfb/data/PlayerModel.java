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
    
    @Column(name = "suicides", nullable = false)
    private long suicides;
    
    @Column(name = "win", nullable = false)
    private int wins;
    
    @Column(name = "losses", nullable = false)
    private int losses;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_seen", nullable = false)
    private Date lastSeen;
       
    @Column(name = "time_played", nullable = false)
    private int timePlayed;
    
    // Transient fields
    private boolean admin;
    
    private boolean cm;
    
    private boolean vip;
    
    private boolean gm;

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
     * @return the gm
     */
    public boolean isGm() {
        return gm;
    }

    /**
     * @param gm the gm to set
     */
    public void setGm(boolean gm) {
        this.gm = gm;
    }

    /**
     * @return the vip
     */
    public boolean isVip() {
        return this.vip;
    }

    /**
     * @param vip the vip to set
     */
    public void setVip(boolean vip) {
        this.vip = vip;
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
     * @return the admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @param admin the admin to set
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * @return the cm
     */
    public boolean isCm() {
        return cm;
    }

    /**
     * @param cm the cm to set
     */
    public void setCm(boolean cm) {
        this.cm = cm;
    }
}
