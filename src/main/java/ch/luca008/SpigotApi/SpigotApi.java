package ch.luca008.SpigotApi;

import ch.luca008.SpigotApi.Api.*;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotApi extends JavaPlugin {

    private static SpigotApi main;
    public static SpigotApi getInstance(){
        return main;
    }

    private static MainApi mainApi;
    private static TeamAPI teamApi;
    private static JSONApi jsonApi;
    private static ScoreboardAPI scoreboardApi;
    private static NBTTagApi nbttagApi;
    private static NPCApi npcApi;
    private static PromptApi promptApi;

    public void onEnable() {
        main = this;

        new ReflectionApi(); //execute the static block code

        mainApi = new MainApi();
        teamApi = new TeamAPI();
        jsonApi = new JSONApi(getDataFolder());
        scoreboardApi = new ScoreboardAPI();
        nbttagApi = new NBTTagApi();
        npcApi = new NPCApi(); //does register listener
        promptApi = new PromptApi();

        Bukkit.getServer().getPluginManager().registerEvents(teamApi, this);

    }

    public void onDisable(){
        HandlerList.unregisterAll(this);
        teamApi.getTeams().forEach(t->t.sendDeletePacket(Bukkit.getOnlinePlayers().toArray(new org.bukkit.entity.Player[0])));
        scoreboardApi.unregisterAll();
        npcApi.unregisterAll(true);
    }

    public static MainApi getMainApi(){
        return mainApi;
    }

    public static TeamAPI getTeamApi(){
        return teamApi;
    }

    public static JSONApi getJSONApi(){
        return jsonApi;
    }

    public static ScoreboardAPI getScoreboardApi(){
        return scoreboardApi;
    }

    public static NBTTagApi getNBTTagApi(){
        return nbttagApi;
    }

    public static NPCApi getNpcApi(){return npcApi;}

    public static PromptApi getPromptApi(){return promptApi;}
}
