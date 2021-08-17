package net.nifheim.gtags.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.nifheim.bukkit.util.SkullCreator;
import net.nifheim.gtags.Gtags;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class Tag {

    private final String name;
    private String displayName;
    private List<String> lore;
    private String permission;
    private final TagDesigner designer;

    public Tag(@NotNull String name) {
        this(name, name, "tags.tag." + name);
    }

    public Tag(@NotNull String name, @NotNull String displayName) {
        this(name, displayName, "tags.tag." + name);
    }

    public Tag(@NotNull String name, @NotNull String displayName, @NotNull String permission) {
        this(name, displayName, permission, null);
    }

    public Tag(@NotNull String name, @NotNull String displayName, @NotNull String permission, @Nullable TagDesigner tagDesigner) {
        this(name, displayName, new ArrayList<>(), permission, tagDesigner);
    }

    public Tag(@NotNull String name, @NotNull String displayName, @NotNull List<String> lore, @NotNull String permission, @Nullable TagDesigner designer) {
        this.name = name;
        this.displayName = displayName;
        this.lore = lore;
        this.permission = permission;
        this.designer = designer;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String getPermission() {
        return permission;
    }

    public void setPermission(@NotNull String permission) {
        this.permission = permission;
    }

    public @Nullable TagDesigner getDesigner() {
        return designer;
    }

    public ItemStack getItem(FileConfiguration configuration, Player player) {
        Component name;
        List<String> lore = new ArrayList<>(this.lore);
        ItemStack itemStack;
        ItemMeta meta;
        if (designer != null) {
            name = replace(configuration.getString("gui.tag.custom.name", ""), player);
            lore.addAll(configuration.getStringList("gui.tag.custom.lore"));
            itemStack = SkullCreator.itemFromUuid(designer.getUniqueId());
        } else {
            name = replace(configuration.getString("gui.tag.normal.name", ""), player);
            lore.addAll(configuration.getStringList("gui.tag.normal.lore"));
            itemStack = new ItemStack(Material.NAME_TAG);
        }
        meta = itemStack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore.stream().map(line -> replace(line, player)).collect(Collectors.toList()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private Component replace(String text, Player player) {
        return LegacyComponentSerializer.legacySection().deserialize(
                ChatColor.translateAlternateColorCodes('&',
                        text.replace("%tag_id%", name)
                                .replace("%tag_name%", displayName)
                                .replace("%status%", Gtags.getPlugin(Gtags.class).getConfig()
                                        .getString("messages.status." + (player.hasPermission(permission) ? "unlocked" : "locked"), ""))
                                .replace("%designer%", designer != null ? designer.getName() : "")
                                .replace('&', LegacyComponentSerializer.SECTION_CHAR)
                )
        ).style(c -> c.decoration(TextDecoration.ITALIC, false), Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
    }
}
