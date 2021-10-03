package me.totalfreedom.totalfreedommod.world;

import org.bukkit.*;

public class Floatlands extends CustomWorld
{

    public Floatlands()
    {
        super("floatlands");
    }

    @Override
    protected World generateWorld()
    {
        final WorldCreator worldCreator = new WorldCreator(getName());
        worldCreator.generateStructures(false);
        worldCreator.type(WorldType.NORMAL);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generator(new CleanroomChunkGenerator("", false));

        final World world = Bukkit.getServer().createWorld(worldCreator);

        assert world != null;
        world.setSpawnFlags(false, false);
        world.setSpawnLocation(0, 50, 0);

        plugin.gr.commitGameRules();

        return world;
    }

}
