package net.nifheim.gtags.tag;

import java.util.UUID;

/**
 * @author Beelzebu
 */
public class TagDesigner {

    private final int id;
    private final String name;
    private final UUID uniqueId;

    public TagDesigner(int id, String name, UUID uniqueId) {
        this.id = id;
        this.name = name;
        this.uniqueId = uniqueId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}
