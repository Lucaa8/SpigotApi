# FileApi
This small API lets you export files from inside your JAR to any directory on the system. It may be useful for default configurations, lang files, etc...

## List files
With this method you can list every folder/file in your final plugin JAR.
```java
@Override
public void onEnable() {
    FileApi.listFiles(YourMainClass.class).forEach(System.out::println);
}
```
Here an output example with the following structure

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/7ddd9671-b736-4000-87d1-afd58ec5615a)


```
[13:29:54] [Server thread/INFO]: META-INF/
[13:29:54] [Server thread/INFO]: META-INF/MANIFEST.MF
[13:29:54] [Server thread/INFO]: ch/
[13:29:54] [Server thread/INFO]: ch/luca008/
[13:29:54] [Server thread/INFO]: ch/luca008/testplugin/
[13:29:54] [Server thread/INFO]: ch/luca008/testplugin/TestPlugin.class
[13:29:54] [Server thread/INFO]: Lang/
[13:29:54] [Server thread/INFO]: Lang/en.json
[13:29:54] [Server thread/INFO]: Lang/fr.json
[13:29:54] [Server thread/INFO]: plugin.yml
```
As you can see, we get all the embed files in the final JAR and not only the `resources` ones.

## Export files
With this method you can export any **file** listed by the `FileApi#listFiles` method. You can choose the output destination.

### Hardcoding
You can hardcode the path of the inside file if you already know which specific file you want to export. E.g. I only want to export `en.json` and `fr.json` then you would hardcode something like this
```java
try {
    FileApi.exportFile(YourMainClass.class, "Lang/en.json", new File(mainInstance.getDataFolder(), "Lang/en.json"));
    FileApi.exportFile(YourMainClass.class, "Lang/fr.json", new File(mainInstance.getDataFolder(), "Lang/fr.json"));
} catch (IOException ex) {
    System.err.println("Failed to export file " + f + ". Error;");
    ex.printStackTrace();
}
```

### Dynamically
If you want to export any file in a given folder you would do a little bit of filtering to find and export the correct files.
```java
l.stream()
    //f would be "Lang/", "Lang/en.json", "Lang/fr.json", "plugin.yml", etc...
    .filter(f->f.startsWith("Lang/")&&f.endsWith(".json"))
    .forEach(f-> {
        try {
            FileApi.exportFile(TestPlugin.class, f, new File(getDataFolder(), f));
            //The output file (3rd arg) will be "plugins/YourPlugin/Lang/en.json" and "plugins/YourPlugin/Lang/fr.json"
        } catch (IOException ex) {
            System.err.println("Failed to export file " + f + ". Error;");
            ex.printStackTrace();
        }
    });
```
