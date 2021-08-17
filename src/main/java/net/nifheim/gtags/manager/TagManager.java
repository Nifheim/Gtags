package net.nifheim.gtags.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.nifheim.gtags.Gtags;
import net.nifheim.gtags.database.TagDatabase;
import net.nifheim.gtags.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class TagManager {

    private final TagDatabase database;
    private final List<Tag> tags = new ArrayList<>();
    private final Cache<UUID, Tag> selectedTags = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(this::onRemoval).build();

    public TagManager(Gtags plugin, TagDatabase database) {
        this.database = database;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> database.getAllTags().thenAccept(storedTags -> {
            tags.clear();
            tags.addAll(storedTags);
        }), 0, 20 * 600);
    }

    public Tag getTag(Player player) {
        return selectedTags.getIfPresent(player.getUniqueId());
    }

    public void loadSelectedTag(Player player) {
        UUID uniqueId = player.getUniqueId();
        database.getTag(uniqueId).thenAccept(tag -> selectedTags.put(uniqueId, tag));
    }

    public List<Tag> getAllTags() {
        return ImmutableList.copyOf(tags);
    }

    public CompletableFuture<Boolean> setTag(@NotNull Player player, @Nullable Tag tag) {
        UUID uniqueId = player.getUniqueId();
        if (tag == null) {
            selectedTags.invalidate(uniqueId);
        } else {
            selectedTags.put(player.getUniqueId(), tag);
        }
        return database.setTag(player.getUniqueId(), tag);
    }

    private void onRemoval(@Nullable UUID key, @Nullable Tag value, RemovalCause cause) {
        if (key == null) {
            return;
        }
        if (cause == RemovalCause.REPLACED || cause == RemovalCause.EXPLICIT) {
            return;
        }
        Player player = Bukkit.getPlayer(key);
        if (player != null) { // player is online, fetch tag again
            loadSelectedTag(player);
        }
    }
}
