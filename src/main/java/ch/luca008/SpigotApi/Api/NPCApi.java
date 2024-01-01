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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.annotation.Nullable;
import java.util.*;

public class NPCApi {

    public static class NPCApiListeners implements Listener {

        private Map<Integer, NPC> getNpcList()
        {
            return SpigotApi.getNpcApi().npcs;
        }

        @EventHandler
        public void onPlayerJoinAddNPCAndSetCallback(PlayerJoinEvent e)
        {
            final Player player = e.getPlayer();
            final Map<Integer, NPC> npcs = getNpcList();
            SpigotApi.getMainApi().players().startHandling(player, "SpigotApi_NPC", (packet, cancellable) -> {
                try {
                    Object[] interaction = EntityPackets.getInteractionFromPacket(packet);
                    if(interaction != null)
                    {
                        int entity = (int)interaction[0];
                        if(npcs.containsKey(entity))
                        {
                            if(npcs.get(entity).syncCallInteraction(player, ((String)interaction[1]).equals("ATTACK") ? ClickType.LEFT : ClickType.RIGHT))
                                cancellable.setCancelled(true);
                        }
                    }
                } catch(Exception ex) {
                    Logger.error("An error occurred during the handling of a packet. Interaction ignored.", getClass().getName());
                    ex.printStackTrace();
                }
            });

            npcs.values().stream().filter(npc->npc.isActive&&npc.position.getWorld()==e.getPlayer().getWorld()).forEach(npc->npc.spawn(List.of(e.getPlayer())));
        }

        @EventHandler
        public void onWorldChange(PlayerChangedWorldEvent e)
        {
            getNpcList().values().stream().filter(npc->npc.isActive&&npc.position.getWorld()==e.getPlayer().getWorld()).forEach(npc->npc.spawn(List.of(e.getPlayer())));
        }

        @EventHandler
        public void onMove(PlayerMoveEvent e)
        {

            if(e.getTo() == null || e.getTo().distanceSquared(e.getFrom()) < 0.001) //avoid updating when its only a direction change (head pitch/yaw)
                return;
            Location to = e.getTo().clone();

            for(NPC npc : getNpcList().values())
            {
                if(npc.isActive&&npc.position.getWorld()==to.getWorld()&&npc.position.distanceSquared(to) <= npc.squaredTrackDistance)
                {
                    npc.lookAt(e.getPlayer(), to);
                }
            }

        }

    }

    @Nullable
    public static ApiProperty getProperty(String playerName) {
        return WebRequest.getSkin(playerName, true);
    }

    public static ApiProperty getProperty(String value, String signature)
    {
        return new ApiProperty("textures", value, signature);
    }

    public interface OnNPCInteract {
        void interact(NPC npc, Player player, ClickType clickType);
    }

    private final Map<Integer,NPC> npcs = new HashMap<>();
    private final NPCApiListeners listeners = new NPCApiListeners();

    public NPCApi()
    {
        Bukkit.getPluginManager().registerEvents(listeners, SpigotApi.getInstance());
    }

    public boolean registerNPC(NPC npc)
    {
        if(npcs.containsKey(npc.id))
        {
            Logger.warn("Cannot register a NPC with a duplicate ID (" + npc.id + "). Consider removing the old one.", NPCApi.class.getName());
            return false;
        }
        npcs.put(npc.id, npc);
        return true;
    }

    public void unregisterNpc(int npcId)
    {
        if(npcs.containsKey(npcId))
        {
            npcs.get(npcId).despawn();
            npcs.remove(npcId);
        }
    }

    public NPC getNpcById(int npcId)
    {
        return npcs.get(npcId);
    }

    public void unregisterAll(boolean unregisterHandler)
    {
        new HashMap<>(npcs).keySet().forEach(this::unregisterNpc);
        if(unregisterHandler)
        {
            HandlerList.unregisterAll(listeners);
        }
    }

    public static class NPC {

        private final int id;
        private final UUID uuid;
        private final String name;
        private final ApiProperty textures;
        private Location position;
        private final double squaredTrackDistance;
        private OnNPCInteract interactCallback;
        private boolean isActive = false;

        //verifier le monde, ajouter le interact callback, spawn le npc on join, etc...
        public NPC(@Nullable UUID uuid, String name, @Nullable ApiProperty textures, Location position, double trackDistance, boolean isActive, OnNPCInteract interactCallback){
            this.id = EntityPackets.nextId();
            this.uuid = uuid == null ? UUID.randomUUID() : uuid;
            this.name = name;
            this.textures = textures;
            this.position = position.clone();
            this.squaredTrackDistance = trackDistance*trackDistance;
            this.interactCallback = interactCallback;
            if(isActive) setActive(true); //spawn the npc to all players in the world
        }

        private Collection<? extends Player> affectedPlayers()
        {
            return this.position.getWorld() == null ? List.of() : this.position.getWorld().getPlayers();
        }

        public int getId(){
            return this.id;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }

        public Location getLocation(){
            return position.clone();
        }

        public void setLocation(Location newLocation){
            this.position = newLocation.clone();
            if(isActive)
                respawn(affectedPlayers());
        }

        public void setInteractCallback(OnNPCInteract newCallback)
        {
            this.interactCallback = newCallback;
        }

        protected boolean syncCallInteraction(Player player, ClickType clickType)
        {
            if(this.interactCallback == null || player == null || !isActive)
                return false;
            Bukkit.getScheduler().runTask(SpigotApi.getInstance(), ()->this.interactCallback.interact(this, player, clickType));
            return true;
        }

        public double getTrackDistance(){
            return Math.sqrt(this.squaredTrackDistance);
        }

        public void setActive(boolean isActive)
        {
            if(this.isActive == isActive)
                return;
            this.isActive = isActive;
            if(isActive)
                spawn(affectedPlayers());
            else
                despawn(affectedPlayers());
        }
        public boolean isActive() {
            return this.isActive;
        }

        protected void spawn(Collection<? extends Player> players){
            createPackets().send(players);
            Bukkit.getScheduler().runTaskLater(SpigotApi.getInstance(), ()->EntityPackets.removeEntity(this.uuid).send(players),10L);
        }

        protected void despawn(Collection<? extends Player> players){
            removePackets().send(players);
        }

        protected void despawn()
        {
            despawn(affectedPlayers());
        }

        protected void respawn(Collection<? extends Player> players){
            despawn(players);
            spawn(players);
        }

        protected void lookAt(Player player, Location locationToLookAt)
        {
            Location npc = this.position.clone();
            npc.setDirection(locationToLookAt.clone().subtract(npc).toVector());
            EntityPackets.updateLook(id, npc.getYaw(), npc.getPitch(), npc.getYaw()).send(player);
        }

        private ApiPacket removePackets(){
            ApiPacket destroy = EntityPackets.destroyEntity(this.id);
            ApiPacket remove = EntityPackets.removeEntity(this.uuid);
            destroy.addAll(remove);
            return destroy;
        }

        private ApiPacket createPackets(){
            ApiPacket addEntity = EntityPackets.addEntity(this.name, this.uuid, this.textures, false, -1, null);
            ApiPacket spawnEntity = EntityPackets.spawnEntity(this.id, this.uuid, this.position, 0.0f, 0.0f, 0.0f);
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

        @Override
        public String toString()
        {
            return "NPC{id="+this.id+
                    ", uuid="+this.uuid+
                    ", name="+this.name+
                    ", world="+this.position.getWorld()+
                    ", x="+this.position.getBlockX()+
                    ", y="+this.position.getBlockY()+
                    ", z="+this.position.getBlockZ()+
                    ", active="+this.isActive+"}";
        }

    }

}
