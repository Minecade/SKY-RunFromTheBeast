package com.minecade.rfb.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBListener implements Listener{

    private final RunFromTheBeastPlugin plugin;
    
    public RFBListener(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // remove join message
        event.setJoinMessage(null);
        // delegate logic to game
        this.plugin.getGame().onPlayerJoin(event.getPlayer());
    }
    
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        // delegate logic to game
        this.plugin.getGame().onEntityDamage(event);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.plugin.getGame().onPlayerRespawn(event);
    }
    
    @EventHandler
    public void onEntityPortalEnterEvent(EntityPortalEnterEvent event) {
        this.plugin.getGame().portalEvent(event);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        this.plugin.getGame().chatMessage(event);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.plugin.getGame().blockBreak(event);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.plugin.getGame().onPlayerInteract(event);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Remove quit message.
        event.setDeathMessage(null);
        this.plugin.getGame().onPlayerDeath(event);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // remove quit message
        event.setQuitMessage(null);
        // delegate logic to game
        this.plugin.getGame().onPlayerQuit(event);
    }
}
