package com.ketrap.ketrap.managers;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.utils.Lang;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RegenerationManager {
    private final keTrap plugin;
    private final Queue<BrokenBlock> brokenBlocks = new LinkedList<BrokenBlock>();
    private UUID currentRegenTrapOwner = null;

    public RegenerationManager(keTrap plugin) {
        this.plugin = plugin;
        this.startRegenTask();
    }

    public void handleBlockBreak(Block block) {
        this.brokenBlocks.add(new BrokenBlock(block.getLocation(), block.getBlockData(), System.currentTimeMillis()));
    }

    private void startRegenTask() {
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            long currentTime = System.currentTimeMillis();
            int maxPerTick = 2;
            boolean wasEmpty = this.brokenBlocks.isEmpty();
            for (int restoredInThisTick = 0; !this.brokenBlocks.isEmpty() && restoredInThisTick < maxPerTick; ++restoredInThisTick) {
                BrokenBlock bb = this.brokenBlocks.peek();
                if (currentTime - bb.timestamp < 120000L) break;
                this.brokenBlocks.poll();
                this.notifyOwnerIfNeeded(bb.location);
                this.restoreBlock(bb);
            }
            if (!wasEmpty && this.brokenBlocks.isEmpty()) {
                this.notifyFinished();
            }
        }, 20L, 20L);
    }

    private void notifyOwnerIfNeeded(Location loc) {
        TrapData trap = this.plugin.getTrapManager().getTrapAtLocation(loc);
        if (trap != null && trap.getOwner() != null && !trap.getOwner().equals(this.currentRegenTrapOwner)) {
            this.currentRegenTrapOwner = trap.getOwner();
            Player owner = Bukkit.getPlayer((UUID)this.currentRegenTrapOwner);
            if (owner != null && owner.isOnline()) {
                Lang.sendMessage(owner, "regeneration-started");
            }
        }
    }

    private void notifyFinished() {
        if (this.currentRegenTrapOwner != null) {
            Player owner = Bukkit.getPlayer((UUID)this.currentRegenTrapOwner);
            if (owner != null && owner.isOnline()) {
                Lang.sendMessage(owner, "regeneration-finished");
            }
            this.currentRegenTrapOwner = null;
        }
    }

    private void restoreBlock(BrokenBlock bb) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            Block block = bb.location.getBlock();
            if (block.getType().isAir()) {
                block.setBlockData(bb.blockData);
            }
        });
    }

    private static class BrokenBlock {
        final Location location;
        final BlockData blockData;
        final long timestamp;

        BrokenBlock(Location location, BlockData blockData, long timestamp) {
            this.location = location;
            this.blockData = blockData;
            this.timestamp = timestamp;
        }
    }
}
