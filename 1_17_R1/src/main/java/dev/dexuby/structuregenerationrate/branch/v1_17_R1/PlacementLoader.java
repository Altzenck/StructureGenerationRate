package dev.dexuby.structuregenerationrate.branch.v1_17_R1;

import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_17_R1.config.StrongholdStructureConfiguration;
import dev.dexuby.structuregenerationrate.branch.v1_17_R1.config.StructureConfiguration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import lombok.RequiredArgsConstructor;
import me.altzenck.util.ReflectionUtils;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PlacementLoader implements IPlacementLoader {

    private boolean initial = true;
    private final StructureGenerationRate pl;
    private static final String STRONGHOLD = "stronghold";

    @Override
    public void load() {
        pl.getCachedDefaultValues().clear();
        for (final World world : Bukkit.getServer().getWorlds()) {
            if(!(world instanceof CraftWorld craftWorld)) continue;
            try {
                final ChunkGenerator generator = craftWorld.getHandle().getChunkSource().getGenerator();
                final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
                if (worldConfig == null) continue;
                Map<StructureFeature<?>, StructureFeatureConfiguration> structureSettings = generator.getSettings().structureConfig();
                for(Map.Entry<StructureFeature<?>, StructureFeatureConfiguration> e : structureSettings.entrySet()) {
                    final String name = e.getKey().getFeatureName().toLowerCase();
                    StructureConfiguration config = (StructureConfiguration) worldConfig.getStructureConfiguration(name);
                    if (config == null) continue;
                    StructureFeatureConfiguration featureConfiguration = e.getValue(), oldFeatureConfiguration = new StructureFeatureConfiguration(1, 0, 0),
                            newFeatureConfiguration = new StructureFeatureConfiguration(
                                    config.spacing() == -1? featureConfiguration.spacing() : config.spacing(),
                                    config.separation() == 1? featureConfiguration.separation() : config.separation(),
                                    config.salt() == -1? featureConfiguration.salt() : config.salt());
                    replaceToConfiguration(featureConfiguration, oldFeatureConfiguration);
                    replaceToConfiguration(newFeatureConfiguration, featureConfiguration);
                    if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                        if (!pl.getCachedDefaultValues().containsKey(name))
                            pl.getCachedDefaultValues().put(world.getName() + ":" + name, StructureConfiguration.fromStructureFeatureConfiguration(oldFeatureConfiguration));
                    }
                    pl.getLogger().info(this.generateDifferenceString(world.getName(), name, oldFeatureConfiguration, newFeatureConfiguration));
                }
                StrongholdStructureConfiguration shConfig = (StrongholdStructureConfiguration) worldConfig.getStructureConfiguration(STRONGHOLD);
                if (shConfig != null) {
                    StrongholdConfiguration strongholdConfiguration = generator.getSettings().stronghold(), oldStrongholdConfiguration = new StrongholdConfiguration(1, 1, 1),
                            newStrongholdConfiguration = new StrongholdConfiguration(
                                    shConfig.distance() == -1? strongholdConfiguration.distance() : shConfig.distance(),
                                    shConfig.spread() == -1? strongholdConfiguration.spread() : shConfig.spread(),
                                    shConfig.count() == -1? strongholdConfiguration.count() : shConfig.count()
                            );
                    replaceToConfiguration(strongholdConfiguration, oldStrongholdConfiguration);
                    replaceToConfiguration(newStrongholdConfiguration, strongholdConfiguration);
                    if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                        if (!pl.getCachedDefaultValues().containsKey(STRONGHOLD))
                            pl.getCachedDefaultValues().put(world.getName() + ":" + STRONGHOLD, StrongholdStructureConfiguration.fromStrongholdConfiguration(oldStrongholdConfiguration));
                    }
                    ReflectionUtils.getDeclaredField(ChunkGenerator.class, generator, "f").cast(List.class).clear(); // strongholdPositions
                    ReflectionUtils.callMethod(ChunkGenerator.class, generator, "i"); // generateStrongholds()
                    pl.getLogger().info(this.generateDifferenceString(world.getName(), STRONGHOLD, oldStrongholdConfiguration, newStrongholdConfiguration));
                }
            } catch (final Exception e) {
                throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
            }
        }

        this.initial = false;
    }

    private void replaceToConfiguration(Object from, Object to) {
        if((from instanceof StructureFeatureConfiguration fromc) && (to instanceof StructureFeatureConfiguration toc)) {
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "b").set(toc.spacing());
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "c").set(toc.separation());
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "d").set(toc.salt());
            return;
        }
        if((from instanceof StrongholdConfiguration fromc) && (to instanceof StrongholdConfiguration toc)) {
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "a").set(toc.distance());
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "b").set(toc.spread());
            ReflectionUtils.getDeclaredField(StructureFeatureConfiguration.class, fromc, "c").set(toc.count());
        }
    }

    private String generateDifferenceString(String worldName, String name, final Object before, final Object after) {
        String fields = "", changes = "";
        if((before instanceof StructureFeatureConfiguration beforec) && (after instanceof StructureFeatureConfiguration afterc)) {
            fields = "spacing, separation, salt";
            changes = beforec.spacing() + " -> " + afterc.spacing() + ", " +
                    beforec.separation() + " -> " + afterc.separation() + ", " +
                    beforec.salt() + " -> " + afterc.salt();
        } else if((before instanceof StrongholdConfiguration beforec) && (after instanceof StrongholdConfiguration afterc)) {
            fields = "distance, spread, count";
            changes = beforec.distance() + " -> " + afterc.distance() + ", " +
                    beforec.spread() + " -> " + afterc.spread() + ", " +
                    beforec.count() + " -> " + afterc.count();
        }
        return String.format(" [%s] Updated values of %s (%s): %s", worldName, name, fields, changes);
    }

    @Override
    public IStructureConfiguration toStructureConfiguration(String structure, String path, Configuration config) {
        if (structure.matches(STRONGHOLD + "s?")) {
            final int distance = config.getInt(path + ".distance", -1);
            final int spread = config.getInt(path + ".spread", -1);
            final int count = config.getInt(path + ".count", -1);
            return new StrongholdStructureConfiguration(distance, spread, count);
        }
        final int spacing = config.getInt(path + ".spacing", -1);
        final int separation = config.getInt(path + ".separation", -1);
        final int salt = config.getInt(path + ".salt", -1);
        return new StructureConfiguration(spacing, separation, salt);
    }
}
