package ch.luca008.SpigotApi.Utils;

import com.mojang.authlib.properties.Property;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class WebRequest {

    public static String nameFinderUrl = "https://api.mojang.com/users/profiles/minecraft/";
    public static String textureFinderUrl = "https://sessionserver.mojang.com/session/minecraft/profile/{UUID}?unsigned=false";

    @Nullable
    public static Property getSkin(@Nonnull String pseudoOrUuid) {
        String u = null;
        try{
            u = UUID.fromString(pseudoOrUuid).toString();
        }catch(Exception e){
            try {
                URL url = new URL(nameFinderUrl + pseudoOrUuid);
                JSONObject json = (JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()));
                u = (String)json.get("id");
            } catch (IOException | ParseException e1) {
                System.err.println("Invalid uuid + can't get uuid while fetching mojang servers.");
                e1.printStackTrace();
            }
        }
        if(u!=null){
            try {
                URL url = new URL(textureFinderUrl.replace("{UUID}",u));
                JSONObject property = (JSONObject)((JSONArray) ((JSONObject) new JSONParser().parse(new InputStreamReader(url.openStream()))).get("properties")).get(0);
                return new Property((String)property.get("name"), (String)property.get("value"), (String)property.get("signature"));
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
