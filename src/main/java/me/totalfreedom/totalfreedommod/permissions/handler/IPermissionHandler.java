package me.totalfreedom.totalfreedommod.permissions.handler;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Taah
 * @project TotalFreedomMod
 * @since 9:05 PM [23-05-2022]
 */
public interface IPermissionHandler
{
    boolean hasPermission(@NotNull OfflinePlayer player, @Nullable String permission);
    boolean hasPermission(@NotNull Player player, @Nullable String permission);

    boolean inGroup(@NotNull OfflinePlayer player, @Nullable String groupName);
    boolean inGroup(@NotNull Player player, @Nullable String groupName);

    String getPrimaryGroup(@NotNull Player player);


    String[] getGroups();
}
