package com.minecade.rfb.data;

import com.minecade.rfb.engine.RFBGame;

public class ServerModel {

    private long serverId;
    
    private RFBGame.Status state = RFBGame.Status.WAITING_FOR_PLAYERS;
    
    private int maxPlayers;
    
    private int onlinePlayers;
    
    private String worldName;

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

    /**
     * @return the worldName
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * @param worldName the worldName to set
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    /**
     * @return the state
     */
    public RFBGame.Status getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(RFBGame.Status state) {
        this.state = state;
    }
}
