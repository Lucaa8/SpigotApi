package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.NPCApi;
import ch.luca008.SpigotApi.Api.ReflectionApi;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;

import java.util.*;

import ch.luca008.SpigotApi.Api.ReflectionApi.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class EntityPackets
{

    private static final String ADD_ENTITY = "AddEntityInfoPacket"; //the base packet with enumset actions and list of Record entries
    private static final String ADD_ENTITY_ENTRY = ADD_ENTITY + "Entry"; //The uuid, gamemode, listed, latency and displayname
    private static final String ADD_ENTITY_ACTION = ADD_ENTITY + "Action"; //ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAME_MODE, UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME
    private static final String SPAWN_ENTITY = "SpawnEntityPacket"; //PacketPlayOutNamedEntitySpawn for 1.20.1 then HeadRotation, or PacketPlayOutSpawnEntity for 1.20.2 and later
    private static final String ENTITY_HEAD_ROT = "EntityHeadRotationPacket"; //For 1.20.1
    private static final String REMOVE_ENTITY = "RemoveEntityInfoPacket";
    private static final String DESTROY_ENTITY = "DestroyEntityPacket";

    private static final Map<String, ClassMapping> mappings = new HashMap<>();

    static {

        Version serverVersion = ReflectionApi.SERVER_VERSION;
        String protocolPackage = "network.protocol.game";
        if(serverVersion == Version.MC_1_20){
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutNamedEntitySpawn"), new HashMap<>(){{ put("id", "a"); put("uuid", "b"); put("x", "c"); put("y", "d"); put("z", "e"); put("yaw", "f"); put("pitch", "g"); }},  new HashMap<>()));
            mappings.put(ENTITY_HEAD_ROT, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutEntityHeadRotation"), new HashMap<>() {{ put("id", "a"); put("head", "b"); }}, new HashMap<>()));
        } else {
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutSpawnEntity"), new HashMap<>(){{ put("id", "c"); put("uuid", "d"); put("type", "e"); put("x", "f"); put("y", "g"); put("z", "h"); put("velX", "i"); put("velY", "j"); put("velZ", "k"); put("pitch", "l"); put("yaw", "m"); put("head", "n"); put("data", "o"); }}, new HashMap<>()));
        }

        mappings.put(ADD_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket"), new HashMap<>() {{ put("actions", "a"); put("entries", "b"); }}, new HashMap<>()));
        //As Record are truly final fields, we cant instantiate the Entry class and later on edit the fields. So we use the c class, which is a static class builder for the b Record.
        mappings.put(ADD_ENTITY_ENTRY, new ClassMapping(ReflectionApi.getPrivateInnerClass(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket"), "c"), new HashMap<>(){{put("uuid", "a"); put("profile", "b"); put("listed", "c"); put("latency", "d"); put("gameMode", "e"); put("displayName", "f"); put("chatSession", "g"); }}, new HashMap<>(){{ put("create", "a"); }}));
        mappings.put(ADD_ENTITY_ACTION, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket$a"), new HashMap<>(){{ put("ADD", "ADD_PLAYER"); put("CHAT", "INITIALIZE_CHAT"); put("GAMEMODE", "UPDATE_GAME_MODE"); put("LISTED", "UPDATE_LISTED"); put("PING", "UPDATE_LATENCY"); put("NAME", "UPDATE_DISPLAY_NAME"); }}, new HashMap<>()));
        //We do not need to add attributes mapping because we can init this packet with de given constructor
        mappings.put(REMOVE_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoRemovePacket"), new HashMap<>(), new HashMap<>()));
        //Same
        mappings.put(DESTROY_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutEntityDestroy"), new HashMap<>(), new HashMap<>()));

    }

    public static void attemptSpawnEntity(Player player)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Hello");
        profile.getProperties().put("textures", NPCApi.getProperty("Lucaa_08"));

        ClassMapping actionsMap = mappings.get(ADD_ENTITY_ACTION);
        ObjectMapping entryMap = mappings.get(ADD_ENTITY_ENTRY).unsafe_newInstance();
        EnumSet actions = EnumSet.of(actionsMap.getEnumValue("ADD"), actionsMap.getEnumValue("LISTED"), actionsMap.getEnumValue("NAME"));

        entryMap.set("uuid", profile.getId())
                .set("profile", profile)
                .set("listed", true)
                .set("latency", 40)
                .set("gameMode", PacketsUtils.NMS_GameMode.CREATIVE.getGameMode())
                .set("displayName", PacketsUtils.getChatComponent("§bHello NPC"))
                .set("chatSession", null);

        Object packet1 = mappings.get(ADD_ENTITY).unsafe_newInstance().set("actions", actions).set("entries", List.of(entryMap.invoke("create"))).packet();
        Object packet2 = mappings.get(SPAWN_ENTITY).unsafe_newInstance().set("id", 144).set("uuid", profile.getId()).set("x", 0.5).set("y", 90.0).set("z", 0.5).set("yaw", (byte)0).set("pitch", (byte)0).packet();
        Object packet3 = mappings.get(ENTITY_HEAD_ROT).unsafe_newInstance().set("id", 144).set("head", (byte)45.0).packet();

        //skin

        ((CraftPlayer)player).getHandle().c.a((Packet<?>)packet1);
        ((CraftPlayer)player).getHandle().c.a((Packet<?>)packet2);
        ((CraftPlayer)player).getHandle().c.a((Packet<?>)packet3);

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
