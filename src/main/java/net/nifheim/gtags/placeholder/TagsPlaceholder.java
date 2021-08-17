package net.nifheim.gtags.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.nifheim.gtags.manager.TagManager;
import net.nifheim.gtags.tag.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class TagsPlaceholder extends PlaceholderExpansion {

    private final TagManager tagManager;

    public TagsPlaceholder(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gtags";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Beelzebu";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equals("tag")) {
            Tag tag = tagManager.getTag(player);
            if (tag != null) {
                return tag.getDisplayName();
            }
        } else if (params.equals("tag_else")) {
            Tag tag = tagManager.getTag(player);
            if (tag != null) {
                return tag.getDisplayName();
            } else {
                return "&cNingún tag seleccionado";
            }
        }
        return "";
    }
}
