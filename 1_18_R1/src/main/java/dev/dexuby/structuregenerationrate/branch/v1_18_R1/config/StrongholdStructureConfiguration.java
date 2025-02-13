package dev.dexuby.structuregenerationrate.branch.v1_18_R1.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;

public record StrongholdStructureConfiguration(int distance, int spread, int count) implements IStructureConfiguration {

    @Override
    public String toString() {
        return String.format("(distance: %d, spread: %d, count: %d)", this.distance, this.spread, this.count);
    }

    public static IStructureConfiguration fromStrongholdConfiguration(StrongholdConfiguration configuration) {
        return new StrongholdStructureConfiguration(configuration.distance(), configuration.spread(), configuration.count());
    }
}
