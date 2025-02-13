package dev.dexuby.structuregenerationrate.branch.v1_14_R1.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;

public record StructureConfiguration(int spacing, int separation, int salt) implements IStructureConfiguration {

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, salt: %d)", this.spacing, this.separation, this.salt);
    }
}
