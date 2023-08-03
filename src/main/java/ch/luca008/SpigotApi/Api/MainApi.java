package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.TeamsPackets;
import ch.luca008.SpigotApi.Packets.TeamsPackets.Mode;
import ch.luca008.SpigotApi.Utils.WebRequest;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumTeamPush;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public class MainApi {
    private final SpigotPlayer players;
    private final SpigotPackets packets;

    public MainApi(){
        this.players = new SpigotPlayer();
        this.packets = new SpigotPackets();
    }

    public SpigotPlayer players(){
        return players;
    }

    public SpigotPackets packets(){
        return packets;
    }

    public static class SpigotPackets{

        private final ScoreboardPackets sb;
        private final TeamPackets teams;

        public SpigotPackets(){
            this.sb = new ScoreboardPackets();
            this.teams = new TeamPackets();
        }

        /**
         * Return null if no bukkit worlds are loaded
         * @param uuid a known uuid or null for random
         * @param nameLen16 a pseudo for this entityplayer (max length = 16). If actual name is longer than 16, the method will cut the end.
         * @param listName the text the players will see (can be > 16). If null or empty, the {@param nameLen16} will be displayed
         * @param skinPseudoOrUuid the playerhead's pseudo or uuid just at the left of the pseudo (only on premium servers). If null the alex or steve will be displayed
         * @param ping the ping at the right of the pseudo. if not set: 5bars. ping bars icon: 0<ms5<150ms<4<300ms<3<600ms<2<1000ms<1<1000ms+. negative ping will display the no connection icon
         * @return An EntityPlayer class. Which contains uuid, name and playerConnection, etc...
         */
        @Nullable
        public EntityPlayer getFakeEntityPlayer(@Nullable UUID uuid, String nameLen16, @Nullable String listName, @Nullable String skinPseudoOrUuid, int...ping){
            World world = Bukkit.getWorlds().get(0);
            if(world!=null){
                GameProfile profile = new GameProfile(uuid==null?UUID.randomUUID():uuid, nameLen16.length() > 16 ? nameLen16.substring(0, 16) : nameLen16);
                Object MinecraftServer = ReflectionApi.invoke(ReflectionApi.getOBCClass(null,"CraftServer"), Bukkit.getServer(), "getServer", new Class<?>[0]);
                Object WorldServer = ReflectionApi.invoke(ReflectionApi.getOBCClass(null,"CraftWorld"), world, "getHandle",new Class<?>[0]);
                EntityPlayer EntityPlayer = new EntityPlayer((net.minecraft.server.MinecraftServer) MinecraftServer, (net.minecraft.server.level.WorldServer) WorldServer, profile);
                if(listName!=null&&listName.length()>0){
                    EntityPlayer.listName = IChatBaseComponent.a(listName);
                }
                if(skinPseudoOrUuid!=null&&skinPseudoOrUuid.length()>0){
                    Property p = WebRequest.getSkin(skinPseudoOrUuid);
                    if(p!=null){
                        profile.getProperties().put("textures", p);
                    }
                }
                if(ping!=null&&ping.length>0){
                    EntityPlayer.f = ping[0];
                }
                return EntityPlayer;
            }
            return null;
        }

        public TeamPackets teams(){
            return teams;
        }

        public ScoreboardPackets scoreboard(){
            return sb;
        }

        public class TeamPackets {
            /**
             *
             * @param uniqueName16Len Maximum length of 16. This name can sort teams in the tab. "00admin","01moderator", etc... Need to be unique
             * @param createOrUpdate The packet is the same to create or update a team, but the client need to knows which action you send
             * @param displayName A displayname (not visible) but can exceed 16 length. Give more detail if you can't put all in the unique name
             * @param allowFriendlyFire true will allow players in the same team to fight each oder.
             * @param seeInvisibleFriendly true will allow players in the same team to see them each oder even if someone is invisible
             * @param nameTagVisibility If the prefix/suffix and color is visible by other teams, own team, etc..
             * @param collisions 1.9+ collisions between players in the same team, enemies teams, etc...
             * @param color pseudo color (1.16+ need to specify the reel color. cannot put "prefix §6" to have a gold pseudo)
             * @param prefix prefix of the team
             * @param suffix suffix of the team
             * @param entitiesIntoTeam only needed when a new team is created. The list of pseudo(players) or uuid(entities) will be added to the team. Can be empty for none
             */
            public Packet<?> getCreateOrUpdateTeamPacket(String uniqueName16Len, Mode createOrUpdate, String displayName, boolean allowFriendlyFire, boolean seeInvisibleFriendly,
                                                      EnumNameTagVisibility nameTagVisibility, EnumTeamPush collisions, EnumChatFormat color,
                                                      String prefix, String suffix, String...entitiesIntoTeam){
                if(createOrUpdate==Mode.CREATE||createOrUpdate==Mode.UPDATE){
                    return TeamsPackets.createOrUpdateTeam(uniqueName16Len, createOrUpdate, displayName, allowFriendlyFire, seeInvisibleFriendly, nameTagVisibility, collisions, color, prefix, suffix, entitiesIntoTeam);
                }
                return null;
            }

            public Packet<?> getDeleteTeamPacket(String uniqueName16Len){
                return TeamsPackets.deleteTeam(uniqueName16Len);
            }

            public Packet<?> getAddEntityTeamPacket(String uniqueName16Len, String...entitiesToAdd){
                return TeamsPackets.updateEntities(uniqueName16Len, Mode.ADD_ENTITY, entitiesToAdd);
            }

            public Packet<?> getRemoveEntityTeamPacket(String uniqueName16Len, String...entitiesToRemove){
                return TeamsPackets.updateEntities(uniqueName16Len, Mode.REMOVE_ENTITY, entitiesToRemove);
            }
        }

        public class ScoreboardPackets {

            /**
             * Display the specified objective at the specified location. To hide the scoreboard see {@link #getScoreboardDisplayHidePacket()}
             * @param uniqueObjectiveName16Len The objective to display (Can be created with {@link #getScoreboardObjectivePacket(String, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Mode, String)})
             */
            public Packet<?> getScoreboardDisplayPacket(String uniqueObjectiveName16Len){
                return ch.luca008.SpigotApi.Packets.ScoreboardPackets.displayObjective(uniqueObjectiveName16Len);
            }

            /**
             * Will hide any scoreboard currently displayed at the specified position. To show a scoreboard see {@link #getScoreboardDisplayPacket(String)}
             */
            public Packet<?> getScoreboardDisplayHidePacket(){
                return getScoreboardDisplayPacket("");
            }

            /**
             * Will create a brand-new objective with the specified unique name. To show this objective see {@link #getScoreboardDisplayPacket(String)}
             * @param uniqueObjectiveName16Len The objective to create, this name must be unique and max 16 char length (will cut if higher).
             * @param mode The mode tells the client what he needs to do with this packet. 0 = create (fails if unique name isn't unique), 1 = delete (see {@link #getScoreboardRemoveObjectivePacket(String)}), 2 = update this objective displaytitle (must exist)
             * @param displayTitle The scoreboard's first line at the top.
             */
            public Packet<?> getScoreboardObjectivePacket(String uniqueObjectiveName16Len, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Mode mode, String displayTitle){
                return ch.luca008.SpigotApi.Packets.ScoreboardPackets.objective(uniqueObjectiveName16Len, displayTitle, mode);
            }

            /**
             * Same packet as {@link #getScoreboardObjectivePacket(String, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Mode, String)} but with mode=1 to remove the specified objective
             * @param uniqueObjectiveName16Len The objective to remove
             */
            public Packet<?> getScoreboardRemoveObjectivePacket(String uniqueObjectiveName16Len){
                return getScoreboardObjectivePacket(uniqueObjectiveName16Len, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Mode.REMOVE, "");
            }

            /**
             * Will add a new entry to the objective specified (create one with {@link #getScoreboardObjectivePacket(String, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Mode, String)})
             * @param uniqueParentObjectiveName16Len the objective in which you want to add this score/line
             * @param scoreName the score name or line text
             * @param score the score value. useful to sort your lines. the highest score is the first line
             */
            public Packet<?> getScoreboardChangeScorePacket(String uniqueParentObjectiveName16Len, String scoreName, int score){
                return ch.luca008.SpigotApi.Packets.ScoreboardPackets.score(uniqueParentObjectiveName16Len, scoreName, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Action.CHANGE, score);
            }

            /**
             * Same packet as {@link #getScoreboardChangeScorePacket(String, String, int)} but will remove the specified score
             * @param uniqueParentObjectiveName16Len the objective in which you want to remove this score/line (must exist)
             * @param scoreName the score/line to remove
             */
            public Packet<?> getScoreboardRemoveScorePacket(String uniqueParentObjectiveName16Len, String scoreName){
                return ch.luca008.SpigotApi.Packets.ScoreboardPackets.score(uniqueParentObjectiveName16Len, scoreName, ch.luca008.SpigotApi.Packets.ScoreboardPackets.Action.REMOVE, 0);
            }
        }
    }

    public static class SpigotPlayer{

        public Object getConnection(Player player) {
            Class<?> craftplayer = ReflectionApi.getOBCClass("entity", "CraftPlayer");
            EntityPlayer ep = (EntityPlayer) ReflectionApi.invoke(craftplayer, player, "getHandle", new Class[0], new Object[0]);
            return ep.c;
        }

        public void sendPacket(Collection<? extends Player> collection, Packet<?> packet) {
            collection.forEach(player -> sendPacket(player, packet));
        }

        public void sendPackets(Collection<? extends Player> collection, Packet<?>[] packets) {
            collection.forEach(player -> sendPackets(player, packets));
        }

        public void sendPacket(Player player, Packet<?> packet) {
            NetworkManager nm = (NetworkManager) ReflectionApi.getField(getConnection(player), "h");
            nm.a(packet);
        }

        public void sendPackets(Player player, Packet<?>[] packets) {
            for (Packet<?> packet : packets) {
                sendPacket(player, packet);
            }
        }

    }

}
