package dev.dexuby.structuregenerationrate.branch.v1_20_R4;

import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_20_R4.config.ConcentricRingsStructureConfiguration;
import dev.dexuby.structuregenerationrate.branch.v1_20_R4.config.RandomSpreadStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import lombok.RequiredArgsConstructor;
import me.altzenck.util.ReflectionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.CraftWorld;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class PlacementLoader implements IPlacementLoader {

    private boolean initial = true;
    private final StructureGenerationRate pl;

    @Override
    @SuppressWarnings({"unchecked", "ConstantValue"})
    public void load() {
        pl.getCachedDefaultValues().clear();
        for (final World world : Bukkit.getServer().getWorlds()) {
            if (!(world instanceof CraftWorld)) continue;
            final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
            if (worldConfig == null) continue;

            final Registry<StructureSet> setRegistry = ((CraftWorld) world).getHandle().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
            final ReflectionUtils.FieldReference<Boolean> frozenRef = ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "l"); //frozen
            if (initial) {
                try {
                    frozenRef.set(false);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            final ReflectionUtils.FieldReference<Map<ResourceKey<StructureSet>, Holder.Reference<StructureSet>>> byKeyRef = ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "g"); // byKey
            for (Map.Entry<ResourceKey<StructureSet>, Holder.Reference<StructureSet>> entry : new ArrayList<>(byKeyRef.get().entrySet())) {
                final String name = Objects.requireNonNull(entry.getKey()).location().getPath();
                if (!entry.getValue().isBound()) {
                    continue;
                }
                final StructureSet structureSet = entry.getValue().value();
                StructurePlacement structurePlacement = structureSet.placement();
                boolean isrsp = structurePlacement instanceof RandomSpreadStructurePlacement,
                        iscrp = structurePlacement instanceof ConcentricRingsStructurePlacement;
                if (!isrsp && !iscrp) continue;
                RandomSpreadStructurePlacement rsplacement = (isrsp)? (RandomSpreadStructurePlacement) structurePlacement : null;
                ConcentricRingsStructurePlacement crplacement = (iscrp)? (ConcentricRingsStructurePlacement) structurePlacement : null;


                final IStructureConfiguration iStructureConfiguration = worldConfig.getStructureConfiguration(name);
                if (iStructureConfiguration == null) continue;
                if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                    if (!pl.getCachedDefaultValues().containsKey(name))
                        pl.getCachedDefaultValues().put(world.getName() + ":" + name, (isrsp)? RandomSpreadStructureConfiguration.fromRandomSpreadStructurePlacement(rsplacement) : ConcentricRingsStructureConfiguration.fromConcentricRingsStructurePlacement(crplacement));
                }

                StructurePlacement oldPlacement = null, updatedPlacement;
                if (isrsp) {
                    final RandomSpreadStructureConfiguration structureConfig = (RandomSpreadStructureConfiguration) iStructureConfiguration;
                    oldPlacement = new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, 1);
                    replaceToPlacement(rsplacement, oldPlacement);
                    updatedPlacement = new RandomSpreadStructurePlacement(
                            rsplacement.locateOffset,
                            StructurePlacement.FrequencyReductionMethod.DEFAULT,
                            1.0F,
                            structureConfig.salt() == -1 ? rsplacement.salt : structureConfig.salt(),
                            Optional.empty(),
                            structureConfig.spacing() == -1 ? rsplacement.spacing() : structureConfig.spacing(),
                            structureConfig.separation() == -1 ? rsplacement.separation() : structureConfig.separation(),
                            structureConfig.spreadType() == null ? rsplacement.spreadType() : structureConfig.spreadType()
                    );
                } else if (iscrp) {
                    final ConcentricRingsStructureConfiguration structureConfig = (ConcentricRingsStructureConfiguration) iStructureConfiguration;
                    oldPlacement = new ConcentricRingsStructurePlacement(1,1,1, HolderSet.empty());
                    replaceToPlacement(crplacement, oldPlacement);
                    updatedPlacement = new ConcentricRingsStructurePlacement(
                            crplacement.locateOffset,
                            StructurePlacement.FrequencyReductionMethod.DEFAULT,
                            crplacement.frequency,
                            structureConfig.salt() == -1 ? crplacement.salt : structureConfig.salt(),
                            Optional.empty(),
                            structureConfig.distance() == -1 ? crplacement.distance() : structureConfig.distance(),
                            structureConfig.spread() == -1 ? crplacement.spread() : structureConfig.spread(),
                            structureConfig.count() == -1 ? crplacement.count() : structureConfig.count(),
                            structureConfig.preferredBiomes().size() == 0? crplacement.preferredBiomes() : structureConfig.preferredBiomes()
                    );
                } else {
                    updatedPlacement = null;
                }
                replaceToPlacement(updatedPlacement, rsplacement);
                final ChunkGeneratorStructureState generatorState = ((CraftWorld) world).getHandle().getChunkSource().getGeneratorState();

                // Replaces the placements of structure sets that have been modified by spigot
                final ReflectionUtils.FieldReference<List<Holder<StructureSet>>> possibleStructureSetsRef = ReflectionUtils.getDeclaredField(ChunkGeneratorStructureState.class, generatorState, "i"); // possibleStructureSets
                possibleStructureSetsRef.get().forEach((holder)->{
                    StructureSet s = holder.value();
                    if(!s.structures().equals(structureSet.structures())) return;
                    StructurePlacement placement1 = s.placement();
                    replaceToPlacement(updatedPlacement, placement1);
                });

                // Clears previous placements
                final Map<Structure, List<StructurePlacement>> placementsForStructure = ReflectionUtils.getDeclaredField(ChunkGeneratorStructureState.class, generatorState, "f").cast(Map.class);
                placementsForStructure.clear();
                final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = ReflectionUtils.getDeclaredField(ChunkGeneratorStructureState.class, generatorState, "g").cast(Map.class);
                ringPositions.clear();

                // All positions are regenerated to apply the new configurations
                ReflectionUtils.callMethod(ChunkGeneratorStructureState.class, generatorState, "e"); // generatePositions()

                pl.getLogger().info(generateDifferenceString(world.getName(), name, oldPlacement, updatedPlacement));
            }
        }
        this.initial = false;
    }

    private static void replaceToPlacement(StructurePlacement from, StructurePlacement to) {
        if (from instanceof RandomSpreadStructurePlacement fromc && to instanceof RandomSpreadStructurePlacement) {
            ReflectionUtils.getDeclaredField(StructurePlacement.class, to, "f").set(from.salt);
            ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "c").set(fromc.spacing());
            ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "d").set(fromc.separation());
            ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "e").set(fromc.spreadType());
            return;
        }
        if (from instanceof ConcentricRingsStructurePlacement fromc && to instanceof ConcentricRingsStructurePlacement) {
            ReflectionUtils.getDeclaredField(StructurePlacement.class, to, "f").set(from.salt);
            ReflectionUtils.getDeclaredField(ConcentricRingsStructurePlacement.class, to, "c").set(fromc.distance()); // distance
            ReflectionUtils.getDeclaredField(ConcentricRingsStructurePlacement.class, to, "d").set(fromc.spread()); // spread
            ReflectionUtils.getDeclaredField(ConcentricRingsStructurePlacement.class, to, "e").set(fromc.count()); // count
            ReflectionUtils.getDeclaredField(ConcentricRingsStructurePlacement.class, to, "f").set(fromc.preferredBiomes()); // preferredBiomes
        }
    }

    private String generateDifferenceString(String worldName, String name, final StructurePlacement before, final StructurePlacement after) {
        String valueNames = "", changes = "";
        if (before instanceof RandomSpreadStructurePlacement beforec && after instanceof RandomSpreadStructurePlacement afterc) {
            valueNames = "spacing, separation, spread type, salt";
            changes = beforec.spacing() + " -> " + afterc.spacing() + ", " +
                    beforec.separation() + " -> " + afterc.separation() + ", " +
                    beforec.spreadType().name() + " -> " + afterc.spreadType().name() + ", " +
                    beforec.salt + " -> " + after.salt;
        } else if (before instanceof ConcentricRingsStructurePlacement beforec && after instanceof ConcentricRingsStructurePlacement afterc) {
            valueNames = "distance, count, preferred biomes, spread, salt";
            changes = beforec.distance() + " -> " + afterc.distance() + ", " +
                    beforec.count() + " -> " + afterc.count() + ", " +
                    Arrays.toString(ConcentricRingsStructureConfiguration.holderSetBiomesToArray(beforec.preferredBiomes())) + " -> " + Arrays.toString(ConcentricRingsStructureConfiguration.holderSetBiomesToArray(afterc.preferredBiomes())) + ", " +
                    beforec.spread() + " -> " + afterc.spread() + ", " +
                    beforec.salt + " -> " + after.salt;
        }
        return String.format(" [%s] Updated values of %s (" + valueNames + "): %s", worldName, name, changes);
    }

    @Override
    public IStructureConfiguration toStructureConfiguration(String structure, String path, Configuration config) {
        if (structure.equals("strongholds")) {
            final int distance = config.getInt(path + ".distance", -1);
            final int spread = config.getInt(path + ".spread", -1);
            final int count = config.getInt(path + ".count", -1);
            final int salt = config.getInt(path + ".salt", -1);
            List<String> preferredBiomes = config.getStringList(path + ".preferred-biomes");
            HolderSet<Biome> biomeSet = preferredBiomes.isEmpty()? HolderSet.empty() : ConcentricRingsStructureConfiguration.biomeListToHolderSet(preferredBiomes);
            return new ConcentricRingsStructureConfiguration(distance, count, biomeSet, spread, salt);
        }
        final int spacing = config.getInt(path + ".spacing", -1);
        final int separation = config.getInt(path + ".separation", -1);
        final RandomSpreadType spreadType = randomSpreadTypeByName(config.getString(path + ".spread-type", "linear"));
        final int salt = config.getInt(path + ".salt", -1);
        return new RandomSpreadStructureConfiguration(spacing, separation, spreadType, salt);
    }

    public static RandomSpreadType randomSpreadTypeByName(String name) {
        try {
            return RandomSpreadType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
