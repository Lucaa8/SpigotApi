package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class EntityPackets
{

    public static ClientboundPlayerInfoUpdatePacket addEntity(EntityPlayer entity)
    {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, entity);
    }

    public static PacketPlayOutNamedEntitySpawn spawnEntity(EntityPlayer entity, Location location, float yaw, float pitch)
    {
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entity);
        ReflectionApi.setField(packet, "c", location.getBlockX()+0.5f);
        ReflectionApi.setField(packet, "d", location.getBlockY()*1.0f);
        ReflectionApi.setField(packet, "e", location.getBlockZ()+0.5f);
        ReflectionApi.setField(packet, "f", (byte)((int)(yaw*256.0F/360.0F)));
        ReflectionApi.setField(packet, "g", (byte)((int)(pitch*256.0F/360.0F)));
        return packet;
    }

    public static ClientboundPlayerInfoRemovePacket removeEntity(EntityPlayer entity)
    {
        GameProfile gp = (GameProfile) ReflectionApi.getField(EntityHuman.class, entity, "cp");
        return new ClientboundPlayerInfoRemovePacket(new ArrayList<>(List.of(gp.getId())));
    }

    public static PacketPlayOutEntityDestroy destroyEntity(EntityPlayer entity)
    {
        return new PacketPlayOutEntityDestroy(entity.getBukkitEntity().getEntityId());
    }

    public static PacketPlayOutEntityHeadRotation headRotation(EntityPlayer entity, float yaw)
    {
        return new PacketPlayOutEntityHeadRotation(entity, (byte)((yaw%360)*256/360));
    }

    /**
     * Add the second layer to this entity's skin
     * @param entity The entity to update on the client
     * @param skinMask See index 17 on <a href="https://wiki.vg/Entity_metadata#Player">Player</a>
     * @return A packet which update this entity on the client
     */
    public static PacketPlayOutEntityMetadata updateSkin(EntityPlayer entity, byte skinMask)
    {
        return new PacketPlayOutEntityMetadata(entity.getBukkitEntity().getEntityId(), new ArrayList<>(List.of(DataWatcher.b.a(new DataWatcherObject<>(17, DataWatcherRegistry.a), skinMask))));
    }

    public static PacketPlayOutEntity.PacketPlayOutEntityLook rotateEntity(EntityPlayer entity, int yaw, int pitch)
    {
        return new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getBukkitEntity().getEntityId(), (byte)((int)(yaw*256.0F/360.0F)), (byte)((int)(pitch*256.0F/360.0F)), true);
    }

}
