# SpigotApi
**Despite the name, this plugin is not affiliated, maintained, sponsored, or endorsed by [Spigot](https://hub.spigotmc.org/).** Moreover, please note that despite extensive testing, certain highly specific behaviors may cause unpredictable issues. Please report any problematic code you encounter at lucadicosola4@gmail.com.

## Description
SpigotApi enhances the capabilities of the standard Bukkit/Spigot API, enabling sophisticated functionalities not fully supported by default. Initially developed for legacy Minecraft versions such as 1.12 or 1.15, SpigotApi addressed the challenges of utilizing NMS (Net Minecraft Server) code due to frequent changes in package names across versions, which made direct access less straightforward. However, from version 1.17 onwards, accessing these packages without the need for reflection has become feasible, allowing more direct usage. Despite this, a thorough understanding of the Minecraft server's internals, from obfuscated enums to packet handling, is essential. SpigotApi simplifies this process, handling the complex aspects so you can easily implement advanced methods within your plugins.

## Version
Currently, the SpigotApi only supports the 1.20 to 1.20.4 Spigot/Paper version. This is because attributes names can change from version to version and you cannot use it with others Minecraft versions. I will add the futures versions of Spigot/Paper when they appear, but I didnt plan to release SpigotApi for older versions. \
**Currently supported versions:**
- 1.20.1
- 1.20.2
- 1.20.4

## Features
- **TeamApi** - Create your custom teams and ranks with custom prefixes, suffixes, colors, tab order and more..
- **ScoreboardApi** - Provides a straightforward way to manage dynamic scoreboards with placeholders that can be changed effortlessly at any time.
- **NPCApi** - Create basic NPCs which look at the player in game, put them skins, easy-to-setup interaction manager.
- **PromptApi** - Enables you to open a sign prompt for a player and await their response, allowing you to execute any desired action with the input inside a sync. callback.
- **NBTTagApi** - Add NBTs on your items to retrieve them easily in inventories
- **SnifferApi** - Listen to every packets a player sends to the server
- **JSONApi** - Store and read information easily in JSON instead of YAML
- **FileApi** - Streamlines the extraction of embedded resources, making it effortless to manage and deploy plugin assets and configurations
- **Items** - An unique way to create, customize, compare and store items easily without YAML or NBT problems.

## Documentation and examples
Find all the documentation you need to use this API in the `examples` directory. You have one markdown file for each API from the features section.

## JavaDoc
This plugin includes a JavaDoc documentation accessible upon downloading the artifact from Maven. However, the current JavaDoc is not good and not complete, as it primarily served as personal reminders for critical details. I plan to enhance this documentation in future versions of SpigotApi to facilitate its use during coding. In the meantime, I recommend referring to the detailed documentation outlined in the previous section for complete guidance.

## Download
Get the latest version of SpigotApi [here](https://mvn.luca-dc.ch/repository/dev-mc/ch/luca008/SpigotApi/latest/SpigotApi-latest.jar) and put it inside your plugins folder. \
**Current version of SpigotApi:** 2.1

## How to use it?
You can get SpigotApi as a maven dependency in your project.

### Add the repository
```xml
<repository>
  <id>dev-mc</id>
  <url>https://mvn.luca-dc.ch/repository/dev-mc/</url>
</repository>
```

### Add the dependency
```xml
<dependency>
  <groupId>ch.luca008</groupId>
  <artifactId>SpigotApi</artifactId>
  <version>latest</version>
  <scope>provided</scope>
</dependency>
```

## Built With

* [IntelliJ IDEA](https://www.jetbrains.com/idea/) - Using Maven
* [Spigot](https://hub.spigotmc.org/)

## Author(s)

The entire code was written by me.

## License
 
This project is licensed under the GPLv3
 
![GNU GPLV3](https://imgur.com/imkUoGR.png)
