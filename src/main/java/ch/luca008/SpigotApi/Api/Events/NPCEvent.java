package ch.luca008.SpigotApi.Api.Events;

import ch.luca008.SpigotApi.Api.NPCApi;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.List;

public class NPCEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum NpcEventType {
        SPAWN, DESPAWN
    }

    private final NPCApi.NPC npc;
    private final Location location;
    private final List<Player> affectedPlayers;
    private final NpcEventType type;
    private boolean cancelled = false;

    public NPCEvent(NPCApi.NPC npc, Location location, List<Player> affectedPlayers, NpcEventType type){
        this.npc = npc;
        this.location = location;
        this.affectedPlayers = affectedPlayers;
        this.type = type;
    }

    public NPCApi.NPC getNpc() {
        return npc;
    }

    public Location getLocation() {
        return location;
    }

    public List<Player> getAffectedPlayers() {
        return affectedPlayers;
    }

    public NpcEventType getType() {
        return type;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

}
