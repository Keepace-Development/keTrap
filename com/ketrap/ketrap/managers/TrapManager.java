package com.ketrap.ketrap.managers;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.utils.Lang;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TrapManager {
    private final keTrap plugin;
    private final Map<Integer, TrapData> traps = new HashMap<Integer, TrapData>();
    private final Map<String, Set<Integer>> chunkTrapMap = new HashMap<String, Set<Integer>>();
    private final Set<UUID> blacklistedPlayers = new HashSet<UUID>();
    private final Map<Location, Integer> locationTrapMap = new HashMap<Location, Integer>();
    private File trapsFile;
    private File blacklistFile;

    public TrapManager(keTrap plugin) {
        this.plugin = plugin;
        this.createDataFiles();
    }

    private String getChunkKey(String world, int x, int z) {
        return world + "," + (x >> 4) + "," + (z >> 4);
    }

    private void indexTrap(TrapData trap) {
        if (trap.getRegions().isEmpty()) {
            return;
        }
        Set<String> keys = new HashSet<String>();
        for (TrapData.TrapRegion trapRegion : trap.getRegions()) {
            int minCX = (int)Math.floor(trapRegion.getMinX()) >> 4;
            int maxCX = (int)Math.floor(trapRegion.getMaxX()) >> 4;
            int minCZ = (int)Math.floor(trapRegion.getMinZ()) >> 4;
            int maxCZ = (int)Math.floor(trapRegion.getMaxZ()) >> 4;
            for (int cx = minCX; cx <= maxCX; ++cx) {
                for (int cz = minCZ; cz <= maxCZ; ++cz) {
                    keys.add(trap.getWorldName() + "," + cx + "," + cz);
                }
            }
        }
        for (String key : keys) {
            this.chunkTrapMap.computeIfAbsent(key, k -> new HashSet<Integer>()).add(trap.getId());
        }
    }

    private void deindexTrap(int trapId) {
        for (Set<Integer> ids : this.chunkTrapMap.values()) {
            ids.remove(trapId);
        }
    }

    private void createDataFiles() {
        File dataFolder = new File(this.plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.trapsFile = new File(dataFolder, "traps.yml");
        if (!this.trapsFile.exists() && this.plugin.getResource("data/traps.yml") != null) {
            this.plugin.saveResource("data/traps.yml", false);
        }
        this.blacklistFile = new File(dataFolder, "blacklist.yml");
    }

    public void load() {
        YamlConfiguration config;
        if (this.trapsFile.exists() && (config = YamlConfiguration.loadConfiguration((File)this.trapsFile)).contains("traps")) {
            for (String trapIdStr : config.getConfigurationSection("traps").getKeys(false)) {
                try {
                    String ownerStr;
                    int trapId = Integer.parseInt(trapIdStr);
                    String path = "traps." + trapId;
                    double x = config.getDouble(path + ".location.x");
                    double y = config.getDouble(path + ".location.y");
                    double z = config.getDouble(path + ".location.z");
                    String world = config.getString(path + ".location.world");
                    Location loc = new Location(this.plugin.getServer().getWorld(world), x, y, z);
                    TrapData trapData = new TrapData(trapId, loc);
                    if (config.contains(path + ".owner") && !(ownerStr = config.getString(path + ".owner")).equals("none")) {
                        trapData.setOwner(UUID.fromString(ownerStr));
                    }
                    trapData.setHealth(config.getDouble(path + ".health", 5000.0));
                    trapData.setMaxHealth(config.getDouble(path + ".maxhealth", 5000.0));
                    trapData.setPvPEnabled(config.getBoolean(path + ".pvp", false));
                    trapData.setActionBarEnabled(config.getBoolean(path + ".actionbar", true));
                    trapData.setBossBarEnabled(config.getBoolean(path + ".bossbar", true));
                    trapData.setFlyEnabled(config.getBoolean(path + ".fly", false));
                    trapData.setTotalTNTPlaced(config.getInt(path + ".totalTNT", 0));
                    trapData.setTotalDamageTaken(config.getDouble(path + ".totalDamage", 0.0));
                    if (config.contains(path + ".bounds")) {
                        double minX = config.getDouble(path + ".bounds.minX");
                        double minY = config.getDouble(path + ".bounds.minY");
                        double minZ = config.getDouble(path + ".bounds.minZ");
                        double maxX = config.getDouble(path + ".bounds.maxX");
                        double maxY = config.getDouble(path + ".bounds.maxY");
                        double maxZ = config.getDouble(path + ".bounds.maxZ");
                        trapData.setBounds(new Location(loc.getWorld(), minX, minY, minZ), new Location(loc.getWorld(), maxX, maxY, maxZ));
                    }
                    if (config.contains(path + ".regions")) {
                        List regionsList = config.getMapList(path + ".regions");
                        for (Map regMap : regionsList) {
                            double minX = ((Number)regMap.get("minX")).doubleValue();
                            double minY = ((Number)regMap.get("minY")).doubleValue();
                            double minZ = ((Number)regMap.get("minZ")).doubleValue();
                            double maxX = ((Number)regMap.get("maxX")).doubleValue();
                            double maxY = ((Number)regMap.get("maxY")).doubleValue();
                            double maxZ = ((Number)regMap.get("maxZ")).doubleValue();
                            trapData.addRegion(new Location(loc.getWorld(), minX, minY, minZ), new Location(loc.getWorld(), maxX, maxY, maxZ));
                        }
                    }
                    if (config.contains(path + ".spawn")) {
                        double sx = config.getDouble(path + ".spawn.x");
                        double sy = config.getDouble(path + ".spawn.y");
                        double sz = config.getDouble(path + ".spawn.z");
                        float yaw = (float)config.getDouble(path + ".spawn.yaw");
                        float pitch = (float)config.getDouble(path + ".spawn.pitch");
                        trapData.setSpawnLocation(new Location(loc.getWorld(), sx, sy, sz, yaw, pitch));
                    }
                    this.traps.put(trapId, trapData);
                    this.locationTrapMap.put(loc, trapId);
                    this.indexTrap(trapData);
                }
                catch (NumberFormatException e) {
                    this.plugin.getLogger().warning("Invalid trap ID: " + trapIdStr);
                }
            }
        }
        if (this.blacklistFile.exists() && (config = YamlConfiguration.loadConfiguration((File)this.blacklistFile)).contains("blacklist")) {
            for (String playerStr : config.getStringList("blacklist")) {
                try {
                    this.blacklistedPlayers.add(UUID.fromString(playerStr));
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid UUID in blacklist: " + playerStr);
                }
            }
        }
        this.plugin.getLogger().info("Loaded " + this.traps.size() + " traps");
    }

    public void save() {
        YamlConfiguration trapsConfig = new YamlConfiguration();
        for (Map.Entry<Integer, TrapData> entry : this.traps.entrySet()) {
            int id = entry.getKey();
            TrapData trap = entry.getValue();
            String path = "traps." + id;
            Location loc = trap.getPrimaryLocation();
            trapsConfig.set(path + ".location.x", (Object)loc.getX());
            trapsConfig.set(path + ".location.y", (Object)loc.getY());
            trapsConfig.set(path + ".location.z", (Object)loc.getZ());
            trapsConfig.set(path + ".location.world", (Object)loc.getWorld().getName());
            trapsConfig.set(path + ".owner", (Object)(trap.getOwner() != null ? trap.getOwner().toString() : "none"));
            trapsConfig.set(path + ".health", (Object)trap.getHealth());
            trapsConfig.set(path + ".maxhealth", (Object)trap.getMaxHealth());
            trapsConfig.set(path + ".pvp", (Object)trap.isPvPEnabled());
            trapsConfig.set(path + ".actionbar", (Object)trap.isActionBarEnabled());
            trapsConfig.set(path + ".bossbar", (Object)trap.isBossBarEnabled());
            trapsConfig.set(path + ".fly", (Object)trap.isFlyEnabled());
            trapsConfig.set(path + ".totalTNT", (Object)trap.getTotalTNTPlaced());
            trapsConfig.set(path + ".totalDamage", (Object)trap.getTotalDamageTaken());
            if (trap.getSpawnLocation() != null) {
                Location sloc = trap.getSpawnLocation();
                trapsConfig.set(path + ".spawn.x", (Object)sloc.getX());
                trapsConfig.set(path + ".spawn.y", (Object)sloc.getY());
                trapsConfig.set(path + ".spawn.z", (Object)sloc.getZ());
                trapsConfig.set(path + ".spawn.yaw", (Object)Float.valueOf(sloc.getYaw()));
                trapsConfig.set(path + ".spawn.pitch", (Object)Float.valueOf(sloc.getPitch()));
            }
            ArrayList regionsList = new ArrayList();
            for (TrapData.TrapRegion region : trap.getRegions()) {
                HashMap<String, Double> regMap = new HashMap<String, Double>();
                regMap.put("minX", region.getMinX());
                regMap.put("minY", region.getMinY());
                regMap.put("minZ", region.getMinZ());
                regMap.put("maxX", region.getMaxX());
                regMap.put("maxY", region.getMaxY());
                regMap.put("maxZ", region.getMaxZ());
                regionsList.add(regMap);
            }
            trapsConfig.set(path + ".regions", regionsList);
            if (trap.getRegions().isEmpty()) continue;
            TrapData.TrapRegion first = trap.getRegions().get(0);
            trapsConfig.set(path + ".bounds.minX", (Object)first.getMinX());
            trapsConfig.set(path + ".bounds.minY", (Object)first.getMinY());
            trapsConfig.set(path + ".bounds.minZ", (Object)first.getMinZ());
            trapsConfig.set(path + ".bounds.maxX", (Object)first.getMaxX());
            trapsConfig.set(path + ".bounds.maxY", (Object)first.getMaxY());
            trapsConfig.set(path + ".bounds.maxZ", (Object)first.getMaxZ());
        }
        try {
            trapsConfig.save(this.trapsFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Could not save traps: " + e.getMessage());
        }
        YamlConfiguration blacklistConfig = new YamlConfiguration();
        ArrayList<String> blacklistUUIDs = new ArrayList<String>();
        for (UUID uuid : this.blacklistedPlayers) {
            blacklistUUIDs.add(uuid.toString());
        }
        blacklistConfig.set("blacklist", blacklistUUIDs);
        try {
            blacklistConfig.save(this.blacklistFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Could not save blacklist: " + e.getMessage());
        }
    }

    public boolean createTrap(int id, Location pos1, Location pos2) {
        if (this.traps.containsKey(id)) {
            return false;
        }
        Location center = pos1.clone();
        center.setX((pos1.getX() + pos2.getX()) / 2.0);
        center.setY((pos1.getY() + pos2.getY()) / 2.0);
        center.setZ((pos1.getZ() + pos2.getZ()) / 2.0);
        TrapData trapData = new TrapData(id, center);
        trapData.setBounds(pos1, pos2);
        this.traps.put(id, trapData);
        this.locationTrapMap.put(center, id);
        this.indexTrap(trapData);
        this.save();
        return true;
    }

    public void reindexTrap(int id) {
        TrapData trap = this.traps.get(id);
        if (trap != null) {
            this.deindexTrap(id);
            this.indexTrap(trap);
        }
    }

    public boolean deleteTrap(int id) {
        TrapData trap = this.traps.remove(id);
        if (trap != null) {
            this.locationTrapMap.remove(trap.getPrimaryLocation());
            this.deindexTrap(id);
            this.save();
            return true;
        }
        return false;
    }

    public TrapData getTrap(int id) {
        return this.traps.get(id);
    }

    public TrapData getTrapByLocation(Location location) {
        Integer trapId = this.locationTrapMap.get(location);
        return trapId != null ? this.traps.get(trapId) : null;
    }

    public TrapData getTrapAtLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        String key = this.getChunkKey(location.getWorld().getName(), location.getBlockX(), location.getBlockZ());
        Set<Integer> candidates = this.chunkTrapMap.get(key);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        for (int trapId : candidates) {
            TrapData trap = this.traps.get(trapId);
            if (trap == null || !trap.isInside(location)) continue;
            return trap;
        }
        return null;
    }

    public Map<Integer, TrapData> getAllTraps() {
        return new HashMap<Integer, TrapData>(this.traps);
    }

    public void setTrapOwner(int id, UUID owner) {
        TrapData trap = this.getTrap(id);
        if (trap != null) {
            trap.setOwner(owner);
            if (owner != null) {
                trap.setPvPEnabled(false);
            }
            this.save();
        }
    }

    public boolean isTrapOwner(int trapId, Player player) {
        TrapData trap = this.getTrap(trapId);
        return trap != null && trap.getOwner() != null && trap.getOwner().equals(player.getUniqueId());
    }

    public boolean canPlayerBuyTrap(Player player) {
        return !this.blacklistedPlayers.contains(player.getUniqueId());
    }

    public void addToBlacklist(UUID uuid) {
        this.blacklistedPlayers.add(uuid);
        this.save();
    }

    public void removeFromBlacklist(UUID uuid) {
        this.blacklistedPlayers.remove(uuid);
        this.save();
    }

    public boolean isBlacklisted(UUID uuid) {
        return this.blacklistedPlayers.contains(uuid);
    }

    public void damageTrap(int trapId, double damage) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            double oldHealth = trap.getHealth();
            trap.damageHealth(damage);
            if (trap.getHealth() <= 0.0 && oldHealth > 0.0) {
                trap.setOwner(null);
                trap.setFlyEnabled(false);
                String broadcastMsg = Lang.get("trap-destroyed-broadcast", "id", String.valueOf(trapId));
                this.plugin.getServer().broadcastMessage(Lang.colorize(broadcastMsg));
            }
            this.save();
        }
    }

    public void healTrap(int trapId, double amount) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setHealth(trap.getHealth() + amount);
            this.save();
        }
    }

    public void increaseMaxHealth(int trapId, double amount) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setMaxHealth(trap.getMaxHealth() + amount);
            trap.setHealth(trap.getMaxHealth());
            this.save();
        }
    }

    public void setPvPEnabled(int trapId, boolean enabled) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setPvPEnabled(enabled);
            this.save();
        }
    }

    public void setActionBarEnabled(int trapId, boolean enabled) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setActionBarEnabled(enabled);
            this.save();
        }
    }

    public void setBossBarEnabled(int trapId, boolean enabled) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setBossBarEnabled(enabled);
            this.save();
        }
    }

    public void setFlyEnabled(int trapId, boolean enabled) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setFlyEnabled(enabled);
            this.save();
        }
    }

    public void trustPlayer(int trapId, UUID player) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.addBoundPlayer(player);
            this.save();
        }
    }

    public void untrustPlayer(int trapId, UUID player) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.removeBoundPlayer(player);
            this.save();
        }
    }

    public boolean isTrusted(int trapId, UUID player) {
        TrapData trap = this.getTrap(trapId);
        return trap != null && trap.isBoundPlayer(player);
    }

    public List<Integer> getPlayerTraps(UUID owner) {
        ArrayList<Integer> playerTraps = new ArrayList<Integer>();
        for (Map.Entry<Integer, TrapData> entry : this.traps.entrySet()) {
            if (entry.getValue().getOwner() == null || !entry.getValue().getOwner().equals(owner)) continue;
            playerTraps.add(entry.getKey());
        }
        return playerTraps;
    }

    public boolean canPlaceTNT(int trapId, UUID playerUUID) {
        TrapData trap = this.getTrap(trapId);
        return trap != null && trap.canPlaceTNT(playerUUID);
    }

    public void setTNTPlacedTime(int trapId, UUID playerUUID) {
        TrapData trap = this.getTrap(trapId);
        if (trap != null) {
            trap.setTNTPlacedTime(playerUUID);
        }
    }
}
