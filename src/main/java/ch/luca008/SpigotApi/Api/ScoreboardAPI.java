package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.ScoreboardPackets.*;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardAPI {

    private final ArrayList<Scoreboard> scoreboards = new ArrayList<>();
    private final ArrayList<PlayerScoreboard> players = new ArrayList<>();

    private Scoreboard _getScoreboard(String uniqueName){
        if(uniqueName==null)return null;
        for(Scoreboard s : scoreboards){
            if(s.name.equals(uniqueName)){
                return s;
            }
        }
        return null;
    }

    public boolean doesScoreboardExist(String name){
        return _getScoreboard(name)!=null;
    }

    public boolean doesScoreboardExist(Scoreboard scoreboard){
        return doesScoreboardExist(scoreboard.name);
    }

    @Nullable
    public Scoreboard retrieveScoreboard(String name){
        return _getScoreboard(name);
    }

    public boolean registerScoreboard(String name, String title, ArrayList<ScoreboardLine> lines){
        if(!doesScoreboardExist(name)){
            scoreboards.add(new Scoreboard(name, title, lines));
            return true;
        }
        return false;
    }

    public boolean registerScoreboard(String name, String title){
        return registerScoreboard(name, title, new ArrayList<>());
    }

    public boolean unregisterScoreboard(Scoreboard scoreboard){
        if(scoreboard!=null&&doesScoreboardExist(scoreboard)){
            scoreboards.remove(scoreboard);
            for(PlayerScoreboard p : players){
                Scoreboard s = p.getParentBoard();
                if(s!=null&&s.getName().equals(scoreboard.getName())){
                    p.setParentBoard(null);
                }
            }
            return true;
        }
        return false;
    }

    public boolean unregisterScoreboard(String name){
        return unregisterScoreboard(_getScoreboard(name));
    }

    public void unregisterAll(){
        for(Scoreboard s : new ArrayList<>(scoreboards)){
            unregisterScoreboard(s);
        }
        scoreboards.clear();
    }

    /**
     * Sets the given registered scoreboard to this player.
     * @param player The player to which the scoreboard will be displayed
     * @param scoreboardName A registered (See {@link #registerScoreboard(String, String)}) scoreboard.<p>If the player already has this scoreboard then nothing happen.<p>Pass null to hide and remove this scoreboard to the player.
     * @return The player's board data with current personal title and lines. Null if the scoreboard was null AND the scoreboard has been removed successfully.
     */
    @Nullable
    public PlayerScoreboard setScoreboard(@Nonnull Player player, @Nullable String scoreboardName) {
        PlayerScoreboard pboard = getScoreboard(player);
        if(pboard == null && scoreboardName != null) {
            PlayerScoreboard newPlayerBoard = new PlayerScoreboard(player, scoreboardName);
            players.add(newPlayerBoard);
            return newPlayerBoard;
        }
        if(scoreboardName != null) {
            pboard.setParentBoard(scoreboardName);
            return pboard;
        }
        return null;
    }

    @Nullable
    public PlayerScoreboard getScoreboard(@Nonnull Player player){
        for(PlayerScoreboard pboard : players){
            if(pboard.player == player){
                return pboard;
            }
        }
        return null;
    }

    public static class PlayerScoreboard{
        private final Player player;
        private String parentBoard;
        private String playerTitle;
        private ArrayList<ScoreboardLine> playerLines = new ArrayList<>();

        private Map<String, String> placeholders = new HashMap<>();

        public PlayerScoreboard(Player player, @Nullable String parentBoard){
            this.player = player;
            setParentBoard(parentBoard);
        }

        public void setParentBoard(String parentBoard){
            Scoreboard currentBoard = getParentBoard();
            Scoreboard newBoard = SpigotApi.getScoreboardApi()._getScoreboard(parentBoard);
            if(currentBoard!=null||newBoard!=null){
                if(currentBoard!=null){
                    playerApi().sendPacket(player, scoreboardApi().getScoreboardDisplayHidePacket());
                    playerApi().sendPacket(player, scoreboardApi().getScoreboardRemoveObjectivePacket(currentBoard.getName()));
                    this.parentBoard = null;
                    this.playerTitle = null;
                    this.playerLines = new ArrayList<>();
                    this.placeholders = new HashMap<>();
                }
                if(newBoard!=null){
                    this.parentBoard = newBoard.getName();
                    playerTitle = newBoard.getTitle();
                    playerLines = cloneLines(newBoard.getLines());
                    playerApi().sendPacket(player, scoreboardApi().getScoreboardObjectivePacket(this.parentBoard, Mode.ADD, this.playerTitle));
                    playerApi().sendPacket(player, scoreboardApi().getScoreboardDisplayPacket(this.parentBoard));
                    ArrayList<ScoreboardLine> reverse = new ArrayList<>(this.playerLines);
                    Collections.reverse(reverse);
                    for(int i=reverse.size()-1;i>=0;i--){
                        for(String placeholder : reverse.get(i).placeholders){
                            this.placeholders.put(placeholder, "");
                        }
                        playerApi().sendPacket(player, scoreboardApi().getScoreboardChangeScorePacket(parentBoard, reverse.get(i).text, i));
                    }
                }
            }
        }

        @Nullable
        public Scoreboard getParentBoard(){
            if(parentBoard==null||parentBoard.isEmpty())return null;
            return SpigotApi.getScoreboardApi().retrieveScoreboard(parentBoard);
        }

        public boolean isParentBoardValid(){
            return getParentBoard()!=null;
        }

        @Nullable
        public String getCurrentTitle(){
            return playerTitle;
        }

        public void setCurrentTitle(String title){
            if(isParentBoardValid()){
                this.playerTitle = title;
                playerApi().sendPacket(player, scoreboardApi().getScoreboardObjectivePacket(this.parentBoard, Mode.CHANGE, title));
            }
        }

        public void setPlaceholder(String placeholder, String value) {
            Scoreboard pboard = getParentBoard();
            if(pboard == null || !this.placeholders.containsKey(placeholder))
                return;
            String old = this.placeholders.put(placeholder, value);
            if(old!=null && !old.equals(value)){
                for(ScoreboardLine line : pboard.getLines()){
                    if(line.placeholders.contains(placeholder)){
                        updateLine(line);
                    }
                }
            }
        }

        private void updateLine(ScoreboardLine initialLine){
            String finalLine = initialLine.text;
            for(String placeholder : initialLine.placeholders){
                finalLine = finalLine.replace(placeholder, this.placeholders.getOrDefault(placeholder, ""));
            }
            ScoreboardLine pLine = getLine(initialLine.line);
            if(pLine!=null&&!pLine.text.equals(finalLine)){
                playerApi().sendPacket(player, scoreboardApi().getScoreboardRemoveScorePacket(parentBoard, pLine.text));
                pLine.setText(finalLine);
                playerApi().sendPacket(player, scoreboardApi().getScoreboardChangeScorePacket(parentBoard, pLine.text, playerLines.size()-pLine.line));
            }
        }

        @Nullable
        public ScoreboardLine getLine(int line){
            for(ScoreboardLine l : playerLines){
                if(l.line==line){
                    return l;
                }
            }
            return null;
        }

        private static MainApi.SpigotPlayer playerApi(){
            return SpigotApi.getMainApi().players();
        }

        private static MainApi.SpigotPackets.ScoreboardPackets scoreboardApi(){
            return SpigotApi.getMainApi().packets().scoreboard();
        }

        private ArrayList<ScoreboardLine> cloneLines(ArrayList<ScoreboardLine> parentLines){
            ArrayList<ScoreboardLine> l = new ArrayList<>();
            for(ScoreboardLine line : parentLines){
                l.add(new ScoreboardLine(line.line, line.text));
            }
            return l;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlayerScoreboard that)) return false;
            return player.equals(that.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player);
        }
    }

    public static class Scoreboard{
        private final String name;
        private final String title;
        private final ArrayList<ScoreboardLine> lines;

        private Scoreboard(String name, String title, ArrayList<ScoreboardLine> lines){
            this.name = name;
            this.title = title;
            this.lines = lines;
            if(!this.lines.isEmpty()){
                ScoreboardLine.sortLines(this.lines);
            }
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public ArrayList<ScoreboardLine> getLines() {
            return lines;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Scoreboard)) return false;
            Scoreboard that = (Scoreboard) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class ScoreboardLine {
        private final int line;
        private String text;

        private final List<String> placeholders = new ArrayList<>();

        private ScoreboardLine(int line, String text){
            this.line = line;
            this.text = text;
            Matcher matcher = Pattern.compile("\\{[_A-Z0-9]*}").matcher(this.text);
            while(matcher.find()) {
                placeholders.add(matcher.group());
            }
        }

        public int getLine(){
            return line;
        }

        private void setText(String text){
            this.text = text;
        }
        public String getText(){
            return text;
        }

        @Override
        public String toString() {
            return "ScoreboardLine{" +
                    "line=" + line +
                    ", text='" + text + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ScoreboardLine)) return false;
            ScoreboardLine that = (ScoreboardLine) o;
            return line == that.line && text.equals(that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, text);
        }

        public static void sortLines(ArrayList<ScoreboardLine> lines){
            lines.sort(Comparator.comparingInt(l -> l.line));
        }
    }

    public static class LinesBuilder {
        private final ArrayList<ScoreboardLine> lines = new ArrayList<>();

        public LinesBuilder add(int line, String text){
            for(ScoreboardLine l : lines){
                if(l.line==line){
                    l.text = text;
                    return this;
                }
            }
            lines.add(new ScoreboardLine(line, text));
            return this;
        }

        public ArrayList<ScoreboardLine> getLines(){
            return lines;
        }
    }

}
