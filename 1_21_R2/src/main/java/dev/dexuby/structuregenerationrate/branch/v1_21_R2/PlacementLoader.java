package dev.dexuby.structuregenerationrate.branch.v1_21_R2;

import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_21_R2.config.StructureConfiguration;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import java.util.*;

@RequiredArgsConstructor
public class PlacementLoader implements IPlacementLoader {
    
    private boolean initial = true;
    private final StructureGenerationRate pl;

    @Override
    @SuppressWarnings("unchecked")
    public void load() {
        pl.getCachedDefaultValues().clear();
        for (final World world : Bukkit.getServer().getWorlds()) {
            if (!(world instanceof CraftWorld)) continue;
            final Registry<StructureSet> setRegistry = ((CraftWorld) world).getHandle().registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
            final ReflectionUtils.FieldReference<Boolean> frozenRef = ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "l"); //frozen
            if (initial) {
                try {
                    frozenRef.set(false);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            final ReflectionUtils.FieldReference<Map<ResourceKey<StructureSet>, Holder.Reference<StructureSet>>> byKeyRef = ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "f"); // byKey
            for (Map.Entry<ResourceKey<StructureSet>, Holder.Reference<StructureSet>> entry : new ArrayList<>(byKeyRef.get().entrySet())) {
                final String name = Objects.requireNonNull(entry.getKey()).location().getPath();
                if (!entry.getValue().isBound()) {
                    continue;
                }
                final StructureSet structureSet = entry.getValue().value();
                StructurePlacement structurePlacement = structureSet.placement();
                if(!(structurePlacement instanceof RandomSpreadStructurePlacement placement)) continue;
                if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                    if (!pl.getCachedDefaultValues().containsKey(name))
                        pl.getCachedDefaultValues().put(name, StructureConfiguration.fromRandomSpreadStructurePlacement(placement));
                }

                final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
                if (worldConfig == null) continue;

                final StructureConfiguration structureConfig = (StructureConfiguration) worldConfig.getStructureConfiguration(name);
                if (structureConfig == null) continue;
                final RandomSpreadStructurePlacement updatedPlacement = new RandomSpreadStructurePlacement(
                        placement.locateOffset,
                        StructurePlacement.FrequencyReductionMethod.DEFAULT,
                        1.0F,
                        structureConfig.salt() == -1 ? placement.salt : structureConfig.salt(),
                        Optional.empty(),
                        structureConfig.spacing() == -1 ? placement.spacing() : structureConfig.spacing(),
                        structureConfig.separation() == -1 ? placement.separation() : structureConfig.separation(),
                        structureConfig.spreadType() == null ? placement.spreadType() : structureConfig.spreadType()
                );
                replaceToPlacement(updatedPlacement, placement);
                final ChunkGeneratorStructureState generatorState = ((CraftWorld) world).getHandle().getChunkSource().getGeneratorState();
                final ReflectionUtils.FieldReference<Map<Structure, List<StructurePlacement>>> placementsForStructureRef = ReflectionUtils.getDeclaredField(ChunkGeneratorStructureState.class, generatorState, "f");
                List<Structure> structures = structureSet.structures().stream().map(s->{
                    Holder<Structure> holder = s.structure();
                    if (holder.isBound()) {
                        return holder.value();
                    }
                    return null;
                }).toList();
                placementsForStructureRef.get().forEach((s, spl)->{
                    if (structures.contains(s)) {
                        spl.forEach((sp)-> replaceToPlacement(updatedPlacement, (RandomSpreadStructurePlacement) sp));
                    }
                });
                final ReflectionUtils.FieldReference<List<Holder<StructureSet>>> possibleStructureSetsRef = ReflectionUtils.getDeclaredField(ChunkGeneratorStructureState.class, generatorState, "i"); // possibleStructureSets
                possibleStructureSetsRef.get().forEach((holder)->{
                    StructureSet s = holder.value();
                    if(!s.structures().equals(structureSet.structures())) return;
                    StructurePlacement placement1 = s.placement();
                    replaceToPlacement(updatedPlacement, (RandomSpreadStructurePlacement) placement1);
                });
                pl.getLogger().info(String.format(" [%s] Updated values of %s (spacing, separation, spread type, salt): %s", world.getName(), name, this.generateDifferenceString(placement, updatedPlacement)));
            }
            try {
                ReflectionUtils.FieldReference<Object> allTagsRef = ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "k"); // allTags
                allTagsRef.set(ReflectionUtils.callMethod(Class.forName("net.minecraft.core.MappedRegistry$TagSet"), "a")); // unbound()
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            ((Map<TagKey<?>, HolderSet.Named<?>>) ReflectionUtils.getDeclaredField(MappedRegistry.class, setRegistry, "j").get()).clear(); // frozenTags
        }
        this.initial = false;
    }

    private static void replaceToPlacement(RandomSpreadStructurePlacement from, RandomSpreadStructurePlacement to) {
        ReflectionUtils.getDeclaredField(StructurePlacement.class, to, "f").set(from.salt);
        ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "c").set(from.spacing());
        ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "d").set(from.separation());
        ReflectionUtils.getDeclaredField(RandomSpreadStructurePlacement.class, to, "e").set(from.spreadType());
    }

    private String generateDifferenceString(final RandomSpreadStructurePlacement before, final RandomSpreadStructurePlacement after) {

        return before.spacing() + " -> " + after.spacing() + ", " +
                before.separation() + " -> " + after.separation() + ", " +
                before.spreadType().name() + " -> " + after.spreadType().name() + ", " +
                before.salt + " -> " + after.salt;

    }

    @Override
    public IStructureConfiguration toStructureConfiguration(String path, Configuration config) {
        final int spacing = config.getInt(path + ".spacing", -1);
        final int separation = config.getInt(path + ".separation", -1);
        final RandomSpreadType spreadType = NMSUtils.RandomSpreadTypeByName(config.getString(path + ".spread-type", "linear"));
        final int salt = config.getInt(path + ".salt", -1);
        return new StructureConfiguration(spacing, separation, spreadType, salt);
    }
}
