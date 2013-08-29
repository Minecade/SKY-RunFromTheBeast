package com.minecade.rfb.listener;

import org.bukkit.event.Listener;

import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBListener implements Listener {
    
    private final RunFromTheBeastPlugin plugin;
    
    /**
     * Listener constructor.
     * @tip: All listeners must be registered in ButterSlapPlugin.
     * @author: kvnamo
     */
    public RFBListener(RunFromTheBeastPlugin plugin){
        this.plugin = plugin;
    }
}
