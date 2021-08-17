package net.nifheim.gtags.listener;

import net.nifheim.gtags.Gtags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Beelzebu
 */
public class PlayerListener implements Listener {

    private final Gtags plugin;

    public PlayerListener(Gtags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.getTagManager().loadSelectedTag(e.getPlayer());
    }
}
