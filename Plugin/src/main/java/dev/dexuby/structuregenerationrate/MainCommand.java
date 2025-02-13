package dev.dexuby.structuregenerationrate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MainCommand implements CommandExecutor, TabCompleter {

    private final MainPl pl;
    private static final String SUB_RELOAD = "reload", SUB_DEFAULTVALUE = "defaultvalue";

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String cmdName, final String[] args) {
        String prefix = "§7[§9StructureGenerationRate§7] ";
        if (args.length == 0) {
            sender.sendMessage(prefix + "§6/sgr §7[§6" + SUB_RELOAD + "§7|§6" + SUB_DEFAULTVALUE + " <structure>§7]");
            return true;
        }
        final String subCommand = args[0];
        if (subCommand.equalsIgnoreCase(SUB_RELOAD)) {
            pl.reloadConfig();
            pl.loadConfig();
            pl.getLoader().load();
            sender.sendMessage(prefix + " §aThe configuration has been reloaded.");
            return true;
        } else if (subCommand.equalsIgnoreCase(SUB_DEFAULTVALUE)) {
            if (args.length != 2) {
                sender.sendMessage(prefix + "§cInvalid arguments! usage: §r/" + SUB_DEFAULTVALUE + " <structure>");
                return true;
            }
            final String name = args[1];
            if (pl.getCachedDefaultValues().containsKey(name)) {
                sender.sendMessage(pl.getCachedDefaultValues().get(name).toString());
            } else {
                sender.sendMessage(prefix + "§cInvalid key.");
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return List.of(SUB_RELOAD, SUB_DEFAULTVALUE);
        if (args.length == 2 && args[0].equalsIgnoreCase(SUB_DEFAULTVALUE)) {
            return pl.getCachedDefaultValues().keySet().stream().toList();
        }
        return Collections.emptyList();
    }
}
