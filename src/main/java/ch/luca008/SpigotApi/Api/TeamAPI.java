package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.PacketsUtils.ChatColor;
import ch.luca008.SpigotApi.Packets.TeamsPackets.Collisions;
import ch.luca008.SpigotApi.Packets.TeamsPackets.Mode;
import ch.luca008.SpigotApi.Packets.TeamsPackets.NameTagVisibility;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TeamAPI implements Listener {

    private final Map<String, Team> teams = new HashMap<>();

    /**
     * A registered team is a non-null team contained into the server's teams map. A team should always be registered before updating his content (because of packets)
     * @param team The team to check
     * @return If the team is registered. To (un)register a team see {@link #registerTeam(Team)} and {@link #unregisterTeam(Team)}
     */
    public boolean isTeamRegistered(Team team){
        if(team!=null){
            return isTeamRegistered(team.getUniqueName());
        }
        return false;
    }

    /**
     * See {@link #isTeamRegistered(Team)}
     * @param uniqueName14Len The unique name of the team (the one which the team gets registered)
     */
    public boolean isTeamRegistered(String uniqueName14Len){
        if(uniqueName14Len!=null){
            return teams.containsKey(uniqueName14Len);
        }else{
            return false;
        }
    }

    /**
     * Try to retrieve a registered team on the server with the unique id specified
     * @param uniqueName14Len The unique name of the team (the one which the team gets registered)
     * @return The team object if found. Otherwise, return null
     */
    @Nullable
    public Team getTeam(String uniqueName14Len){
        if(isTeamRegistered(uniqueName14Len)){
            return teams.get(uniqueName14Len);
        }
        return null;
    }

    public Collection<Team> getTeams(){
        return teams.values();
    }

    /**
     * Register a new team on the server (if not-null & not already registered). Then send create packet to everyone
     * @param team The new team to register
     * @return return false if the specified team was null or already registered. Otherwise, return true (and the team is registered)
     */
    public boolean registerTeam(Team team){
        if(team!=null&&!isTeamRegistered(team)){
            teams.put(team.getUniqueName(), team);
            team.sendCreatePacket(Bukkit.getOnlinePlayers().toArray(new Player[0]));
            return true;
        }
        return false;
    }

    /**
     * Unregister a team if found and then send delete packet to everyone
     * @param team A registered team (return false if null)
     * @return If the team was successfully removed
     */
    public boolean unregisterTeam(Team team){
        if(team!=null&&isTeamRegistered(team)){
            teams.remove(team.getUniqueName());
            team.sendDeletePacket(Bukkit.getOnlinePlayers().toArray(new Player[0]));
            return true;
        }
        return false;
    }

    /**
     * See {@link #unregisterTeam(Team)}}
     * @param uniqueName14Len The unique name of the team (the one which the team gets registered)
     */
    public boolean unregisterTeam(String uniqueName14Len){
        if(isTeamRegistered(uniqueName14Len)){
            return unregisterTeam(getTeam(uniqueName14Len));
        }
        return false;
    }

    /**
     * @param name a player's NAME not displayname. Can be either an entityplayer (fakeplayer), or an entity's uuid
     * @return null if no team was found for the given name or uuid or the current team if found
     */
    @Nullable
    public Team getPlayerTeam(@Nonnull String name){
        for(Team t : teams.values()){
            if(t.hasEntry(name)){
                return t;
            }
        }
        return null;
    }

    /**
     * Add a player or entity to the specified team. Then send update packets to everyone
     * @param team A registered team (if not return false and do nothing)
     * @param name A player NAME (not displayname), entityplayer NAME or entity uuid
     * @param force true: Tell the server to remove player from his current team(if found) and then add him to the new one. false: do nothing if current team != null
     * @return If the player was added successfully to the new team
     */
    public boolean addPlayer(Team team, String name, boolean force){
        if(team==null||!isTeamRegistered(team)||name==null||name.isEmpty()){
            return false;
        }
        Team t = getPlayerTeam(name);
        if(t!=null){
            if(force){
                removePlayer(name);
            }else{
                return false;
            }
        }
        team.addEntries(name);
        return true;
    }

    /**
     * See {@link #addPlayer(Team, String, boolean)}
     * @param uniqueName14Len The unique name of the team (the one which the team gets registered)
     */
    public boolean addPlayer(String uniqueName14Len, String name, boolean force){
        if(isTeamRegistered(uniqueName14Len)){
            return addPlayer(getTeam(uniqueName14Len), name, force);
        }
        return false;
    }

    /**
     * Remove the specified player from his current team if found or do nothing
     * @param name A player NAME (not displayname), entityplayer NAME or entity uuid
     * @return If the player was removed successfully from any team (means {@link #getPlayerTeam(String)} should return null for this name)
     */
    public boolean removePlayer(String name){
        if(name==null||name.isEmpty()){
            return false;
        }
        Team t = getPlayerTeam(name);
        if(t==null||!isTeamRegistered(t)){
            return false;
        }
        t.removeEntries(name);
        return true;
    }

    @EventHandler
    public void onJoinRegisterTeams(PlayerJoinEvent e){
        Player p = e.getPlayer();
        for(Team team : getTeams()){
            team.sendCreatePacket(p);
        }
    }

    @EventHandler
    public void onLeaveUnregisterTeams(PlayerQuitEvent e){
        Player p = e.getPlayer();
        removePlayer(p.getName());
        for(Team team : getTeams()){
            team.sendDeletePacket(p);
        }
    }

    public static class Team {

        private final String uniqueName;
        private final String displayName;
        private final int sortOrder;
        private String prefix;
        private String suffix;
        private ChatColor color;
        private final NameTagVisibility nameTagVisibility;
        private final Collisions collisions;
        private final ArrayList<String> entries;

        private Team(String uniqueName, String displayName, int sortOrder, String prefix, String suffix, ChatColor color, NameTagVisibility nameTagVisibility, Collisions collisions, ArrayList<String> entries) {
            this.uniqueName = uniqueName;
            this.displayName = displayName;
            this.sortOrder = sortOrder;
            this.prefix = prefix;
            this.suffix = suffix;
            this.color = color;
            this.nameTagVisibility = nameTagVisibility;
            this.collisions = collisions;
            this.entries = entries;
        }

        public String getReelUniqueName(){
            if(hasSortOrder())return String.format("%02d", getSortOrder())+getUniqueName();
            return getUniqueName();
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean hasSortOrder(){
            return sortOrder>=0;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public ChatColor getColor() {
            return color;
        }

        public NameTagVisibility getNameTagVisibility() {
            return nameTagVisibility;
        }

        public Collisions getCollisions() {
            return collisions;
        }

        public boolean hasEntry(String entry){
            return getEntries().contains(entry);
        }

        public ArrayList<String> getEntries() {
            return entries;
        }

        public void setPrefix(String prefix, boolean sendUpdate) {
            this.prefix = prefix;
            if(sendUpdate){
                update();
            }
        }

        public void setSuffix(String suffix, boolean sendUpdate) {
            this.suffix = suffix;
            if(sendUpdate){
                update();
            }
        }

        public void setColor(ChatColor color, boolean sendUpdate) {
            this.color = color;
            if(sendUpdate){
                update();
            }
        }

        public void addEntries(String...entries){
            List<String> toAddList = new ArrayList<>();
            for(String entry : entries){
                if(!hasEntry(entry)){
                    this.entries.add(entry);
                    toAddList.add(entry);
                }
            }
            if(!toAddList.isEmpty()){
                updateEntries(Mode.ADD_ENTITY, toAddList);
            }
        }

        public void removeEntries(String...entries){
            List<String> toRemoveList = new ArrayList<>();
            for(String entry : entries){
                if(hasEntry(entry)){
                    this.entries.remove(entry);
                    toRemoveList.add(entry);
                }
            }
            if(!toRemoveList.isEmpty()){
                updateEntries(Mode.REMOVE_ENTITY, toRemoveList);
            }
        }

        private void update(){
            MainApi api = SpigotApi.getMainApi();
            Object[] updatePackets = api.packets().teams().getCreateOrUpdateTeamPacket(getReelUniqueName(), Mode.UPDATE, getDisplayName(),
                    getNameTagVisibility(), getCollisions(), getColor(), getPrefix(), getSuffix());
            if(updatePackets!=null)
            {
                api.players().sendPackets(Bukkit.getOnlinePlayers(), updatePackets);
            }
        }

        private void updateEntries(Mode action, List<String> entries){
            MainApi api = SpigotApi.getMainApi();
            Object[] updatePackets = null;
            if(action==Mode.ADD_ENTITY){
                updatePackets = api.packets().teams().getAddEntityTeamPacket(getReelUniqueName(), entries.toArray(new String[0]));
            }else if(action==Mode.REMOVE_ENTITY){
                updatePackets = api.packets().teams().getRemoveEntityTeamPacket(getReelUniqueName(), entries.toArray(new String[0]));
            }
            if(updatePackets!=null){
                api.players().sendPackets(Bukkit.getOnlinePlayers(), updatePackets);
            }
        }

        public void sendCreatePacket(Player...players){
            MainApi api = SpigotApi.getMainApi();
            Collection<Player> pls = Arrays.stream(players).collect(Collectors.toList());
            Object[] createPackets = api.packets().teams().getCreateOrUpdateTeamPacket(getReelUniqueName(), Mode.CREATE, getDisplayName(),
                    getNameTagVisibility(), getCollisions(), getColor(), getPrefix(), getSuffix(), entries.toArray(new String[0]));
            if(createPackets != null)
            {
                api.players().sendPackets(pls, createPackets);
            }
        }

        public void sendDeletePacket(Player...players){
            MainApi api = SpigotApi.getMainApi();
            Collection<Player> pls = Arrays.stream(players).collect(Collectors.toList());
            Object[] deletePackets = api.packets().teams().getDeleteTeamPacket(getReelUniqueName());
            if(deletePackets != null)
            {
                api.players().sendPackets(pls, deletePackets);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Team team)) return false;
            return uniqueName.equals(team.uniqueName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uniqueName);
        }

    }

    public static class TeamBuilder{

        private final String uniqueName;
        private String displayName;
        private int sortOrder = -1;
        private String prefix = "";
        private String suffix = "";
        private ChatColor color = ChatColor.WHITE;
        private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
        private Collisions collisions = Collisions.ALWAYS;
        private ArrayList<String> entries = new ArrayList<>();

        /**
         * If you want to add a sort order (tab) you will need to limit the unique name length to 14. Sort order takes 2 spaces. (for example: 02)
         */
        public TeamBuilder(String uniqueName){
            this.uniqueName = uniqueName;
            this.displayName = uniqueName; //can be changed further
        }

        public TeamBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public TeamBuilder setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public TeamBuilder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public TeamBuilder setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public TeamBuilder setColor(ChatColor color) {
            this.color = color;
            return this;
        }

        public TeamBuilder setNameTagVisibility(NameTagVisibility nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this;
        }

        public TeamBuilder setCollisions(Collisions collisions) {
            this.collisions = collisions;
            return this;
        }

        public TeamBuilder setEntries(ArrayList<String> entries) {
            this.entries = entries;
            return this;
        }

        public Team create(){
            if(uniqueName==null||uniqueName.length()==0||(uniqueName.length()>14&&sortOrder>=0)||sortOrder>99){
                Logger.error("Cannot create a team with a null uniqueName or with a total length greater than 16. Consider than sortOrder takes up 2 characters.", TeamAPI.class.getName());
                return null;
            }
            return new Team(uniqueName, displayName, sortOrder, prefix, suffix, color, nameTagVisibility, collisions, entries);
        }
    }

}
