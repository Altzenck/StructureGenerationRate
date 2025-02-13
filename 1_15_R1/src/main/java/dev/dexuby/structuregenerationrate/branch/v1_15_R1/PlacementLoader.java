package dev.dexuby.structuregenerationrate.branch.v1_15_R1;

import dev.dexuby.structuregenerationrate.IPlacementLoader;
import dev.dexuby.structuregenerationrate.StructureGenerationRate;
import dev.dexuby.structuregenerationrate.branch.v1_15_R1.config.StrongholdStructureConfiguration;
import dev.dexuby.structuregenerationrate.branch.v1_15_R1.config.StructureConfiguration;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import lombok.RequiredArgsConstructor;
import me.altzenck.util.ReflectionUtils;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class PlacementLoader implements IPlacementLoader {

    private boolean initial = true;
    private final StructureGenerationRate pl;
    private static final String STRONGHOLD = "stronghold";
    private static final Map<String, String[]> translator = new HashMap<>();
    static {
        translator.put("strongholds", new String[]{"e", "f", "g"}); // distance, count, spread
        translator.put("villages", new String[]{"a", "b"});
        translator.put("monuments", new String[]{"c", "d"});
        translator.put("temples", new String[]{"h", "i"});
        translator.put("ocean_ruins", new String[]{"j", "k"});
        translator.put("endcity", new String[]{"l", "m"});
        translator.put("shipwrecks", new String[]{"n", "o"});
        translator.put("mansion", new String[]{"p", "q"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load() {
        pl.getCachedDefaultValues().clear();
        for (final World world : Bukkit.getServer().getWorlds()) {
            if(!(world instanceof CraftWorld craftWorld)) continue;
            try {
                final ChunkGenerator<?> generator = craftWorld.getHandle().getChunkProvider().getChunkGenerator();
                final WorldConfiguration worldConfig = pl.getConfiguration().getWorldConfiguration(world);
                if (worldConfig == null) continue;
                GeneratorSettingsDefault settingsDefault = generator.getSettings();
                for (Map.Entry<String, IStructureConfiguration> entry: worldConfig.structureConfigurations().entrySet()) {
                    final String name = entry.getKey();
                    final IStructureConfiguration config = entry.getValue();
                    String[] fields = translator.get(name);
                    if (fields == null) continue;

                    IStructureConfiguration oldConfig;
                    int[] newConfig = new int[3];
                    if (fields.length == 3) {
                        ReflectionUtils.FieldReference<Integer> oldDistanceRef = ReflectionUtils.getDeclaredField(GeneratorSettingsDefault.class, settingsDefault, fields[0]);
                        ReflectionUtils.FieldReference<Integer> oldCountRef = ReflectionUtils.getDeclaredField(GeneratorSettingsDefault.class, settingsDefault, fields[1]);
                        ReflectionUtils.FieldReference<Integer> oldSpreadRef = ReflectionUtils.getDeclaredField(GeneratorSettingsDefault.class, settingsDefault, fields[2]);
                        oldConfig = new StrongholdStructureConfiguration(oldDistanceRef.get(), oldCountRef.get(), oldSpreadRef.get());
                        StrongholdStructureConfiguration shConfig = (StrongholdStructureConfiguration) config;
                        oldDistanceRef.set(newConfig[0] = (shConfig.distance() < 0? oldDistanceRef.get() : shConfig.distance()));
                        oldCountRef.set(newConfig[2] = (shConfig.count() < 0? oldCountRef.get() : shConfig.count()));
                        oldSpreadRef.set(newConfig[1] = (shConfig.count() < 0? oldSpreadRef.get() : shConfig.spread()));
                    } else {
                        ReflectionUtils.FieldReference<Integer> oldSpacingRef = ReflectionUtils.getDeclaredField(GeneratorSettingsDefault.class, settingsDefault, fields[0]);
                        ReflectionUtils.FieldReference<Integer> oldSeparationRef = ReflectionUtils.getDeclaredField(GeneratorSettingsDefault.class, settingsDefault, fields[1]);
                        oldConfig = new StructureConfiguration(oldSpacingRef.get(), oldSpacingRef.get(), 0);
                        StructureConfiguration structureConfig = (StructureConfiguration) config;
                        oldSpacingRef.set(newConfig[0] = (structureConfig.spacing() < 0? oldSpacingRef.get() : structureConfig.spacing()));
                        oldSeparationRef.set(newConfig[1] = (structureConfig.separation() < 0? oldSeparationRef.get() : structureConfig.separation()));
                        newConfig[2] = 0;
                        Set<StructureGenerator<?>> generators = ReflectionUtils.getDeclaredField(WorldChunkManager.class, generator.getWorldChunkManager(), "a").cast(Map.class).keySet();
                        for (StructureGenerator<?> g : generators) {
                            if (g.b().equalsIgnoreCase(STRONGHOLD)) {
                                ReflectionUtils.getDeclaredField(WorldGenStronghold.class, g, "aq").set(null); // strongholdPos
                                ReflectionUtils.callMethod(WorldGenStronghold.class, g, "a", ReflectionUtils.param(ChunkGenerator.class, generator)); // generatePositions
                                break;
                            }
                        }
                    }
                    if (pl.getConfiguration().doCacheDefaultValues() && this.initial) {
                        if (!pl.getCachedDefaultValues().containsKey(name))
                            pl.getCachedDefaultValues().put(world.getName() + ":" + name, oldConfig);
                    }
                    pl.getLogger().info(generateDifferenceString(craftWorld.getName(), name, oldConfig, newConfig));
                }
            } catch (final Exception e) {
                throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
            }
        }

        this.initial = false;
    }

    private String generateDifferenceString(String worldName, String name, final IStructureConfiguration before, final int[] after) {
        String fields = "", changes = "";
        if(before instanceof StructureConfiguration) {
            StructureConfiguration beforec = (StructureConfiguration) before;
            fields = "spacing, separation, salt";
            changes = beforec.spacing() + " -> " + after[0] + ", " +
                    beforec.separation() + " -> " + after[1] + ", " +
                    beforec.salt() + " -> " + after[2];
        } else if(before instanceof StrongholdStructureConfiguration) {
            StrongholdStructureConfiguration beforec = (StrongholdStructureConfiguration) before;
            fields = "distance, spread, count";
            changes = beforec.distance() + " -> " + after[0] + ", " +
                    beforec.spread() + " -> " + after[1] + ", " +
                    beforec.count() + " -> " + after[2];
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
