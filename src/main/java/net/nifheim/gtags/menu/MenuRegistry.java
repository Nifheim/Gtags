package net.nifheim.gtags.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.nifheim.bukkit.menulib.Menu;

/**
 * @author Beelzebu
 */
public final class MenuRegistry {

    private static final Map<String, Menu> menuHolder = new HashMap<>();

    public static Menu getMenu(UUID uniqueId, int page, Supplier<Menu> menu) {
        return menuHolder.computeIfAbsent(uniqueId.toString() + page, id -> menu.get());
    }
}
