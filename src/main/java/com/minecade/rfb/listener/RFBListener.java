package com.minecade.rfb.listener;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.WorldInitEvent;

import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBListener implements Listener {
    
    private final RunFromTheBeastPlugin plugin;
    
    /**
     * Listener constructor.
     * @tip: All listeners must be registered in ButterSlapPlugin.
     * @author: jdgil
     */
    public RFBListener(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
    }
    
    /**
     * On world initialization
     * @param event
     * @author: jdgil
     */
    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        this.plugin.getMatch().initWorld(event); 
        this.plugin.getServer().getLogger().info("onWorldInit");
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
        this.plugin.getMatch().playerJoin(event);
        this.plugin.getServer().getLogger().info("onPlayerJoin: " + event.getPlayer().getName());
    }
    
    /**
     * Called by PlayerQuitEvent when player exits the match.
     * @param playerQuitEvent
     * @author kvnamo
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        // Remove quit message.
        event.setQuitMessage(null);  
        this.plugin.getMatch().playerQuit(event);
        this.plugin.getServer().getLogger().info("onPlayerQuit");
    }
    
    /**
     * Called by PlayerToggleFlightEvent when player try to fly
     * @param PlayerToggleFlightEvent
     * @author jdgil
     */
    @EventHandler
    public void onPlayerToggleFlightEvent(PlayerToggleFlightEvent  event){
        this.plugin.getMatch().playerToggleFlight(event);
        this.plugin.getServer().getLogger().info("onPlayerToggleFlightEvent");
    }
    
    /**
     * Called by EntityShootBowEvent when player shoot an arrow.
     * @param EntityShootBowEvent
     * @author jdgil
     */
    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent  event){
        this.plugin.getMatch().entityShootBowEvent(event);
        this.plugin.getServer().getLogger().info("onEntityShootBowEvent");
    }
    
    /**
     * Called by InventoryPickupItemEvent when player pick up a item
     * @param playerQuitEvent
     * @author jdgil
     */
    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event){
        this.plugin.getMatch().inventoryOpenEvent(event);
        this.plugin.getServer().getLogger().info("onInventoryOpenEvent");
    }
    
    /**
     * Call when a entity is damage.
     * @param event
     * @author victorv, kvnamo
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        this.plugin.getMatch().entityDamage(event);
        this.plugin.getServer().getLogger().info("onEntityDamage");
    }
    
    /** 
     * Call by AsyncPlayerChatEvent on player chat
     * @param event
     * @author kvnamo
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        this.plugin.getMatch().chatMessage(event);
        this.plugin.getServer().getLogger().info("onPlayerChat");
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
     * @author kvnamo
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }
    
    /**
     * Calls when an item spawns on the ground.
     * 
     * @param event
     * @author kvnamo
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }
    
    /**
     * Calls when an player respawns.
     * 
     * @param event
     * @author jdgil
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.plugin.getMatch().playerRespawn(event);
        this.plugin.getServer().getLogger().info("onPlayerRespawn");
    }
    
    /**
     * Calls when an item is dropped.
     * 
     * @param event
     * @author kvnamo
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
        }
    
    /**
     * Call when a block breaks.
     * @param event
     * @author: jdgil
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        this.plugin.getMatch().blockBreak(event);
    }
    
    /**
     * Called by PlayerDeathEvent when player dies
     * @param playerDeathEvent
     * @author kvnamo
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Remove quit message.
        event.setDeathMessage(null);
        this.plugin.getMatch().playerDeath(event);
        plugin.getServer().getLogger().info(String.format("onPlayerDeath - Player: [%s]", event.getEntity()));
    }
    
    /**w
     * Call by PlayerInteractEvent handler when player does something.
     * @param playerInteractEvent
     * @author kvnamo
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            this.plugin.getMatch().rightClick(event);
        }
    }
}
