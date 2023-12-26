package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.ApiPacket;
import ch.luca008.SpigotApi.Packets.PacketsUtils;
import ch.luca008.SpigotApi.Packets.ScoreboardsPackets;
import ch.luca008.SpigotApi.Packets.ScoreboardsPackets.RenderType;
import ch.luca008.SpigotApi.Packets.TeamsPackets;
import ch.luca008.SpigotApi.Packets.TeamsPackets.Collisions;
import ch.luca008.SpigotApi.Packets.TeamsPackets.NameTagVisibility;
import ch.luca008.SpigotApi.SpigotApi;
import com.google.protobuf.Api;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;

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
             * @param nameTagVisibility If the prefix/suffix and color is visible by other teams, own team, etc..
             * @param collisions 1.9+ collisions between players in the same team, enemies teams, etc...
             * @param color pseudo color (1.16+ need to specify the reel color. cannot put "prefix §6" to have a gold pseudo)
             * @param prefix prefix of the team
             * @param suffix suffix of the team
             * @param entitiesIntoTeam only needed when a new team is created. The list of pseudo(players) or uuid(entities) will be added to the team. Can be empty for none
             * @return In most cases only 1 object with the corresponding packet, but it's a security if in some later version we need 2 or more packets to do this action.
             */
            public ApiPacket getCreateOrUpdateTeamPacket(String uniqueName16Len, TeamsPackets.Mode createOrUpdate, String displayName,
                                                        NameTagVisibility nameTagVisibility, Collisions collisions, PacketsUtils.ChatColor color,
                                                        String prefix, String suffix, String...entitiesIntoTeam){
                return TeamsPackets.createOrUpdateTeam(uniqueName16Len, createOrUpdate, displayName, nameTagVisibility, collisions, color, prefix, suffix, entitiesIntoTeam);
            }

            public ApiPacket getDeleteTeamPacket(String uniqueName16Len){
                return TeamsPackets.deleteTeam(uniqueName16Len);
            }

            public ApiPacket getAddEntityTeamPacket(String uniqueName16Len, String...entitiesToAdd){
                return TeamsPackets.updateEntities(uniqueName16Len, TeamsPackets.Mode.ADD_ENTITY, entitiesToAdd);
            }

            public ApiPacket getRemoveEntityTeamPacket(String uniqueName16Len, String...entitiesToRemove){
                return TeamsPackets.updateEntities(uniqueName16Len, TeamsPackets.Mode.REMOVE_ENTITY, entitiesToRemove);
            }
        }

        public class ScoreboardPackets {

            /**
             * Display the specified objective at the specified location. To hide the scoreboard see {@link #getScoreboardDisplayHidePacket(ScoreboardsPackets.Slot)}
             * @param uniqueObjectiveName16Len The objective to display (Can be created with {@link #getScoreboardObjectivePacket(String, ScoreboardsPackets.Mode, String, RenderType)})
             */
            public ApiPacket getScoreboardDisplayPacket(String uniqueObjectiveName16Len, ScoreboardsPackets.Slot slot){
                return ScoreboardsPackets.displayObjective(slot, uniqueObjectiveName16Len);
            }

            /**
             * Will hide any scoreboard currently displayed at the specified position. To show a scoreboard see {@link #getScoreboardDisplayPacket(String, ScoreboardsPackets.Slot)}
             */
            public ApiPacket getScoreboardDisplayHidePacket(ScoreboardsPackets.Slot slot){
                return getScoreboardDisplayPacket(null, slot);
            }

            /**
             * Will create a brand-new objective with the specified unique name. To show this objective see {@link #getScoreboardDisplayPacket(String, ScoreboardsPackets.Slot)}
             * @param uniqueObjectiveName16Len The objective to create, this name must be unique and max 16 char length (will cut if higher).
             * @param mode The mode tells the client what he needs to do with this packet. 0 = create (fails if unique name isn't unique), 1 = delete (see {@link #getScoreboardRemoveObjectivePacket(String)}), 2 = update this objective displaytitle (must exist)
             * @param displayTitle The scoreboard's first line at the top.
             * @param type If the scoreboard is integer scores (sidebar) or hearts (belowName and list)
             */
            public ApiPacket getScoreboardObjectivePacket(String uniqueObjectiveName16Len, ScoreboardsPackets.Mode mode, String displayTitle, RenderType type){
                return ScoreboardsPackets.objective(mode, uniqueObjectiveName16Len, displayTitle, type);
            }

            /**
             * Same packet as {@link #getScoreboardObjectivePacket(String, ScoreboardsPackets.Mode, String, RenderType)} but with mode=REMOVE(1) to remove the specified objective
             * @param uniqueObjectiveName16Len The objective to remove
             */
            public ApiPacket getScoreboardRemoveObjectivePacket(String uniqueObjectiveName16Len){
                return getScoreboardObjectivePacket(uniqueObjectiveName16Len, ScoreboardsPackets.Mode.REMOVE, "", null);
            }

            /**
             * Will add a new entry to the objective specified (create one with {@link #getScoreboardObjectivePacket(String, ScoreboardsPackets.Mode, String, RenderType)})
             * @param uniqueParentObjectiveName16Len the objective in which you want to add this score/line
             * @param scoreName An unique name for the score in this parent objective (this text won't be displayed on the board client-side, but will be used to edit or remove this line later on). On 1.20.2 and older, this parameter can be ignored as edit and removal is done with displayName directly
             * @param scoreDisplayName The formatted text which will be displayed on the client's board. On 1.20.2 and older, this parameter is the unique identifier AND the displayed text. In 1.20.3 and newer this parameter is the displayed text only, and you won't be able to remove the line with this text later on, you'll need scoreName.
             * @param score the score value. useful to sort your lines. the highest score is the first line
             */
            public ApiPacket getScoreboardChangeScorePacket(String uniqueParentObjectiveName16Len, @Nullable String scoreName, String scoreDisplayName, int score){
                return ScoreboardsPackets.score(uniqueParentObjectiveName16Len, scoreName, scoreDisplayName, ScoreboardsPackets.Action.CHANGE, score);
            }

            /**
             * Same packet as {@link #getScoreboardChangeScorePacket(String, String, String, int)} but will remove the specified score
             * @param uniqueParentObjectiveName16Len the objective in which you want to remove this score/line (must exist)
             * @param scoreName the score/line to remove.<p>In 1.20.2 and older, this parameter is scoreDisplayName of {@link #getScoreboardChangeScorePacket(String, String, String, int)}.<p>On 1.20.3 and newer, this parameter is scoreName of {@link #getScoreboardChangeScorePacket(String, String, String, int)}
             */
            public ApiPacket getScoreboardRemoveScorePacket(String uniqueParentObjectiveName16Len, String scoreName){
                return ScoreboardsPackets.score(uniqueParentObjectiveName16Len, scoreName, scoreName, ScoreboardsPackets.Action.REMOVE, 0);
            }

        }
    }

    public static class SpigotPlayer{

        public PlayerSniffer handlePacket(Player player, PacketReceived callback){
            return new PlayerSniffer(player, callback);
        }

    }

    public interface PacketReceived {

        void receive(Object packet, Cancellable cancellable);

    }

    public static class PlayerSniffer implements Listener {

        private final Player player;
        private final PacketReceived callback;

        private PlayerSniffer(Player player, PacketReceived callback){
            this.player = player;
            this.callback = callback;
            register();
        }

        private void register(){

            Bukkit.getServer().getPluginManager().registerEvents(this, SpigotApi.getInstance());

            PacketsUtils.getPlayerChannel(player).pipeline().addBefore("packet_handler", this.player.getName(), new ChannelDuplexHandler(){
                public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                    Cancellable c = new Cancellable() {
                        private boolean cancelled = false;
                        @Override
                        public boolean isCancelled() {
                            return cancelled;
                        }

                        @Override
                        public void setCancelled(boolean cancel) {
                            this.cancelled = cancel;
                        }
                    };

                    if(packet instanceof Packet<?> p)
                    {
                        PlayerSniffer.this.callback.receive(p, c);
                    }

                    if(!c.isCancelled()){
                        super.channelRead(channelHandlerContext, packet);
                    }
                }
            });

        }

        public void unregister(){

            HandlerList.unregisterAll(this);

            final Channel channel = PacketsUtils.getPlayerChannel(player);
            EventLoop loop = channel.eventLoop();
            loop.submit(() -> {
                channel.pipeline().remove(this.player.getName());
                return null;
            });

        }

        @EventHandler
        public void onQuitUnregister(PlayerQuitEvent e){

            if(e.getPlayer() == player){
                unregister();
            }

        }

    }

}
