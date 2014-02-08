package com.minecade.rfb.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.minecade.engine.data.MinecadeAccount;
import com.minecade.engine.data.MinecadePersistence;
import com.minecade.rfb.engine.RFBGame;
import com.minecade.rfb.engine.RFBGame.Status;
import com.minecade.rfb.engine.RFBPlayer;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;
import com.minecade.rfb.util.Callback;

public class RFBPersistence extends MinecadePersistence {

    private final RunFromTheBeastPlugin plugin;
    private ServerModel serverModel;
    private int lastPlayerCount;

    public RFBPersistence(final RunFromTheBeastPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }
    
    public void loadPlayerDataAsynchronously(final Player player, final Callback<RFBPlayer> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (isPlayerBanned(player.getName())) {
                        // TODO it would be better if we queried this just once
                        // at startup...
                        callback.error(plugin.getConfig().getString("server.ban-message"));
                    } else {
                        final PlayerModel playerModel = getPlayerModel(player.getName());
                        final MinecadeAccount minecadeAccount = getMinecadeAccount(player.getName());
                        final RFBPlayer rfbPlayer = new RFBPlayer(plugin, player);
                        rfbPlayer.setPlayerModel(playerModel);
                        rfbPlayer.setMinecadeAccount(minecadeAccount);
                        callback.addResult(rfbPlayer);
                        callback.done();
                    }
                } catch (Exception e) {
                    callback.error(String.format("Error while loading player data for: [%s] - %s", player.getName(),
                            e.getMessage()));
                }
                // re-synchronize callback to run in the main server thread
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }
    
    public PlayerModel getPlayerModel(String username){
        if (StringUtils.isNotBlank(username) && this.connect()) {
            username = username.toLowerCase();
            PreparedStatement stmt = null;
            ResultSet set = null;
            PlayerModel model = new PlayerModel();
            model.setUsername(username);
            try {
                stmt = connection.prepareStatement("SELECT * FROM run_beast.players WHERE username = ?;");
                stmt.setString(1, username);
                set = stmt.executeQuery();
                if (!set.first()) {
                    stmt.close();
                    stmt = this.connection.prepareStatement("INSERT INTO run_beast.players(username) VALUES (?)");
                    stmt.setString(1, username);
                    stmt.executeUpdate();
                } else {
                    model.setId(set.getInt("id"));
                    model.setKills(set.getInt("kills"));
                    model.setDeaths(set.getInt("deaths"));
                    model.setSuicides(set.getInt("suicides"));
                    model.setWins(set.getInt("win"));
                    model.setLosses(set.getInt("losses"));
                    model.setLastSeen(set.getTimestamp("last_seen"));
                    model.setTimePlayed(set.getInt("time_played"));
                    model.setBeastPass(set.getTimestamp("beast_pass"));
                    model.setButterCoins(set.getInt("butter_coins"));
                }
                return model;
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Failed to load data from database for player: " + username + ", error: " + ex.getMessage());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (set != null) {
                        set.close();
                    }
                } catch (final SQLException e) {
                    this.plugin.getLogger().severe("Error closing resources while loading from the database for player: " + username);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public void updatePlayerAsynchronously(final PlayerModel playerModel){
        // Update player model on DB
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            // TODO upgrade this with possible callback to notify the player in
            // case of error
            @Override
            public void run() {
                if (playerModel != null && StringUtils.isNotBlank(playerModel.getUsername()) && RFBPersistence.this.connect()) {
                    playerModel.setUsername(playerModel.getUsername().toLowerCase());
                    PreparedStatement stmt = null;
                    try {
                        stmt = RFBPersistence.this.connection.prepareStatement("UPDATE run_beast.players SET kills = ?, deaths = ?, " +
                                "suicides = ?, win = ?, losses = ?, last_seen = ?, time_played = ?, butter_coins = ? WHERE run_beast.players.username = ?");
                        stmt.setLong(1, playerModel.getKills());
                        stmt.setLong(2, playerModel.getDeaths());
                        stmt.setLong(3, playerModel.getSuicides());
                        stmt.setLong(4, playerModel.getWins());
                        stmt.setLong(5, playerModel.getLosses());
                        stmt.setTimestamp(6, new Timestamp(new Date().getTime()));
                        stmt.setInt(7, playerModel.getTimePlayed());
                        stmt.setInt(8, playerModel.getButterCoins());
                        stmt.setString(9, playerModel.getUsername());
                        stmt.executeUpdate();
                        
                    } catch (Exception ex) {
                        RFBPersistence.this.plugin.getLogger().severe("Failed to update data to database for player: " + 
                            playerModel.getUsername() + ", error: " + ex.getMessage());
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                            }
                        } catch (final SQLException e) {
                            RFBPersistence.this.plugin.getLogger().severe("Error closing resources while loading from the database for player: " + 
                                playerModel.getUsername());
                            e.printStackTrace();
                        }
                    }
                    
                }
            }
        });
    }
    
    public ServerModel getServerById(long serverId) {
        if (this.connect()) {
            PreparedStatement stmt = null;
            ResultSet set = null;
            ServerModel model = new ServerModel();
            model.setServerId(serverId);
            try {
                stmt = connection.prepareStatement("SELECT * FROM run_beast.servers WHERE id = ?;");
                stmt.setLong(1, model.getServerId());
                set = stmt.executeQuery();
                if (set.first()) {
                    model.setMaxPlayers(set.getInt("max_players"));
                    model.setOnlinePlayers(set.getInt("online_players"));
                    model.setState(RFBGame.Status.valueOf(set.getString("state")));
                    return model;
                }
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Failed to load data from database for server: " + serverId + ", error: " + ex.getMessage());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (set != null) {
                        set.close();
                    }
                } catch (final SQLException e) {
                    this.plugin.getLogger().severe("Error closing resources while loading from the database for server: " + serverId);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public void updateServerPlayers() {
        final int playerCount = this.plugin.getServer().getOnlinePlayers().length;
        if (playerCount == this.lastPlayerCount) {
            return;
        }
        
        this.lastPlayerCount = playerCount;
        
        if (this.connect()) {
            PreparedStatement stmt = null;
            try {
                stmt = this.connection.prepareStatement("UPDATE run_beast.servers SET online_players = ? where id = ?");
                stmt.setInt(1, this.lastPlayerCount);
                stmt.setInt(2, getServerId());
                stmt.executeUpdate();
            } catch (Exception ex) {
                RFBPersistence.this.plugin.getLogger().severe("Failed to update data to database for server: " + 
                        getServerId() + ", error: " + ex.getMessage());
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (final SQLException e) {
                        RFBPersistence.this.plugin.getLogger().severe("Error closing resources while loading from the database for server: " + 
                                getServerId());
                        e.printStackTrace();
                    }
                }
        }
    }
    
    public void updateServerStatus(RFBGame.Status serverState) {
        
        if (serverState != null && this.connect()) {
            PreparedStatement stmt = null;
            try {
                stmt = this.connection.prepareStatement("UPDATE run_beast.servers SET state = ? where id = ?");
                stmt.setString(1, serverState.toString());
                stmt.setInt(2, getServerId());
                stmt.executeUpdate();
                
                
            } catch (Exception ex) {
                RFBPersistence.this.plugin.getLogger().severe("Failed to update data to database for server: " + 
                        getServerId() + ", error: " + ex.getMessage());
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (final SQLException e) {
                        RFBPersistence.this.plugin.getLogger().severe("Error closing resources while loading from the database for server: " + 
                                getServerId());
                        e.printStackTrace();
                    }
                }
        }
    }
    
    public void createOrUpdateServerAsynchronously() {
//      if (10 == this.plugin.getConfig().getInt("server.id")) {
//          return;
//      }
      if (serverModel == null) {
          serverModel = new ServerModel();
      }
      serverModel.setServerId(this.plugin.getConfig().getInt("server.id"));
      serverModel.setMaxPlayers(Bukkit.getServer().getMaxPlayers());
      serverModel.setOnlinePlayers(Bukkit.getServer().getOnlinePlayers().length);
      if (null == plugin.getGame().getNextMatch() || serverModel.getOnlinePlayers() >= serverModel.getMaxPlayers()) {
          serverModel.setWorldName("FULL");
          serverModel.setState(Status.FULL);
      } else {
          serverModel.setWorldName(plugin.getGame().getNextMatch().getName());
          serverModel.setState(Status.WAITING_FOR_PLAYERS);
      }
      
      final ServerModel server = serverModel;

      Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
          @Override
          public void run() {
           // create a new bean that is managed by bukkit
            if(RFBPersistence.this.connect()){
                PreparedStatement stmt = null;
                ResultSet set = null;
                try {
                    stmt = connection.prepareStatement("SELECT * FROM run_beast.servers WHERE id = ?;");
                    stmt.setLong(1, server.getServerId());
                    set = stmt.executeQuery();
                    if (!set.first()) {
                        stmt = RFBPersistence.this.connection.prepareStatement("INSERT INTO run_beast.servers " +
                                "(id, state, max_players, online_players, world_name) VALUES (?, ?, ?, ?, ?)");
                        stmt.setLong(1, server.getServerId());
                        stmt.setString(2, server.getState().toString());
                        stmt.setInt(3, server.getMaxPlayers());
                        stmt.setInt(4, server.getOnlinePlayers());
                        stmt.setString(5, server.getWorldName());
                        stmt.executeUpdate();
                    } else {
                        stmt.close();
                        stmt = RFBPersistence.this.connection.prepareStatement("UPDATE run_beast.servers SET state = ?, max_players = ?, " +
                        		"online_players = ?, world_name = ? where id = ?");
                        stmt.setString(1, server.getState().toString());
                        stmt.setInt(2, server.getMaxPlayers());
                        stmt.setInt(3, server.getOnlinePlayers());
                        stmt.setString(4, server.getWorldName());
                        stmt.setLong(5, server.getServerId());
                        stmt.executeUpdate();
                    }
                } catch (Exception ex) {
                    RFBPersistence.this.plugin.getLogger().severe("Failed to save data from database for server: " + getServerId() + ", error: " + ex.getMessage());
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        if (set != null) {
                            set.close();
                        }
                    } catch (final SQLException e) {
                        RFBPersistence.this.plugin.getLogger().severe("Error closing resources while loading from the database for server: " + getServerId());
                        e.printStackTrace();
                    }
                }
            }
          }
      });
  }
    
    private int getServerId() {
        return plugin.getConfig().getInt("server.id");
    }
}
