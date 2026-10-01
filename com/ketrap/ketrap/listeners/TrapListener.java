package com.ketrap.ketrap.listeners;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.managers.SelectionManager;
import com.ketrap.ketrap.managers.TrapManager;
import com.ketrap.ketrap.utils.Lang;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class TrapListener
implements Listener {
    private final keTrap plugin;
    private final TrapManager trapManager;
    private final SelectionManager selectionManager;

    public TrapListener(keTrap plugin) {
        this.plugin = plugin;
        this.trapManager = plugin.getTrapManager();
        this.selectionManager = plugin.getSelectionManager();
    }

    @EventHandler
    public void onToolInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!this.selectionManager.isInToolMode(player)) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (item.getType() == Material.GOLDEN_AXE) {
            event.setCancelled(true);
            Action action = event.getAction();
            if (action == Action.LEFT_CLICK_BLOCK) {
                this.selectionManager.setPos1(player, block.getLocation());
                Lang.sendMessage(player, "pos1-selected");
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                this.selectionManager.setPos2(player, block.getLocation());
                Lang.sendMessage(player, "pos2-selected");
            }
        } else if (item.getType() == Material.DIAMOND_AXE) {
            event.setCancelled(true);
            Action action = event.getAction();
            if (action == Action.LEFT_CLICK_BLOCK) {
                this.selectionManager.setPos1(player, block.getLocation());
                Lang.sendMessage(player, "pos1-selected");
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                this.selectionManager.setPos2(player, block.getLocation());
                Lang.sendMessage(player, "pos2-selected");
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onBlockPlace(BlockPlaceEvent event) {
        boolean isOwnerOrTrusted;
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Location loc = block.getLocation();
        TrapData trap = this.trapManager.getTrapAtLocation(loc);
        if (trap == null && block.getType() == Material.TNT && !player.hasPermission("ketrap.admin.tnt")) {
            event.setCancelled(true);
            Lang.sendMessage(player, "invalid-block-placement");
            return;
        }
        if (trap == null) {
            String spawnWorld = this.plugin.getConfig().getString("trap.spawn-world", "Spawn");
            if (!loc.getWorld().getName().equalsIgnoreCase(spawnWorld)) return;
            if (!player.hasPermission("ketrap.admin.build")) {
                event.setCancelled(true);
                Lang.sendMessage(player, "global-build-denied");
            }
            return;
        }
        if (trap.getOwner() == null) {
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-place-block");
            return;
        }
        boolean bl = isOwnerOrTrusted = trap.getOwner() != null && (trap.getOwner().equals(player.getUniqueId()) || trap.isBoundPlayer(player.getUniqueId()));
        if (block.getType() == Material.TNT && !isOwnerOrTrusted) {
            event.setCancelled(false);
        }
        if (isOwnerOrTrusted && event.isCancelled()) {
            event.setCancelled(false);
        }
        if (block.getType() == Material.TNT) {
            if (isOwnerOrTrusted) {
                event.setCancelled(true);
                Lang.sendMessage(player, "cannot-place-tnt");
                return;
            }
            if (!trap.canPlaceTNT(player.getUniqueId())) {
                event.setCancelled(true);
                long timeLeft = (15000L - (System.currentTimeMillis() - trap.getLastTNTPlacedTime(player.getUniqueId()))) / 1000L;
                Lang.sendMessage(player, "tnt-cooldown-msg", "time", String.valueOf(timeLeft));
                return;
            }
            event.setCancelled(true);
            block.setType(Material.AIR);
            TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc.add(0.5, 0.0, 0.5), EntityType.TNT);
            tnt.setFuseTicks(40);
            tnt.setSource((Entity)player);
            this.trapManager.setTNTPlacedTime(trap.getId(), player.getUniqueId());
        } else if (!isOwnerOrTrusted) {
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-place-block");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onBlockBreak(BlockBreakEvent event) {
        boolean isOwnerOrTrusted;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        TrapData trap = this.trapManager.getTrapAtLocation(loc);
        if (trap == null) {
            String spawnWorld = this.plugin.getConfig().getString("trap.spawn-world", "Spawn");
            if (!loc.getWorld().getName().equalsIgnoreCase(spawnWorld)) return;
            if (!player.hasPermission("ketrap.admin.build")) {
                event.setCancelled(true);
                Lang.sendMessage(player, "global-break-denied");
            }
            return;
        }
        if (trap.getOwner() == null) {
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-break-block");
            return;
        }
        boolean bl = isOwnerOrTrusted = trap.getOwner() != null && (trap.getOwner().equals(player.getUniqueId()) || trap.isBoundPlayer(player.getUniqueId()));
        if (isOwnerOrTrusted && event.isCancelled()) event.setCancelled(false);
        if (!isOwnerOrTrusted) {
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-break-block");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        TrapData trap = this.trapManager.getTrapAtLocation(loc);
        if (trap != null) {
            Player owner;
            event.setCancelled(false);
            if (event.blockList().isEmpty()) {
                int radius = (int)Math.ceil(event.getYield());
                if (radius <= 0) radius = 4;
                for (int x = -radius; x <= radius; ++x) {
                    for (int y = -radius; y <= radius; ++y) {
                        for (int z = -radius; z <= radius; ++z) {
                            Block b;
                            Location bLoc = loc.clone().add((double)x, (double)y, (double)z);
                            if (bLoc.distance(loc) <= (double)radius && (b = bLoc.getBlock()).getType() != Material.AIR
                                && b.getType() != Material.BEDROCK && b.getType() != Material.BARRIER && trap.isInside(bLoc)) {
                                event.blockList().add(b);
                            }
                        }
                    }
                }
            } else {
                event.blockList().removeIf(block -> !trap.isInside(block.getLocation()));
            }
            event.blockList().forEach(block -> this.plugin.getRegenerationManager().handleBlockBreak((Block)block));
            double tntDamage = this.plugin.getConfig().getDouble("trap.tnt-damage", 100.0);
            this.trapManager.damageTrap(trap.getId(), tntDamage);
            String msg = Lang.get("trap-damaged", "health", String.valueOf((int)trap.getHealth()), "maxhealth", String.valueOf((int)trap.getMaxHealth()));
            if (trap.getOwner() != null && (owner = this.plugin.getServer().getPlayer(trap.getOwner())) != null && owner.isOnline()) {
                owner.sendTitle(Lang.colorize("&c&lTRAP HASAR ALDI!"), Lang.colorize(msg), 10, 60, 20);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player victim;
        TrapData trap;
        Projectile projectile;
        if (event.getDamager() instanceof TNTPrimed) {
            Location loc = event.getDamager().getLocation();
            TrapData trap2 = this.trapManager.getTrapAtLocation(loc);
            if (trap2 != null) {
                if (event.getEntity() instanceof Player) event.setCancelled(true);
            } else {
                TrapData victimTrap = this.trapManager.getTrapAtLocation(event.getEntity().getLocation());
                if (victimTrap != null && event.getEntity() instanceof Player) event.setCancelled(true);
            }
            return;
        }
        if (event.getDamager() instanceof Projectile && (projectile = (Projectile)event.getDamager()).getShooter() instanceof Player
            && event.getEntity() instanceof Player) {
            Player victim2 = (Player)event.getEntity();
            Player attacker = (Player)projectile.getShooter();
            TrapData victimTrap = this.trapManager.getTrapAtLocation(victim2.getLocation());
            if (victimTrap != null) {
                if (victim2.equals(attacker)) return;
                if (victimTrap.getOwner() != null && !victimTrap.isPvPEnabled()) {
                    boolean attackerIsOwnerOrTrusted;
                    boolean bl = attackerIsOwnerOrTrusted = victimTrap.getOwner().equals(attacker.getUniqueId()) || victimTrap.isBoundPlayer(attacker.getUniqueId());
                    if (!attackerIsOwnerOrTrusted) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player
            && (trap = this.trapManager.getTrapAtLocation((victim = (Player)event.getEntity()).getLocation())) != null
            && trap.getOwner() != null && !trap.isPvPEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        boolean isOwnerOrTrusted;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        if (item != null && item.getType() == Material.TNT && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            boolean isOwnerOrTrusted2;
            event.setCancelled(true);
            if (block == null) return;
            TrapData trap = this.trapManager.getTrapAtLocation(block.getLocation());
            if (trap == null) {
                if (!player.hasPermission("ketrap.admin.tnt")) {
                    Lang.sendMessage(player, "invalid-block-placement");
                }
                return;
            }
            boolean bl = isOwnerOrTrusted2 = trap.getOwner() != null && (trap.getOwner().equals(player.getUniqueId()) || trap.isBoundPlayer(player.getUniqueId()));
            if (isOwnerOrTrusted2) {
                Lang.sendMessage(player, "cannot-place-tnt");
                return;
            }
            if (!trap.canPlaceTNT(player.getUniqueId())) {
                long timeLeft = (15000L - (System.currentTimeMillis() - trap.getLastTNTPlacedTime(player.getUniqueId()))) / 1000L;
                Lang.sendMessage(player, "tnt-cooldown-msg", "time", String.valueOf(timeLeft));
                return;
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
            Location loc = block.getRelative(event.getBlockFace()).getLocation();
            TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc.add(0.5, 0.0, 0.5), EntityType.TNT);
            tnt.setFuseTicks(40);
            tnt.setSource((Entity)player);
            this.trapManager.setTNTPlacedTime(trap.getId(), player.getUniqueId());
            return;
        }
        if (block == null) return;
        TrapData trap = this.trapManager.getTrapAtLocation(block.getLocation());
        if (trap == null) return;
        if (this.selectionManager.isInToolMode(player) && item != null
            && (item.getType() == Material.GOLDEN_AXE || item.getType() == Material.DIAMOND_AXE)) return;
        if (item != null && item.getType() == Material.FIREWORK_ROCKET) return;
        if (trap.getOwner() == null) {
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-use-item");
            return;
        }
        boolean bl = isOwnerOrTrusted = trap.getOwner() != null && (trap.getOwner().equals(player.getUniqueId()) || trap.isBoundPlayer(player.getUniqueId()));
        if (isOwnerOrTrusted) {
            if (event.isCancelled()) event.setCancelled(false);
        } else {
            Material type;
            if (item != null && ((type = item.getType()) == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE || type == Material.ELYTRA)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.ALLOW);
                return;
            }
            event.setCancelled(true);
            Lang.sendMessage(player, "cannot-use-item");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) return;
        Player player = event.getPlayer();
        TrapData toTrap = this.trapManager.getTrapAtLocation(to);
        TrapData fromTrap = this.trapManager.getTrapAtLocation(from);
        int prevId = this.plugin.getPlayerTrapIds().getOrDefault(player.getUniqueId(), -1);
        int newId = toTrap != null ? toTrap.getId() : -1;
        if (prevId != newId) {
            this.plugin.getPlayerTrapIds().put(player.getUniqueId(), newId);
            if (toTrap != null && this.plugin.getConfig().getBoolean("sounds.enter.enabled", true)) {
                try {
                    Sound s = Sound.valueOf(this.plugin.getConfig().getString("sounds.enter.sound", "BLOCK_NOTE_BLOCK_PLING"));
                    float vol = (float)this.plugin.getConfig().getDouble("sounds.enter.volume", 0.4);
                    float pit = (float)this.plugin.getConfig().getDouble("sounds.enter.pitch", 1.2);
                    player.playSound(player.getLocation(), s, vol, pit);
                } catch (IllegalArgumentException e) {}
            } else if (fromTrap != null && this.plugin.getConfig().getBoolean("sounds.exit.enabled", true)) {
                try {
                    Sound s = Sound.valueOf(this.plugin.getConfig().getString("sounds.exit.sound", "BLOCK_NOTE_BLOCK_PLING"));
                    float vol = (float)this.plugin.getConfig().getDouble("sounds.exit.volume", 0.4);
                    float pit = (float)this.plugin.getConfig().getDouble("sounds.exit.pitch", 0.8);
                    player.playSound(player.getLocation(), s, vol, pit);
                } catch (IllegalArgumentException e) {}
            }
        }
        if (player.getAllowFlight()) {
            boolean canFlyHere = false;
            if (toTrap != null && toTrap.getOwner() != null && (toTrap.getOwner().equals(player.getUniqueId()) || toTrap.isBoundPlayer(player.getUniqueId()))) {
                canFlyHere = true;
            }
            if (player.hasPermission("essentials.fly") || player.hasPermission("essentials.fly.safelogin")) {
                canFlyHere = true;
            }
            if (!canFlyHere && !player.hasPermission("ketrap.admin.fly")) {
                player.setFlying(false);
                player.setAllowFlight(false);
                Lang.sendMessage(player, "fly-no-permission");
            }
        } else if (toTrap != null && toTrap.getOwner() != null && toTrap.isFlyEnabled()
            && (toTrap.getOwner().equals(player.getUniqueId()) || toTrap.isBoundPlayer(player.getUniqueId()))) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) return;
        Player player = event.getPlayer();
        if (player.hasPermission("essentials.fly") || player.hasPermission("essentials.fly.safelogin")) {
            if (player.hasPermission("ketrap.admin.fly")) return;
            TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
            if (trap != null && trap.getOwner() != null) {
                boolean isOwnerOrTrusted;
                boolean bl = isOwnerOrTrusted = trap.getOwner().equals(player.getUniqueId()) || trap.isBoundPlayer(player.getUniqueId());
                if (!isOwnerOrTrusted) {
                    event.setCancelled(true);
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    Lang.sendMessage(player, "fly-cannot-enable-inside-trap");
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.getAllowFlight()) {
            if (player.hasPermission("essentials.fly") || player.hasPermission("essentials.fly.safelogin") || player.hasPermission("ketrap.admin.fly")) return;
            player.setFlying(false);
            player.setAllowFlight(false);
            Lang.sendMessage(player, "fly-no-permission");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() == EntityType.ENDER_PEARL && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player)event.getEntity().getShooter();
            String spawnWorld = this.plugin.getConfig().getString("trap.spawn-world", "Spawn");
            if (!player.getWorld().getName().equalsIgnoreCase(spawnWorld)) return;
            if (!player.hasPermission("ketrap.admin.enderpearl")) {
                event.setCancelled(true);
                Lang.sendMessage(player, "enderpearl-blocked");
            }
        }
    }
}
