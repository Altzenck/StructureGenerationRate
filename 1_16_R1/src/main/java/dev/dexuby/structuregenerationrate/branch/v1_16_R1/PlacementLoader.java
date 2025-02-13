package dev.dexuby.structuregenerationrate.branch.v1_16_R1;

import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_16_R1.config.StrongholdStructureConfiguration;
import dev.dexuby.structuregenerationrate.branch.v1_16_R1.config.StructureConfiguration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import lombok.RequiredArgsConstructor;
import me.altzenck.util.ReflectionUtils;
import net.minecraft.server.v1_16_R1.StructureGenerator;
import net.minecraft.server.v1_16_R1.StructureSettingsFeature;
import net.minecraft.server.v1_16_R1.StructureSettingsStronghold;
import net.minecraft.server.v1_16_R1.ChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
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
                final ChunkGenerator generator = craftWorld.getHandle().getChunkProvider().getChunkGenerator();
                final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
                if (worldConfig == null) continue;
                Map<StructureGenerator<?>, StructureSettingsFeature> structureSettings = generator.getSettings().a(); // structureConfig
                for(Map.Entry<StructureGenerator<?>, StructureSettingsFeature> e : structureSettings.entrySet()) {
                    final String name = e.getKey().i().toLowerCase();
                    StructureConfiguration config = (StructureConfiguration) worldConfig.getStructureConfiguration(name);
                    if (config == null) continue;
                    StructureSettingsFeature featureConfiguration = e.getValue(), oldFeatureConfiguration = new StructureSettingsFeature(1, 0, 0),
                            newFeatureConfiguration = new StructureSettingsFeature(
                                    config.spacing() == -1? featureConfiguration.a() : config.spacing(),
                                    config.separation() == 1? featureConfiguration.b() : config.separation(),
                                    config.salt() == -1? featureConfiguration.c() : config.salt());
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
                    StructureSettingsStronghold strongholdConfiguration = generator.getSettings().b(), oldStrongholdConfiguration = new StructureSettingsStronghold(1, 1, 1),
                            newStrongholdConfiguration = new StructureSettingsStronghold(
                                    shConfig.distance() == -1? strongholdConfiguration.a() : shConfig.distance(),
                                    shConfig.spread() == -1? strongholdConfiguration.a() : shConfig.spread(),
                                    shConfig.count() == -1? strongholdConfiguration.a() : shConfig.count()
                            );
                    replaceToConfiguration(strongholdConfiguration, oldStrongholdConfiguration);
                    replaceToConfiguration(newStrongholdConfiguration, strongholdConfiguration);
                    if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                        if (!pl.getCachedDefaultValues().containsKey(STRONGHOLD))
                            pl.getCachedDefaultValues().put(world.getName() + ":" + STRONGHOLD, StrongholdStructureConfiguration.fromStrongholdConfiguration(oldStrongholdConfiguration));
                    }
                    ReflectionUtils.getDeclaredField(ChunkGenerator.class, generator, "f").cast(List.class).clear(); // strongholdPositions
                    ReflectionUtils.callMethod(ChunkGenerator.class, generator, "g"); // generateStrongholds()
                    pl.getLogger().info(this.generateDifferenceString(world.getName(), STRONGHOLD, oldStrongholdConfiguration, newStrongholdConfiguration));
                }
            } catch (final Exception e) {
                throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
            }
        }

        this.initial = false;
    }

    private void replaceToConfiguration(Object from, Object to) {
        if((from instanceof StructureSettingsFeature fromc) && (to instanceof StructureSettingsFeature toc)) {
            ReflectionUtils.getDeclaredField(StructureSettingsFeature.class, fromc, "b").set(toc.a());
            ReflectionUtils.getDeclaredField(StructureSettingsFeature.class, fromc, "c").set(toc.b());
            ReflectionUtils.getDeclaredField(StructureSettingsFeature.class, fromc, "d").set(toc.c());
            return;
        }
        if((from instanceof StructureSettingsStronghold fromc) && (to instanceof StructureSettingsStronghold toc)) {
            ReflectionUtils.getDeclaredField(StructureSettingsStronghold.class, fromc, "a").set(toc.a());
            ReflectionUtils.getDeclaredField(StructureSettingsStronghold.class, fromc, "b").set(toc.b());
            ReflectionUtils.getDeclaredField(StructureSettingsStronghold.class, fromc, "c").set(toc.c());
        }
    }

    private String generateDifferenceString(String worldName, String name, final Object before, final Object after) {
        String fields = "", changes = "";
        if((before instanceof StructureSettingsFeature beforec) && (after instanceof StructureSettingsFeature afterc)) {
            fields = "spacing, separation, salt";
            changes = beforec.a() + " -> " + afterc.a() + ", " +
                    beforec.b() + " -> " + afterc.b() + ", " +
                    beforec.c() + " -> " + afterc.c();
        } else if((before instanceof StructureSettingsStronghold beforec) && (after instanceof StructureSettingsStronghold afterc)) {
            fields = "distance, spread, count";
            changes = beforec.a() + " -> " + afterc.a() + ", " +
                    beforec.b() + " -> " + afterc.b() + ", " +
                    beforec.c() + " -> " + afterc.c();
        }
        return String.format(" [%s] Updated values of %s (%s): %s", worldName, name, fields, changes);
    }

    @Override
    public IStructureConfiguration toStructureConfiguration(String structure, String path, Configuration config) {
        if (structure.equals(STRONGHOLD)) {
            final int distance = config.getInt(path + ".distance", -1);
            final int spread = config.getInt(path + ".spread", -1);
            final int count = config.getInt(path + ".count", -1);
            return new StrongholdStructureConfiguration(distance, spread, count);
        }
        final int spacing = config.getInt(path + ".spacing", -1);
        final int separation = config.getInt(path + ".separation", -1);
        final int salt = config.getInt(path + ".salt", -1);
        return new StrongholdStructureConfiguration(spacing, separation, salt);
    }
}
