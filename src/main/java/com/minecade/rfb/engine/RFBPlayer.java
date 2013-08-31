package com.minecade.rfb.engine;

import org.bukkit.entity.Player;

import com.minecade.engine.enums.PlayerTagEnum;
import com.minecade.rfb.data.PlayerModel;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBPlayer {

    private Player bukkitPlayer;

    private boolean inAir;

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

    private PlayerModel playerModel;

    public PlayerModel getPlayerModel() {
        return this.playerModel;
    }

    public void setPlayerModel(PlayerModel playerModel) {
        this.playerModel = playerModel;
    }

    private String lastMessage;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * 
     * @return
     * @author jdgil
     */
    public PlayerTagEnum getTag() {

        if (this.getBukkitPlayer().isOp())
            return PlayerTagEnum.OP;

        if (this.playerModel.isAdmin())
            return PlayerTagEnum.ADMIN;

        if (this.playerModel.isCm())
            return PlayerTagEnum.CM;

        if (this.playerModel.isGm())
            return PlayerTagEnum.GM;

        if (this.playerModel.isVip())
            return PlayerTagEnum.VIP;

        return PlayerTagEnum.DEFAULT;
    }

    /**
     * @param bukkitPlayer
     * @author jdgil
     */
    public RFBPlayer(RunFromTheBeastPlugin plugin, Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        plugin.getPersistence().getPlayer(this);
    }

}
