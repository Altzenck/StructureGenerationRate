package dev.dexuby.structuregenerationrate;

import dev.dexuby.structuregenerationrate.config.Configuration;
import dev.dexuby.structuregenerationrate.config.ConfigurationImpl;
import dev.dexuby.structuregenerationrate.config.IStructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfigurationImpl;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Constructor;
import java.util.*;

public class MainPl extends JavaPlugin implements StructureGenerationRate, CommandExecutor {

    @Getter
    private final Map<String, IStructureConfiguration> cachedDefaultValues = new HashMap<>();
    @Getter
    private Configuration configuration;
    private IPlacementLoader loader;

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        this.loadConfig();
        super.getCommand("sgr").setExecutor(this);
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

    private void loadConfig() {

        final boolean cacheDefaultValues = getConfig().getBoolean("cache-default-values");
        final Map<String, WorldConfigurationImpl> worldConfigurations = new HashMap<>();
        for (final String key : getConfig().getConfigurationSection("worlds").getKeys((false))) {
            final Map<String, IStructureConfiguration> structureConfigurations = new HashMap<>();
            for (final String subKey : getConfig().getConfigurationSection("worlds." + key + ".structures").getKeys(false)) {
                final String path = "worlds." + key + ".structures." + subKey;
                structureConfigurations.put(subKey, loader.toStructureConfiguration(path, getConfig()));
            }
            worldConfigurations.put(key, new WorldConfigurationImpl(structureConfigurations));
        }
        this.configuration = new ConfigurationImpl(cacheDefaultValues, worldConfigurations);

    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String cmdName, final String[] args) {

        if (args.length == 0) return false;

        final String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("reload")) {
            super.reloadConfig();
            this.loadConfig();
            loader.load();
            return true;
        } else if (subCommand.equalsIgnoreCase("defaultvalue")) {
            if (args.length < 2) return false;
            final String name = args[1];
            if (this.cachedDefaultValues.containsKey(name)) {
                sender.sendMessage(this.cachedDefaultValues.get(name).toString());
            } else {
                sender.sendMessage("Invalid key.");
            }
            return true;
        }

        return false;

    }

    public String getServerVersion() {
        /*
        final String packageName = this.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.')+1).toLowerCase();
         */
        final String bukkitVersionOld = Bukkit.getBukkitVersion();
        final String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "").replace(".",
                "");
        if (bukkitVersion.equals("v1_18_R2")) {
            return "v1_18_R2";
        } else if (bukkitVersionOld.contains("1.21.4")) {
            return "v1_21_R3";
        } else if (bukkitVersionOld.contains("1.21.3")) {
            // 1.21.3
            return "v1_21_R2";
        } else if (bukkitVersionOld.contains("1.21")) {
            return "v1_21_R1";
        }
        return "null";
    }

}
