package com.minecade.rfb.worlds;

import com.minecade.engine.MapLocation;
import com.minecade.engine.MinecadePlugin;

public class LevelsWorld extends RFBBaseWorld {

    public LevelsWorld(MinecadePlugin plugin) {
        
        super(plugin);
        
        world.setPVP(true);
        
        // set map spawn locations.
        addLocation(new MapLocation(-9, 22, -12));
        addLocation(new MapLocation(-9, 22, -12));
        addLocation(new MapLocation(-9, 22, -12));
        addLocation(new MapLocation(-9, 22, -12));
        
        setBeastSpawnLocation(new MapLocation(-3.6, 22, -12.3));
        setSpectatorSpawnLocation(new MapLocation(-8.6, 20, -1.32));
        
        addFreeRunnersLocation(new MapLocation(-8.6, 20, -1.32));
        addFreeRunnersLocation(new MapLocation(-8.6, 20, 2.26));
        addFreeRunnersLocation(new MapLocation(-10.30, 20, 2.26));
        addFreeRunnersLocation(new MapLocation(-10.30, 20, -1.32));
    }

}
