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
        addLocation(new MapLocation(538, 5, -874));
        addLocation(new MapLocation(538, 5, -876));
        addLocation(new MapLocation(540, 5, -874));
        addLocation(new MapLocation(540, 5, -876));
        
        setBeastSpawnLocation(new MapLocation(459, 4, -915));
    }

}
