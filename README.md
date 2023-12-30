# SpigotApi
## Description
SpigotApi allows you to do some fancy things not fully supported by the standard Bukkit/Spigot API. SpigotApi was firstly developped for older Minecraft versions like 1.2 or 1.15. During these versions you couldnt use NMS as "easily" as today because of the package name changing every versions. Right now (after 1.17 and newer versions) you can access those package without reflection and use them as you wish, but you need to understand how the Minecraft server is working, from obfuscated enums to packets. SpigotApi does all the dirty work for you, and you can use some cool methods inside your plugins!

## Version
Currently the SpigotApi only supports the 1.20 to 1.20.4 Spigot/Paper version. This is because attributes names can change from version to version and you cannot use it with others Minecraft versions. I will add the futures versions of Spigot/Paper when they appear, but I didnt plan to release SpigotApi for older versions.

## Features
### Summary
- **TeamAPI** - Create your custom teams and ranks with custom prefixes, suffixes, colors and more..
- **ScoreboardApi** - Create multiples scoreboards with default values (placeholders), set them to your players and update them easily whenever you want!
- **NPCApi** - Create basic NPCs which look at the player in game, put them skins, easy-to-setup interaction manager.
- **PromptApi** - Open a sign to a player and wait for their response, do whatever you want with it inside a sync. callback.
- **NBTTagApi** - Add NBTs on your items to retrieve them easily in inventories
- **SnifferApi** - Listen to every packets a player receive
- **JSONApi** - Store and read information easily in JSON instead of YAML
  
### TeamAPI
This API registers your teams on the server side and send them when a player connects to the server. With this implementation you can; \
- Sort yours teams in the tab
- Add a prefix
- Add a suffix
- Set the team color
- Set the nametag visibility
- Disable the collisions
- Update a player's team with immediate effect (he doesn't need to reconnect)
- Update an already existing team's info (like prefix, color..) with immediate effect (players do not need to reconnect)

But you can't;
- Toggle friendly fire and see invisible friends properties (those settings are managed server side so it would be a pain to re-code it)

Here are some examples of uses;
1) Create a Team (maybe in your plugin enable)
```java
TeamAPI.Team apiTeam = new TeamAPI.TeamBuilder("red")
        .setDisplayName("Red")
        .setPrefix("Red | ")
        .setColor(PacketsUtils.ChatColor.RED)
        .setSortOrder(10)
        .setCollisions(TeamsPackets.Collisions.NEVER)
        .create();
boolean isRegistered = SpigotApi.getTeamApi().registerTeam(apiTeam);
if(isRegistered){
    System.out.println("New team registered!");
} else {
    System.err.println("The api failed to register your team");
}
```
2) Then set a player inside this team when he joins
```java
@EventHandler
public void OnPlayerJoinSetTeam(PlayerJoinEvent e)
{
  SpigotApi.getTeamApi().addPlayer("red", e.getPlayer().getName());
}
```
PS: The API does not save the player's team when he leaves! 

3) You can create a blue team, but if for some reason you want it to appear below the red team in the tab (by default the **b**lue will appear before **r**ed as the team are sorted by their ascii values). The TeamAPI handles that for you! Just set a sort order bigger than the red team and voila! (If you create a rank system, maybe keep 5-10 values between each team so you can add a new one later without the need to shift all the others)
   
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/be3d4532-b1ed-4e53-b454-3db98592662d)

### ScoreboardAPI
This API registers scoreboards and display them to players. This API is powerful thanks to *placeholders*. You can create your scoreboard's lines with some default values and then update each placeholder with a different value for each player, at any time, you can re-re-update the value! You can also change a player's scoreboard with immediate effect (he does not need to reconnect)

Here are some examples of uses;
1) Register a new scoreboard
```java
ArrayList<ScoreboardLine> lines = new LinesBuilder()
        .add("empty1", 0, "§0")
        .add("playername", 1, "§eYour name: §6{PLAYER}")
        .add("empty2", 2, "§1")
        .add("playermoney", 3, "§eYour money: §6{MONEY}§e$")
        .getLines();

SpigotApi.getScoreboardApi().registerScoreboard("defaultBoard", "§bMyServer", lines); //first parameter is the intern name, which you can use to retrieve this scoreboard later on. 2nd parameter is the displayed title on the scoreboard
```
In 1.20.2 and newer you can set blank lines with an empty string (i.e: .add("empty1", 0, "").add("empty2", 2, "")). This is because from the 1.20.2 NMS changed how scoreboard is displaying scores internally. The unique name of the score and the formatted text are two separate values. \
"player" and "money" are now placeholders (dont worry you can still write *money* without the {} and the value wont be touched) which you can edit later on.

2) Set the scoreboard to a player and update the placeholders
```java
@EventHandler
public void OnPlayerJoinSetTeam(PlayerJoinEvent e)
{
    Player player = e.getPlayer();
    PlayerScoreboard board = SpigotApi.getScoreboardApi().setScoreboard(player, "defaultBoard");
    board.setPlaceholder("player", player.getName());
    board.setPlaceholder("{MONEY}", String.valueOf(10));
}
```
As you can see you can update placeholders with their name or with their original form (the two lines are working perfectly) \
Like the TeamAPI, the ScoreboardAPI does not save the current player's scoreboard if he leaves.

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/099c0df6-9344-4b52-9b6d-d6a8b16b3b2b)

3) Update the placeholders later on
```java
@EventHandler
public void MyCustomMoneyEvent(PlayerMoneyChangeEvent e)
{
    Player player = e.getPlayer();
    PlayerScoreboard board = SpigotApi.getScoreboardApi().getScoreboard(player);
    if(board != null && board.getParentBoard().getName().equals("defaultBoard"))
    {
        board.setPlaceholder("money", String.valueOf(e.getNewBalance()));
    }
}
```
4) Remove or change the entire scoreboard
```java
Player player = e.getPlayer();
SpigotApi.getScoreboardApi().setScoreboard(player, null); //null remove any scoreboard currently displayed with immediate changes (as you call this line, the player's scoreboard is gone)
//or you can set another scoreboard's intern name instead of null and the scoreboard will switch
```

### NPCApi
This API allows you to create NPC quickly and easily. Change their position, set them a skin, track the player eyes and add a click listener on him! You can combine the TeamApi and this Api to put the NPCs on a team, to display prefixes, remove colisions, and so on..!

NPC do spawn when the player joins the world. If he quits the world the NPC is destroyed on the client, and if he comes back then the NPC is recreated. Everything is handled for you! You just need to register the NPC once and voila! Even if you decide to spawn (despawn) a NPC after a player joined, the NPC will be created (destroyed) immediatly (the player do not need to change world/reconnect). You can even show/hide an already spawned NPC with a simple method call!

Here are some examples of uses;
1) Create a NPC
```java
Location spawn = new Location(Bukkit.getWorld("world"), 0.5f, 100.0f, 0.5f);
NPCApi.OnNPCInteract clickHandler = (npc, player, clickType) -> {
    if(clickType == ClickType.LEFT) {
        player.sendMessage("§cHey!! Do not hurt me!");
    } else { //right click
        player.sendMessage("§eHello §6" + player.getName() + "§e, I am " + npc.getName() + " and I have a special quest for you!");
    }
};
NPCApi.NPC myNPC = new NPCApi.NPC(UUID.randomUUID(), "Bob", NPCApi.getProperty("Luca008"), spawn, 10.0, true, clickHandler);
SpigotApi.getNpcApi().registerNPC(myNPC);
```

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/fd1356a0-92d1-4529-8409-bae6e70de742)

