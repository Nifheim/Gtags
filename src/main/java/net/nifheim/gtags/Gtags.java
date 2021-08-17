package net.nifheim.gtags;

import java.io.IOException;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.gtags.database.TagDatabase;
import net.nifheim.gtags.listener.PlayerListener;
import net.nifheim.gtags.manager.TagManager;
import net.nifheim.gtags.menu.TagMenu;
import net.nifheim.gtags.placeholder.TagsPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Gtags extends JavaPlugin {

    private TagDatabase tagDatabase;
    private TagManager tagManager;

    public TagManager getTagManager() {
        return tagManager;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String database = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");
        tagDatabase = new TagDatabase(this, host, port, database, username, password);
        tagManager = new TagManager(this, tagDatabase);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        new TagsPlaceholder(tagManager).register();
        CommandAPI.registerCommand(this, new Command("tags") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    new TagMenu(Gtags.this, player, tagManager.getAllTags()).open(player);
                }
                return true;
            }
        });
        for (Player player : Bukkit.getOnlinePlayers()) {
            tagManager.loadSelectedTag(player);
        }
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister(this);
        try {
            tagDatabase.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
