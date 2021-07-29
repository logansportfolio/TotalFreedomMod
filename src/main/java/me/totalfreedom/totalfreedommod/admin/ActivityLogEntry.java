package me.totalfreedom.totalfreedommod.admin;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import me.totalfreedom.totalfreedommod.config.IConfig;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ActivityLogEntry implements IConfig
{

    public static final String FILENAME = "activitylog.yml";
    private final List<String> ips = Lists.newArrayList();
    private final List<String> timestamps = Lists.newArrayList();
    private final List<String> durations = Lists.newArrayList();
    private String configKey;

    public ActivityLogEntry(Player player)
    {
        this.configKey = player.getUniqueId().toString();
    }

    public ActivityLogEntry(UUID uuid)
    {
        this.configKey = uuid.toString();
    }

    public ActivityLogEntry(String uuid)
    {
        this.configKey = uuid;
    }

    public static String getFILENAME()
    {
        return FILENAME;
    }

    public void loadFrom(Player player)
    {
        configKey = player.getName().toLowerCase();
    }

    @Override
    public void loadFrom(ConfigurationSection cs)
    {
        ips.clear();
        ips.addAll(cs.getStringList("ips"));
        timestamps.clear();
        timestamps.addAll(cs.getStringList("timestamps"));
        durations.clear();
        durations.addAll(cs.getStringList("durations"));
    }

    @Override
    public void saveTo(ConfigurationSection cs)
    {
        Validate.isTrue(isValid(), "Could not save activity entry: " + getUUID() + ". Entry not valid!");
        cs.set("uuid", getUUID().toString());
        cs.set("ips", Lists.newArrayList(ips));
        cs.set("timestamps", Lists.newArrayList(timestamps));
        cs.set("durations", Lists.newArrayList(durations));
    }

    public void addLogin()
    {
        Date currentTime = Date.from(Instant.now());
        timestamps.add("Login: " + FUtil.dateToString(currentTime));
    }

    public void addLogout()
    {
        // Fix of Array index out of bonds issue: FS-131
        String lastLoginString = "";
        if(timestamps.size() > 1)
        {
            lastLoginString = timestamps.get(timestamps.size() - 1);
        } else if (timestamps.size() == 1)
        {
            lastLoginString = timestamps.get(0);
        }
        Date currentTime = Date.from(Instant.now());
        timestamps.add("Logout: " + FUtil.dateToString(currentTime));
        lastLoginString = lastLoginString.replace("Login: ", "");
        Date lastLogin = FUtil.stringToDate(lastLoginString);

        long duration = currentTime.getTime() - lastLogin.getTime();
        long seconds = duration / 1000 % 60;
        long minutes = duration / (60 * 1000) % 60;
        long hours = duration / (60 * 60 * 1000);
        durations.add(hours + " hours, " + minutes + " minutes, and " + seconds + " seconds");
    }

    public void addIp(String ip)
    {
        if (!ips.contains(ip))
        {
            ips.add(ip);
        }
    }

    public void addIps(List<String> ips)
    {
        for (String ip : ips)
        {
            addIp(ip);
        }
    }

    public void removeIp(String ip)
    {
        ips.remove(ip);
    }

    public void clearIPs()
    {
        ips.clear();
    }

    public int getTotalSecondsPlayed()
    {
        int result = 0;
        for (String duration : durations)
        {
            String[] spl = duration.split(" ");
            result += Integer.parseInt(spl[0]) * 60 * 60;
            result += Integer.parseInt(spl[2]) * 60;
            result += Integer.parseInt(spl[5]);
        }
        return result;
    }

    @Override
    public boolean isValid()
    {
        return configKey != null
                && getUUID() != null;
    }

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public List<String> getIps()
    {
        return ips;
    }

    public List<String> getTimestamps()
    {
        return timestamps;
    }

    public List<String> getDurations()
    {
        return durations;
    }

    public UUID getUUID() {
        return UUID.fromString(configKey);
    }

    public String getName() {
        return FUtil.getNameFromUUID(getUUID());
    }
}
