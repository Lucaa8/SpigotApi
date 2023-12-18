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

import java.util.*;

import ch.luca008.SpigotApi.Api.ReflectionApi.*;

public class EntityPackets
{

    private static final String ADD_ENTITY = EntityPackets.class.getName() + "_AddEntityPacket"; //the base packet with enumset actions and list of Record entries
    private static final String ADD_ENTITY_RECORD = ADD_ENTITY + "Record"; //The uuid, gamemode, listed, latency and displayname
    private static final String SPAWN_ENTITY =  EntityPackets.class.getName() + "_SpawnEntityPacket";

    public static Map<String, ReflectionApi.ClassMapping> getMappings()
    {
        Map<String, ClassMapping> mappings = new HashMap<>();
        Version serverVersion = ReflectionApi.SERVER_VERSION;
        if(serverVersion == Version.MC_1_20)
        {
            //comment gérer le fait que le packet de spawn en 1.20 ne prend pas la rotation de la tete en paramètres? créer le packet EntityHeadRotation et lenvoyer que si v == 1.20.1?
            Map<String, ApiField> PacketFields = new HashMap<>(){{ put("id", new ApiField("a")); put("uuid", new ReflectionApi.ApiField("b")); put("x", new ReflectionApi.ApiField("c")); put("y", new ReflectionApi.ApiField("d")); put("z", new ReflectionApi.ApiField("e")); put("yaw", new ReflectionApi.ApiField("f")); put("pitch", new ReflectionApi.ApiField("g")); }};
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayOutNamedEntitySpawn"), PacketFields,  new HashMap<>()));
        }
        else if(serverVersion == Version.MC_1_20_2 || serverVersion == Version.MC_1_20_3)
        {
            //PacketPlayOutSpawnEntity packet8 =
            // new PacketPlayOutSpawnEntity(id, gp.getId(), loc.getX(), loc.getY(), loc.getZ(), 0F, 0f, EntityTypes.bv, 0, new Vec3D(0,0,0), 0f);
            Map<String, ApiField> PacketFields = new HashMap<>(){{
                put("id", new ApiField("c"));
                put("uuid", new ApiField("d"));
                //EntityTypes : e
                put("x", new ApiField("f"));
                put("y", new ApiField("g"));
                put("z", new ApiField("h"));
            }};
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayOutSpawnEntity"), PacketFields, new HashMap<>()));
        }
        return mappings;
    }

    public static ClientboundPlayerInfoUpdatePacket addEntity(EntityPlayer entity)
    {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, entity);
    }

    public static PacketPlayOutNamedEntitySpawn spawnEntity(EntityPlayer entity, Location location, float yaw, float pitch)
    {
        /*ObjectMapping PacketObject = ReflectionApi.getMappingForClass(SPAWN_ENTITY).unsafe_newInstance();
        PacketObject.set("id", 144);
        PacketObject.set("uuid", UUID.randomUUID());
        PacketObject.set("x", 0.5);
        PacketObject.set("y", 100.0);
        PacketObject.set("z", 0.5);
        PacketObject.set("yaw", (byte)((int)(90.0*256.0F/360.0F)));
        PacketObject.set("pitch", (byte)0);
        return PacketObject.packet();*/
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
