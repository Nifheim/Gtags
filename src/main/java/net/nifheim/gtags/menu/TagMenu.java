package net.nifheim.gtags.menu;

import java.util.List;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.nifheim.bukkit.menulib.Menu;
import net.nifheim.bukkit.menulib.MenuSlot;
import net.nifheim.gtags.Gtags;
import net.nifheim.gtags.tag.Tag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class TagMenu extends Menu {

    private static final int[] GLASS_PANE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 50, 51, 52, 53};
    private static final int[] TAG_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final Gtags plugin;

    public TagMenu(Gtags plugin, Player player, List<Tag> tags) {
        this(plugin, player, tags, null, 1, (int) Math.ceil(tags.size() / (double) TAG_SLOTS.length));
    }

    private TagMenu(Gtags plugin, Player player, List<Tag> tags, TagMenu previous, int page, int pages) {
        super(plugin.getConfig().getString("messages.menu.title", "").replace("%current%", String.valueOf(page)).replace("%total%", String.valueOf(pages)), 6, null);
        this.plugin = plugin;
        if (tags.size() == 0) {
            throw new IllegalArgumentException("Can't create menu without tags");
        }
        for (int slot : GLASS_PANE_SLOTS) {
            setItem(slot, build(Material.GRAY_STAINED_GLASS_PANE, "&fmc.nifheim.net", null));
        }
        // set back button if required
        if (previous != null) {
            setItem(44, new MenuSlot(getItem("gui.prev"), previous::open));
        }
        // set player skull
        setItem(48, new MenuSlot(getItem("gui.close"), Player::closeInventory));
        updateHead(player);
        // set provided tags
        int tagsAmmount = tags.size();
        for (int i = 0; i < Math.min(tagsAmmount, TAG_SLOTS.length); i++) {
            Tag tag = tags.get(i);
            setItem(TAG_SLOTS[i], new MenuSlot(tag.getItem(plugin.getConfig(), player), p -> {
                if (tag.isUnlocked(p)) {
                    plugin.getTagManager().setTag(p, tag).thenAccept(v -> {
                        if (v) {
                            updateHead(player);
                            player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, .6f));
                        } else {
                            player.playSound(Sound.sound(Key.key("minecraft:entity.villager.no"), Sound.Source.PLAYER, 1, .6f));
                            player.sendMessage(Component.text("error").color(TextColor.color(0xFF1800)));
                        }
                    });
                    return;
                }
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "")));
                player.playSound(Sound.sound(Key.key("minecraft:entity.villager.no"), Sound.Source.PLAYER, 1, 1));
            }));
        }
        // set next button if required
        if (tagsAmmount > TAG_SLOTS.length) {
            setItem(53, new MenuSlot(getItem("gui.next"), MenuRegistry.getMenu(player.getUniqueId(), page + 1, () -> new TagMenu(plugin, player, tags.subList(tagsAmmount - 1, tags.size()), this, page + 1, pages))::open));
        }
    }

    private void updateHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(player);
        headMeta.displayName(LegacyComponentSerializer.legacySection().deserialize(PlaceholderAPI.setPlaceholders(player, plugin.getConfig().getString("gui.skull.name", "&7")).replace('&', LegacyComponentSerializer.SECTION_CHAR)).style(c -> c.decoration(TextDecoration.ITALIC, false), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
        headMeta.lore(plugin.getConfig().getStringList("gui.skull.lore").stream().map(line -> LegacyComponentSerializer.legacySection().deserialize(PlaceholderAPI.setPlaceholders(player, line).replace('&', LegacyComponentSerializer.SECTION_CHAR)).style(c -> c.decoration(TextDecoration.ITALIC, false), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)).collect(Collectors.toList()));
        head.setItemMeta(headMeta);
        setItem(49, new MenuSlot(head, p -> plugin.getTagManager().setTag(p, null).thenAccept(v -> {
            player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, .6f));
            updateHead(player);
        })));
    }

    private ItemStack getItem(String path) {
        Material material;
        String materialPath = plugin.getConfig().getString(path + ".material", "STONE").toUpperCase();
        if ((material = Material.getMaterial(materialPath)) == null) {
            material = Material.STONE;
        }
        String name = plugin.getConfig().getString(path + ".name", "");
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        return build(material, name, lore);
    }

    private @NotNull
    ItemStack build(@NotNull Material material, @NotNull String displayName, @Nullable List<String> lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (lore != null) {
            meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
