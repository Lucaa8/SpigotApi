package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.ClassMapping;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class SignPackets {

    private static final String BASE_BLOCK_POS = "BaseBlockPosition";
    private static final Class<?> BLOCK_POS;
    private static final Class<?> OPEN_SIGN;
    private static final String UPDATE_SIGN = "SignUpdatePacket";

    private static final Map<String, ClassMapping> mappings = new HashMap<>();

    static {

        BLOCK_POS = ReflectionApi.getNMSClass("core", "BlockPosition");
        OPEN_SIGN = ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayOutOpenSignEditor");

        mappings.put(BASE_BLOCK_POS, new ClassMapping(ReflectionApi.getNMSClass("core", "BaseBlockPosition"), new HashMap<>(){{ put("x", "a"); put("y", "b"); put("z", "c"); }}, new HashMap<>()));
        mappings.put(UPDATE_SIGN, new ClassMapping(ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayInUpdateSign"), new HashMap<>(){{ put("pos", "b"); put("lines", "c"); put("isFrontText", "d"); }}, new HashMap<>()));

    }

    private static Object newBlockPos(Location loc)
    {
        return ReflectionApi.newInstance(BLOCK_POS, new Class[]{int.class, int.class, int.class}, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static ApiPacket openSign(Location signLocation)
    {
        Object position = newBlockPos(signLocation);
        return ApiPacket.create(ReflectionApi.newInstance(OPEN_SIGN, new Class[]{position.getClass(), boolean.class}, position, true));
    }

    public static boolean isUpdateSignPacket(Object packet)
    {
        return packet.getClass() == mappings.get(UPDATE_SIGN).getMappedClass();
    }

    public static boolean getPacketInInfo(Object in_SignUpdatePacket, Location out_signPosition, String[] out_signLines)
    {
        ClassMapping packet = mappings.get(UPDATE_SIGN);

        if(packet.getMappedClass() != in_SignUpdatePacket.getClass())
            return false;

        if(out_signLines == null || out_signLines.length != 4)
        {
            Logger.warn("Tried to get the lines from an Update Sign Packet with an wrong array candidate. Please be sure that out_signLines is not null and is length 4.", SignPackets.class.getName());
            return false;
        }

        Object signPosition = packet.getFieldValue("pos", in_SignUpdatePacket);
        ClassMapping baseBlock = mappings.get(BASE_BLOCK_POS);

        out_signPosition.setX((int)baseBlock.getFieldValue("x", signPosition));
        out_signPosition.setY((int)baseBlock.getFieldValue("y", signPosition));
        out_signPosition.setZ((int)baseBlock.getFieldValue("z", signPosition));

        String[] signLines = (String[]) packet.getFieldValue("lines", in_SignUpdatePacket);
        for (int i = 0; i < 4; i++) {
            out_signLines[i] = signLines[i];
        }

        return true;

    }

}
