package ch.luca008.SpigotApi;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.MainApi;
import ch.luca008.SpigotApi.Api.ScoreboardAPI;
import ch.luca008.SpigotApi.Api.TeamAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

//Ajouter NBTTagsApi dans SpigotApi
//Convertir le plugin Nametags avec SpigotApi et LuckPerms 1.20
//Refaire plugin Scoreboard pour choisir et changer son scoreboard grâce à l'inventaire

public class SpigotApi extends JavaPlugin {

    private static SpigotApi main;
    public static SpigotApi getInstance(){
        return main;
    }

    private static MainApi mainApi;
    private static TeamAPI teamApi;
    private static JSONApi jsonApi;
    private static ScoreboardAPI scoreboardApi;

    public void onEnable() {
        main = this;
        mainApi = new MainApi();
        teamApi = new TeamAPI();
        jsonApi = new JSONApi(getDataFolder());
        scoreboardApi = new ScoreboardAPI();

        Bukkit.getServer().getPluginManager().registerEvents(teamApi, this);
    }

    public void onDisable(){
        HandlerList.unregisterAll(this);
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

}
