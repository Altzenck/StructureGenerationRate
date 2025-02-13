package dev.dexuby.structuregenerationrate.config;

import java.util.Map;

public record WorldConfigurationImpl(Map<String, IStructureConfiguration> structureConfigurations) implements WorldConfiguration {

    @Override
    public IStructureConfiguration getStructureConfiguration(final String name) {
        return this.structureConfigurations.get(name);
    }

}
