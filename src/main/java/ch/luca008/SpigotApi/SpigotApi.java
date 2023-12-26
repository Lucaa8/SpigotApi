package ch.luca008.SpigotApi;

import ch.luca008.SpigotApi.Api.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotApi extends JavaPlugin implements Listener {

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
        npcApi = new NPCApi();
        promptApi = new PromptApi();

        Bukkit.getServer().getPluginManager().registerEvents(teamApi, this);
        Bukkit.getServer().getPluginManager().registerEvents(promptApi, this);
        Bukkit.getServer().getPluginManager().registerEvents(npcApi, this);

        //TODO: remove
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        //TODO: prompt and nbttags apis, then remove implements listener
        //TODO: check skull nbt
    }

    public void onDisable(){
        HandlerList.unregisterAll((Plugin) this);
        teamApi.getTeams().forEach(t->t.sendDeletePacket(Bukkit.getOnlinePlayers().toArray(new Player[0])));
        scoreboardApi.unregisterAll();
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
