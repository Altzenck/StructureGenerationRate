package dev.dexuby.structuregenerationrate.branch.v1_18_R2;

import com.mojang.serialization.Lifecycle;
import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_18_R2.config.StructureConfiguration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.OptionalInt;

@RequiredArgsConstructor
public class PlacementLoader implements IPlacementLoader {
    
    private boolean initial = true;
    private final StructureGenerationRate pl;

    @Override
    public void load() {
        pl.getCachedDefaultValues().clear();

        for (final World world : Bukkit.getServer().getWorlds()) {
            if (!(world instanceof CraftWorld)) continue;
            final ChunkGenerator chunkGenerator = ((CraftWorld) world).getHandle().getChunkSource().getGenerator();
            final MappedRegistry<StructureSet> mappedRegistry = (MappedRegistry<StructureSet>) chunkGenerator.structureSets;
            if (initial) {
                try {
                    final Field frozenField = MappedRegistry.class.getDeclaredField("bL");
                    frozenField.setAccessible(true);
                    frozenField.setBoolean(mappedRegistry, false);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }

            for (final Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : mappedRegistry.entrySet()) {
                final String name = chunkGenerator.structureSets.getKey(entry.getValue()).getPath();
                final StructurePlacement structurePlacement = entry.getValue().placement();
                if (!(structurePlacement instanceof RandomSpreadStructurePlacement)) continue;

                final RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) structurePlacement;
                if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                    if (!pl.getCachedDefaultValues().containsKey(name))
                        pl.getCachedDefaultValues().put(name, StructureConfiguration.fromRandomSpreadStructurePlacement(placement));
                }

                final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
                if (worldConfig == null) continue;

                final StructureConfiguration structureConfig = (StructureConfiguration) worldConfig.getStructureConfiguration(name);
                if (structureConfig == null) continue;

                final RandomSpreadStructurePlacement updatedPlacement = new RandomSpreadStructurePlacement(
                        structureConfig.spacing() == -1 ? placement.spacing() : structureConfig.spacing(),
                        structureConfig.separation() == -1 ? placement.separation() : structureConfig.separation(),
                        structureConfig.spreadType() == null ? placement.spreadType() : structureConfig.spreadType(),
                        structureConfig.salt() == -1 ? placement.salt() : structureConfig.salt(),
                        placement.locateOffset()
                );

                mappedRegistry.registerOrOverride(OptionalInt.empty(), entry.getKey(), new StructureSet(entry.getValue().structures(), updatedPlacement), Lifecycle.stable());
                pl.getLogger().info(String.format(" [%s] Updated values of %s (spacing, separation, spread type, salt): %s", world.getName(), name, this.generateDifferenceString(placement, updatedPlacement)));
            }
        }

        this.initial = false;
    }

    private String generateDifferenceString(final RandomSpreadStructurePlacement before, final RandomSpreadStructurePlacement after) {

        return before.spacing() + " -> " + after.spacing() + ", " +
                before.separation() + " -> " + after.separation() + ", " +
                before.spreadType().name() + " -> " + after.spreadType().name() + ", " +
                before.salt() + " -> " + after.salt();

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
