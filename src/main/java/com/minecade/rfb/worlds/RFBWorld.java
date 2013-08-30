/**
 * 
 */
package com.minecade.rfb.worlds;

import com.minecade.engine.MapLocation;
import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.MinecadeWorld;

/**
 * @author Jdgil
 *
 */
public class RFBWorld extends MinecadeWorld{

    public RFBWorld(MinecadePlugin plugin) {
        
        super("Run From The Beast World", "runfrombeastworld", plugin);
        // player v.s player enable
        world.setPVP(true);
        
        // set map spawn locations.
        addLocation(new MapLocation(538, 5, -874));
        addLocation(new MapLocation(538, 5, -876));
        addLocation(new MapLocation(540, 5, -874));
        addLocation(new MapLocation(540, 5, -876));
        
    }

}
