package com.ketrap.ketrap.listeners;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.managers.TrapManager;
import com.ketrap.ketrap.utils.Lang;
import com.ketrap.ketrap.utils.MenuHolder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener
implements Listener {
    private final TrapManager trapManager;

    public MenuListener(keTrap plugin) {
        this.trapManager = plugin.getTrapManager();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) return;
        MenuHolder holder = (MenuHolder)event.getInventory().getHolder();
        String title = ChatColor.stripColor((String)event.getView().getTitle());
        String menuType = holder.getMenuType();
        boolean isMarket = menuType.equals("market");
        boolean isMyTraps = menuType.equals("myTraps");
        boolean isPreview = menuType.equals("preview");
        boolean isConfirm = menuType.equals("confirm");
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().equals((Object)event.getWhoClicked().getInventory())) return;
        Player player = (Player)event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        if (clickedItem.getType() == Material.ARROW) {
            String name = ChatColor.stripColor((String)clickedItem.getItemMeta().getDisplayName());
            int currentPage = 0;
            if (title.contains("Sayfa")) {
                try { currentPage = Integer.parseInt(title.split("Sayfa ")[1]) - 1; } catch (Exception e) {}
            }
            if (name.equalsIgnoreCase(ChatColor.stripColor((String)Lang.get("menu-next-page")))) {
                keTrap.getInstance().getMenuManager().openTrapMarket(player, currentPage + 1);
            } else if (name.equalsIgnoreCase(ChatColor.stripColor((String)Lang.get("menu-previous-page")))) {
                keTrap.getInstance().getMenuManager().openTrapMarket(player, Math.max(0, currentPage - 1));
            }
            return;
        }
        if (isConfirm) {
            if (clickedItem.getType() == Material.RED_TERRACOTTA) {
                player.closeInventory();
                keTrap.getInstance().getMenuManager().openTrapMarket(player, 0);
            } else if (clickedItem.getType() == Material.LIME_TERRACOTTA) {
                ItemStack centerItem = event.getInventory().getItem(13);
                if (centerItem != null && centerItem.getItemMeta() != null) {
                    String centerName = ChatColor.stripColor((String)centerItem.getItemMeta().getDisplayName());
                    String idStr = centerName.replaceAll("[^0-9]", "");
                    if (!idStr.isEmpty()) {
                        try {
                            int id = Integer.parseInt(idStr);
                            TrapData trap = this.trapManager.getTrap(id);
                            if (trap != null) this.executePurchase(player, trap);
                        } catch (NumberFormatException e) {}
                    }
                }
            }
            return;
        }
        if (clickedItem.getType() == Material.IRON_DOOR || clickedItem.getType() == Material.IRON_TRAPDOOR) {
            if (clickedItem.getItemMeta() == null) return;
            String displayName = ChatColor.stripColor((String)clickedItem.getItemMeta().getDisplayName());
            try {
                String idStr = displayName.replaceAll("[^0-9]", "");
                if (idStr.isEmpty()) return;
                int id = Integer.parseInt(idStr);
                TrapData trap = this.trapManager.getTrap(id);
                if (trap != null) {
                    if (event.getClick().isLeftClick()) {
                        if (trap.getOwner() == null) {
                            keTrap.getInstance().getMenuManager().openPurchaseConfirmation(player, trap);
                        } else if (isMyTraps) {
                            this.handleMyTrapsClick(player, trap);
                        }
                    } else if (event.getClick().isRightClick()) {
                        if (trap.getOwner() == null) {
                            keTrap.getInstance().getMenuManager().openPurchaseConfirmation(player, trap);
                        } else {
                            keTrap.getInstance().getMenuManager().openTrapPreview(player, trap);
                        }
                    }
                }
            } catch (NumberFormatException e) {}
        }
    }

    private void executePurchase(Player player, TrapData trap) {
        if (trap.getOwner() != null) {
            Lang.sendMessage(player, "trap-is-rented");
            return;
        }
        if (!this.trapManager.canPlayerBuyTrap(player)) {
            Lang.sendMessage(player, "blacklist-player");
            return;
        }
        if (this.trapManager.getPlayerTraps(player.getUniqueId()).size() >= 3) {
            Lang.sendMessage(player, "trap-limit-reached");
            return;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("dClans")) {
            try {
                Object dClansPlugin = Bukkit.getPluginManager().getPlugin("dClans");
                if (dClansPlugin != null) {
                    Class<?> apiClass = Class.forName("com.dclans.dclans.api.dClansAPI");
                    Object clanObj = apiClass.getMethod("getPlayerClan", UUID.class).invoke(null, player.getUniqueId());
                    if (clanObj == null) {
                        Lang.sendMessage(player, "must-have-clan");
                        return;
                    }
                    Object leaderUUID = clanObj.getClass().getMethod("getLeader").invoke(clanObj);
                    if (!player.getUniqueId().equals(leaderUUID)) {
                        Lang.sendMessage(player, "must-be-clan-leader");
                        return;
                    }
                }
            } catch (Exception e) {
                keTrap.getInstance().getLogger().warning("dClans hook failed: " + e.getMessage());
            }
        }
        double price = keTrap.getInstance().getConfig().getDouble("trap.purchase-price", 100000.0);
        if (!keTrap.getEconomy().has((OfflinePlayer)player, price)) {
            Lang.sendMessage(player, "not-enough-money");
            return;
        }
        keTrap.getEconomy().withdrawPlayer((OfflinePlayer)player, price);
        this.trapManager.setTrapOwner(trap.getId(), player.getUniqueId());
        Lang.sendMessage(player, "trap-purchased", "id", String.valueOf(trap.getId()));
        player.closeInventory();
    }

    private void handleMyTrapsClick(Player player, TrapData trap) {
        player.performCommand("trap info " + trap.getId());
        player.closeInventory();
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}
