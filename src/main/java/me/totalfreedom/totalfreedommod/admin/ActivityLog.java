package me.totalfreedom.totalfreedommod.admin;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;

import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.config.YamlConfig;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

//TODO: convert to uuids
public class ActivityLog extends FreedomService
{

    public static final String FILENAME = "activitylog.yml";

    private final Map<UUID, ActivityLogEntry> allActivityLogs = Maps.newHashMap();
    private final Map<UUID, ActivityLogEntry> activityLogs = Maps.newHashMap();
    private final Map<String, ActivityLogEntry> ipTable = Maps.newHashMap();

    private final YamlConfig config;

    public ActivityLog()
    {
        this.config = new YamlConfig(plugin, FILENAME, true);
    }

    public static String getFILENAME()
    {
        return FILENAME;
    }

    @Override
    public void onStart()
    {
        load();
    }

    @Override
    public void onStop()
    {
        save();
    }

    public void load()
    {
        config.load();

        allActivityLogs.clear();
        activityLogs.clear();
        ipTable.clear();
        for (String key : config.getKeys(false))
        {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null)
            {
                FLog.warning("Invalid activity log format: " + key);
                continue;
            }

            ActivityLogEntry activityLogEntry = new ActivityLogEntry(key);
            activityLogEntry.loadFrom(section);

            if (!activityLogEntry.isValid())
            {
                FLog.warning("Could not load activity log: " + key + ". Missing details!");
                continue;
            }

            allActivityLogs.put(UUID.fromString(key), activityLogEntry);
        }

        updateTables();
        FLog.info("Loaded " + allActivityLogs.size() + " activity logs");
    }

    public void save()
    {
        // Clear the config
        for (String key : config.getKeys(false))
        {
            config.set(key, null);
        }

        for (ActivityLogEntry activityLog : allActivityLogs.values())
        {
            activityLog.saveTo(config.createSection(activityLog.getConfigKey()));
        }

        config.save();
    }

    public ActivityLogEntry getActivityLog(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return getActivityLog((Player)sender);
        }

        return getEntryByUUID(FUtil.getUUIDFromName(sender.getName()));
    }

    public ActivityLogEntry getActivityLog(Player player)
    {
        ActivityLogEntry activityLog = getEntryByUUID(player.getUniqueId());
        if (activityLog == null)
        {
            String ip = FUtil.getIp(player);
            activityLog = getEntryByIp(ip);
            if (activityLog != null)
            {
                // Set the new username
                save();
                updateTables();
            }
            else
            {
                activityLog = new ActivityLogEntry(player);
                allActivityLogs.put(activityLog.getUUID(), activityLog);
                updateTables();

                activityLog.saveTo(config.createSection(activityLog.getConfigKey()));
                config.save();
            }
        }
        String ip = FUtil.getIp(player);
        if (!activityLog.getIps().contains(ip))
        {
            activityLog.addIp(ip);
            save();
            updateTables();
        }
        return activityLog;
    }

    public ActivityLogEntry getEntryByUUID(UUID uuid)
    {
        return activityLogs.get(uuid);
    }

    public ActivityLogEntry getEntryByIp(String ip)
    {
        return ipTable.get(ip);
    }

    public void updateTables()
    {
        activityLogs.clear();
        ipTable.clear();

        for (ActivityLogEntry activityLog : allActivityLogs.values())
        {
            activityLogs.put(activityLog.getUUID(), activityLog);

            for (String ip : activityLog.getIps())
            {
                ipTable.put(ip, activityLog);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (plugin.al.isAdmin(player))
        {
            getActivityLog(event.getPlayer()).addLogin();
            plugin.acl.save();
            plugin.acl.updateTables();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (plugin.al.isAdmin(player))
        {
            getActivityLog(event.getPlayer()).addLogout();
            plugin.acl.save();
            plugin.acl.updateTables();
        }
    }

    public Map<UUID, ActivityLogEntry> getAllActivityLogs()
    {
        return allActivityLogs;
    }

    public Map<UUID, ActivityLogEntry> getActivityLogs()
    {
        return activityLogs;
    }

    public Map<String, ActivityLogEntry> getIpTable()
    {
        return ipTable;
    }

    public YamlConfig getConfig()
    {
        return config;
    }
}