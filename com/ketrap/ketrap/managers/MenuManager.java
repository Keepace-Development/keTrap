package com.ketrap.ketrap.managers;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.utils.Lang;
import com.ketrap.ketrap.utils.MenuHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuManager {
    private final keTrap plugin;
    private final TrapManager trapManager;

    public MenuManager(keTrap plugin) {
        this.plugin = plugin;
        this.trapManager = plugin.getTrapManager();
    }

    public void openTrapMarket(Player player, int page) {
        String title = Lang.get("menu-title-market") + (page > 0 ? " - Sayfa " + (page + 1) : "");
        Inventory inv = Bukkit.createInventory((InventoryHolder)new MenuHolder("market"), (int)54, (String)title);
        ArrayList<TrapData> allTraps = new ArrayList<TrapData>(this.trapManager.getAllTraps().values());
        allTraps.sort(Comparator.comparingInt(TrapData::getId));
        int start = page * 45;
        int end = Math.min(start + 45, allTraps.size());
        for (int i = start; i < end; ++i) {
            TrapData trap = (TrapData)allTraps.get(i);
            inv.setItem(i - start, this.createTrapItem(trap, true));
        }
        if (page > 0) {
            inv.setItem(45, this.createNavigationItem(Material.ARROW, Lang.get("menu-previous-page")));
        }
        if (end < allTraps.size()) {
            inv.setItem(53, this.createNavigationItem(Material.ARROW, Lang.get("menu-next-page")));
        }
        player.openInventory(inv);
    }

    public void openPurchaseConfirmation(Player player, TrapData trap) {
        String title = Lang.get("menu-title-confirm");
        Inventory inv = Bukkit.createInventory((InventoryHolder)new MenuHolder("confirm"), (int)27, (String)title);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; ++i) {
            inv.setItem(i, glass);
        }
        ItemStack cancel = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(Lang.get("menu-confirm-cancel"));
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(11, cancel);
        inv.setItem(13, this.createTrapItem(trap, true));
        ItemStack buy = new ItemStack(Material.LIME_TERRACOTTA);
        ItemMeta buyMeta = buy.getItemMeta();
        if (buyMeta != null) {
            buyMeta.setDisplayName(Lang.get("menu-confirm-buy"));
            buy.setItemMeta(buyMeta);
        }
        inv.setItem(15, buy);
        player.openInventory(inv);
    }

    public void openMyTraps(Player player, int page) {
        String title = Lang.get("menu-title-my-traps");
        Inventory inv = Bukkit.createInventory((InventoryHolder)new MenuHolder("myTraps"), (int)54, (String)title);
        List<Integer> playerTrapIds = this.trapManager.getPlayerTraps(player.getUniqueId());
        int start = page * 45;
        int end = Math.min(start + 45, playerTrapIds.size());
        for (int i = start; i < end; ++i) {
            TrapData trap = this.trapManager.getTrap(playerTrapIds.get(i));
            if (trap == null) continue;
            inv.setItem(i - start, this.createTrapItem(trap, false));
        }
        if (page > 0) {
            inv.setItem(45, this.createNavigationItem(Material.ARROW, Lang.get("menu-previous-page")));
        }
        if (end < playerTrapIds.size()) {
            inv.setItem(53, this.createNavigationItem(Material.ARROW, Lang.get("menu-next-page")));
        }
        player.openInventory(inv);
    }

    public void openTrapPreview(Player player, TrapData trap) {
        String title = Lang.get("menu-title-preview", "id", String.valueOf(trap.getId()));
        if (title == null || title.isEmpty()) {
            title = "Trap #" + trap.getId() + " Onizleme";
        }
        Inventory inv = Bukkit.createInventory((InventoryHolder)new MenuHolder("preview"), (int)27, (String)title);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; ++i) {
            inv.setItem(i, glass);
        }
        ItemStack info = new ItemStack(Material.ENDER_EYE);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(Lang.get("menu-preview-info-name", "id", String.valueOf(trap.getId())));
            String ownerName = "Yok";
            String clanName = "Yok";
            if (trap.getOwner() != null) {
                ownerName = Bukkit.getOfflinePlayer((UUID)trap.getOwner()).getName();
                if (ownerName == null) {
                    ownerName = "Bilinmiyor";
                }
                if (Bukkit.getPluginManager().isPluginEnabled("dClans")) {
                    try {
                        Object dClansPlugin = Bukkit.getPluginManager().getPlugin("dClans");
                        if (dClansPlugin != null) {
                            Class<?> apiClass = Class.forName("com.dclans.dclans.api.dClansAPI");
                            Object clanObj = apiClass.getMethod("getPlayerClan", UUID.class).invoke(null, trap.getOwner());
                            if (clanObj != null) {
                                Object tag = clanObj.getClass().getMethod("getTag").invoke(clanObj);
                                if (tag != null) clanName = (String) tag;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            ArrayList<String> lore = new ArrayList<String>();
            for (String line : Lang.getList("menu-preview-lore")) {
                lore.add(line.replace("{owner}", ownerName).replace("{clan}", clanName).replace("{health}", String.valueOf((int)trap.getHealth())).replace("{maxhealth}", String.valueOf((int)trap.getMaxHealth())).replace("{id}", String.valueOf(trap.getId())));
            }
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inv.setItem(13, info);
        player.openInventory(inv);
    }

    private ItemStack createTrapItem(TrapData trap, boolean market) {
        Material material = trap.getOwner() == null ? Material.IRON_DOOR : Material.IRON_TRAPDOOR;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            boolean isOwned;
            meta.setDisplayName(Lang.get("menu-trap-item-name", "id", String.valueOf(trap.getId())));
            ArrayList<String> lore = new ArrayList<String>();
            boolean bl = isOwned = trap.getOwner() != null;
            if (!isOwned) {
                String status = Lang.get("menu-status-available");
                double price = this.plugin.getConfig().getDouble("trap.purchase-price", 100000.0);
                for (String line : Lang.getList("menu-trap-item-lore-market")) {
                    lore.add(line.replace("{price}", String.valueOf(price)).replace("{status}", status));
                }
            } else {
                String offlineName = Bukkit.getOfflinePlayer((UUID)trap.getOwner()).getName();
                String ownerName = offlineName != null ? offlineName : "Bilinmiyor";
                for (String line : Lang.getList("menu-trap-item-lore-owned")) {
                    lore.add(line.replace("{owner}", ownerName).replace("{health}", String.valueOf((int)trap.getHealth())).replace("{maxhealth}", String.valueOf((int)trap.getMaxHealth())));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Lang.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
