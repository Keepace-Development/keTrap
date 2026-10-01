package com.ketrap.ketrap.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SelectionManager {
    private final Map<UUID, Location> pos1 = new HashMap<UUID, Location>();
    private final Map<UUID, Location> pos2 = new HashMap<UUID, Location>();
    private final Set<UUID> toolMode = new HashSet<UUID>();

    public void setPos1(Player player, Location location) {
        this.pos1.put(player.getUniqueId(), location);
    }

    public void setPos2(Player player, Location location) {
        this.pos2.put(player.getUniqueId(), location);
    }

    public Location getPos1(Player player) {
        return this.pos1.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return this.pos2.get(player.getUniqueId());
    }

    public boolean hasPos1(Player player) {
        return this.pos1.containsKey(player.getUniqueId());
    }

    public boolean hasPos2(Player player) {
        return this.pos2.containsKey(player.getUniqueId());
    }

    public boolean hasCompleteSelection(Player player) {
        return this.hasPos1(player) && this.hasPos2(player);
    }

    public void clearSelection(Player player) {
        UUID uuid = player.getUniqueId();
        this.pos1.remove(uuid);
        this.pos2.remove(uuid);
    }

    public void toggleToolMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.toolMode.contains(uuid)) {
            this.toolMode.remove(uuid);
        } else {
            this.toolMode.add(uuid);
            this.clearSelection(player);
        }
    }

    public boolean isInToolMode(Player player) {
        return this.toolMode.contains(player.getUniqueId());
    }

    public void setToolMode(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            this.toolMode.add(uuid);
            this.clearSelection(player);
        } else {
            this.toolMode.remove(uuid);
        }
    }
}
