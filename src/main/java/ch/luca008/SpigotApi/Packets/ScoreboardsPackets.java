package ch.luca008.SpigotApi.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Api.ReflectionApi.ClassMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.Version;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardsPackets {

    private static final int METHOD_ADD = 0;
    private static final int METHOD_REMOVE = 1;
    private static final int METHOD_CHANGE = 2;

    private static final Class<?> DISPLAY_SLOT;
    private static final Class<?> SCORE_ACTION;
    private static final Class<?> RENDER_TYPE;
    //For 1.20.4 and later
    private static final Class<?> NUMBER_FORMAT;
    private static final Class<?> BLANK_FORMAT;

    private static final String SET_OBJECTIVE = "SetObjectivePacket";
    private static final String DISPLAY_OBJECTIVE = "SetDisplayObjectivePacket";
    private static final String SET_SCORE = "SetScorePacket";
    private static final String RESET_SCORE = "ResetScorePacket"; //For 1.20.4 and later

    private static final Map<String, ClassMapping> mappings = new HashMap<>();

    static {

        Version v = ReflectionApi.SERVER_VERSION;

        String protocolPackage = "network.protocol.game";
        Class<?> setObjPacketClass = ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutScoreboardObjective");
        Class<?> setScorePacketClass = ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutScoreboardScore");
        if(v == Version.MC_1_20 || v == Version.MC_1_20_2) {
            mappings.put(SET_OBJECTIVE, new ClassMapping(setObjPacketClass, new HashMap<>(){{ put("objectiveName", "d"); put("displayName", "e"); put("renderType", "f"); put("method", "g"); }}, new HashMap<>()));
            mappings.put(SET_SCORE, new ClassMapping(setScorePacketClass, new HashMap<>(){{ put("owner", "a"); put("objectiveName", "b"); put("score", "c"); put("method", "d"); }}, new HashMap<>()));
            SCORE_ACTION = ReflectionApi.getNMSClass("server", "ScoreboardServer$Action");
            NUMBER_FORMAT = null;
            BLANK_FORMAT = null;
        } else {
            mappings.put(SET_OBJECTIVE, new ClassMapping(setObjPacketClass, new HashMap<>(){{ put("objectiveName", "d"); put("displayName", "e"); put("renderType", "f"); put("numberFormat", "g"); put("method", "h"); }}, new HashMap<>()));
            mappings.put(SET_SCORE, new ClassMapping(setScorePacketClass, new HashMap<>(), new HashMap<>()));
            mappings.put(RESET_SCORE, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "ClientboundResetScorePacket"), new HashMap<>(), new HashMap<>()));
            SCORE_ACTION = null;
            NUMBER_FORMAT = ReflectionApi.getNMSClass("network.chat.numbers", "NumberFormat");
            BLANK_FORMAT = ReflectionApi.getNMSClass("network.chat.numbers", "BlankFormat");
        }

        mappings.put(DISPLAY_OBJECTIVE, new ClassMapping(ReflectionApi.getNMSClass(protocolPackage, "PacketPlayOutScoreboardDisplayObjective"), new HashMap<>(){{ put("slot", "a"); put("objectiveName", "b"); }}, new HashMap<>()));
        if(v == Version.MC_1_20_2 || v == Version.MC_1_20_3) {
            DISPLAY_SLOT = ReflectionApi.getNMSClass("world.scores", "DisplaySlot");
        } else {
            DISPLAY_SLOT = null;
        }

        RENDER_TYPE = ReflectionApi.getNMSClass("world.scores.criteria", "IScoreboardCriteria$EnumScoreboardHealthDisplay");

    }


    public enum Mode
    {
        ADD(METHOD_ADD),
        REMOVE(METHOD_REMOVE),
        CHANGE(METHOD_CHANGE);

        private final int method;
        Mode(int method)
        {
            this.method = method;
        }
    }

    public enum Action
    {
        CHANGE,
        REMOVE;

        private Object getAction() {
            return ReflectionApi.getEnumValue(SCORE_ACTION, this.name());
        }

    }

    public enum Slot
    {
        LIST,
        SIDEBAR,
        BELOW_NAME;

        private Object getDisplaySlot() {
            if(DISPLAY_SLOT == null)
                return this.ordinal();
            return ReflectionApi.getEnumValue(DISPLAY_SLOT, this.name());
        }

    }

    public enum RenderType
    {
        INTEGER,
        HEARTS;

        private Object getRenderType() {
            return ReflectionApi.getEnumValue(RENDER_TYPE, this.name());
        }
    }

    /**
     * Create, update or delete a client-side objective. From Minecraft 1.20.3/4 and later, red scores values will be hidden.
     * @param uniqueName a unique identifier for this objective. Will be used later on to display the objective and add scores to it.
     * @param displayName a title for this scoreboard. Maybe null if mode == Mode.REMOVE
     * @param mode a mode to tell the client what to do with this packet. See {@link Mode}
     * @return The requested packet
     */
    public static ApiPacket objective(Mode mode, String uniqueName, @Nullable String displayName, @Nullable RenderType type)
    {
        ReflectionApi.ObjectMapping packetBuilder = mappings.get(SET_OBJECTIVE)
                .unsafe_newInstance()
                .set("objectiveName", uniqueName)
                .set("displayName", PacketsUtils.getChatComponent(displayName))
                .set("renderType", type == null ? null : type.getRenderType())
                .set("method", mode.method);
        if(ReflectionApi.SERVER_VERSION == Version.MC_1_20_3)
        {
            packetBuilder.set("numberFormat", ReflectionApi.getStaticField(BLANK_FORMAT, "a"));
        }
        return ApiPacket.create(packetBuilder.packet());
    }

    /**
     * Display or hide an existing objective.
     * @param uniqueName The unique objective name (not displayname), null or empty to remove it from the given slot
     * @return The requested packet
     */
    public static ApiPacket displayObjective(Slot slot, @Nullable String uniqueName)
    {
        return ApiPacket.create(mappings.get(DISPLAY_OBJECTIVE).unsafe_newInstance().set("slot", slot.getDisplaySlot()).set("objectiveName", uniqueName == null ? "" : uniqueName).packet());
    }

    /**
     * Add or remove a record into an existing objective.
     * @param objectiveName16 The parent objective that will get this score line. Null when removing (Action.REMOVE)
     * @param lineId Only need for 1.20.4 and later, that's the intern name for the score line, and lineText40 the formatted display name on the scoreboard
     * @param lineText40 The text to display or remove (remove the scoreboard line with lineId on 1.20.4 and later)
     * @param mode ADD this line or REMOVE it? See {@link Action}
     * @param score Basically, the line order
     * @return The requested packet
     */
    public static ApiPacket score(String objectiveName16, @Nullable String lineId, String lineText40, Action mode, int score)
    {
        if(ReflectionApi.SERVER_VERSION == Version.MC_1_20_3)
        {
            if(mode == Action.REMOVE)
                return ApiPacket.create(mappings.get(RESET_SCORE).newInstance(new Class[]{String.class, String.class}, lineId, objectiveName16).packet());
            else {
                Object displayName = PacketsUtils.getChatComponent(lineText40);
                return ApiPacket.create(mappings.get(SET_SCORE).newInstance(new Class[]{String.class, String.class, int.class, displayName.getClass().getInterfaces()[0], NUMBER_FORMAT}, lineId, objectiveName16, score, displayName, null).packet());
            }
        } else {
            Object packet = mappings.get(SET_SCORE).unsafe_newInstance()
                    .set("objectiveName", objectiveName16)
                    .set("owner", lineText40)
                    .set("score", score)
                    .set("method", mode.getAction())
                    .packet();
            return ApiPacket.create(packet);
        }
    }

    /**
     * Special packets set which update a board line text.
     * @param objectiveName16 Parent objective name
     * @param lineId For 1.20.3 and newer, identifies the line to update
     * @param oldLineText For 1.20.2 and older, removes this line from the scoreboard
     * @param newLineText For 1.20.3 and newer, just update this line's displayed text. On 1.20.2 and older, add this formatted text at the specified score/line
     * @param score Just the line order
     * @return one update packet for 1.20.3 and newer, two packets (one remove and one change) for 1.20.2 and older
     */
    public static ApiPacket updateScore(String objectiveName16, @Nullable String lineId, @Nullable String oldLineText, String newLineText, int score)
    {
        if(ReflectionApi.SERVER_VERSION == Version.MC_1_20_3)
        {
            return score(objectiveName16, lineId, newLineText, Action.CHANGE, score);
        } else {
            ApiPacket packetRemove = score(objectiveName16, null, oldLineText, Action.REMOVE, 0);
            ApiPacket packetChange = score(objectiveName16, null, newLineText, Action.CHANGE, score);
            packetRemove.addAll(packetChange);
            return packetRemove;
        }
    }

}