package dev.dexuby.structuregenerationrate;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import org.bukkit.configuration.Configuration;

public interface IPlacementLoader {

    void load();

    IStructureConfiguration toStructureConfiguration(String structure, String path, Configuration config);
}