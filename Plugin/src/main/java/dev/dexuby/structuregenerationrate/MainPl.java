package dev.dexuby.structuregenerationrate;

import dev.dexuby.structuregenerationrate.config.Configuration;
import dev.dexuby.structuregenerationrate.config.ConfigurationImpl;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfigurationImpl;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MainPl extends JavaPlugin implements StructureGenerationRate {

    @Getter
    private final Map<String, IStructureConfiguration> cachedDefaultValues = new HashMap<>();
    @Getter
    private Configuration configuration;
    @Getter(value = AccessLevel.PACKAGE)
    private IPlacementLoader loader;
    private Integer serverVersionNumber;

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        this.loadConfig();
        super.getCommand("sgr").setExecutor(new MainCommand(this));
        loader.load();
    }

    @Override
    public void onLoad() {
        try {
            Class<?> loaderClass = Class.forName("dev.dexuby.structuregenerationrate.branch." + getServerVersion() + ".PlacementLoader");
            Constructor<?> loaderConstructor = loaderClass.getConstructor(StructureGenerationRate.class);
            loader = (IPlacementLoader) loaderConstructor.newInstance(this);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unsupported server version! Supported versions: 1.18.2, 1.21.x");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final String[] COMMON_STRUCTURES = {
            "mineshaft",
            "desert_pyramid",
            "jungle_pyramid",
            "ocean_ruin",
            "buried_treasure",
            "ruined_portal",
            "endcity",
            "shipwreck",
            "igloo",
            "monument",
            "village",
            "pillager_outpost",
            "nether_fossil",
            "mansion",
            "swamp_hut"
    };

    public String retroCompatibilityConvert(String structureName) {
           if (getVersionNumber() >= 1182)
               return structureName;
           for (String s : COMMON_STRUCTURES) {
               switch (structureName) {
                   case "jungle_pyramid":
                   case "jungle_temples":
                       return COMMON_STRUCTURES[2];
                   case "endcity":
                   case "end_cities":
                       return COMMON_STRUCTURES[6];
                   case "monument":
                   case "ocean_monuments":
                       return COMMON_STRUCTURES[9];
                   case "mansion":
                   case "woodland_mansions":
                       return COMMON_STRUCTURES[13];
               }
               if (structureName.matches(s + "s?"))
                   return s;
           }
           return structureName;
    }

     void loadConfig() {

        final boolean cacheDefaultValues = getConfig().getBoolean("cache-default-values");
        final Map<String, WorldConfigurationImpl> worldConfigurations = new HashMap<>();
        for (final String key : getConfig().getConfigurationSection("worlds").getKeys((false))) {
            final Map<String, IStructureConfiguration> structureConfigurations = new HashMap<>();
            for (String subKey : getConfig().getConfigurationSection("worlds." + key + ".structures").getKeys(false)) {
                final String path = "worlds." + key + ".structures." + subKey;
                subKey = retroCompatibilityConvert(subKey);
                structureConfigurations.put(subKey, loader.toStructureConfiguration(subKey, path, getConfig()));
            }
            worldConfigurations.put(key, new WorldConfigurationImpl(structureConfigurations));
        }
        this.configuration = new ConfigurationImpl(cacheDefaultValues, worldConfigurations);

    }

    private static final Map<Integer, Integer> serverVersions = new HashMap<>();

    static {
        serverVersions.put(16, 3);
        serverVersions.put(17, 1);
        serverVersions.put(18, 2);
        serverVersions.put(19, 3);
        serverVersions.put(20, 4);
        serverVersions.put(21, 3);
    }

    public int getVersionNumber() {
        if (serverVersionNumber != null)
            return serverVersionNumber;
        String[] ns = getServerVersion().replace("v", "").split("_", 3);
        int main = Integer.parseInt(ns[1]);
        int release = Integer.parseInt(ns[2]);
        return serverVersionNumber = 1000 + main*10 + release;
    }

    public String getServerVersion() {
        /*
        final String packageName = this.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.')+1).toLowerCase();
         */
        final String bukkitVersionOld = Bukkit.getBukkitVersion();
        final String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "").replace(".",
                "");
        BiFunction<Integer, Integer, String> toString = (m, r) -> "v1_" + m + "_R" + r;
        for (Map.Entry<Integer, Integer> version : serverVersions.entrySet()) {
            int main = version.getKey();
            int release = version.getValue();
            if (main >= 20) {
                for (int subv = (main == 20)? 6 : 4; subv >= 0; subv--) {
                    if (bukkitVersionOld.contains("1." + main + (subv != 0? "." + subv : ""))) {
                        switch (subv) {
                            case 0:
                            case 1:
                                release = 1;
                                break;
                            case 2:
                                if (main == 20)
                                    release = 2;
                            case 3:
                                if (main == 21)
                                    release = 2;
                                break;
                            case 4:
                                release = 3;
                                break;
                            case 5:
                            case 6:
                                release = 4;
                        }
                        return toString.apply(main, release);
                    }
                }
                continue;
            }
            for (; release > 0; release--) {
                String releaseVersion = toString.apply(main, release);
                if (bukkitVersion.equals(releaseVersion))
                    return releaseVersion;
            }
        }
        return "null";
    }

}
