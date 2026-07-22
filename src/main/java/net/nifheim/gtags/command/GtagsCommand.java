package net.nifheim.gtags.command;

import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.nifheim.gtags.Gtags;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class GtagsCommand extends Command implements PluginIdentifiableCommand {

    private final Gtags plugin;

    protected GtagsCommand(Gtags plugin) {
        super("gtags", "base command for gtags plugin", "please use /gtags", new ArrayList<>());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission("gtags.admin")){
            return true;
        }
        if (args.length == 0){
            sender.sendMessage(Component.text(""));
        }
        return true;
    }


    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
}
