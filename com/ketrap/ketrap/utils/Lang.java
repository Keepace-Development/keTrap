package com.ketrap.ketrap.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Lang {
    private static FileConfiguration langConfig;
    private static String language;

    public static void load(JavaPlugin plugin) {
        try {
            File langFolder = new File(plugin.getDataFolder(), "lang");
            if (!langFolder.exists()) langFolder.mkdirs();
            File en = new File(langFolder, "en.yml");
            File tr = new File(langFolder, "tr.yml");
            if (!en.exists() || !tr.exists()) {
                plugin.saveResource("lang/en.yml", false);
                plugin.saveResource("lang/tr.yml", false);
            }
            language = plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
            File selected = language.startsWith("tr") ? tr : en;
            langConfig = YamlConfiguration.loadConfiguration((File)selected);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load lang files");
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        if (langConfig == null) return key;
        String message = langConfig.getString(key, "");
        return message.isEmpty() ? key : Lang.colorize(message);
    }

    public static List<String> getList(String key) {
        if (langConfig == null) return new ArrayList<String>();
        List list = langConfig.getStringList(key);
        ArrayList<String> coloredList = new ArrayList<String>();
        for (String s : list) coloredList.add(Lang.colorize(s));
        return coloredList;
    }

    public static String get(String key, String ... replacements) {
        String message = Lang.get(key);
        if (replacements.length % 2 != 0) throw new IllegalArgumentException("Replacements must be in pairs");
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return message;
    }

    public static void sendMessage(Player player, String key) {
        player.sendMessage(Lang.get("prefix") + Lang.get(key));
    }

    public static void sendMessage(Player player, String key, String ... replacements) {
        player.sendMessage(Lang.get("prefix") + Lang.get(key, replacements));
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }

    public static boolean isLanguageTurkish() { return language.startsWith("tr"); }

    static { language = "en"; }
}
