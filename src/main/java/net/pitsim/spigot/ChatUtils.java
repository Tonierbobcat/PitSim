package net.pitsim.spigot;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ChatUtils {
    public static String parse(String message, Player papiTarget) {
        return parse(PlaceholderAPI.setPlaceholders(papiTarget, message));
    }
    public static String parse(String message) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(message));
    }
}
