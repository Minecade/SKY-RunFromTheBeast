/**
 * 
 */
package com.minecade.rfb.worlds;

import com.minecade.engine.MapLocation;
import com.minecade.engine.MinecadePlugin;

/**
 * @author Jdgil
 *
 */
public class HaloRaceWorld extends RFBBaseWorld {

    public HaloRaceWorld(MinecadePlugin plugin) {
        
        super(plugin);
        
        // player v.s player enable
        world.setPVP(true);
        
        // set map spawn locations.
        addLocation(new MapLocation(540.5, 5, -869.4));
        addLocation(new MapLocation(540.5, 5, -871.5));
        addLocation(new MapLocation(538, 5, -871.5));
        addLocation(new MapLocation(538, 5, -869.4));
        
        setBeastSpawnLocation(new MapLocation(534.56, 5, -875.43));
        setSpectatorSpawnLocation(new MapLocation(523, 4, -872.8));
        
        addFreeRunnersLocation(new MapLocation(538, 5, -874));
        addFreeRunnersLocation(new MapLocation(538, 5, -876));
        addFreeRunnersLocation(new MapLocation(540, 5, -874));
        addFreeRunnersLocation(new MapLocation(540, 5, -876));
    }
}
