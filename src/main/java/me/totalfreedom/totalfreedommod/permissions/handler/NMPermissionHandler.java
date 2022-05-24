package me.totalfreedom.totalfreedommod.permissions.handler;

import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.milkbowl.vault.permission.Permission;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.NetworkManagerProvider;
import nl.chimpgamer.networkmanager.api.models.permissions.PermissionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
            return true;
        }
        PermissionPlayer permissionPlayer = this.plugin.getPermissionManager().getPermissionPlayer(player.getUniqueId());
        if (permissionPlayer == null)
        {
            FLog.debug("Unable to find permissions player in NetworkManager. Returning true.");
            return true;
        }
        Boolean has = permissionPlayer.hasPermission(permission);
        if (has == null)
        {
            FLog.debug("NetworkManager is idiotic and has a chance of returning null on a Boolean object. Returning true.");
            return true;
        }
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
        return permissionPlayer.getGroups().stream().anyMatch(group -> group.getName().equals(groupName));
    }

    @Override
    public boolean inGroup(@NotNull Player player, @Nullable String groupName)
    {
        return this.inGroup((OfflinePlayer) player, groupName);
    }
}
