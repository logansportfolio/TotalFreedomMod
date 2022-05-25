package me.totalfreedom.totalfreedommod.permissions.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.milkbowl.vault.permission.Permission;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.NetworkManagerProvider;
import nl.chimpgamer.networkmanager.api.models.permissions.Group;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;


/**
 * @author Taah
 * @project TotalFreedomMod
 * @since 9:10 PM [23-05-2022]
 */
public class NMPermissionHandler implements IPermissionHandler
{
    private NetworkManagerPlugin plugin;

    public NMPermissionHandler(TotalFreedomMod plugin)
    {
        if (plugin.permissionHandler != null)
        {
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("NetworkManager"))
        {
            plugin.permissionHandler = new DefaultPermissionHandler();
            return;
        }
        this.plugin = NetworkManagerProvider.get();
    }

    @Override
    public boolean hasPermission(@NotNull OfflinePlayer player, @Nullable String permission)
    {
        if (permission == null)
        {
            FLog.debug("Permission was null!");
            return true;
        }
        PermissionPlayer permissionPlayer = this.plugin.getPermissionManager().getPermissionPlayer(player.getUniqueId());
        if (permissionPlayer == null)
        {
            FLog.debug("Unable to find permissions player in NetworkManager. Returning false.");
            return false;
        }
        if (permissionPlayer.getAllPermissions() == null)
        {
            FLog.debug("Screw you NetworkManager for telling me all the permissions for player is null.");
            return false;
        }
        Boolean has = permissionPlayer.hasPermission(permission);
        if (has == null)
        {
//            FLog.debug("NetworkManager is idiotic and has a chance of returning null on a Boolean object. Returning false.");
            return false;
        }
//        FLog.debug("Player has perm? " + has);
//        FLog.debug("Player permissions for: " + permissionPlayer.getUuid() + "\n" + new GsonBuilder().setPrettyPrinting().create().toJson(permissionPlayer.getAllPermissions()));
//        permissionPlayer.getPermissions().forEach(permission1 -> FLog.debug(String.format("%s:%s", permission1.getPermissionString(), permission1.hasExpired())));
        return has;
    }

    @Override
    public boolean hasPermission(@NotNull Player player, @Nullable String permission)
    {
        return hasPermission((OfflinePlayer) player, permission);
    }

    @Override
    public boolean inGroup(@NotNull OfflinePlayer player, @Nullable String groupName)
    {
        if (groupName == null)
        {
            FLog.debug("NM Perms: Setting permission access to false, group is null");
            return false;
        }
        PermissionPlayer permissionPlayer = this.plugin.getPermissionManager().getPermissionPlayer(player.getUniqueId());
        if (permissionPlayer == null)
        {
            FLog.debug("NM Perms: Setting permission access to false, player not found in NM for '" + player.getUniqueId() + "'");
            return false;
        }
        if (permissionPlayer.getGroups().isEmpty())
        {
            FLog.debug("NM Perms: Setting permission access to false, player groups are empty for '" + player.getUniqueId() + "'");
            return false;
        }
//        FLog.debug("Group Name requested for: " + player.getUniqueId() + " - " + groupName);
//        FLog.debug("Player has? " + (permissionPlayer.getGroups().stream().anyMatch(group -> group.getName().equals(groupName))));
//        permissionPlayer.getGroups().forEach(group -> FLog.debug("Player group: " + group.getName()));
        return permissionPlayer.getGroups().stream().anyMatch(group -> group.getName().equals(groupName));
    }

    @Override
    public boolean inGroup(@NotNull Player player, @Nullable String groupName)
    {
        return this.inGroup((OfflinePlayer) player, groupName);
    }

    @Override
    public String[] getGroups()
    {
        return this.plugin.getPermissionManager().getGroups().values().stream().map(Group::getName).toArray(String[]::new);
    }

    @Override
    public String getPrimaryGroup(@NotNull Player player)
    {
        PermissionPlayer permissionPlayer = this.plugin.getPermissionManager().getPermissionPlayer(player.getUniqueId());
        if (permissionPlayer == null || permissionPlayer.getPrimaryGroup() == null)
        {
            FLog.warning("NM Perms: Couldn't find player's primary group due to them not be found our list");
            return ConfigEntry.PERMISSIONS_GROUPS_DEFAULT.getString();
        }
//        permissionPlayer.getGroups().forEach(group -> FLog.debug(String.format("Group for %s: %s", permissionPlayer.getUuid(), group.getName())));
//        FLog.debug(String.format("%s Primary Group: %s", permissionPlayer.getUuid(), permissionPlayer.getGroups().stream().sorted(Comparator.comparingInt(Group::getRank)).map(Group::getName).findFirst().orElse(ConfigEntry.PERMISSIONS_GROUPS_DEFAULT.getString())));
        return permissionPlayer.getGroups().stream().sorted((o1, o2) -> o1.getRank() - o2.getRank()).map(Group::getName).findFirst().orElse(ConfigEntry.PERMISSIONS_GROUPS_DEFAULT.getString());
    }

    @Override
    public String getPrefix(@NotNull Player player)
    {
        PermissionPlayer permissionPlayer = this.plugin.getPermissionManager().getPermissionPlayer(player.getUniqueId());
        if (permissionPlayer == null)
        {
            FLog.warning("NM Perms: Couldn't find player's primary group due to them not be found our list");
            return "[Error Loading Player]";
        }
        Group group = this.plugin.getPermissionManager().getGroup(getPrimaryGroup(player));
        if (group == null)
        {
            return "[Error Finding Group]";
        }
        return group.getPrefix(null);
    }
}
