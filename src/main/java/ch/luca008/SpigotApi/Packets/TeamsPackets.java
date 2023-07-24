package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeamBase.*;

import java.util.*;

public class TeamsPackets
{

    public enum Mode
    {
        CREATE(0),
        DELETE(1),
        UPDATE(2),
        ADD_ENTITY(3),
        REMOVE_ENTITY(4);

        private final int mode;

        Mode(int mode)
        {
            this.mode = mode;
        }

        public int getMode()
        {
            return this.mode;
        }
    }

    private static PacketPlayOutScoreboardTeam packet(String uniqueName,
                                                      Mode mode,
                                                      String displayName,
                                                      boolean friendlyFire,
                                                      boolean seeInvisibleFriendly,
                                                      EnumNameTagVisibility nameTagVisibility,
                                                      EnumTeamPush teamPush,
                                                      EnumChatFormat teamColor,
                                                      String prefix,
                                                      String suffix,
                                                      String...entitiesArray)
    {
        net.minecraft.world.scores.ScoreboardTeam team = new net.minecraft.world.scores.ScoreboardTeam(new Scoreboard(), uniqueName);
        if(mode==Mode.DELETE) {
            return PacketPlayOutScoreboardTeam.a(team);
        } else {
            Collection<String> entities = mode != Mode.UPDATE ? new ArrayList<>(Arrays.asList(entitiesArray)) : Collections.emptyList();
            Optional<Object> needsTeam = Optional.empty();
            if (mode == Mode.CREATE || mode == Mode.UPDATE) {
                PacketPlayOutScoreboardTeam.b packetTeamB = new PacketPlayOutScoreboardTeam.b(team);
                ReflectionApi.setField(packetTeamB, "a", IChatBaseComponent.b(displayName));
                ReflectionApi.setField(packetTeamB, "b", IChatBaseComponent.b(prefix));
                ReflectionApi.setField(packetTeamB, "c", IChatBaseComponent.b(suffix));
                ReflectionApi.setField(packetTeamB, "d", nameTagVisibility.e);
                ReflectionApi.setField(packetTeamB, "e", teamPush.e);
                ReflectionApi.setField(packetTeamB, "f", teamColor);
                int g = 0;
                if (friendlyFire)
                    g |= 0x1;
                if (seeInvisibleFriendly)
                    g |= 0x2;
                ReflectionApi.setField(packetTeamB, "g", g);
                needsTeam = Optional.of(packetTeamB);
            }
            return (PacketPlayOutScoreboardTeam) ReflectionApi.newInstance(PacketPlayOutScoreboardTeam.class, new Class<?>[]{String.class, int.class, Optional.class, Collection.class}, uniqueName, mode.mode, needsTeam, entities);
        }
    }

    public static PacketPlayOutScoreboardTeam createOrUpdateTeam(String uniqueName,
                                                                 Mode mode,
                                                                 String displayName,
                                                                 boolean friendlyFire,
                                                                 boolean seeInvisibleFriendly,
                                                                 EnumNameTagVisibility nameTagVisibility,
                                                                 EnumTeamPush teamPush,
                                                                 EnumChatFormat teamColor,
                                                                 String prefix,
                                                                 String suffix,
                                                                 String...entities)
    {
        return packet(uniqueName, (mode == Mode.CREATE || mode == Mode.UPDATE) ? mode : Mode.CREATE, displayName, friendlyFire, seeInvisibleFriendly, nameTagVisibility, teamPush, teamColor, prefix, suffix, entities);
    }

    public static PacketPlayOutScoreboardTeam deleteTeam(String uniqueName)
    {
        return packet(uniqueName, Mode.DELETE, "", false, false, null, null, null, "", "");
    }

    public static PacketPlayOutScoreboardTeam updateEntities(String uniqueName, Mode mode, String...entities)
    {
        if(mode == Mode.ADD_ENTITY || mode == Mode.REMOVE_ENTITY)
            return packet(uniqueName, mode, "", false, false, null, null, null, "", "", entities);
        return null;
    }

}
