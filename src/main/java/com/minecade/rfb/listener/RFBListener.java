package com.minecade.rfb.listener;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.WorldInitEvent;

import com.minecade.rfb.engine.RFBMatch;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBListener implements Listener {
    
    private final RunFromTheBeastPlugin plugin;
    private final RFBMatch match;
    
    /**
     * Listener constructor.
     * @tip: All listeners must be registered in ButterSlapPlugin.
     * @author: jdgil
     */
    public RFBListener(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
        this.match = new RFBMatch(this.plugin);
    }
    
    /**
     * On world initialization
     * @param event
     * @author: jdgil
     */
    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        this.match.initWorld(event); 
        this.plugin.getServer().getLogger().info("onWorldInit");
    }
    
    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (!event.getPlayer().isOp() && GameMode.CREATIVE.equals(event.getNewGameMode())) {
            event.setCancelled(true);
        }
        this.plugin.getServer().getLogger().info("onPlayerGameModeChange");
    }
    
    /**
     * Call when a block breaks.
     * @param event
     * @author: jdgil
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        this.match.blockBreak(event);
    }
    
    /**
     * Called by PlayerJoinEvent when player joins the match.
     * @param playerJoinEvent
     * @author: jdgil
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Remove join message.
        event.setJoinMessage(null);
        this.match.playerJoin(event);
        this.plugin.getServer().getLogger().info("onPlayerJoin");
    }
    
    /**
     * Call by PlayerToggleFlightEvent on flight attempt
     * @param event
     * @author kvnamo
     */
    @EventHandler
    public void onFlightAttempt(PlayerToggleFlightEvent event) { 
        if(!GameMode.CREATIVE.equals(event.getPlayer().getGameMode())){
            this.match.superJump(event);
        }
        this.plugin.getServer().getLogger().info("onSuperJump");
    }
}
