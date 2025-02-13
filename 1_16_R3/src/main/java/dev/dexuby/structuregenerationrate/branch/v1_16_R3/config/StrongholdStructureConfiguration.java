package dev.dexuby.structuregenerationrate.branch.v1_16_R3.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.server.v1_16_R3.StructureSettingsStronghold;

public record StrongholdStructureConfiguration(int distance, int spread, int count) implements IStructureConfiguration {

    @Override
    public String toString() {
        return String.format("(distance: %d, spread: %d, count: %d)", this.distance, this.spread, this.count);
    }

    public static IStructureConfiguration fromStrongholdConfiguration(StructureSettingsStronghold configuration) {
        return new StrongholdStructureConfiguration(configuration.a(), configuration.b(), configuration.c());
    }
}
