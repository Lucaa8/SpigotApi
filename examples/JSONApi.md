# JSONApi
With this API you can write and read basic information inside JSON files. The API let you chain keys to write inside any deeper JSON Object without the need to create section or whatever like with the Bukkit YAML methods.
## Writer
The Writer is used to store information inside a JSON Object and then write it in any file. **All the directories to the desired file need to exist.** You can start a writer from an existing JSONObject ([JSON simple](https://code.google.com/archive/p/json-simple/) library) or an empty JSON.
```java
JSONApi.JSONWriter writer = SpigotApi.getJSONApi().getWriter(null); //Replace null with a JSONObject instance if you want a writer from an existing JSON.
writer.write("Name", "Luca008")
    .write("Health", 12)
    .writeArray("Description", List.of("Line1", "Line2", 3, 4)) //List<Object> later replaced by JSONArray
    .write("Location.X", 111.11)
    .write("Location.Y", 222.22)
    .write("Location.Z", 333.33)
    .write("Location.Direction.Pitch", 45.0)
    .write("Location.Direction.Yaw", 90.0)
    .writeToFile(new File(getDataFolder(), "player.json"), true);
    //the true can be replaced by false if you dont care about pretty JSON (with indentation)
```
Result in the `plugins/your_plugin/player.json`
```json
{
  "Description": [
    "Line1",
    "Line2",
    3,
    4
  ],
  "Health": 12,
  "Name": "Luca008",
  "Location": {
    "X": 111.11,
    "Y": 222.22,
    "Z": 333.33,
    "Direction": {
      "Pitch": 45.0,
      "Yaw": 90.0
    }
  }
}
```
## Reader
Now that you stored some information you can read it back with the API's reader.
```java
JSONApi.JSONReader reader = SpigotApi.getJSONApi().readerFromFile(new File(getDataFolder(), "player.json"));
System.out.println(reader.getString("Name"));
System.out.println(reader.getInt("Health"));
for(Object obj : reader.getArray("Description")){
    //String str = (String) obj; //IF you are sure you only put String (or int, etc...) inside the List<Object> before writing then you can cast
    System.out.println(obj);
}
System.out.println(reader.getJson("Location").getDouble("X")); //Access deeper keys
```

## Pretty Printing
If you want to print some of this JSON in the console for debbuging purposes you can use the static `JSONApi#prettyJson` method.
```java
System.out.println(JSONApi.prettyJson(writer.asJson()));
System.out.println(JSONApi.prettyJson(reader.asJson()));
```