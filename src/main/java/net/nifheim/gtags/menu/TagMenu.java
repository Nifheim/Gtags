package net.nifheim.gtags.menu;

import java.util.List;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.nifheim.bukkit.util.ItemBuilder;
import net.nifheim.bukkit.util.SkullCreator;
import net.nifheim.bukkit.util.menu.BaseMenu;
import net.nifheim.gtags.Gtags;
import net.nifheim.gtags.tag.Tag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Beelzebu
 */
public class TagMenu extends BaseMenu {

    private static final int[] GLASS_PANE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 50, 51, 52, 53};
    private static final int[] TAG_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final Gtags plugin;

    public TagMenu(Gtags plugin, Player player, List<Tag> tags) {
        this(plugin, player, tags, null, 1, tags.size() / TAG_SLOTS.length);
    }

    private TagMenu(Gtags plugin, Player player, List<Tag> tags, TagMenu previous, int page, int pages) {
        super(6, "Tags disponibles (" + page + "/" + pages + ")");
        this.plugin = plugin;
        if (tags.size() == 0) {
            throw new IllegalArgumentException("Can't create menu without tags");
        }
        for (int slot : GLASS_PANE_SLOTS) {
            setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).displayname("&f").build());
        }
        // set back button if required
        if (previous != null) {
            setItem(44, getItem("gui.prev"), previous::open);
        }
        // set player skull
        setItem(48, getItem("gui.close"), Player::closeInventory);
        updateHead(player);
        // set provided tags
        int tagsAmmount = tags.size();
        for (int i = 0; i < Math.min(tagsAmmount, TAG_SLOTS.length); i++) {
            Tag tag = tags.get(i);
            setItem(TAG_SLOTS[i], tag.getItem(plugin.getConfig(), player), p -> {
                if (p.hasPermission(tag.getPermission())) {
                    if (tag.getDesigner() == null || tag.getDesigner().getUniqueId().equals(p.getUniqueId())) {
                        plugin.getTagManager().setTag(p, tag).thenAccept(v -> {
                            if (v) {
                                updateHead(player);
                                player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, .6f));
                            } else {
                                player.playSound(Sound.sound(Key.key("minecraft:entity.villager.no"), Sound.Source.PLAYER, 1, .6f));
                            }
                        });
                        return;
                    } else {
                        p.sendMessage(tag.getDesigner() != null ? (tag.getDesigner().getName() + " " + tag.getDesigner().getUniqueId().equals(p.getUniqueId())) : "false");
                    }
                }
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "")));
                player.playSound(Sound.sound(Key.key("minecraft:entity.villager.no"), Sound.Source.PLAYER, 1, 1));
            });
        }
        // set next button if required
        if (tagsAmmount > TAG_SLOTS.length) {
            setItem(53, getItem("gui.next"), new TagMenu(plugin, player, tags.subList(tagsAmmount - 1, tags.size()), this, page + 1, pages)::open);
        }
    }

    private void updateHead(Player player) {
        ItemStack head = SkullCreator.itemFromUuid(player.getUniqueId());
        ItemMeta headMeta = head.getItemMeta();
        headMeta.displayName(LegacyComponentSerializer.legacySection().deserialize(PlaceholderAPI.setPlaceholders(player, plugin.getConfig().getString("gui.skull.name", "&7")).replace('&', LegacyComponentSerializer.SECTION_CHAR)).style(c -> c.decoration(TextDecoration.ITALIC, false), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
        headMeta.lore(plugin.getConfig().getStringList("gui.skull.lore").stream().map(line -> LegacyComponentSerializer.legacySection().deserialize(PlaceholderAPI.setPlaceholders(player, line).replace('&', LegacyComponentSerializer.SECTION_CHAR)).style(c -> c.decoration(TextDecoration.ITALIC, false), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)).collect(Collectors.toList()));
        head.setItemMeta(headMeta);
        setItem(49, head, p -> plugin.getTagManager().setTag(p, null).thenAccept(v -> {
            player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, .6f));
            updateHead(player);
        }));
    }

    private ItemStack getItem(String path) {
        Material material = Material.STONE;
        String materialPath = plugin.getConfig().getString(path + ".material", "STONE").toUpperCase();
        if (Material.getMaterial(materialPath) != null) {
            material = Material.getMaterial(materialPath);
        }
        String name = plugin.getConfig().getString(path + ".name", "");
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        return new ItemBuilder(material).displayname(name).lore(lore).build();
    }

}
