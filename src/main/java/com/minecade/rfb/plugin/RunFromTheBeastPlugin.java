package com.minecade.rfb.plugin;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.command.CommandFactory;
import com.minecade.engine.utils.PassManager;
import com.minecade.rfb.data.RFBPersistence;
import com.minecade.rfb.engine.RFBGame;

import com.minecade.rfb.listener.RFBListener;

public class RunFromTheBeastPlugin extends MinecadePlugin {

    private static final String RUNFROMTHEBEAST_COMMANDS_PACKAGE = "com.minecade.rfb.command";
    private RFBPersistence persistence;

    private RFBGame game;

    public RFBPersistence getPersistence() {
        return persistence;
    }

    @Override
    public void onEnable() {
        super.setPassManager(new PassManager(this, "Beast"));
        super.onEnable();
        getLogger().info("onEnable has been invoked!");

        // Save config.yml default values and completes the new values from the
        // jar file
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        // Register listeners
        getServer().getPluginManager().registerEvents(new RFBListener(this), this);

        // register commands
        CommandFactory.registerCommands(this, RUNFROMTHEBEAST_COMMANDS_PACKAGE);

        // Initialize persistence
        this.persistence = new RFBPersistence(this);

        // initialize game
        this.game = new RFBGame(this);

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
        return ChatColor
                .translateAlternateColorCodes('&', announcements.get(getRandom().nextInt(announcements.size())));
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     * @author: kvnamo
     */
    @Override
    public void onDisable() {
        getLogger().info("onDisable has been invoked!");
        // This will unregister all events from the specified plugin-
        HandlerList.unregisterAll(this);
    }

    @Override
    public String forceStart() {
        return this.game.forceStartMatch();
    }
    
     public String beBeast(Player player) {
     //return this.match.forceBeBeast(player);
         return null;
     }

    /**
     * @return the game
     */
    public RFBGame getGame() {
        return game;
    }
}
