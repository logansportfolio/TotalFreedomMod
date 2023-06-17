package me.totalfreedom.totalfreedommod;

import io.papermc.lib.PaperLib;

import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MovementValidator extends FreedomService
{

    public static final int MAX_XYZ_COORD = 29999998;
    public static final int MAX_DISTANCE_TRAVELED = 100;

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        // Check absolute value to account for negatives
        if (Math.abs(Objects.requireNonNull(event.getTo()).getX()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getZ()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getY()) >= MAX_XYZ_COORD)
        {
            event.setCancelled(true); // illegal position, cancel it
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        final Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        double distance = from.distanceSquared(to);

        if (distance >= MAX_DISTANCE_TRAVELED)
        {
            event.setCancelled(true);
            player.kickPlayer(ChatColor.RED + "You were moving too quickly!");
        }
        // Check absolute value to account for negatives
        if (Math.abs(event.getTo().getX()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getZ()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getY()) >= MAX_XYZ_COORD)
        {
            event.setCancelled(true);
            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final Player player = event.getPlayer();

        // Validate position
        if (Math.abs(player.getLocation().getX()) >= MAX_XYZ_COORD || Math.abs(player.getLocation().getZ()) >= MAX_XYZ_COORD || Math.abs(player.getLocation().getY()) >= MAX_XYZ_COORD)
        {
            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation()); // Illegal position, teleport to spawn
        }
    }
}