package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.ClassMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.ObjectMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.Version;
import ch.luca008.SpigotApi.Utils.Logger;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class EntityPackets
{

    private static final String ADD_ENTITY = "AddEntityInfoPacket"; //the base packet with enumset actions and list of Record entries
    private static final String ADD_ENTITY_ENTRY = ADD_ENTITY + "Entry"; //The uuid, gamemode, listed, latency and displayname
    private static final String ADD_ENTITY_ACTION = ADD_ENTITY + "Action"; //ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAME_MODE, UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME
    private static final String SPAWN_ENTITY = "SpawnEntityPacket"; //PacketPlayOutNamedEntitySpawn for 1.20.1 then HeadRotation, or PacketPlayOutSpawnEntity for 1.20.2 and later
    private static final String ENTITY_HEAD_ROT = "EntityHeadRotationPacket"; //For 1.20.1
    private static final String REMOVE_ENTITY = "RemoveEntityInfoPacket";
    private static final String DESTROY_ENTITY = "DestroyEntityPacket";
    private static final String ENTITY_METADATA = "EntityMetaDataPacket";
    private static final String IN_USE_ENTITY = "UseEntityPacket"; //PlayIn
    private static final String IN_USE_ENTITY_ACTION = IN_USE_ENTITY + "Action"; //PlayIn
    private static final String IN_USE_ENTITY_INTERACTION = IN_USE_ENTITY + "InteractionAction"; //PlayIn

    private static final AtomicInteger ENTITY_COUNTER;
    private static final Object data_watcher;
    //See index 17 on https://wiki.vg/Entity_metadata#Player for the 2 next attributes
    private static final int skinId = 17;
    private static final byte byteMask = (byte)0x7F;
    private static final Object type_player;
    //Used to convert F3 pitch and yaw to client values
    private static final Function<Float, Byte> convert = (x)->(byte)(x*256.0F/360.0F);

    private static final Map<String, ClassMapping> mappings = new HashMap<>();


    static {

        Version serverVersion = ReflectionApi.SERVER_VERSION;
        String protocolPackage = "network.protocol.game";
        String syncherPackage = "network.syncher";
        if(serverVersion == Version.MC_1_20){
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutNamedEntitySpawn"), new HashMap<>(){{ put("id", "a"); put("uuid", "b"); put("x", "c"); put("y", "d"); put("z", "e"); put("yaw", "f"); put("pitch", "g"); }},  new HashMap<>()));
            mappings.put(ENTITY_HEAD_ROT, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutEntityHeadRotation"), new HashMap<>() {{ put("id", "a"); put("head", "b"); }}, new HashMap<>()));
            type_player = null;
        } else {
            mappings.put(SPAWN_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutSpawnEntity"), new HashMap<>(){{ put("id", "c"); put("uuid", "d"); put("type", "e"); put("x", "f"); put("y", "g"); put("z", "h"); put("velX", "i"); put("velY", "j"); put("velZ", "k"); put("pitch", "l"); put("yaw", "m"); put("head", "n"); put("data", "o"); }}, new HashMap<>()));

            String playerField = "bt";
            if(serverVersion == Version.MC_1_20_3)
            {
                playerField = "bv";
            }
            type_player = ReflectionApi.getStaticField(ReflectionApi.getNMSClass("world.entity", "EntityTypes"), playerField);
        }

        ENTITY_COUNTER = (AtomicInteger) ReflectionApi.getStaticField(ReflectionApi.getNMSClass("world.entity", "Entity"), "d");

        Object byte_serializer = ReflectionApi.getStaticField(ReflectionApi.getNMSClass(syncherPackage, "DataWatcherRegistry"), "a");
        Class<?> dw_b = ReflectionApi.getNMSClass(syncherPackage, "DataWatcher$b");
        Class<?> i_dws = ReflectionApi.getNMSClass(syncherPackage, "DataWatcherSerializer");
        if(byte_serializer != null && dw_b != null && i_dws != null)
        {
            data_watcher = ReflectionApi.newInstance(dw_b, new Class[]{int.class, i_dws, Object.class}, skinId, byte_serializer, byteMask);
        } else {
            data_watcher = null;
            Logger.error("Failed to get the DataWatcher to update NPC skins. Please consider reporting this problem as your Spigot version may be bad supported by the API.");
        }

        mappings.put(ADD_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket"), new HashMap<>() {{ put("actions", "a"); put("entries", "b"); }}, new HashMap<>()));
        //As Record are truly final fields, we cant instantiate the Entry class and later on edit the fields. So we use the c class, which is a static class builder for the b Record.
        mappings.put(ADD_ENTITY_ENTRY, new ClassMapping(ReflectionApi.getPrivateInnerClass(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket"), "c"), new HashMap<>(){{put("uuid", "a"); put("profile", "b"); put("listed", "c"); put("latency", "d"); put("gameMode", "e"); put("displayName", "f"); put("chatSession", "g"); }}, new HashMap<>(){{ put("create", "a"); }}));
        mappings.put(ADD_ENTITY_ACTION, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoUpdatePacket$a"), new HashMap<>(){{ put("ADD", "ADD_PLAYER"); put("CHAT", "INITIALIZE_CHAT"); put("GAMEMODE", "UPDATE_GAME_MODE"); put("LISTED", "UPDATE_LISTED"); put("PING", "UPDATE_LATENCY"); put("NAME", "UPDATE_DISPLAY_NAME"); }}, new HashMap<>()));
        //We do not need to add attributes mapping because we can init this packet with de given constructor
        mappings.put(REMOVE_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundPlayerInfoRemovePacket"), new HashMap<>(), new HashMap<>()));
        //Same
        mappings.put(DESTROY_ENTITY, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutEntityDestroy"), new HashMap<>(), new HashMap<>()));
        //Same
        mappings.put(ENTITY_METADATA, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutEntityMetadata"), new HashMap<>(), new HashMap<>()));
        Class<?> PacketPlayInUseEntity = ReflectionApi.getNMSClass(protocolPackage, "PacketPlayInUseEntity");
        mappings.put(IN_USE_ENTITY, new ClassMapping(PacketPlayInUseEntity, new HashMap<>(){{ put("entityId", "a"); put("action", "b"); put("usingSecondaryAction", "c"); }}, new HashMap<>()));
        mappings.put(IN_USE_ENTITY_ACTION, new ClassMapping(ReflectionApi.getPrivateInnerClass(PacketPlayInUseEntity, "EnumEntityUseAction"), new HashMap<>(), new HashMap<>(){{ put("getType", "a"); }}));
        mappings.put(IN_USE_ENTITY_INTERACTION, new ClassMapping(ReflectionApi.getPrivateInnerClass(PacketPlayInUseEntity, "d"), new HashMap<>(){{ put("hand", "a"); }}, new HashMap<>()));

    }

    public static int nextId()
    {
        return ENTITY_COUNTER.incrementAndGet();
    }

    public static Object[] addEntity(String name, @Nullable UUID uuid, @Nullable Property textures, boolean listed, int latency, @Nullable String displayName)
    {

        if(displayName == null)
            displayName = name;

        GameProfile profile = new GameProfile(uuid == null ? UUID.randomUUID() : uuid, name);
        if(textures != null)
            profile.getProperties().put("textures", textures);

        ClassMapping actionsMap = mappings.get(ADD_ENTITY_ACTION);
        ObjectMapping entryMap = mappings.get(ADD_ENTITY_ENTRY).unsafe_newInstance();

        EnumSet actions = EnumSet.of(actionsMap.getEnumValue("ADD"));

        if(listed)
            actions.add(actionsMap.getEnumValue("LISTED"));

        if(latency >= 0)
            actionsMap.getEnumValue("PING");

        actions.add(actionsMap.getEnumValue("NAME"));

        entryMap.set("uuid", profile.getId())
                .set("profile", profile)
                .set("listed", listed)
                .set("latency", latency)
                .set("gameMode", PacketsUtils.NMS_GameMode.CREATIVE.getGameMode())
                .set("displayName", PacketsUtils.getChatComponent(displayName))
                .set("chatSession", null);

        return new Object[]{ mappings.get(ADD_ENTITY).unsafe_newInstance().set("actions", actions).set("entries", List.of(entryMap.invoke("create"))).packet() };
    }

    public static Object[] spawnEntity(int entityId, UUID npc, Location location, float yaw, float pitch, float headYaw)
    {

        Object PacketPlayOutSpawnEntity = mappings.get(SPAWN_ENTITY).unsafe_newInstance()
                .set("id", entityId).set("uuid", npc)
                .set("type", type_player)
                .set("x", location.getBlockX()+0.5f).set("y", location.getBlockY()*1.0f).set("z", location.getBlockZ()+0.5f)
                .set("velX", 0).set("velY", 0).set("velZ", 0)
                .set("yaw", convert.apply(yaw)).set("pitch", convert.apply(pitch)).set("head", convert.apply(headYaw))
                .set("data", 0).packet();

        Object PacketPlayOutEntityHeadRotation = null;
        if(ReflectionApi.SERVER_VERSION == Version.MC_1_20)
        {
            PacketPlayOutEntityHeadRotation = mappings.get(ENTITY_HEAD_ROT).unsafe_newInstance().set("id", entityId).set("head", convert.apply(headYaw)).packet();
        }

        Object[] packets = new Object[PacketPlayOutEntityHeadRotation==null?1:2];
        packets[0] = PacketPlayOutSpawnEntity;
        if(packets.length == 2)
            packets[1] = PacketPlayOutEntityHeadRotation;

        return packets;
    }

    public static Object[] removeEntity(UUID uuid)
    {
        return new Object[]{mappings.get(REMOVE_ENTITY).newInstance(new Class[]{List.class}, List.of(uuid)).packet()};
    }

    public static Object[] destroyEntity(int entityId)
    {
        return new Object[]{mappings.get(DESTROY_ENTITY).newInstance(new Class[]{int[].class}, (Object) new int[]{entityId}).packet()}; //do not remove this cast
    }

    /**
     * Add the second layer to this entity's skin
     * @param entityId The entity to update on the client
     * @return A packet which update this entity on the client
     */
    public static Object[] updateSkin(int entityId)
    {
        return new Object[]{mappings.get(ENTITY_METADATA).newInstance(new Class[]{int.class, List.class}, entityId, List.of(data_watcher)).packet()};
    }

    public static Object[] getInteractionFromPacket(Object packet)
    {

        ClassMapping mapping = mappings.get(IN_USE_ENTITY);
        if(packet.getClass() != mapping.getMappedClass())
            return null;

        Object action = mapping.getFieldValue("action", packet);
        if(action == null)
            return null;

        Enum actionType = (Enum)mappings.get(IN_USE_ENTITY_ACTION).invoke(action, "getType");
        if(actionType == null || actionType.name().equals("INTERACT_AT"))
            return null;

        //When Right-clicking an entity on 1.9 or later, the server will send 2 UseEntity packet, one with MAIN_HAND and one with OFF_HAND, so all right-clicks are duplicated.
        //I remove one of them so we keep only 1 callback call.
        if(actionType.name().equals("INTERACT"))
        {
            Enum<?> hand = (Enum<?>) mappings.get(IN_USE_ENTITY_INTERACTION).getFieldValue("hand", action);
            if(hand == null || hand.name().equals("OFF_HAND"))
                return null;
        }

        return new Object[]{mapping.getFieldValue("entityId", packet), actionType.name()};

    }

}
