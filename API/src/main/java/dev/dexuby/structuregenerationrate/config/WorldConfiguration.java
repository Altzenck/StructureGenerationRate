package dev.dexuby.structuregenerationrate.config;

import java.util.Map;

public interface WorldConfiguration {

    IStructureConfiguration getStructureConfiguration(String name);

    Map<String, IStructureConfiguration> structureConfigurations();
}
