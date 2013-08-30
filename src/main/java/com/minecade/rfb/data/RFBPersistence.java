package com.minecade.rfb.data;

import com.avaje.ebean.SqlUpdate;
import com.minecade.engine.data.MinecadePersistence;
import com.minecade.rfb.enums.RFBStatus;
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
     * Update server players.
     *
     * @param serverState the server state
     */
    public void updateServerStatus(RFBStatus serverState) {
        StringBuilder dml = new StringBuilder("update servers set state=:state ");
        
        if(serverState == RFBStatus.RESTARTING) {
            dml.append(", online_players=:online_players ");
        }        
        
        dml.append("where id = :id");
        
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(dml.toString())
                .setParameter("state", serverState.toString())
                .setParameter("id", getServerId());
        
        if(serverState == RFBStatus.RESTARTING) {
            update.setParameter("online_players", 0);
        }
        
        update.execute();        
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

        server.setMaxPlayers(plugin.getConfig().getInt("server.max-players"));
        server.setOnlinePlayers(0);
        server.setState(RFBStatus.WAITING_FOR_PLAYERS);
        
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
