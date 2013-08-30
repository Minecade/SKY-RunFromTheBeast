package com.minecade.rfb.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.minecade.rfb.enums.RFBStatus;

// This class is an entity that should be persisted
@Entity
// Name of the table in the database/file
@Table(name = "servers")
public class ServerModel {

    @Id
    @Column(name = "id", unique = true)
    private long serverId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private RFBStatus state = RFBStatus.WAITING_FOR_PLAYERS;
    
    @Column(name = "max_players", nullable = false)
    private int maxPlayers;
    
    @Column(name = "online_players", nullable = false)
    private int onlinePlayers;

    /**
     * @return the serverId
     */
    public long getServerId() {
        return serverId;
    }

    /**
     * @param serverId the serverId to set
     */
    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    /**
     * @return the state
     */
    public RFBStatus getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(RFBStatus state) {
        this.state = state;
    }

    /**
     * @return the maxPlayers
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * @param maxPlayers the maxPlayers to set
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * @return the onlinePlayers
     */
    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    /**
     * @param onlinePlayers the onlinePlayers to set
     */
    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
