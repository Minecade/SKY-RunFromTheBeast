package com.minecade.rfb.worlds;

import com.minecade.engine.MapLocation;
import com.minecade.engine.MinecadePlugin;

public class DamnedTunnelsWorld extends RFBBaseWorld{
    
public DamnedTunnelsWorld(MinecadePlugin plugin) {
        
        super(plugin);
        
        // player v.s player enable
        world.setPVP(true);
        
        // set map spawn locations.
        addLocation(new MapLocation(12, 33, -37));
        addLocation(new MapLocation(12, 33, -37));
        addLocation(new MapLocation(12, 33, -37));
        addLocation(new MapLocation(12, 33, -37));
        
        setBeastSpawnLocation(new MapLocation(36, 34, -37));
        
        addFreeRunnersLocation(new MapLocation(20, 62, -60));
        addFreeRunnersLocation(new MapLocation(20, 62, -60));
        addFreeRunnersLocation(new MapLocation(20, 62, -60));
        addFreeRunnersLocation(new MapLocation(20, 62, -60));
    }
}
