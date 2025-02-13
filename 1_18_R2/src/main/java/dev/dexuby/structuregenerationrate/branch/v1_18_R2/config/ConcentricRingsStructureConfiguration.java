package dev.dexuby.structuregenerationrate.branch.v1_18_R2.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

public record ConcentricRingsStructureConfiguration(int distance, int count, int spread) implements IStructureConfiguration {

    @Override
    public String toString() {
        return String.format("(distance: %d, count: %d, spread: %d)", this.distance, this.count, this.spread);
    }

    public static ConcentricRingsStructureConfiguration fromConcentricRingsStructurePlacement(final ConcentricRingsStructurePlacement placement) {
        return new ConcentricRingsStructureConfiguration(placement.distance(), placement.count(), placement.spread());
    }
}
