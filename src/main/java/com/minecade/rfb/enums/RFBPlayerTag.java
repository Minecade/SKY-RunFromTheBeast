package com.minecade.rfb.enums;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

public enum RFBPlayerTag {
    DEFAULT(ChatColor.WHITE),
    GM(ChatColor.AQUA),
    OP(ChatColor.RED), 
    PRO(ChatColor.BLUE),
    VIP(ChatColor.GREEN);
    
    private ChatColor teamColor;

    /**
     * GRPlayerTag constructor
     * @param teamColor
     * @author kvnamo
     */
    private RFBPlayerTag(ChatColor teamColor) {
        this.teamColor = teamColor;
    }
    
    /**
     * Gets the prefix
     * @return
     * @author kvnamo
     */
    public String getPrefix(){
        return !RFBPlayerTag.DEFAULT.name().equals(this.name()) ?
            String.format("[%s%s%s] ", this.teamColor, this.name(), ChatColor.RESET) : StringUtils.EMPTY;
    }
    
    /**
     * Gets the color
     * @return
     * @author kvnamo
     */
    public ChatColor getColor(){
        return this.teamColor;
    }
}
