package me.totalfreedom.totalfreedommod.player;

import com.google.common.collect.Maps;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerList extends FreedomService
{

    public final Map<UUID, FPlayer> playerMap = Maps.newHashMap(); // uuid,dataMap
    //public final Map<String, PlayerData> dataMap = Maps.newHashMap(); // username, data

    @Override
    public void onStart()
    {
        loadMasterBuilders();
    }

    @Override
    public void onStop()
    {
    }

    public FPlayer getPlayerSync(Player player)
    {
        synchronized (playerMap)
        {
            return getPlayer(player);
        }
    }

    public void loadMasterBuilders()
    {
        ResultSet resultSet = plugin.sql.getMasterBuilders();

        if (resultSet == null)
        {
            return;
        }

        try
        {
            while (resultSet.next())
            {
                PlayerData playerData = load(resultSet);
            }
        }
        catch (SQLException e)
        {
            FLog.severe("Failed to parse master builders: " + e.getMessage());
        }
    }

    public String getIp(OfflinePlayer player)
    {
        if (player.isOnline())
        {
            return FUtil.getIp(Objects.requireNonNull(player.getPlayer()));
        }

        final PlayerData entry = getData(player.getUniqueId());

        return (entry == null ? null : entry.getIps().iterator().next());
    }

    public List<String> getMasterBuilderNames()
    {
        return playerMap.values().stream().map(FPlayer::getPlayerData).filter(PlayerData::isMasterBuilder).map(p -> FUtil.getNameFromUUID(p.getUniqueId())).collect(Collectors.toList());
    }

    public boolean canManageMasterBuilders(UUID uuid)
    {
        PlayerData data = getData(uuid);
        String name = FUtil.getNameFromUUID(uuid);

        return (!ConfigEntry.HOST_SENDER_NAMES.getStringList().contains(name.toLowerCase()) && data != null && !ConfigEntry.SERVER_OWNERS.getStringList().contains(data.getName()))
                && !ConfigEntry.SERVER_EXECUTIVES.getStringList().contains(data.getName())
                && !isTelnetMasterBuilder(data)
                && !ConfigEntry.HOST_SENDER_NAMES.getStringList().contains(name.toLowerCase());
    }

    public boolean isTelnetMasterBuilder(PlayerData playerData)
    {
        Admin admin = plugin.al.getEntryByUUID(playerData.getUniqueId());
        return admin != null && admin.getRank().isAtLeast(Rank.ADMIN) && playerData.isMasterBuilder();
    }

    // May not return null
    public FPlayer getPlayer(Player player)
    {
        FPlayer tPlayer = playerMap.get(player.getUniqueId());
        if (tPlayer != null)
        {
            return tPlayer;
        }

        tPlayer = new FPlayer(plugin, player);
        playerMap.put(player.getUniqueId(), tPlayer);

        return tPlayer;
    }

    /*public PlayerData loadByName(String name)
    {
        return load(plugin.sql.getPlayerByName(name));
    }

    public PlayerData loadByIp(String ip)
    {
        return load(plugin.sql.getPlayerByIp(ip));
    }*/

    public PlayerData loadByUUID(UUID uuid)
    {
        return load(plugin.sql.getPlayerByUUID(uuid));
    }

    private PlayerData loadByIp(String ip)
    {
        return load(plugin.sql.getPlayerByIp(ip));
    }

    public PlayerData load(ResultSet resultSet)
    {
        if (resultSet == null)
        {
            return null;
        }
        return new PlayerData(resultSet);
    }

    public Boolean isPlayerImpostor(Player player)
    {
        PlayerData playerData = getData(player);
        return plugin.dc.enabled
                && !plugin.al.isAdmin(player)
                && (playerData.hasVerification())
                && !playerData.getIps().contains(FUtil.getIp(player));
    }

    public boolean IsImpostor(Player player)
    {
        return isPlayerImpostor(player) || plugin.al.isAdminImpostor(player);
    }

    public void verify(Player player, String backupCode)
    {
        PlayerData playerData = getData(player);
        if (backupCode != null)
        {
            playerData.removeBackupCode(backupCode);
        }

        playerData.addIp(FUtil.getIp(player));
        save(playerData);

        if (plugin.al.isAdminImpostor(player))
        {
            Admin admin = plugin.al.getEntryByUUID(player.getUniqueId());
            admin.setLastLogin(new Date());
            admin.addIp(FUtil.getIp(player));
            plugin.al.updateTables();
            plugin.al.save(admin);
        }

        plugin.rm.updateDisplay(player);
    }

    public void syncIps(Admin admin)
    {
        PlayerData playerData = getData(admin.getUniqueId());
        playerData.clearIps();
        playerData.addIps(admin.getIps());
        plugin.pl.save(playerData);
    }

    public void syncIps(PlayerData playerData)
    {
        Admin admin = plugin.al.getEntryByUUID(playerData.getUniqueId());

        if (admin != null && admin.isActive())
        {
            admin.clearIPs();
            admin.addIps(playerData.getIps());
            plugin.al.updateTables();
            plugin.al.save(admin);
        }
    }


    public void save(PlayerData player)
    {
        try
        {
            ResultSet currentSave = plugin.sql.getPlayerByUUID(player.getUniqueId());
            for (Map.Entry<String, Object> entry : player.toSQLStorable().entrySet())
            {
                Object storedValue = plugin.sql.getValue(currentSave, entry.getKey(), entry.getValue());
                if (storedValue != null && !storedValue.equals(entry.getValue()) || storedValue == null && entry.getValue() != null || entry.getValue() == null)
                {
                    plugin.sql.setPlayerValue(player, entry.getKey(), entry.getValue());
                }
            }
        }
        catch (SQLException e)
        {
            FLog.severe("Failed to save player: " + e.getMessage());
        }
    }

    public PlayerData getData(Player player)
    {
        // Check for existing data
        PlayerData playerData = playerMap.get(player.getUniqueId()).getPlayerData();
        if (playerData != null)
        {
            return playerData;
        }

        // Load data
        playerData = loadByUUID(player.getUniqueId());

        /*if (playerData == null)
        {
            playerData = loadByIp(FUtil.getIp(player));
            if (playerData != null)
            {
                plugin.sql.updatePlayerName(playerData.getName(), player.getName());
                playerMap.get(player.getUniqueId()).setPlayerData(playerData);
                return playerData;
            }
        }
        else
        {
            playerMap.get(player.getUniqueId()).setPlayerData(playerData);
            return playerData;
        }*/

        playerMap.get(player.getUniqueId()).setPlayerData(playerData);

        // Create new data if nonexistent
        FLog.info("Creating new player verification entry for " + player.getName());

        // Create new player
        playerData = new PlayerData(player);
        playerData.addIp(FUtil.getIp(player));

        // Store player
        playerMap.get(player.getUniqueId()).setPlayerData(playerData);

        // Save player
        plugin.sql.addPlayer(playerData);
        return playerData;

    }

    /*@Deprecated
    public PlayerData getData(String username)
    {
        throw new UnsupportedOperationException();
        // Check for existing data
        PlayerData playerData = dataMap.get(username);
        if (playerData != null)
        {
            return playerData;
        }

        playerData = loadByName(username);

        if (playerData != null)
        {
            dataMap.put(username, playerData);
        }
        else
        {
            return null;
        }

        return playerData;
    }*/

    public PlayerData getData(UUID uuid)
    {
        PlayerData playerData = playerMap.get(uuid).getPlayerData();
        if (playerData != null)
        {
            return playerData;
        }

        playerData = loadByUUID(uuid);

        if (playerData != null)
        {
            playerMap.get(uuid).setPlayerData(playerData);
        }
        else
        {
            return null;
        }

        return playerData;
    }

    public PlayerData getDataByUUID(UUID uuid)
    {
        PlayerData player = loadByUUID(uuid);

        if (player != null)
        {
            playerMap.get(uuid).setPlayerData(player);
        }

        return player;
    }

     public PlayerData getDataByIp(String ip)
    {
        PlayerData player = loadByIp(ip);

        if (player != null)
        {
            playerMap.get(player.getUniqueId()).setPlayerData(player);
        }

        return player;
    }

    public Map<UUID, FPlayer> getPlayerMap()
    {
        return playerMap;
    }

}