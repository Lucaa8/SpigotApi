package ch.luca008.SpigotApi.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.function.BiFunction;

public class WebRequest {

    private static String gitVersionUrl = "https://raw.githubusercontent.com/Lucaa8/SpigotApi/master/version.md";
    public static String nameFinderUrl = "https://api.mojang.com/users/profiles/minecraft/";
    public static BiFunction<String, Boolean, String> textureFinderUrl = (uuid, signed) -> "https://sessionserver.mojang.com/session/minecraft/profile/"+uuid+"?unsigned=" + (!signed);

    @Nullable
    public static String getAccountID(@Nonnull String pseudoOrUuid)
    {
        String u = null;
        try{
            u = UUID.fromString(pseudoOrUuid).toString();
        }catch(Exception e){
            try {
                URL url = new URL(nameFinderUrl + pseudoOrUuid);
                JSONObject json = (JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()));
                u = (String)json.get("id");
            } catch (IOException | ParseException ex) {
                Logger.error("Invalid uuid or cannot get uuid while fetching Mojang servers.", WebRequest.class.getName());
                ex.printStackTrace();
            }
        }
        return u;
    }

    @Nullable
    public static ApiProperty getSkin(@Nonnull String pseudoOrUuid, boolean signed) {
        String u = getAccountID(pseudoOrUuid);
        if(u!=null){
            try {
                URL url = new URL(textureFinderUrl.apply(u, signed));
                JSONObject property = (JSONObject)((JSONArray) ((JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()))).get("properties")).get(0);
                String name = (String)property.get("name");
                String value = (String)property.get("value");
                String signature = property.containsKey("signature") ? (String)property.get("signature") : null;
                return new ApiProperty(name, value, signature);
            } catch (IOException | ParseException ex) {
                Logger.error("Something went wrong while fetching the Mojang servers.", WebRequest.class.getName());
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    public static String getLastPluginVersion()
    {
        try {
            URL url = new URL(gitVersionUrl);
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                line = reader.readLine();
            }
            return line.replace("\n", "");
        } catch (IOException ignored) {} //error log is created in the SpigotApi#checkVersion method
        return null;
    }

}
