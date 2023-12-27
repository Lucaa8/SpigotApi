package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.ApiPacket;
import ch.luca008.SpigotApi.Packets.EntityPackets;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.ApiProperty;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.luca008.SpigotApi.Utils.WebRequest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NPCApi implements Listener {

    public interface OnNPCInteract {
        void interact(int npcId, Player player, ClickType clickType);
    }

    private final Map<Integer,OnNPCInteract> callbacks = new HashMap<>();

    @EventHandler
    public void onPlayerJoinSetNPCCallback(PlayerJoinEvent e)
    {
        final Player player = e.getPlayer();
        SpigotApi.getMainApi().players().startHandling(player, "SpigotApi_NPC", (packet, cancellable) -> {
            try {
                Object[] interaction = EntityPackets.getInteractionFromPacket(packet);
                if(interaction != null)
                {
                    int entity = (int)interaction[0];
                    if(callbacks.containsKey(entity))
                    {
                        Bukkit.getScheduler().runTask(SpigotApi.getInstance(), ()->callbacks.get(entity).interact(entity, player, ((String)interaction[1]).equals("ATTACK") ? ClickType.LEFT : ClickType.RIGHT));
                        cancellable.setCancelled(true);
                    }
                }
            } catch(Exception ex) {
                Logger.error("An error occurred during the handling of a packet. Interaction ignored.", getClass().getName());
                ex.printStackTrace();
            }
        });
    }

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

    @Nullable
    public static ApiProperty getProperty(String playerName) {
        return WebRequest.getSkin(playerName, true);
    }

    public static ApiProperty getProperty(String value, String signature)
    {
        return new ApiProperty("textures", value, signature);
    }

    public void addInteractHandler(NPC npc, OnNPCInteract handler)
    {
        if(callbacks.containsKey(npc.id))
        {
            Logger.error("NPC " + npc.name + " (id=" + npc.id + ", uuid=" + npc.uuid + ") already got an Interact Handler. Please remove the old one before adding this one", getClass().getName());
            return;
        }
        callbacks.put(npc.id, handler);
    }

    public void removeInteractHandler(NPC npc)
    {
        removeInteractHandler(npc.id);
    }

    public void removeInteractHandler(int id)
    {
        callbacks.remove(id);
    }

    public static class NPC {

        private final int id;
        private final UUID uuid;
        private final String name;
        private final ApiProperty textures;
        private Location position;
        private Directions direction;

        public NPC(@Nullable UUID uuid, String name, @Nullable ApiProperty textures, Location position, Directions direction, boolean spawn){
            this.id = EntityPackets.nextId();
            this.uuid = uuid == null ? UUID.randomUUID() : uuid;
            this.name = name;
            this.textures = textures;
            this.position = position;
            this.direction = direction;
            if(spawn){
                spawn();
            }
        }

        public int getId()
        {
            return this.id;
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
            createPackets().send(player);
            Bukkit.getScheduler().runTaskLater(SpigotApi.getInstance(), ()->EntityPackets.removeEntity(this.uuid).send(player),10L);
        }

        public void spawn(){
            createPackets().sendToOnline();
            Bukkit.getScheduler().runTaskLater(SpigotApi.getInstance(), ()->EntityPackets.removeEntity(this.uuid).sendToOnline(),10L);
        }

        public void despawn(Player player){
            removePackets().send(player);
        }

        public void despawn(){
            removePackets().sendToOnline();
        }

        public void respawn(Player player){
            despawn(player);
            spawn(player);
        }

        public void respawn(){
            despawn();
            spawn();
        }

        protected ApiPacket removePackets(){
            ApiPacket destroy = EntityPackets.destroyEntity(this.id);
            ApiPacket remove = EntityPackets.removeEntity(this.uuid);
            destroy.addAll(remove);
            return destroy;
        }

        protected ApiPacket createPackets(){
            int yaw = this.direction == Directions.OTHER ? (int)this.position.getYaw() : this.direction.getBodyYaw();
            int pitch = this.direction == Directions.OTHER ? (int)this.position.getPitch() : this.direction.getPitch();

            ApiPacket addEntity = EntityPackets.addEntity(this.name, this.uuid, this.textures, false, -1, null);
            ApiPacket spawnEntity = EntityPackets.spawnEntity(this.id, this.uuid, this.position, yaw, pitch, this.direction.getHeadYaw());
            ApiPacket updateSkin = EntityPackets.updateSkin(this.id);

            addEntity.addAll(spawnEntity);
            addEntity.addAll(updateSkin);

            return addEntity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NPC npc)) return false;
            return id == npc.id && uuid.equals(npc.uuid) && name.equals(npc.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, id, name);
        }
    }

}
