package net.pitsim.spigot;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ChatUtils {
    public static String parse(String message, Player papiTarget) {
        return parse(PlaceholderAPI.setPlaceholders(papiTarget, message));
    }
    public static String parse(String message) {
        Bukkit.getLogger().log(Level.INFO, "Parsing: " + message + "...");
        String result = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(message));
        Bukkit.getLogger().log(Level.INFO, "Result: " + result + "!");
        return result;
    }
}
