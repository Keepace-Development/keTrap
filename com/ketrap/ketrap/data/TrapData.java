package com.ketrap.ketrap.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;

public class TrapData {
    private final int id;
    private UUID owner;
    private double health;
    private double maxHealth;
    private boolean pvpEnabled;
    private List<UUID> boundPlayers;
    private List<Location> boundLocations;
    private Location primaryLocation;
    private final Map<UUID, Long> playerTNTCooldowns = new HashMap<UUID, Long>();
    private boolean actionBarEnabled;
    private boolean bossBarEnabled;
    private boolean flyEnabled;
    private long damageStartTime;
    private int totalTNTPlaced;
    private double totalDamageTaken;
    private final List<TrapRegion> regions = new ArrayList<TrapRegion>();
    private String worldName;
    private Location spawnLocation;
    private Map<String, Object> customData;

    public TrapData(int id, Location location) {
        this.id = id;
        this.primaryLocation = location;
        this.worldName = location.getWorld().getName();
        this.owner = null;
        this.pvpEnabled = true;
        this.boundPlayers = new ArrayList<UUID>();
        this.boundLocations = new ArrayList<Location>();
        this.actionBarEnabled = true;
        this.bossBarEnabled = true;
        this.flyEnabled = false;
        this.damageStartTime = -1L;
        this.totalTNTPlaced = 0;
        this.totalDamageTaken = 0.0;
        this.customData = new HashMap<String, Object>();
        this.maxHealth = 5000.0;
        this.health = 5000.0;
        this.setBounds(location, location);
    }

    public void setBounds(Location loc1, Location loc2) {
        this.regions.clear();
        this.addRegion(loc1, loc2);
    }

    public void addRegion(Location loc1, Location loc2) {
        double minX = Math.min(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double maxY = Math.max(loc1.getY(), loc2.getY());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());
        this.regions.add(new TrapRegion(minX, minY, minZ, maxX, maxY, maxZ));
        this.worldName = loc1.getWorld().getName();
    }

    public List<TrapRegion> getRegions() {
        return new ArrayList<TrapRegion>(this.regions);
    }

    public boolean isInside(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(this.worldName)) {
            return false;
        }
        double px = loc.getX();
        double py = loc.getY();
        double pz = loc.getZ();
        for (TrapRegion region : this.regions) {
            if (!region.isInside(px, py, pz)) continue;
            return true;
        }
        return false;
    }

    public int getId() { return this.id; }
    public UUID getOwner() { return this.owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public double getHealth() { return this.health; }

    public void setHealth(double health) {
        this.health = Math.min(health, this.maxHealth);
        if (this.health <= 0.0) this.health = 0.0;
    }

    public double getMaxHealth() { return this.maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }

    public void damageHealth(double damage) {
        this.setHealth(this.health - damage);
        this.setDamageStartTime(System.currentTimeMillis());
        this.totalDamageTaken += damage;
    }

    public boolean hasPvPEnabled() { return this.pvpEnabled; }
    public boolean isPvPEnabled() { return this.pvpEnabled; }
    public void setPvPEnabled(boolean enabled) { this.pvpEnabled = enabled; }

    public List<UUID> getBoundPlayers() { return new ArrayList<UUID>(this.boundPlayers); }

    public void addBoundPlayer(UUID uuid) {
        if (!this.boundPlayers.contains(uuid)) this.boundPlayers.add(uuid);
    }

    public void removeBoundPlayer(UUID uuid) { this.boundPlayers.remove(uuid); }
    public boolean isBoundPlayer(UUID uuid) { return this.boundPlayers.contains(uuid); }

    public List<Location> getBoundLocations() { return new ArrayList<Location>(this.boundLocations); }

    public void addBoundLocation(Location location) {
        if (!this.boundLocations.contains(location)) this.boundLocations.add(location);
    }

    public void clearBoundLocations() { this.boundLocations.clear(); }
    public Location getPrimaryLocation() { return this.primaryLocation; }
    public Location getCenter() { return this.primaryLocation; }

    public void setPrimaryLocation(Location location) { this.primaryLocation = location; }

    public boolean canPlaceTNT(UUID playerUUID) {
        if (!this.playerTNTCooldowns.containsKey(playerUUID)) return true;
        return System.currentTimeMillis() - this.playerTNTCooldowns.get(playerUUID) >= 15000L;
    }

    public long getLastTNTPlacedTime(UUID playerUUID) {
        return this.playerTNTCooldowns.getOrDefault(playerUUID, -1L);
    }

    public void setTNTPlacedTime(UUID playerUUID) {
        this.playerTNTCooldowns.put(playerUUID, System.currentTimeMillis());
        this.totalTNTPlaced++;
    }

    public int getTotalTNTPlaced() { return this.totalTNTPlaced; }
    public void setTotalTNTPlaced(int count) { this.totalTNTPlaced = count; }

    public double getTotalDamageTaken() { return this.totalDamageTaken; }
    public void setTotalDamageTaken(double damage) { this.totalDamageTaken = damage; }

    public boolean isActionBarEnabled() { return this.actionBarEnabled; }
    public void setActionBarEnabled(boolean enabled) { this.actionBarEnabled = enabled; }
    public boolean isBossBarEnabled() { return this.bossBarEnabled; }
    public void setBossBarEnabled(boolean enabled) { this.bossBarEnabled = enabled; }
    public boolean isFlyEnabled() { return this.flyEnabled; }
    public void setFlyEnabled(boolean enabled) { this.flyEnabled = enabled; }
    public long getDamageStartTime() { return this.damageStartTime; }
    public void setDamageStartTime(long time) { this.damageStartTime = time; }

    public boolean isRecovering() {
        if (this.damageStartTime == -1L) return false;
        return System.currentTimeMillis() - this.damageStartTime < 120000L;
    }

    public Object getCustomData(String key) { return this.customData.get(key); }
    public void setCustomData(String key, Object value) { this.customData.put(key, value); }
    public boolean isOwned() { return this.owner != null; }

    public double getMinX() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMinX(); }
    public double getMinY() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMinY(); }
    public double getMinZ() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMinZ(); }
    public double getMaxX() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMaxX(); }
    public double getMaxY() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMaxY(); }
    public double getMaxZ() { return this.regions.isEmpty() ? 0.0 : this.regions.get(0).getMaxZ(); }
    public String getWorldName() { return this.worldName; }
    public Location getSpawnLocation() { return this.spawnLocation; }
    public void setSpawnLocation(Location spawnLocation) { this.spawnLocation = spawnLocation; }

    public static class TrapRegion {
        private final double minX, minY, minZ, maxX, maxY, maxZ;

        public TrapRegion(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        public boolean isInside(double px, double py, double pz) {
            return px >= this.minX && px < this.maxX + 1.0
                && py >= this.minY && py < this.maxY + 1.0
                && pz >= this.minZ && pz < this.maxZ + 1.0;
        }

        public double getMinX() { return minX; }
        public double getMinY() { return minY; }
        public double getMinZ() { return minZ; }
        public double getMaxX() { return maxX; }
        public double getMaxY() { return maxY; }
        public double getMaxZ() { return maxZ; }
    }
}
