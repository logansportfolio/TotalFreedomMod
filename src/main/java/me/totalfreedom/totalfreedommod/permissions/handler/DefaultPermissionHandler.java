package me.totalfreedom.totalfreedommod.permissions.handler;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Taah
 * @project TotalFreedomMod
 * @since 9:10 PM [23-05-2022]
 */
public class DefaultPermissionHandler implements IPermissionHandler
{
    @Override
    public boolean hasPermission(@NotNull OfflinePlayer player, @Nullable String permission)
    {
        if (permission == null)
        {
            return true;
        }
        throw new UnsupportedOperationException("Unable to use Bukkit's native permission system for permissions!");
    }

    @Override
    public boolean hasPermission(@NotNull Player player, @Nullable String permission)
    {
        return permission == null || player.hasPermission(permission);
    }

    @Override
    public boolean inGroup(@NotNull OfflinePlayer player, @Nullable String groupName)
    {
        throw new UnsupportedOperationException("Unable to use Bukkit's native permission system for groups!");
    }

    @Override
    public boolean inGroup(@NotNull Player player, @Nullable String groupName)
    {
        throw new UnsupportedOperationException("Unable to use Bukkit's native permission system for groups!");
    }

    @Override
    public String getPrimaryGroup(@NotNull Player player)
    {
        throw new UnsupportedOperationException("Unable to use Bukkit's native permission system for groups!");
    }

    @Override
    public String[] getGroups()
    {
        throw new UnsupportedOperationException("Unable to use Bukkit's native permission system for groups!");
    }
}
