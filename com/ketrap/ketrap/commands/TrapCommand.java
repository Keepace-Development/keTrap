package com.ketrap.ketrap.commands;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.managers.SelectionManager;
import com.ketrap.ketrap.managers.TrapManager;
import com.ketrap.ketrap.utils.Lang;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TrapCommand
implements CommandExecutor,
TabCompleter {
    private final keTrap plugin = keTrap.getInstance();
    private final TrapManager trapManager = this.plugin.getTrapManager();
    private final SelectionManager selectionManager = this.plugin.getSelectionManager();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + "This command is for players only");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            Lang.sendMessage(player, "command-usage");
            return true;
        }
        String subcommand = args[0].toLowerCase();
        try {
            switch (subcommand) {
                case "tool":
                case "arac": {
                    return this.handleTool(player, args);
                }
                case "toolbagla":
                case "alanbagla": {
                    return this.handleToolBagla(player, args);
                }
                case "create":
                case "olustur": {
                    return this.handleCreate(player, args);
                }
                case "bagla":
                case "bagla2": {
                    return this.handleBagla(player, args);
                }
                case "menu":
                case "menu2": {
                    return this.handleMenu(player, args);
                }
                case "actionbar": {
                    return this.handleActionbar(player, args);
                }
                case "bossbar": {
                    return this.handleBossbar(player, args);
                }
                case "can": {
                    return this.handleHealth(player, args);
                }
                case "devret": {
                    return this.handleTransfer(player, args);
                }
                case "fly":
                case "ucus": {
                    return this.handleFly(player, args);
                }
                case "info":
                case "bilgi": {
                    return this.handleInfo(player, args);
                }
                case "pvp": {
                    return this.handlePvP(player, args);
                }
                case "satinal":
                case "satin al": {
                    return this.handleSatinal(player, args);
                }
                case "temizle":
                case "reset": {
                    return this.handleTemizle(player, args);
                }
                case "guven":
                case "trust": {
                    return this.handleTrust(player, args);
                }
                case "istatistik":
                case "stats": {
                    return this.handleStats(player, args);
                }
                case "admin": {
                    return this.handleAdmin(player, args);
                }
            }
            Lang.sendMessage(player, "command-usage");
            return true;
        }
        catch (Exception e) {
            Lang.sendMessage(player, "error-occurred");
            this.plugin.getLogger().severe("Command error: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private boolean handleTool(Player player, String[] args) {
        if (!player.isOp()) {
            Lang.sendMessage(player, "no-permission");
            return true;
        }
        this.selectionManager.setToolMode(player, true);
        ItemStack tool = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Lang.colorize("&e[selection tool]"));
            tool.setItemMeta(meta);
        }
        player.getInventory().addItem(new ItemStack[]{tool});
        Lang.sendMessage(player, "tool-received");
        return true;
    }

    private boolean handleToolBagla(Player player, String[] args) {
        if (!player.isOp()) {
            Lang.sendMessage(player, "no-permission");
            return true;
        }
        this.selectionManager.setToolMode(player, true);
        ItemStack tool = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Lang.colorize("&b[area binder]"));
            tool.setItemMeta(meta);
        }
        player.getInventory().addItem(new ItemStack[]{tool});
        Lang.sendMessage(player, "toolbagla-received");
        return true;
    }

    private boolean handleCreate(Player player, String[] args) {
        Location pos2;
        Location pos1;
        int trapId;
        block7: {
            block6: {
                if (!player.isOp()) {
                    Lang.sendMessage(player, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    Lang.sendMessage(player, "create-usage");
                    return true;
                }
                trapId = Integer.parseInt(args[1]);
                if (this.selectionManager.hasCompleteSelection(player)) break block6;
                Lang.sendMessage(player, "invalid-selection");
                return true;
            }
            pos1 = this.selectionManager.getPos1(player);
            pos2 = this.selectionManager.getPos2(player);
            if (this.trapManager.getTrap(trapId) == null) break block7;
            Lang.sendMessage(player, "trap-already-exists", "id", String.valueOf(trapId));
            return true;
        }
        try {
            this.trapManager.createTrap(trapId, pos1, pos2);
            Lang.sendMessage(player, "trap-created", "id", String.valueOf(trapId));
            this.selectionManager.clearSelection(player);
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-trap-id");
        }
        return true;
    }

    private boolean handleBagla(Player player, String[] args) {
        TrapData trap;
        int trapId;
        block7: {
            block6: {
                if (!player.hasPermission("ketrap.create")) {
                    Lang.sendMessage(player, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    Lang.sendMessage(player, "bagla-usage");
                    return true;
                }
                trapId = Integer.parseInt(args[1]);
                if (this.selectionManager.hasCompleteSelection(player)) break block6;
                Lang.sendMessage(player, "invalid-selection");
                return true;
            }
            trap = this.trapManager.getTrap(trapId);
            if (trap != null) break block7;
            Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
            return true;
        }
        try {
            Location pos1 = this.selectionManager.getPos1(player);
            Location pos2 = this.selectionManager.getPos2(player);
            trap.addRegion(pos1, pos2);
            this.trapManager.reindexTrap(trapId);
            this.trapManager.save();
            Lang.sendMessage(player, "area-bound", "id", String.valueOf(trapId));
            this.selectionManager.clearSelection(player);
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-number");
        }
        return true;
    }

    private boolean handleTemizle(Player player, String[] args) {
        TrapData trap;
        int trapId;
        block7: {
            block6: {
                if (!player.hasPermission("ketrap.create")) {
                    Lang.sendMessage(player, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.colorize("&7Kullanim: /trap temizle <id> (Secili alani ana alan yapar, digerlerini siler)"));
                    return true;
                }
                trapId = Integer.parseInt(args[1]);
                if (this.selectionManager.hasCompleteSelection(player)) break block6;
                Lang.sendMessage(player, "invalid-selection");
                return true;
            }
            trap = this.trapManager.getTrap(trapId);
            if (trap != null) break block7;
            Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
            return true;
        }
        try {
            Location pos1 = this.selectionManager.getPos1(player);
            Location pos2 = this.selectionManager.getPos2(player);
            trap.setBounds(pos1, pos2);
            this.trapManager.reindexTrap(trapId);
            this.trapManager.save();
            Lang.sendMessage(player, "area-bound", "id", String.valueOf(trapId));
            this.selectionManager.clearSelection(player);
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-number");
        }
        return true;
    }

    private boolean handleMenu(Player player, String[] args) {
        this.plugin.getMenuManager().openTrapMarket(player, 0);
        return true;
    }

    private boolean handleActionbar(Player player, String[] args) {
        if (args.length < 2) {
            Lang.sendMessage(player, "actionbar-usage");
            return true;
        }
        boolean enabled = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("acik");
        TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "info-usage");
            return true;
        }
        if (trap.isActionBarEnabled() == enabled) {
            Lang.sendMessage(player, "already-set");
            return true;
        }
        trap.setActionBarEnabled(enabled);
        this.trapManager.save();
        player.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("actionbar-toggled", "status", enabled ? Lang.get("pvp-enabled") : Lang.get("pvp-disabled")));
        return true;
    }

    private boolean handleBossbar(Player player, String[] args) {
        if (args.length < 2) {
            Lang.sendMessage(player, "bossbar-usage");
            return true;
        }
        boolean enabled = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("acik");
        TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "info-usage");
            return true;
        }
        if (trap.isBossBarEnabled() == enabled) {
            Lang.sendMessage(player, "already-set");
            return true;
        }
        trap.setBossBarEnabled(enabled);
        this.trapManager.save();
        player.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("bossbar-toggled", "status", enabled ? Lang.get("pvp-enabled") : Lang.get("pvp-disabled")));
        return true;
    }

    private boolean handleHealth(Player player, String[] args) {
        double healthAmount;
        double cost;
        TrapData trap;
        block10: {
            block9: {
                block8: {
                    block7: {
                        if (args.length < 3 || !args[1].equalsIgnoreCase("arttir") && !args[1].equalsIgnoreCase("arttir2")) {
                            Lang.sendMessage(player, "can-arttir-usage");
                            return true;
                        }
                        int trapId = Integer.parseInt(args[2]);
                        trap = this.trapManager.getTrap(trapId);
                        if (trap != null) break block7;
                        Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
                        return true;
                    }
                    if (trap.getOwner() != null && trap.getOwner().equals(player.getUniqueId())) break block8;
                    Lang.sendMessage(player, "not-trap-owner");
                    return true;
                }
                cost = 1000.0;
                healthAmount = 100.0;
                if (!(trap.getHealth() >= trap.getMaxHealth())) break block9;
                Lang.sendMessage(player, "trap-full-health");
                return true;
            }
            if (keTrap.getEconomy().has((OfflinePlayer)player, cost)) break block10;
            Lang.sendMessage(player, "not-enough-money");
            return true;
        }
        try {
            keTrap.getEconomy().withdrawPlayer((OfflinePlayer)player, cost);
            this.trapManager.healTrap(trap.getId(), healthAmount);
            Lang.sendMessage(player, "health-increased", "amount", String.valueOf(healthAmount));
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-number");
        }
        return true;
    }

    private boolean handleTransfer(Player player, String[] args) {
        Player targetPlayer;
        int trapId;
        block4: {
            if (args.length < 3) {
                Lang.sendMessage(player, "devret-usage");
                return true;
            }
            trapId = Integer.parseInt(args[1]);
            targetPlayer = Bukkit.getPlayer((String)args[2]);
            if (targetPlayer != null) break block4;
            Lang.sendMessage(player, "player-not-found");
            return true;
        }
        try {
            this.trapManager.setTrapOwner(trapId, targetPlayer.getUniqueId());
            Lang.sendMessage(player, "trap-transferred", "id", String.valueOf(trapId), "player", targetPlayer.getName());
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-number");
        }
        return true;
    }

    private boolean handleFly(Player player, String[] args) {
        if (args.length < 2) {
            Lang.sendMessage(player, "fly-usage");
            return true;
        }
        boolean enabled = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("acik");
        TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "info-usage");
            return true;
        }
        if (trap.isFlyEnabled() == enabled) {
            Lang.sendMessage(player, "already-set");
            return true;
        }
        trap.setFlyEnabled(enabled);
        this.trapManager.save();
        if (enabled) {
            Lang.sendMessage(player, "fly-enabled");
        } else {
            Lang.sendMessage(player, "fly-disabled");
        }
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        String clanName;
        int trapId;
        if (args.length < 2) {
            TrapData currentTrap = this.trapManager.getTrapAtLocation(player.getLocation());
            if (currentTrap == null) {
                Lang.sendMessage(player, "info-usage");
                return true;
            }
            trapId = currentTrap.getId();
        } else {
            try {
                trapId = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                Lang.sendMessage(player, "invalid-number");
                return true;
            }
        }
        TrapData trap = this.trapManager.getTrap(trapId);
        if (trap == null) {
            Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
            return true;
        }
        String ownerName = trap.getOwner() == null ? (Lang.isLanguageTurkish() ? "Yok" : "None") : Bukkit.getOfflinePlayer((UUID)trap.getOwner()).getName();
        clanName = Lang.isLanguageTurkish() ? "Yok" : "None";
        if (trap.getOwner() != null && Bukkit.getPluginManager().isPluginEnabled("dClans")) {
            String dClansClan = this.getdClansClanName(trap.getOwner());
            if (dClansClan != null) {
                clanName = dClansClan;
            }
        }
        StringBuilder trustedBuilder = new StringBuilder();
        if (trap.getBoundPlayers().isEmpty()) {
            trustedBuilder.append(Lang.isLanguageTurkish() ? "Yok" : "None");
        } else {
            int i = 0;
            while (i < trap.getBoundPlayers().size()) {
                String name = Bukkit.getOfflinePlayer((UUID)trap.getBoundPlayers().get(i)).getName();
                trustedBuilder.append(name != null ? name : "Bilinmiyor");
                if (i < trap.getBoundPlayers().size() - 1) {
                    trustedBuilder.append(", ");
                }
                ++i;
            }
        }
        String pvpStatus = trap.isPvPEnabled() ? (Lang.isLanguageTurkish() ? "&aAcik" : "&aEnabled") : (Lang.isLanguageTurkish() ? "&cKapali" : "&cDisabled");
        String locationStr = String.valueOf(trap.getCenter().getBlockX()) + ", " + trap.getCenter().getBlockY() + ", " + trap.getCenter().getBlockZ();
        player.sendMessage(Lang.get("trap-info", "id", String.valueOf(trap.getId()), "owner", ownerName, "clan", clanName, "trusted", trustedBuilder.toString(), "health", String.valueOf((int)trap.getHealth()), "maxhealth", String.valueOf((int)trap.getMaxHealth()), "pvp", pvpStatus, "location", locationStr));
        return true;
    }

    private String getdClansClanName(UUID ownerUUID) {
        try {
            Object dClansPlugin = Bukkit.getPluginManager().getPlugin("dClans");
            if (dClansPlugin == null) return null;
            Class<?> apiClass = Class.forName("com.dclans.dclans.api.dClansAPI");
            Object clanObj = apiClass.getMethod("getPlayerClan", UUID.class).invoke(null, ownerUUID);
            if (clanObj == null) return null;
            return (String) clanObj.getClass().getMethod("getName").invoke(clanObj);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean handlePvP(Player player, String[] args) {
        if (args.length < 2) {
            Lang.sendMessage(player, "pvp-usage");
            return true;
        }
        boolean enabled = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("acik");
        TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "not-in-a-trap");
            return true;
        }
        if (trap.getOwner() == null || !trap.getOwner().equals(player.getUniqueId())) {
            Lang.sendMessage(player, "not-trap-owner");
            return true;
        }
        if (trap.isPvPEnabled() == enabled) {
            Lang.sendMessage(player, "already-set");
            return true;
        }
        trap.setPvPEnabled(enabled);
        this.trapManager.save();
        Lang.sendMessage(player, "pvp-toggled", "status", enabled ? Lang.get("pvp-enabled") : Lang.get("pvp-disabled"));
        return true;
    }

    private boolean handleSatinal(Player player, String[] args) {
        double price;
        int trapId;
        block12: {
            block10: {
                block11: {
                    block9: {
                        TrapData trap;
                        block8: {
                            if (args.length < 2) {
                                Lang.sendMessage(player, "satinal-usage");
                                return true;
                            }
                            trapId = Integer.parseInt(args[1]);
                            trap = this.trapManager.getTrap(trapId);
                            if (trap != null) break block8;
                            Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
                            return true;
                        }
                        if (trap.getOwner() == null) break block9;
                        Lang.sendMessage(player, "trap-is-rented");
                        return true;
                    }
                    if (!Bukkit.getPluginManager().isPluginEnabled("dClans")) break block10;
                    if (this.isPlayerClanLeader(player.getUniqueId())) break block10;
                    Lang.sendMessage(player, "must-be-clan-leader");
                    return true;
                }
            }
            price = this.plugin.getConfig().getDouble("trap.purchase-price", 100000.0);
            if (keTrap.getEconomy().has((OfflinePlayer)player, price)) break block12;
            Lang.sendMessage(player, "not-enough-money");
            return true;
        }
        try {
            keTrap.getEconomy().withdrawPlayer((OfflinePlayer)player, price);
            this.trapManager.setTrapOwner(trapId, player.getUniqueId());
            Lang.sendMessage(player, "trap-purchased", "id", String.valueOf(trapId));
        }
        catch (NumberFormatException e) {
            Lang.sendMessage(player, "invalid-number");
        }
        return true;
    }

    private boolean isPlayerClanLeader(UUID playerUUID) {
        try {
            Object dClansPlugin = Bukkit.getPluginManager().getPlugin("dClans");
            if (dClansPlugin == null) return false;
            Class<?> apiClass = Class.forName("com.dclans.dclans.api.dClansAPI");
            Object clanObj = apiClass.getMethod("getPlayerClan", UUID.class).invoke(null, playerUUID);
            if (clanObj == null) return false;
            Object leaderUUID = clanObj.getClass().getMethod("getLeader").invoke(clanObj);
            return playerUUID.equals(leaderUUID);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean handleTrust(Player player, String[] args) {
        String targetName;
        UUID targetUUID;
        if (args.length < 2) {
            Lang.sendMessage(player, "trust-usage");
            return true;
        }
        TrapData trap = this.trapManager.getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "not-in-a-trap");
            return true;
        }
        if (trap.getOwner() == null || !trap.getOwner().equals(player.getUniqueId())) {
            Lang.sendMessage(player, "not-trap-owner");
            return true;
        }
        Player targetPlayer = Bukkit.getPlayer((String)args[1]);
        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
            targetName = targetPlayer.getName();
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String)args[1]);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                Lang.sendMessage(player, "player-not-found");
                return true;
            }
            targetUUID = offlinePlayer.getUniqueId();
            targetName = offlinePlayer.getName();
        }
        if (trap.isBoundPlayer(targetUUID)) {
            this.trapManager.untrustPlayer(trap.getId(), targetUUID);
            Lang.sendMessage(player, "player-untrusted", "player", targetName);
        } else {
            this.trapManager.trustPlayer(trap.getId(), targetUUID);
            Lang.sendMessage(player, "player-trusted", "player", targetName);
        }
        return true;
    }

    private boolean handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("ketrap.admin")) {
            Lang.sendMessage(player, "no-permission");
            return true;
        }
        if (args.length < 2) {
            Lang.sendMessage(player, "admin-usage");
            return true;
        }
        return new TrapAdminCommand().onCommand((CommandSender)player, null, "trap", args);
    }

    private boolean handleStats(Player player, String[] args) {
        int trapId;
        if (args.length < 2) {
            TrapData currentTrap = this.trapManager.getTrapAtLocation(player.getLocation());
            if (currentTrap == null) {
                Lang.sendMessage(player, "info-usage");
                return true;
            }
            trapId = currentTrap.getId();
        } else {
            try {
                trapId = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Lang.sendMessage(player, "invalid-number");
                return true;
            }
        }
        TrapData trap = this.trapManager.getTrap(trapId);
        if (trap == null) {
            Lang.sendMessage(player, "trap-not-found", "id", String.valueOf(trapId));
            return true;
        }
        String ownerName = trap.getOwner() == null ? (Lang.isLanguageTurkish() ? "Yok" : "None") : Bukkit.getOfflinePlayer(trap.getOwner()).getName();
        player.sendMessage(Lang.colorize("&6===== &eTrap #" + trapId + " Istatistik &6====="));
        player.sendMessage(Lang.colorize("&eSahip: &f" + ownerName));
        player.sendMessage(Lang.colorize("&eCan: &f" + (int)trap.getHealth() + "&7/" + (int)trap.getMaxHealth()));
        player.sendMessage(Lang.colorize("&eToplam TNT: &f" + trap.getTotalTNTPlaced()));
        player.sendMessage(Lang.colorize("&eToplam Hasar: &f" + (int)trap.getTotalDamageTaken()));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String sub;
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            ArrayList<String> subcommands = new ArrayList<String>();
            subcommands.addAll(Arrays.asList("menu", "menu2", "bilgi", "info"));
            if (player.hasPermission("ketrap.buy")) {
                subcommands.addAll(Arrays.asList("satinal", "satin al"));
            }
            if (player.hasPermission("ketrap.trust")) {
                subcommands.addAll(Arrays.asList("guven", "trust"));
            }
            if (player.hasPermission("ketrap.use")) {
                subcommands.addAll(Arrays.asList("can", "health", "devret", "transfer", "ucus", "fly", "pvp", "actionbar", "bossbar", "istatistik", "stats"));
            }
            if (player.hasPermission("ketrap.create")) {
                subcommands.addAll(Arrays.asList("olustur", "create", "bagla", "bagla2", "arac", "tool"));
            }
            if (player.hasPermission("ketrap.admin")) {
                subcommands.add("admin");
            }
            for (String sub2 : subcommands) {
                if (!sub2.startsWith(args[0].toLowerCase())) continue;
                completions.add(sub2);
            }
            return completions;
        }
        if (args.length == 2) {
            sub = args[0].toLowerCase();
            if (sub.equals("bilgi") || sub.equals("info") || sub.equals("istatistik") || sub.equals("stats") || sub.equals("satinal") || sub.equals("satin al") || sub.equals("buy") || sub.equals("bagla") || sub.equals("bagla2") || sub.equals("bind")) {
                ArrayList<String> trapIds = new ArrayList<String>();
                for (TrapData trap : this.trapManager.getAllTraps().values()) {
                    trapIds.add(String.valueOf(trap.getId()));
                }
                return trapIds;
            }
            if (sub.equals("guven") || sub.equals("trust") || sub.equals("devret") || sub.equals("transfer")) {
                ArrayList<String> playerNames = new ArrayList<String>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                return playerNames;
            }
            if (sub.equals("actionbar") || sub.equals("bossbar") || sub.equals("pvp") || sub.equals("fly") || sub.equals("ucus")) {
                return Arrays.asList("acik", "kapali", "on", "off");
            }
            if (sub.equals("can") || sub.equals("health")) {
                return Arrays.asList("arttir");
            }
            if (sub.equals("admin")) {
                return Arrays.asList("satinal", "devret", "sil", "kara-liste", "reload", "temizle");
            }
        }
        if (args.length == 3) {
            sub = args[0].toLowerCase();
            if (sub.equals("can") || sub.equals("health")) {
                ArrayList<String> trapIds = new ArrayList<String>();
                for (TrapData trap : this.trapManager.getAllTraps().values()) {
                    if (trap.getOwner() == null || !trap.getOwner().equals(player.getUniqueId())) continue;
                    trapIds.add(String.valueOf(trap.getId()));
                }
                return trapIds;
            }
            if (sub.equals("admin")) {
                String adminSub = args[1].toLowerCase();
                if (adminSub.equals("devret") || adminSub.equals("transfer")) {
                    ArrayList<String> playerNames = new ArrayList<String>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        playerNames.add(p.getName());
                    }
                    return playerNames;
                }
                if (adminSub.equals("sil") || adminSub.equals("delete") || adminSub.equals("temizle") || adminSub.equals("clear")) {
                    ArrayList<String> trapIds = new ArrayList<String>();
                    for (TrapData trap : this.trapManager.getAllTraps().values()) {
                        trapIds.add(String.valueOf(trap.getId()));
                    }
                    return trapIds;
                }
            }
        }
        return Collections.emptyList();
    }
}
