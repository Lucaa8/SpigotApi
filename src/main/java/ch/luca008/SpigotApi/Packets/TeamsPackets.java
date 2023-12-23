package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.ClassMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.ObjectMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.Version;
import ch.luca008.SpigotApi.Utils.Logger;

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

    private static Object parametersObject(String displayName, String prefix, String suffix, NameTagVisibility nameTagVisibility, Collisions teamPush, PacketsUtils.ChatColor teamColor)
    {
        return mappings.get(TEAM_PACKET_PARAMETERS).unsafe_newInstance()
                .set("displayName", PacketsUtils.getChatComponent(displayName))
                .set("playerPrefix", PacketsUtils.getChatComponent(prefix))
                .set("playerSuffix", PacketsUtils.getChatComponent(suffix))
                .set("nametagVisibility", nameTagVisibility.mcName) //ok on client
                .set("collisionRule", teamPush.mcName) //not ok PUSH_OWN_TEAM pushes everyone bc the server thinks no one has any team so "default team"
                .set("color", teamColor.getEnumValue())
                .set("options", 0) //not ok but dont care
                .packet();
    }

    private static Object[] packet(String uniqueName, Mode mode, Optional<Object> parameters, String...entitiesArray)
    {
        ObjectMapping packet = mappings.get(TEAM_PACKET).unsafe_newInstance();
        if(mode==Mode.DELETE) {
            return new Object[]{packet.set("method", Mode.DELETE.mode).set("name", uniqueName).packet()};
        } else {
            Collection<String> entities = mode != Mode.UPDATE ? new ArrayList<>(Arrays.asList(entitiesArray)) : Collections.emptyList();
            return new Object[]{packet.set("method", mode.mode).set("name", uniqueName).set("parameters", parameters).set("players", entities).packet()};
        }
    }

    public static Object[] createOrUpdateTeam(String uniqueName, Mode mode, String displayName, NameTagVisibility nameTagVisibility, Collisions teamPush, PacketsUtils.ChatColor teamColor, String prefix, String suffix, String...entities)
    {
        if (mode != Mode.CREATE && mode != Mode.UPDATE) {
            Logger.error("Cannot use another mode than CREATE or UPDATE in the createOrUpdateTeam method.", TeamsPackets.class.getName());
            return null;
        }
        return packet(uniqueName, mode, Optional.of(parametersObject(displayName, prefix, suffix, nameTagVisibility, teamPush, teamColor)), entities);
    }

    public static Object[] deleteTeam(String uniqueName)
    {
        return packet(uniqueName, Mode.DELETE, Optional.empty());
    }

    public static Object[] updateEntities(String uniqueName, Mode mode, String...entities)
    {
        if(mode == Mode.ADD_ENTITY || mode == Mode.REMOVE_ENTITY)
            return packet(uniqueName, mode, Optional.empty(), entities);
        Logger.error("Cannot use another mode than ADD_ENTITY or REMOVE_ENTITY in the updateEntities method.", TeamsPackets.class.getName());
        return null;
    }

}
