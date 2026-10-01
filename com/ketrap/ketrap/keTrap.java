package com.ketrap.ketrap;

import com.ketrap.ketrap.commands.TrapCommand;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.listeners.MenuListener;
import com.ketrap.ketrap.listeners.TrapListener;
import com.ketrap.ketrap.managers.MenuManager;
import com.ketrap.ketrap.managers.RegenerationManager;
import com.ketrap.ketrap.managers.SelectionManager;
import com.ketrap.ketrap.managers.TrapManager;
import com.ketrap.ketrap.data.TrapData.TrapRegion;
import com.ketrap.ketrap.utils.Lang;
import com.ketrap.ketrap.utils.PlaceholderHook;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class keTrap
extends JavaPlugin {
    private static keTrap instance;
    private TrapManager trapManager;
    private SelectionManager selectionManager;
    private MenuManager menuManager;
    private RegenerationManager regenerationManager;
    private static Economy econ;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<UUID, BossBar>();
    private final Map<UUID, Integer> playerTrapIds = new HashMap<UUID, Integer>();

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        Lang.load(this);
        if (!this.setupEconomy()) {
            this.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", this.getDescription().getName()));
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.trapManager = new TrapManager(this);
        this.trapManager.load();
        this.selectionManager = new SelectionManager();
        this.menuManager = new MenuManager(this);
        this.regenerationManager = new RegenerationManager(this);
        Objects.requireNonNull(this.getCommand("trap")).setExecutor((CommandExecutor)new TrapCommand());
        Objects.requireNonNull(this.getCommand("trap")).setTabCompleter((TabCompleter)new TrapCommand());
        this.getServer().getPluginManager().registerEvents((Listener)new TrapListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new MenuListener(this), (Plugin)this);
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderHook.register(this);
        }
        this.startBarTask();
        this.startParticleTask();
        this.checkRequiredPlugins();
        this.getLogger().info("=====================================");
        this.getLogger().info("keTrap v" + this.getDescription().getVersion() + " enabled");
        this.getLogger().info("=====================================");
    }

    public void onDisable() {
        this.trapManager.save();
        this.getLogger().info("keTrap disabled");
    }

    private void checkRequiredPlugins() {
        boolean hasVault = this.getServer().getPluginManager().getPlugin("Vault") != null;
        boolean hasPlaceholderAPI = this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        boolean hasdClans = this.getServer().getPluginManager().getPlugin("dClans") != null;
        if (!hasVault) {
            this.getLogger().warning("Vault not found! Economy features will not work.");
        }
        if (!hasPlaceholderAPI) {
            this.getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
        if (!hasdClans) {
            this.getLogger().info("dClans not found! Clan features disabled. Install dClans for clan integration.");
        }
    }

    public static keTrap getInstance() {
        return instance;
    }

    public TrapManager getTrapManager() {
        return this.trapManager;
    }

    public SelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    public MenuManager getMenuManager() {
        return this.menuManager;
    }

    public RegenerationManager getRegenerationManager() {
        return this.regenerationManager;
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = (Economy)rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    private void startBarTask() {
        this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> {
            try {
                for (Player player : this.getServer().getOnlinePlayers()) {
                    if (player == null || !player.isOnline()) continue;
                    TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
                    if (trap != null) {
                        String actionBarMsg;
                        String ownerName = "Yok";
                        if (trap.getOwner() != null) {
                            Player owner = this.getServer().getPlayer(trap.getOwner());
                            ownerName = owner != null ? owner.getName() : this.getServer().getOfflinePlayer(trap.getOwner()).getName();
                        }
                        if (ownerName == null) {
                            ownerName = "Unknown";
                        }
                        if (trap.isActionBarEnabled() && (actionBarMsg = Lang.get("action-bar-trap", "id", String.valueOf(trap.getId()), "owner", ownerName, "health", String.valueOf((int)trap.getHealth()), "maxhealth", String.valueOf((int)trap.getMaxHealth()), "pvp", trap.isPvPEnabled() ? "Acik" : "Kapali")) != null && !actionBarMsg.isEmpty()) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText((String)Lang.colorize(actionBarMsg)));
                        }
                        if (trap.isBossBarEnabled()) {
                            String bossBarTitle = Lang.get("bossbar-trap", "id", String.valueOf(trap.getId()), "owner", ownerName, "health", String.valueOf((int)trap.getHealth()), "maxhealth", String.valueOf((int)trap.getMaxHealth()));
                            if (bossBarTitle == null || bossBarTitle.isEmpty()) continue;
                            BossBar bossBar = this.getBossBar(player);
                            bossBar.setTitle(Lang.colorize(bossBarTitle));
                            double progress = trap.getHealth() / trap.getMaxHealth();
                            bossBar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
                            if (!bossBar.getPlayers().contains(player)) {
                                bossBar.addPlayer(player);
                            }
                            bossBar.setVisible(true);
                            continue;
                        }
                        this.removeBossBar(player);
                        continue;
                    }
                    this.removeBossBar(player);
                }
            }
            catch (Exception exception) {
            }
        }, 20L, 20L);
    }

    private BossBar getBossBar(Player player) {
        BossBar bar = this.playerBossBars.get(player.getUniqueId());
        if (bar == null) {
            bar = this.getServer().createBossBar("", BarColor.GREEN, BarStyle.SOLID, new BarFlag[0]);
            bar.addPlayer(player);
            this.playerBossBars.put(player.getUniqueId(), bar);
        }
        return bar;
    }

    private void removeBossBar(Player player) {
        BossBar bar = this.playerBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setVisible(false);
            bar.removePlayer(player);
            this.playerBossBars.remove(player.getUniqueId());
        }
    }

    public Map<UUID, Integer> getPlayerTrapIds() {
        return this.playerTrapIds;
    }

    private void startParticleTask() {
        if (!this.getConfig().getBoolean("particles.enabled", true)) return;
        int interval = this.getConfig().getInt("particles.interval", 60);
        this.getServer().getScheduler().runTaskTimer((Plugin)this, () -> {
            try {
                Color ownedColor = this.parseColor(this.getConfig().getString("particles.owned-color", "#00FF00"));
                Color unownedColor = this.parseColor(this.getConfig().getString("particles.unowned-color", "#FF4500"));
                int count = this.getConfig().getInt("particles.count", 1);
                double size = this.getConfig().getDouble("particles.size", 0.2);
                Particle.DustOptions ownedDust = new Particle.DustOptions(ownedColor, (float)size);
                Particle.DustOptions unownedDust = new Particle.DustOptions(unownedColor, (float)size);
                for (com.ketrap.ketrap.data.TrapData trap : this.trapManager.getAllTraps().values()) {
                    Particle.DustOptions dust = trap.isOwned() ? ownedDust : unownedDust;
                    for (TrapRegion region : trap.getRegions()) {
                        double minX = region.getMinX(), minY = region.getMinY(), minZ = region.getMinZ();
                        double maxX = region.getMaxX(), maxY = region.getMaxY(), maxZ = region.getMaxZ();
                        org.bukkit.World world = this.getServer().getWorld(trap.getWorldName());
                        if (world == null) continue;
                        this.spawnEdgeParticles(world, minX, minY, minZ, maxX, maxY, maxZ, dust, count);
                    }
                }
            } catch (Exception e) {}
        }, 40L, (long)interval);
    }

    private void spawnEdgeParticles(org.bukkit.World world, double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, Particle.DustOptions dust, int count) {
        double step = 2.0;
        for (double x = minX; x <= maxX; x += step) {
            world.spawnParticle(Particle.DUST, new Location(world, x, minY, minZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, x, maxY, maxZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, x, minY, maxZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, x, maxY, minZ), count, dust);
        }
        for (double z = minZ; z <= maxZ; z += step) {
            world.spawnParticle(Particle.DUST, new Location(world, minX, minY, z), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, maxX, maxY, z), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, minX, maxY, z), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, maxX, minY, z), count, dust);
        }
        for (double y = minY; y <= maxY; y += step) {
            world.spawnParticle(Particle.DUST, new Location(world, minX, y, minZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, maxX, y, maxZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, minX, y, maxZ), count, dust);
            world.spawnParticle(Particle.DUST, new Location(world, maxX, y, minZ), count, dust);
        }
    }

    private Color parseColor(String hex) {
        try {
            java.awt.Color c = java.awt.Color.decode(hex);
            return Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
        } catch (NumberFormatException e) {
            return Color.GREEN;
        }
    }

    static {
        econ = null;
    }
}
