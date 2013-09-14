package com.minecade.rfb.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.MinecadeWorld;
import com.minecade.engine.command.CommandFactory;
import com.minecade.rfb.data.RFBPersistence;
import com.minecade.rfb.engine.RFBMatch;
import com.minecade.rfb.listener.RFBListener;
import com.minecade.rfb.worlds.DamnedTunnelsWorld;
import com.minecade.rfb.worlds.HaloRaceWorld;
import com.minecade.rfb.worlds.LevelsWorld;
import com.minecade.rfb.worlds.RFBLobbyWorld;

public class RunFromTheBeastPlugin extends MinecadePlugin {

    private MinecadeWorld lobby;
    
    private List<MinecadeWorld> runFromTheBeastWorlds = new ArrayList<MinecadeWorld>();
    
    private static final String RUNFROMTHEBEAST_COMMANDS_PACKAGE = "com.minecade.rfb.command";
    
    private RFBPersistence persistence;  
    
    public RFBPersistence getPersistence() {
        return persistence;
    }
    
    /**
     * Returns a random world.
     * @return
     * 
     */
    public MinecadeWorld getRandomWorld() {
        return runFromTheBeastWorlds.get(getRandom().nextInt(runFromTheBeastWorlds.size()));
    }
    
    /**
     * @return the lobby
     */
    public MinecadeWorld getLobby() {
        return lobby;
    }
    
    private RFBMatch match; 
    
    /**
     * Get match
     * @return match
     * @author kvnamo
     */
    public RFBMatch getMatch() {
        return match;
    }

    /**
     * (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     * @tip: 
     * - Handling Reload: you would need to find all players currently online during
     *   onEnable and store the correct information for that player in the HashMap.
     * @author: kvnamo
     */
    @Override
    public void onEnable(){
        super.onEnable();
        getLogger().info("onEnable has been invoked!");
        
        // Save config.yml default values and completes the new values from the jar file
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new RFBListener(this), this);
        
        // register commands
        CommandFactory.registerCommands(this, RUNFROMTHEBEAST_COMMANDS_PACKAGE);
        
        // Initialize persistence
        this.persistence = new RFBPersistence(this);
        
        // Initialize match.
        this.match = new RFBMatch(this);
        
        // Initialize Worlds
        getLogger().info("onEnable: Creating Worlds...");
        lobby = new RFBLobbyWorld(this); 
        runFromTheBeastWorlds.add(new HaloRaceWorld(this));
        runFromTheBeastWorlds.add(new DamnedTunnelsWorld(this));
        runFromTheBeastWorlds.add(new LevelsWorld(this));
        getLogger().info("onEnable: Worlds Created...");
        
        // Create or update server status in DB.
        this.persistence.createOrUpdateServer();
        
        // Register Bungeecord
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        
        // Send an announcement every 5 minutes
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                final String announcement = RunFromTheBeastPlugin.this.getRandomAnnouncement();
                for (final Player online : RunFromTheBeastPlugin.this.getServer().getOnlinePlayers()) {
                    online.sendMessage(announcement);
                }
            }
        }, 6000L, 6000L);

        // Update player count every 5 seconds if it has changed
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                RunFromTheBeastPlugin.this.persistence.updateServerPlayers();
            }
        }, 100L, 100L);
    }
    
    /**
     * Gets the random announcement.
     *
     * @return the random announcement
     */
    public String getRandomAnnouncement() {
        final List<String> announcements = getConfig().getStringList("server.announcements");
        return ChatColor.translateAlternateColorCodes('&', announcements.get(getRandom().nextInt(announcements.size())));
    }
    
   /**
    * (non-Javadoc)
    * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
    * @author: kvnamo
    */
    @Override
    public void onDisable() {
        getLogger().info("onDisable has been invoked!");
        // This will unregister all events from the specified plugin-
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Force match start
     * @author kvnamo
     */
    @Override
    public String forceStart() {
        return this.match.forceStartMatch();
    }
}
