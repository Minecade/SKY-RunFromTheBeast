package com.minecade.rfb.task;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import com.minecade.rfb.engine.RFBPlayer;

public class SuperJumpTask extends BukkitRunnable {

    private RFBPlayer player;
    
    /**
     * SuperJumpTask constructor
     * @param match
     * @author kvnamo
     */
    public SuperJumpTask(RFBPlayer player){
        this.player = player;
    }
    
    /**
     * Sync task runned by bukkit scheduler
     * @author kvnamo
     */
    @Override
    public void run() { 
        if(!Material.AIR.equals(this.player.getBukkitPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType())){
            this.player.setInAir(false);
            super.cancel();
        }
    }
}
