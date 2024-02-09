package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class JSONApi {

    private File pluginDir;

    public JSONApi(File pluginDir){
        this.pluginDir = pluginDir;
    }

    public JSONReader getReader(JSONObject json){
        return new JSONReader(json);
    }
    public JSONWriter getWriter(@Nullable JSONObject json){return new JSONWriter(json==null?new JSONObject():json);}

    public static String prettyJson(JSONObject json){
        return prettyJson(json.toJSONString());
    }
    public static String prettyJson(String json){
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(new JsonParser().parse(json));
    }

    /**
     * Il file is null or doesn't exist this method return a new empty JSONObject. Otherwise, return the file's content parsed into a JSONObject
     */
    @Nonnull
    public JSONObject readFromFile(File file){
        if(file!=null&&file.exists()){
            try(BufferedReader r = Files.newBufferedReader(Paths.get(file.toURI()))){
                return (JSONObject) new JSONParser().parse(r);
            }catch(ParseException | IOException e){
                System.err.println("Can't parse file '"+file.getName()+"' to JSONObject.");
            }
        }
        return new JSONObject();
    }

    @Nonnull
    public JSONReader readerFromFile(File file){
        return getReader(readFromFile(file));
    }

    /**
     * @param file The file in which the content will be written. If the file doesn't exist the method try to create it.
     * @param prettyPrinting If true the method will call {@link #prettyJson(JSONObject)} before writing the json content
     * @return null if the file arg is null, cannot be created or written. Otherwise, return the same file as in argument
     */
    @Nullable
    public File writeToFile(File file, JSONObject jsonContent, boolean prettyPrinting){
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            Files.write(Paths.get(file.toURI()), (prettyPrinting?prettyJson(jsonContent):jsonContent.toJSONString()).getBytes(StandardCharsets.UTF_8));
            return file;
        }catch(IOException e){
            System.err.println("Can't write on path " + file.getPath() + ".");
            e.printStackTrace();
        }
        return null;
    }

    public static class JSONReader{

        private final JSONObject json;

        private JSONReader(JSONObject json){
            this.json = json;
        }

        public JSONObject asJson(){
            return json;
        }

        /**
         * return if the current jsonobject contains the specified key
         */
        public boolean c(String key){
            return json.containsKey(key);
        }

        public int getInt(String key){
            Object o = json.get(key);
            if(o instanceof Long) return ((Long)json.get(key)).intValue();
            else return (int)o;
        }

        public long getLong(String key){
            return ((Long)json.get(key)).longValue();
        }

        public String getString(String key){
            return (String)json.get(key);
        }

        public boolean getBool(String key){return (boolean)json.get(key);}

        public double getDouble(String key){return (double)json.get(key);}

        public JSONArray getArray(String key){
            return (JSONArray) json.get(key);
        }

        public JSONReader getJson(String key){
            return new JSONReader((JSONObject) json.get(key));
        }
    }

    public static class JSONWriter{

        private final JSONObject json;

        private JSONWriter(JSONObject json){
            this.json = json;
        }

        private JSONObject _write(JSONObject json, String key, Object value){
            if(!key.contains(".")) {
                json.put(key, value);
                return json;
            }
            String currentKey = key.split("\\.")[0];
            String nextKeys = key.substring(key.indexOf(".")+1);
            Object next = json.getOrDefault(currentKey, new JSONObject());
            if(!(next instanceof JSONObject jnext)){
                Logger.error("Subkey \"" + currentKey + "\" in the keys chain \"" + key + "\" was not a JSONObject.", getClass().getName());
                return null;
            }
            if(!json.containsKey(currentKey))
                json.put(currentKey, jnext);
            return _write(jnext, nextKeys, value);
        }

        public JSONWriter write(String key, Object value){
            _write(this.json, key, value);
            return this;
        }

        public JSONWriter writeArray(String key, List<Object> list){
            return writeArray(key, list.toArray(new Object[0]));
        }

        public JSONWriter writeArray(String key, Object[] array){
            JSONArray jarr = new JSONArray();
            Collections.addAll(jarr, array);
            _write(this.json, key, jarr);
            return this;
        }

        public JSONObject asJson(){
            return json;
        }

        /**
         * See {@link #writeToFile(File, JSONObject, boolean)}
         */
        @Nullable
        public File writeToFile(File file, boolean prettyPrinting){
            return SpigotApi.getJSONApi().writeToFile(file, asJson(), prettyPrinting);
        }

    }
}
