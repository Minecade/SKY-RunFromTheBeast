package com.minecade.rfb.task;

import org.bukkit.scheduler.BukkitRunnable;

import com.minecade.rfb.engine.RFBMatch;

public class TimerTask extends BukkitRunnable{

    private RFBMatch match;
    private int countdown;
    private boolean initMatch;
    private boolean startMatch;
    private boolean endMatch;
    
    /**
     * Timer task constructor
     * @param match
     * @param delay
     * @author jdgil
     */
    public TimerTask(RFBMatch match, int countdown, boolean initMatch, boolean startMatch, boolean endMatch){
        this.match = match;
        this.countdown = countdown;
        this.initMatch = initMatch;
        this.startMatch = startMatch;
        this.endMatch = endMatch;
    }
    
    /**
     * Sync task runned by bukkit scheduler
     * @author kvnamo
     */
    @Override
    public void run() {
        if(this.countdown <= 0){
            if(this.initMatch){
                this.match.initMatch();
            }
            else if(this.startMatch){
                this.match.startMatch();
            }
            else if(this.endMatch){
                this.match.stopGame();
            }
            super.cancel();
        }
        this.match.setRemainingTime(this.countdown--);
    }
}

