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
    
    private int unridePassengerCount;
    
    private MinecadeAccount minecadeAccount;

    public String getLastDamageBy() {
        return lastDamageBy;
    }

    public void setLastDamageBy(String lastDamageBy) {
        this.lastDamageBy = lastDamageBy;
    }

    public boolean isInAir() {
        return inAir;
    }
    
    public boolean isVip() {
        return null != minecadeAccount ? minecadeAccount.isVip() : false;
    }
    
    public boolean isTitan() {
        return null != minecadeAccount ? minecadeAccount.isTitan() : false;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }
    
    public void sendMessage(String message) {
        bukkitPlayer.sendMessage(message);
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

    public MinecadeAccount getMinecadeAccount() {
        return this.minecadeAccount;
    }

    public void setMinecadeAccount(MinecadeAccount minecadeAccount) {
        this.minecadeAccount = minecadeAccount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public RFBPlayer(RunFromTheBeastPlugin plugin, Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.playerModel = plugin.getPersistence().getPlayerModel(bukkitPlayer.getName());
        this.minecadeAccount = plugin.getPersistence().getMinecadeAccount(bukkitPlayer.getName());
    }

    public int getUnridePassengerCount() {
        return unridePassengerCount;
    }

    public void setUnridePassengerCount(int unridePassenger) {
        this.unridePassengerCount = unridePassenger;
    }
    
    public void addUnridePassengerCount() {
        this.unridePassengerCount = this.unridePassengerCount + 1;
    }

}
