package dev.dexuby.structuregenerationrate;

import dev.dexuby.structuregenerationrate.config.Configuration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;

import java.util.Map;
import java.util.logging.Logger;

public interface StructureGenerationRate {

    Logger getLogger();

    Map<String, IStructureConfiguration> getCachedDefaultValues();

    Configuration getConfiguration();


}
