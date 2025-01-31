package dev.dexuby.structuregenerationrate.branch.v1_21_R3.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public record StructureConfiguration(int spacing, int separation, RandomSpreadType spreadType, int salt) implements IStructureConfiguration {

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, spread-type: %s, salt: %d)", this.spacing, this.separation, this.spreadType.name(), this.salt);

    }

    public static StructureConfiguration fromRandomSpreadStructurePlacement(final RandomSpreadStructurePlacement placement) {
        return new StructureConfiguration(placement.spacing(), placement.separation(), placement.spreadType(), placement.salt);
    }

}
