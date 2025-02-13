package dev.dexuby.structuregenerationrate.branch.v1_21_R1.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public record RandomSpreadStructureConfiguration(int spacing, int separation, RandomSpreadType spreadType, int salt) implements IStructureConfiguration {

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, spread-type: %s, salt: %d)", this.spacing, this.separation, this.spreadType.name(), this.salt);

    }

    public static RandomSpreadStructureConfiguration fromRandomSpreadStructurePlacement(final RandomSpreadStructurePlacement placement) {
        return new RandomSpreadStructureConfiguration(placement.spacing(), placement.separation(), placement.spreadType(), placement.salt);
    }

}
