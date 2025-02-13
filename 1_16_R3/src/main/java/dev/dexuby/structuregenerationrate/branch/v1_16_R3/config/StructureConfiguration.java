package dev.dexuby.structuregenerationrate.branch.v1_16_R3.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.server.v1_16_R3.StructureSettingsFeature;

public record StructureConfiguration(int spacing, int separation, int salt) implements IStructureConfiguration {

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, salt: %d)", this.spacing, this.separation, this.salt);

    }

    public static IStructureConfiguration fromStructureFeatureConfiguration(StructureSettingsFeature featureConfiguration) {
        return new StructureConfiguration(featureConfiguration.a(), featureConfiguration.b(), featureConfiguration.c());
    }
}
