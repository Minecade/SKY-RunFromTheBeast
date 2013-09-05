package com.minecade.rfb.engine;

import org.bukkit.entity.Player;

import com.minecade.engine.data.MinecadeAccount;
import com.minecade.rfb.data.PlayerModel;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBPlayer {

    private Player bukkitPlayer;

    private boolean inAir;
    
    private String lastDamageBy;
    
    private PlayerModel playerModel;
    
    private String lastMessage;

    public String getLastDamageBy() {
        return lastDamageBy;
    }

    public void setLastDamageBy(String lastDamageBy) {
        this.lastDamageBy = lastDamageBy;
    }

    public boolean isInAir() {
        return inAir;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }
    
    public PlayerModel getPlayerModel() {
        return this.playerModel;
    }

    public void setPlayerModel(PlayerModel playerModel) {
        this.playerModel = playerModel;
    }
    
    private MinecadeAccount minecadeAccount;

    /**
     * Gets the minecadeAccount
     * @return minecadeAccount
     * @author kunamo
     */
    public MinecadeAccount getMinecadeAccount() {
        return this.minecadeAccount;
    }

    /**
     * Sets the minecadeAccount
     * @author kunamo
     */
    public void setMinecadeAccount(MinecadeAccount minecadeAccount) {
        this.minecadeAccount = minecadeAccount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * @param bukkitPlayer
     * @author jdgil
     */
    public RFBPlayer(RunFromTheBeastPlugin plugin, Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.playerModel = plugin.getPersistence().getPlayer(bukkitPlayer.getName());
        this.minecadeAccount = plugin.getPersistence().getMinecadeAccount(bukkitPlayer.getName());
    }

}
