package me.totalfreedom.totalfreedommod.permissions.handler;

import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
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
public class VaultPermissionHandler implements IPermissionHandler
{
    private Permission permissions;
    private Chat chat;

    public VaultPermissionHandler(TotalFreedomMod plugin)
    {
        if (plugin.permissionHandler != null)
        {
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault"))
        {
            plugin.permissionHandler = new DefaultPermissionHandler();
            return;
        }

        this.permissions = setupPerms(plugin);
        this.chat = setupChat(plugin);

        plugin.permissionHandler = this;
    }

    @Override
    public boolean hasPermission(@NotNull OfflinePlayer player, @Nullable String permission)
    {
        if (this.permissions == null)
        {
            FLog.debug("Can't use Vault permissions system, there is no plugin using Vault permissions.");
            return true;
        }
        return permission == null || this.permissions.playerHas(null, player, permission);
    }

    @Override
    public boolean hasPermission(@NotNull Player player, @Nullable String permission)
    {
        if (this.permissions == null)
        {
            FLog.debug("Can't use Vault permissions system, there is no plugin using Vault permissions.");
            return true;
        }
        return permission == null || this.permissions.playerHas(player, permission);
    }

    @Override
    public boolean inGroup(@NotNull OfflinePlayer player, @Nullable String groupName)
    {
        if (this.permissions == null)
        {
            FLog.debug("Can't use Vault permissions system for groups, there is no plugin using Vault permissions.");
            return true;
        }
        if (groupName == null)
        {
            FLog.debug("Vault Perms: Group name is null, returning false for group check");
            return false;
        }
        return this.permissions.playerInGroup(null, player, groupName);
    }

    @Override
    public boolean inGroup(@NotNull Player player, @Nullable String groupName)
    {
        if (this.permissions == null)
        {
            FLog.debug("Can't use Vault permissions system for groups, there is no plugin using Vault permissions.");
            return false;
        }
        if (groupName == null)
        {
            FLog.debug("Vault Perms: Group name is null, returning false for group check");
            return false;
        }
        return this.permissions.playerInGroup(player, groupName);
    }

    @Override
    public String[] getGroups()
    {
        if (this.permissions == null)
        {
            FLog.debug("Can't use Vault permissions system for group listing, there is no plugin using Vault permissions.");
            return new String[0];
        }
        return this.permissions.getGroups();
    }

    @Override
    public String getPrimaryGroup(@NotNull Player player)
    {
        return this.permissions.getPrimaryGroup(player);
    }

    @Override
    public String getPrefix(@NotNull Player player)
    {
        if (this.chat == null)
        {
            return "[No Chat Plugin]";
        }
        return this.chat.getPlayerPrefix(player);
    }

    public Permission getPermissions()
    {
        return permissions;
    }

    private Permission setupPerms(TotalFreedomMod plugin)
    {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null)
        {
            FLog.warning("Switching back to Bukkit's default permissions from Vault's due to no permission system found.");
            plugin.permissionHandler = new DefaultPermissionHandler();
            return null;
        }
        return rsp.getProvider();
    }

    private Chat setupChat(TotalFreedomMod plugin)
    {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null)
        {
//            FLog.warning("Switching back to Bukkit's default permissions from Vault's due to no permission system found.");
//            plugin.permissionHandler = new DefaultPermissionHandler();
            return null;
        }
        return rsp.getProvider();
    }
}
