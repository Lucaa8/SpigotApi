package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.*;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeamBase.*;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamsPackets
{

    private static final String TEAM_PACKET = "SetPlayerTeamPacket";
    private static final String TEAM_PACKET_PARAMETERS = TEAM_PACKET + "Parameters";

    private static final Map<String, ClassMapping> mappings = new HashMap<>();

    static {

        //not used yet bc all supported versions have the same fields, but maybe we'll need it in the future
        Version v = ReflectionApi.SERVER_VERSION;

        mappings.put(TEAM_PACKET, new ClassMapping(ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam"), new HashMap<>(){{ put("METHOD_ADD", "a"); put("METHOD_REMOVE", "b"); put("METHOD_CHANGE", "c"); put("METHOD_JOIN", "d"); put("METHOD_LEAVE", "e"); put("method", "h"); put("name", "i"); put("players", "j"); put("parameters", "k"); }}, new HashMap<>()));
        mappings.put(TEAM_PACKET_PARAMETERS, new ClassMapping(ReflectionApi.getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam$b"), new HashMap<>(){{ put("displayName", "a"); put("playerPrefix", "b"); put("playerSuffix", "c"); put("nametagVisibility", "d"); put("collisionRule", "e"); put("color", "f"); put("options", "g"); }}, new HashMap<>()));

    }


    public enum Mode
    {
        CREATE("METHOD_ADD"),
        DELETE("METHOD_REMOVE"),
        UPDATE("METHOD_CHANGE"),
        ADD_ENTITY("METHOD_JOIN"),
        REMOVE_ENTITY("METHOD_LEAVE");

        private final int mode;

        Mode(String mode)
        {
            this.mode = (int) mappings.get(TEAM_PACKET).getFieldValue(mode, null);
        }

        public int getMode()
        {
            return this.mode;
        }
    }

    public enum NameTagVisibility {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String mcName;
        NameTagVisibility(String mcName)
        {
            this.mcName = mcName;
        }
    }

    public enum Collisions {
        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");

        private final String mcName;
        Collisions(String mcName)
        {
            this.mcName = mcName;
        }
    }

    public static void attemptTeam(Player p)
    {

        Mode create = Mode.CREATE;
        ObjectMapping packet_create_params = mappings.get(TEAM_PACKET_PARAMETERS).unsafe_newInstance();
        packet_create_params
                .set("displayName", PacketsUtils.getChatComponent("§aTestTeam"))
                .set("playerPrefix", PacketsUtils.getChatComponent("§cAdmin | "))
                .set("playerSuffix", PacketsUtils.getChatComponent(""))
                .set("nametagVisibility", NameTagVisibility.ALWAYS.mcName)
                .set("collisionRule", Collisions.NEVER)
                .set("color", "")
                .set("options", 0);

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
