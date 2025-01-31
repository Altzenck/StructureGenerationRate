package dev.dexuby.structuregenerationrate.config;

import org.bukkit.World;

public interface Configuration {
    boolean doCacheDefaultValues();
    WorldConfiguration getWorldConfiguration(final World world);
}
