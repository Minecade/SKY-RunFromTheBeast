package com.minecade.rfb.worlds;

import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.MinecadeWorld;

public class RFBLobbyWorld extends MinecadeWorld {

    public RFBLobbyWorld(MinecadePlugin plugin) {
        super("Run From The Beast Lobby", "runfrombeastlobby", plugin);
        // player v.s player enable
        world.setPVP(false);
    } 
}
