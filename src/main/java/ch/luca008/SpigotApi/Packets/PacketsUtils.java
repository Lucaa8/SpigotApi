package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.*;
import ch.luca008.SpigotApi.Utils.Logger;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PacketsUtils {

    private static final Map<String,ClassMapping> mappings = new HashMap<>();

    private static final Class<?> CRAFT_PLAYER;
    private static final Class<?> ENUM_GAMEMODE;
    private static final String CHAT_COLOR = "EnumChatFormat";
    private static final String CHAT_COMPONENT = "ChatComponent";
    private static final String ENTITY_PLAYER = "EntityPlayer";
    private static final Field network_manager;
    private static final Field netty_channel;
    private static final Method sendPacket;

    static {

        Version v = ReflectionApi.SERVER_VERSION;

        CRAFT_PLAYER = ReflectionApi.getOBCClass("entity", "CraftPlayer");
        ENUM_GAMEMODE = ReflectionApi.getNMSClass("world.level", "EnumGamemode");
        mappings.put(CHAT_COMPONENT, new ClassMapping(ReflectionApi.getNMSClass("network.chat", "IChatBaseComponent"), new HashMap<>(), new HashMap<>(){{ put("literal", "a"); }}));
        mappings.put(ENTITY_PLAYER, new ClassMapping(ReflectionApi.getNMSClass("server.level", "EntityPlayer"), new HashMap<>(){{ put("connection", "c"); }}, new HashMap<>()));
        mappings.put(CHAT_COLOR, new ClassMapping(ReflectionApi.getNMSClass("", "EnumChatFormat"), new HashMap<>(), new HashMap<>(){{ put("byName", "b"); }}));

        Class<?> playerConn;
        String fieldConn;
        String nettyChan;
        if(v == Version.MC_1_20)
        {
            playerConn = ReflectionApi.getNMSClass("server.network", "PlayerConnection");
            fieldConn = "h";
            nettyChan = "m";
        } else {
            playerConn = ReflectionApi.getNMSClass("server.network", "ServerCommonPacketListenerImpl");
            fieldConn = "c";
            nettyChan = "n";
        }
        //I think the worst code possible but what else can I do ??? final fields are such a nightmare
        Field tmp_nm = null;
        Field tmp_nc = null;
        Method tmp_sp = null;
        try{
            Class<?> networkClass = ReflectionApi.getNMSClass("network", "NetworkManager");
            tmp_nm = playerConn.getDeclaredField(fieldConn);
            tmp_nm.setAccessible(true);
            tmp_nc = networkClass.getField(nettyChan);
            tmp_nc.setAccessible(true);
            tmp_sp = networkClass.getMethod("a", ReflectionApi.getNMSClass("network.protocol", "Packet"));
        } catch (Exception ex) {
            Logger.error("Something went wrong while getting the PlayerConnection field and/or the sendPacket method. Are you sure you're using a Spigot/Paper build?");
        }
        network_manager = tmp_nm;
        netty_channel = tmp_nc;
        sendPacket = tmp_sp;

    }

    public enum NMS_GameMode {
        SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR;

        public Enum<?> getGameMode()
        {
            return ReflectionApi.getEnumValue(ENUM_GAMEMODE, this.name());
        }

    }

    public enum ChatColor {

        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET;

        private final Enum<?> mcEnumChatColor;
        ChatColor()
        {
            mcEnumChatColor = (Enum<?>) mappings.get(CHAT_COLOR).invoke(null, "byName", new Class[]{String.class}, this.name());
        }
        public Enum<?> getEnumValue()
        {
            return mcEnumChatColor;
        }

    }

    public static Object getChatComponent(@Nullable String message)
    {
        return mappings.get(CHAT_COMPONENT).invoke(null, "literal", new Class[]{String.class}, message);
    }

    public static Object getEntityPlayer(Player player)
    {
        return ReflectionApi.invoke(CRAFT_PLAYER, player, "getHandle", null);
    }

    public static Object getNetworkManager(Player player)
    {
        return getNetworkManager(getEntityPlayer(player));
    }

    public static Object getNetworkManager(Object entityPlayer)
    {
        try{
            Object playerConn = mappings.get(ENTITY_PLAYER).getFieldValue("connection", entityPlayer);
            return network_manager.get(playerConn);
        } catch (Exception e) {
            Logger.error("Failed to get the NetworkManager of the given player.");
            e.printStackTrace();
        }
        return null;
    }

    public static Channel getPlayerChannel(Player player)
    {
        return getPlayerChannel(getEntityPlayer(player));
    }

    public static Channel getPlayerChannel(Object entityPlayer)
    {
        try{
            return (Channel) netty_channel.get(getNetworkManager(entityPlayer));
        } catch (Exception e) {
            Logger.error("Failed to get the netty channel for player " + entityPlayer);
            e.printStackTrace();
        }
        return null;
    }

    public static void sendPacket(Player player, Object packet)
    {
        sendPacket(getEntityPlayer(player), packet);
    }

    public static void sendPacket(Object entityPlayer, Object packet)
    {
        if(packet == null)
        {
            Logger.error("Tried to send a null packet. Aborting");
            return;
        }
        try{
            sendPacket.invoke(getNetworkManager(entityPlayer), packet);
        } catch (Exception e) {
            Logger.error("Failed to send the given packet " + packet.getClass().getName());
            e.printStackTrace();
        }
    }

}
