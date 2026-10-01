package com.ketrap.ketrap.utils;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook
extends PlaceholderExpansion {
    private final keTrap plugin;

    public PlaceholderHook(keTrap plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public String getIdentifier() { return "ketrap"; }

    @NotNull
    public String getAuthor() { return "Keepace"; }

    @NotNull
    public String getVersion() { return this.plugin.getDescription().getVersion(); }

    public boolean persist() { return true; }

    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("trap_health_")) {
            try {
                int id = Integer.parseInt(params.replace("trap_health_", ""));
                TrapData trap = this.plugin.getTrapManager().getTrap(id);
                return trap != null ? String.valueOf((int)trap.getHealth()) : "0";
            } catch (NumberFormatException e) { return "0"; }
        }
        if (params.startsWith("trap_maxhealth_")) {
            try {
                int id = Integer.parseInt(params.replace("trap_maxhealth_", ""));
                TrapData trap = this.plugin.getTrapManager().getTrap(id);
                return trap != null ? String.valueOf((int)trap.getMaxHealth()) : "0";
            } catch (NumberFormatException e) { return "0"; }
        }
        if (params.startsWith("trap_owner_")) {
            try {
                int id = Integer.parseInt(params.replace("trap_owner_", ""));
                TrapData trap = this.plugin.getTrapManager().getTrap(id);
                if (trap == null || trap.getOwner() == null) return "None";
                String name = this.plugin.getServer().getOfflinePlayer(trap.getOwner()).getName();
                return name != null ? name : "Unknown";
            } catch (NumberFormatException e) { return "None"; }
        }
        if (player != null) {
            List<Integer> playerTraps = this.plugin.getTrapManager().getPlayerTraps(player.getUniqueId());
            boolean hasTrap = !playerTraps.isEmpty();
            if (params.equals("player_trap_id")) {
                return hasTrap ? "&c" + String.valueOf(playerTraps.get(0)) : "&c-";
            }
            if (params.equals("player_trap_health")) {
                if (hasTrap) {
                    TrapData trap = this.plugin.getTrapManager().getTrap(playerTraps.get(0));
                    return trap != null ? "&c" + (int)trap.getHealth() : "&c-";
                }
                return "&c-";
            }
        }
        return null;
    }

    public static void register(keTrap plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(plugin).register();
        }
    }
}
