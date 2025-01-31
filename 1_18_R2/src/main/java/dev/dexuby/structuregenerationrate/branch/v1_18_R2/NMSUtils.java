package dev.dexuby.structuregenerationrate.branch.v1_18_R2;

import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class NMSUtils {

    public static RandomSpreadType RandomSpreadTypeByName(String name) {
        try {
            return RandomSpreadType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
