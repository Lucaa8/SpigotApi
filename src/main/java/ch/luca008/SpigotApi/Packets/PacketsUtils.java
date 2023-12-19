package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PacketsUtils {

    private static final Map<String,ClassMapping> mappings = new HashMap<>();

    private static final Class<?> ENUM_GAMEMODE;
    private static final String CHAT_COMPONENT = "ChatComponent";

    static {

        Version v = ReflectionApi.SERVER_VERSION;

        ENUM_GAMEMODE = ReflectionApi.getNMSClass("world.level", "EnumGamemode");
        //mappings.put(ENUM_GAMEMODE, new ClassMapping(ReflectionApi.getNMSClass("world.level", "EnumGamemode"), new HashMap<>(){{ put("SURVIVAL","a"); put("CREATIVE", "b"); put("ADVENTURE", "c"); put("SPECTATOR", "d"); }}, new HashMap<>()));
        mappings.put(CHAT_COMPONENT, new ClassMapping(ReflectionApi.getNMSClass("network.chat", "IChatBaseComponent"), new HashMap<>(), new HashMap<>(){{ put("literal", "a"); }}));

    }

    public enum NMS_GameMode {
        SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR;

        public Enum<?> getGameMode()
        {
            return ReflectionApi.getEnumValue(ENUM_GAMEMODE, this.name());
        }

    }

    public static Object getChatComponent(@Nullable String message)
    {
        return mappings.get(CHAT_COMPONENT).invoke(null, "literal", new Class[]{String.class}, message);
    }

}
