package me.totalfreedom.totalfreedommod.config;

import java.text.ParseException;
import org.bukkit.configuration.ConfigurationSection;

public interface IConfig
{
    void loadFrom(ConfigurationSection cs) throws ParseException;

    void saveTo(ConfigurationSection cs);

    boolean isValid();
}