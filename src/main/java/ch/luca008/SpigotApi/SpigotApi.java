package ch.luca008.SpigotApi;

import ch.luca008.SpigotApi.Api.*;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.luca008.SpigotApi.Utils.WebRequest;
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

        checkVersion();

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

    private void checkVersion()
    {
        String currentVersion = getDescription().getVersion();
        String latestVersion = WebRequest.getLastPluginVersion();
        if(latestVersion == null)
        {
            Logger.warn("Unable to verify if SpigotApi v" + currentVersion + " is the latest version due to an error encountered while attempting to fetch the latest version information from GitHub. Please check your network connection", SpigotApi.class.getName());
        }
        else if(!latestVersion.equals(currentVersion))
        {
            Logger.warn("A new version of SpigotApi is available! Current version: " + currentVersion + ", Latest version: " + latestVersion + ". Please consider updating to access new features and improvements.", SpigotApi.class.getName());
            Logger.info("Check the GitHub README.md file to download it! https://github.com/Lucaa8/SpigotApi?tab=readme-ov-file#download");
        }
        else
        {
            Logger.info("SpigotApi v" + currentVersion + " is the latest version!");
        }
    }

}
