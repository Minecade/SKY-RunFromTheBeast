/**
 * 
 */
package com.minecade.rfb.worlds;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.minecade.engine.MapLocation;
import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.MinecadeWorld;

/**
 * @author VictorV
 *
 */
public class RFBBaseWorld extends MinecadeWorld {
    
    public Location beastSpawnLocation;
    private final List<Location> runnersFreeSpawnLocation = new ArrayList<>(); 

    /**
     * @param worldName
     * @param worldLocation
     * @param plugin
     */
    public RFBBaseWorld(String worldName, String worldLocation, MinecadePlugin plugin) {
        super(worldName, worldLocation, plugin);
        
    }
    
    /**
     * Instantiates a new rFB base world.
     *
     * @param plugin the plugin
     */
    public RFBBaseWorld(MinecadePlugin plugin) {
        super(plugin);
    }

    /**
     * @return the beastSpawnLocation
     */
    public Location getBeastSpawnLocation() {
        return beastSpawnLocation;
    }

    /**
     * @param beastSpawnLocation the beastSpawnLocation to set
     */
    public void setBeastSpawnLocation(MapLocation location) {
        this.beastSpawnLocation = location.toLocation(world);
    }

    /**
     * Adds a location to possible entity spawn points.
     * @param location 
     */
    public void addFreeRunnersLocation(MapLocation location) {
        runnersFreeSpawnLocation.add(location.toLocation(world));
    }
    
    /**
     * Returns a spawn point for entities from a list.
     * @return 
     */
    public Location getFreeRunnersRandomSpawn() {
        return runnersFreeSpawnLocation.get(plugin.getRandom().nextInt(runnersFreeSpawnLocation.size()));
    }
}
