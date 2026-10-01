package com.ketrap.ketrap.commands;

import com.ketrap.ketrap.keTrap;
import com.ketrap.ketrap.data.TrapData;
import com.ketrap.ketrap.utils.Lang;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrapAdminCommand
implements CommandExecutor {
    private final keTrap plugin = keTrap.getInstance();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + "Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ketrap.admin")) {
            Lang.sendMessage(player, "no-permission");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-usage"));
            return true;
        }
        String subcommand = args[1].toLowerCase();
        try {
            switch (subcommand) {
                case "delete":
                case "sil": {
                    return this.handleDelete(sender, args);
                }
                case "kara":
                case "blacklist": {
                    return this.handleBlacklist(sender, args);
                }
                case "reload":
                case "yenile": {
                    return this.handleReload(sender);
                }
                case "devret":
                case "transfer": {
                    return this.handleAdminTransfer(sender, args);
                }
                case "temizle":
                case "clear": {
                    return this.handleAdminClear(sender, args);
                }
                case "guven":
                case "trust": {
                    return this.handleAdminTrust(sender, args);
                }
            }
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-usage"));
            return true;
        }
        catch (Exception e) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.colorize("&cError: " + e.getMessage()));
            this.plugin.getLogger().severe("Admin command error: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-delete-usage"));
            return true;
        }
        try {
            int trapId = Integer.parseInt(args[2]);
            if (this.plugin.getTrapManager().deleteTrap(trapId)) {
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-deleted", "id", String.valueOf(trapId)));
            } else {
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-not-found", "id", String.valueOf(trapId)));
            }
        }
        catch (NumberFormatException e) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("invalid-number"));
        }
        return true;
    }

    private boolean handleBlacklist(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-blacklist-usage"));
            return true;
        }
        String playerName = args[2];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8)));
        UUID playerUUID = offlinePlayer.getUniqueId();
        if (this.plugin.getTrapManager().isBlacklisted(playerUUID)) {
            this.plugin.getTrapManager().removeFromBlacklist(playerUUID);
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("player-unblacklisted", "player", playerName));
        } else {
            this.plugin.getTrapManager().addToBlacklist(playerUUID);
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("player-blacklisted", "player", playerName));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        this.plugin.reloadConfig();
        Lang.load(this.plugin);
        sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-reload"));
        return true;
    }

    private boolean handleAdminTransfer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 3) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-transfer-usage", "usage", "/trap admin devret <oyuncu>"));
            return true;
        }
        TrapData trap = this.plugin.getTrapManager().getTrapAtLocation(player.getLocation());
        if (trap == null) {
            Lang.sendMessage(player, "not-in-a-trap");
            return true;
        }
        String targetName = args[2];
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer((String)targetName);
        if (!targetOffline.hasPlayedBefore() && !targetOffline.isOnline()) {
            Lang.sendMessage(player, "player-not-found");
            return true;
        }
        this.plugin.getTrapManager().setTrapOwner(trap.getId(), targetOffline.getUniqueId());
        sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-transferred", "id", String.valueOf(trap.getId()), "player", targetOffline.getName()));
        return true;
    }

    private boolean handleAdminClear(CommandSender sender, String[] args) {
        int trapId;
        block6: {
            TrapData trap;
            block5: {
                if (args.length < 3) {
                    sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-clear-usage"));
                    return true;
                }
                trapId = Integer.parseInt(args[2]);
                trap = this.plugin.getTrapManager().getTrap(trapId);
                if (trap != null) break block5;
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-not-found", "id", String.valueOf(trapId)));
                return true;
            }
            if (trap.getOwner() != null) break block6;
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-already-unowned"));
            return true;
        }
        try {
            this.plugin.getTrapManager().setTrapOwner(trapId, null);
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-cleared-admin", "id", String.valueOf(trapId)));
        }
        catch (NumberFormatException e) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("invalid-number"));
        }
        return true;
    }

    private boolean handleAdminTrust(CommandSender sender, String[] args) {
        int trapId;
        if (args.length < 5) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-trust-usage"));
            return true;
        }
        String action = args[2].toLowerCase();
        try {
            trapId = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("invalid-number"));
            return true;
        }
        TrapData trap = this.plugin.getTrapManager().getTrap(trapId);
        if (trap == null) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("trap-not-found", "id", String.valueOf(trapId)));
            return true;
        }
        String targetName = args[4];
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer((String)targetName);
        if (!targetOffline.hasPlayedBefore() && !targetOffline.isOnline()) {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("player-not-found"));
            return true;
        }
        UUID targetUUID = targetOffline.getUniqueId();
        if (action.equals("ekle") || action.equals("add")) {
            if (!trap.isBoundPlayer(targetUUID)) {
                this.plugin.getTrapManager().trustPlayer(trapId, targetUUID);
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-player-trusted", "player", targetOffline.getName(), "id", String.valueOf(trapId)));
            } else {
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.colorize("&cBu oyuncu zaten trapa yetkili olarak eklenmis."));
            }
        } else if (action.equals("cikar") || action.equals("cikart") || action.equals("cikar2") || action.equals("remove")) {
            if (trap.isBoundPlayer(targetUUID)) {
                this.plugin.getTrapManager().untrustPlayer(trapId, targetUUID);
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-player-untrusted", "player", targetOffline.getName(), "id", String.valueOf(trapId)));
            } else {
                sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.colorize("&cBu oyuncu trapa yetkili olarak eklenmemis."));
            }
        } else {
            sender.sendMessage(String.valueOf(Lang.get("prefix")) + Lang.get("admin-trust-usage"));
        }
        return true;
    }
}
