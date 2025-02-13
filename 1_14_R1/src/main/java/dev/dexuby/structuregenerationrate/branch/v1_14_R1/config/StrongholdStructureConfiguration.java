package dev.dexuby.structuregenerationrate.branch.v1_14_R1.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;

public record StrongholdStructureConfiguration(int distance, int spread, int count) implements IStructureConfiguration {

    @Override
    public String toString() {
        return String.format("(distance: %d, spread: %d, count: %d)", this.distance, this.spread, this.count);
    }
}
