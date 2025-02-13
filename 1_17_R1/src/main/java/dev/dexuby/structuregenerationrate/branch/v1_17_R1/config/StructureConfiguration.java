package dev.dexuby.structuregenerationrate.branch.v1_17_R1.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public record StructureConfiguration(int spacing, int separation, int salt) implements IStructureConfiguration {

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, salt: %d)", this.spacing, this.separation, this.salt);

    }

    public static IStructureConfiguration fromStructureFeatureConfiguration(StructureFeatureConfiguration featureConfiguration) {
        return new StructureConfiguration(featureConfiguration.spacing(), featureConfiguration.separation(), featureConfiguration.salt());
    }
}
