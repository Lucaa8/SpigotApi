package ch.luca008.SpigotApi.Utils;

import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;

public class Logger {

    public static void info(String message)
    {
        log(Level.INFO, message, null);
    }

    public static void info(String message, Plugin sender)
    {
        log(Level.INFO, message, sender);
    }

    public static void info(String message, @Nonnull String sender)
    {
        _log(Level.INFO, message, sender);
    }

    public static void warn(String message)
    {
        log(Level.WARNING, message, null);
    }

    public static void warn(String message, Plugin sender)
    {
        log(Level.WARNING, message, sender);
    }

    public static void warn(String message, @Nonnull String sender)
    {
        _log(Level.WARNING, message, sender);
    }

    public static void error(String message)
    {
        log(Level.SEVERE, message, null);
    }

    public static void error(String message, Plugin sender)
    {
        log(Level.SEVERE, message, sender);
    }

    public static void error(String message, @Nonnull String sender)
    {
        _log(Level.SEVERE, message, sender);
    }

    private static void _log(Level type, String message, String sender)
    {
        Bukkit.getLogger().log(type, "[" + sender + "] " + message);
    }

    private static void log(Level type, String message, @Nullable Plugin sender)
    {
        _log(type, message, sender==null ? SpigotApi.getInstance().getName() : sender.getName());
    }

}
