package dev.dexuby.structuregenerationrate.branch.v1_19_R3.config;

import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class ConcentricRingsStructureConfiguration implements IStructureConfiguration {

    private final int distance, count;
    private final HolderSet<Biome> preferredBiomes;
    private final int spread, salt;
    private String[] cachedPreferredBiomes;

    public int distance() {
        return distance;
    }

    public int count() {
        return count;
    }

    public HolderSet<Biome> preferredBiomes() {
        return preferredBiomes;
    }

    public String[] cachedPreferredBiomes() {
        if (cachedPreferredBiomes == null)
            cachedPreferredBiomes = holderSetBiomesToArray(preferredBiomes);
        return cachedPreferredBiomes;
    }

    public int spread() {
        return spread;
    }

    public int salt() {
        return salt;
    }

    @Override
    public String toString() {

        return String.format("(distance: %d, count: %d, preferredBiomes: %s, spread: %d, salt: %d)", this.distance, this.count, Arrays.toString(cachedPreferredBiomes()) , this.spread, this.salt);
    }

    public static ConcentricRingsStructureConfiguration fromConcentricRingsStructurePlacement(final ConcentricRingsStructurePlacement placement) {
        return new ConcentricRingsStructureConfiguration(placement.distance(), placement.count(), placement.preferredBiomes(), placement.spread(), placement.salt);
    }

    public static String[] holderSetBiomesToArray(HolderSet<Biome> biomesSet) {
        ArrayList<String> biomeNames = new ArrayList<>();
        biomesSet.forEach(h ->{
            if (!h.isBound()) return;
            for (Map.Entry<ResourceKey<Biome>, Biome> e : ((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registries
                    .BIOME).entrySet()) {
                if (h.value().equals(e.getValue())) {
                    biomeNames.add(e.getKey().location().toString());
                    break;
                }
            }
        });
        return biomeNames.toArray(String[]::new);
    }

    public static HolderSet<Biome> biomeListToHolderSet(List<String> biomes) {
        ArrayList<Holder<Biome>> biomeHolders = new ArrayList<>();
        biomes.forEach(b->{
            for (Map.Entry<ResourceKey<Biome>, Biome> e : ((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registries.BIOME).entrySet()) {
                if (e.getKey().location().toString().equals("minecraft:" + b))
                    biomeHolders.add(Holder.direct(e.getValue()));
            }
        });
        return HolderSet.direct(biomeHolders);
    }
}
