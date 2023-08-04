package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.EntityPackets;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class NPCApi {

    public enum Directions {
        OTHER(0, 0, 0), NORTH(228, 180, 0), EAST(-138, -90, 0), WEST(138, 90, 0), SOUTH(0, 0, 0);

        private final int bodyYaw;
        private final int headYaw;
        private final int pitch;

        Directions(int bodyYaw, int headYaw, int pitch)
        {
            this.bodyYaw = bodyYaw;
            this.headYaw = headYaw;
            this.pitch = pitch;
        }

        public int getBodyYaw(){return bodyYaw;}
        public int getHeadYaw(){return headYaw;}
        public int getPitch(){return pitch;}

    }

    public static Property getProperty(String playerName) {
        return WebRequest.getSkin(playerName);
    }

    public static Property getProperty(String value, String signature)
    {
        return new Property("textures", value, signature);
    }

    public EntityPlayer createEntity(World world, String name, Property skin)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        if(skin != null){
            profile.getProperties().put("textures", skin);
        }
        MinecraftServer server = (net.minecraft.server.MinecraftServer)ReflectionApi.invoke(ReflectionApi.getOBCClass(null,"CraftServer"), Bukkit.getServer(), "getServer", new Class<?>[0]);
        WorldServer _world = (net.minecraft.server.level.WorldServer)ReflectionApi.invoke(ReflectionApi.getOBCClass(null,"CraftWorld"), world, "getHandle",new Class<?>[0]);
        EntityPlayer ep = new EntityPlayer(server, _world, profile);
        ep.listName = IChatBaseComponent.a(name);
        ep.f = 1;
        ep.d.a(EnumGamemode.b);
        return ep;
    }

    public static class NPC {

        private static final MainApi.SpigotPlayer api = SpigotApi.getMainApi().players();
        public final EntityPlayer entity;
        public final UUID uuid;
        public final int bukkitId;
        public final String name;
        private Location position;
        private Directions direction;

        public NPC(EntityPlayer entity, Location position, Directions direction, boolean spawn){
            this.entity = entity;
            this.uuid = entity.fM().getId();
            this.name = entity.fM().getName();
            this.bukkitId = entity.getBukkitEntity().getEntityId();
            this.position = position;
            this.direction = direction;
            if(spawn){
                spawn();
            }
        }

        public Location getLocation(){
            return position;
        }

        public void setLocation(Location newLocation){
            this.position = newLocation;
            respawn();
        }

        public Directions getDirection(){
            return direction;
        }

        public void setDirection(Directions newDirection){
            this.direction = newDirection;
            respawn();
        }

        public void spawn(Player player){
            api.sendPackets(player, createPackets());
            Bukkit.getScheduler().runTaskLater(SpigotApi.getInstance(), ()-> api.sendPacket(player, EntityPackets.removeEntity(this.entity)),10L);
        }

        public void spawn(){
            api.sendPackets(Bukkit.getOnlinePlayers(), createPackets());
            Bukkit.getScheduler().runTaskLater(SpigotApi.getInstance(), ()-> api.sendPacket(Bukkit.getOnlinePlayers(), EntityPackets.removeEntity(this.entity)),10L);
        }

        public void despawn(Player player){
            api.sendPackets(player, removePackets());
        }

        public void despawn(){
            api.sendPackets(Bukkit.getOnlinePlayers(), removePackets());
        }

        public void respawn(Player player){
            despawn(player);
            spawn(player);
        }

        public void respawn(){
            despawn();
            spawn();
        }

        protected Packet<?>[] removePackets(){
            return new Packet<?>[]{
                EntityPackets.destroyEntity(this.entity),
                EntityPackets.removeEntity(this.entity)
            };
        }

        protected Packet<?>[] createPackets(){
            int yaw = this.direction == Directions.OTHER ? (int)this.position.getYaw() : this.direction.getBodyYaw();
            int pitch = this.direction == Directions.OTHER ? (int)this.position.getPitch() : this.direction.getPitch();
            return new Packet<?>[]{
              EntityPackets.addEntity(this.entity),
              EntityPackets.spawnEntity(this.entity, this.position, yaw, pitch),
              EntityPackets.headRotation(this.entity, this.direction.getHeadYaw()),
              EntityPackets.updateSkin(this.entity, (byte)0x7F)
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NPC npc)) return false;
            return bukkitId == npc.bukkitId && uuid.equals(npc.uuid) && name.equals(npc.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, bukkitId, name);
        }
    }

}
