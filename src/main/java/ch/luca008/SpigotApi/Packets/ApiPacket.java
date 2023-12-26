package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApiPacket {

    private static final Class<?> mcPacket = ReflectionApi.getNMSClass("network.protocol", "Packet");

    private final List<Object> packets;

    private ApiPacket(List<Object> packets)
    {
        this.packets = packets;
    }

    /**
     * Create a list of packets to send to any player
     * @param packet_s may be a single packet object or an array of packets
     * @return an ApiPacket instance
     */
    public static ApiPacket create(Object packet_s)
    {

        final List<Object> assignablePackets = new ArrayList<>();

        if(!(packet_s instanceof Object[]))
        {
            packet_s = new Object[]{packet_s};
        }

        for(Object packet : (Object[])packet_s)
        {
            if(mcPacket.isAssignableFrom(packet.getClass()))
                assignablePackets.add(packet);
            else
                Logger.warn("Skipping packet with class " + packet.getClass().getName(), ApiPacket.class.getName());
        }

        return new ApiPacket(assignablePackets);

    }

    public void add(Object packet)
    {
        if(mcPacket.isAssignableFrom(packet.getClass()))
            packets.add(packet);
        else
            Logger.warn("Cannot add packet with class " + packet.getClass().getName(), this.getClass().getName());
    }

    public void addAll(ApiPacket packet)
    {
        this.packets.addAll(packet.packets);
    }

    public void send(Player player)
    {
        for(Object packet : packets)
        {
            PacketsUtils.sendPacket(player, packet);
        }
    }

    public void send(Collection<? extends Player> players)
    {
        for(Player p : players)
        {
            send(p);
        }
    }

    public void sendToOnline()
    {
        send(Bukkit.getOnlinePlayers());
    }

}
