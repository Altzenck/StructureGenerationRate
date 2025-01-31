package dev.dexuby.structuregenerationrate.config;

import lombok.Getter;
import org.bukkit.World;
import java.util.Map;

public class ConfigurationImpl implements Configuration {

    private final boolean cacheDefaultValues;
    @Getter
    private final Map<String, WorldConfigurationImpl> worldConfigurations;

    public ConfigurationImpl(final boolean cacheDefaultValues,
                             final Map<String, WorldConfigurationImpl> worldConfigurations) {

        this.cacheDefaultValues = cacheDefaultValues;
        this.worldConfigurations = worldConfigurations;

    }

    @Override
    public boolean doCacheDefaultValues() {

        return this.cacheDefaultValues;

    }

    @Override
    public WorldConfigurationImpl getWorldConfiguration(final World world) {

        return this.worldConfigurations.get(world.getName());

    }

}
