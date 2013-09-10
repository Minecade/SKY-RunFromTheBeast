package com.minecade.rfb.data;

import java.util.Date;

import com.avaje.ebean.SqlUpdate;
import com.minecade.engine.data.MinecadeAccount;
import com.minecade.engine.data.MinecadePersistence;
import com.minecade.rfb.enums.RFBStatusEnum;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBPersistence extends MinecadePersistence { 

    private final RunFromTheBeastPlugin plugin;
    private int lastPlayerCount;

    /**
     * BSPersistence constructor
     * @param plugin
     * @author: kvnamo
     */
    public RFBPersistence(final RunFromTheBeastPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }
    
    public ServerModel getServerById(long serverId) {
        return plugin.getDatabase().find(ServerModel.class).where().eq("serverId", serverId).findUnique();
    }

    /**
     * Update server players.
     */
    public void updateServerPlayers() {
        final int playerCount = this.plugin.getServer().getOnlinePlayers().length;
        if (playerCount == this.lastPlayerCount) {
            return;
        }
        
        this.lastPlayerCount = playerCount;
        
        String dml = "update servers set online_players=:online_players where id = :id";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(dml)
                .setParameter("online_players", playerCount)
                .setParameter("id", getServerId());
        update.execute();
    }
    
    /**
     * Gets the player by name.
     * @param playerName
     * @author kvnamo
     */
    public PlayerModel getPlayer(String playerName){
        // Find player in database
        PlayerModel playerModel = this.plugin.getDatabase().find(PlayerModel.class)
            .where().eq("username", playerName).findUnique();
        
        // Creates a new bean that is managed by bukkit
        if(playerModel == null){ 
            playerModel = this.plugin.getDatabase().createEntityBean(PlayerModel.class);
            playerModel.setUsername(playerName);
            playerModel.setKills(0);
            playerModel.setDeaths(0);
            playerModel.setWins(0);
            playerModel.setLosses(0);
            playerModel.setTimePlayed(0); 
            playerModel.setLastSeen((new Date()));  
            
            // Stores the bean
            this.plugin.getDatabase().save(playerModel);
        }
        
        return playerModel;
    }
    
    /**
     * Gets the minecade account
     * @param playerName
     * @return MinecadeAccount
     */
    public MinecadeAccount getMinecadeAccount(String playerName){
        return super.getMinecadeAccount(playerName);
    }

    /**
     * Update server players.
     *
     * @param serverState the server state
     */
    public void updateServerStatus(RFBStatusEnum serverState) {
        StringBuilder dml = new StringBuilder("update servers set state=:state ");
        
        if(serverState == RFBStatusEnum.RESTARTING) {
            dml.append(", online_players=:online_players ");
        }        
        
        dml.append("where id = :id");
        
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(dml.toString())
                .setParameter("state", serverState.toString())
                .setParameter("id", getServerId());
        
        if(serverState == RFBStatusEnum.RESTARTING) {
            update.setParameter("online_players", 0);
        }
        
        update.execute();        
    }
    
    /**
     * Update player
     * @param playerModel
     * @author jdgil
     */
    public void updatePlayer(PlayerModel playerModel){
        this.plugin.getDatabase().update(playerModel);
    }

    /**
     * Creates the or update server in the db.
     */
    public void createOrUpdateServer() {
        ServerModel server = getServerById(getServerId());
        if(server == null) {
         // create a new bean that is managed by bukkit
            server = plugin.getDatabase().createEntityBean(ServerModel.class);
            server.setServerId(getServerId());
        }        

        server.setMaxPlayers(plugin.getConfig().getInt("match.required-players"));
        server.setOnlinePlayers(0);
        server.setState(RFBStatusEnum.WAITING_FOR_PLAYERS);
        
        // store the bean
        plugin.getDatabase().save(server);
    }

    /**
     * Gets the server id.
     * 
     * @return the server id
     */
    private int getServerId() {
        return plugin.getConfig().getInt("server.id");
    }
}
