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
  SpigotApi.getTeamApi().addPlayer("red", e.getPlayer().getName(), true); //Check section 4) to learn more about the 3rd param
}
```
PS: The API does not save the player's team when he leaves! 

3) You can create a blue team, but if for some reason you want it to appear below the red team in the tab (by default the **b**lue will appear before **r**ed as the team are sorted by their ascii values). The TeamAPI handles that for you! Just set a sort order bigger than the red team and voila! (If you create a rank system, maybe keep 5-10 values between each team so you can add a new one later without the need to shift all the others)
   
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/be3d4532-b1ed-4e53-b454-3db98592662d)

4) Change or remove a player's team

To remove a player from his team it's actually easy (If the player does not have any team then this line will be ignored silently)
```java
SpigotApi.getTeamApi().removePlayer(player.getName());
```
To change a player's team there are two ways to do it
```java
//1.1) You can remove the player from his current team
SpigotApi.getTeamApi().removePlayer(player.getName());
//1.2) Then you add it in another team without forcing it (if the 3rd param is false and the player is already in a team, this will fail and the player keeps his current team)
SpigotApi.getTeamApi().addPlayer("blue", player.getName(), false);
//2) Or you can force the team change with
SpigotApi.getTeamApi().addPlayer("blue", player.getName(), true); //Which will remove the player from his current team if any and then add it in the blue team
```

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
1) Create a NPC and store his id for later use
```java
private int npcId = -1;
public void onEnable()
{
    Location spawn = new Location(Bukkit.getWorld("world"), 0.5f, 100.0f, 0.5f);
    NPCApi.OnNPCInteract clickHandler = (npc, player, clickType) -> {
        if(clickType == ClickType.LEFT) {
            player.sendMessage("§cHey!! Do not hurt me!");
        } else { //right click
            player.sendMessage("§eHello §6" + player.getName() + "§e, I am " + npc.getName() + " and I have a special quest for you!");
        }
    };
    NPCApi.NPC myNPC = new NPCApi.NPC(UUID.randomUUID(), "Bob", NPCApi.getProperty("Luca008"), spawn, 10.0, true, clickHandler);
    npcId = myNPC.getId();
    SpigotApi.getNpcApi().registerNPC(myNPC);
}
```

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/fd1356a0-92d1-4529-8409-bae6e70de742)

2) Get back your NPC later and hide it or change his location
```java
NPCApi.NPC myNPC = SpigotApi.getNpcApi().getNpcById(npcId);
//myNPC.setActive(false); //Remove the NPC for all players
myNPC.setLocation(myNPC.getLocation().add(1.2f, 0f, 0f)); //add 1.2 block to his x coordinate
```
You can change the NPC's location even if it's not active the change takes effect immediatly if you call NPC#getLocation(), and the NPC will be spawned at the new position

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/8a184de7-9266-4244-8abe-fef05352f28d)

3) Add your NPC in a Team to display prefixes/suffixes and removing the collisions with him

Those code blocks need to be executed only **once**, then you can move it, hide/show it and he will keep his team until his unregister! (Or until you remove it from his team)
```java
//Create the NPC team with the TeamAPI
SpigotApi.getTeamApi().registerTeam(new TeamAPI.TeamBuilder("npc")
    .setDisplayName("NPC")
    .setPrefix("[NPC] ") //Set his prefix
    .setSuffix(" §7(Sidequest)") //Set his suffix (Optional, here I display his quest type)
    .setColor(PacketsUtils.ChatColor.YELLOW)
    .setCollisions(TeamsPackets.Collisions.NEVER)
    .create());
```
```java
//Get your NPC and add it in the team! Soo easy!
NPCApi.NPC myNPC = SpigotApi.getNpcApi().getNpcById(npcId);
SpigotApi.getTeamApi().addPlayer("npc", myNPC.getName(), true);
//SpigotApi.getTeamApi().removePlayer(myNPC.getName()); //To remove the NPC from his current team
```

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/b0a4b4f4-afee-464a-9e05-7e0e3a6a3b1d)

### PromptApi
With this API you can open a sign to any online player on your server with the text of your choice, written in any color of your choice and then collect the result when the player is done editing the sign! \
The Minecraft client sends the same update sign packet when a player clicks on the "done" button or press on his ESC keybind to leave the sign. Because of this it's not possible to have a nice "cancellable" system. It's for this reason this API provides a cancel command which you will understand easily through the following example!

```java
@EventHandler
public void onPlayerMessage(AsyncPlayerChatEvent e)
{
    Player player = e.getPlayer();
    PromptApi.PromptCallback callback = (cancelled, lines, line) -> {
        if(cancelled)
            return;
        float price = Float.parseFloat(lines[1].split(":")[1]);
        if(price > 0){
            getConfig().set("Item1.Price", price);
            player.sendMessage("§aYou changed the price of Item1 to: §b" + price + "§a$!");
        }
    };
    DyeColor linesColor = DyeColor.BLUE;
    String cancelText = "exit";
    SpigotApi.getPromptApi().promptPlayer(player, callback, linesColor, cancelText, "Item1 price", "Price:"+getConfig().get("Item1.Price"), "'exit' on the first", "line to cancel");
}
```
In this example, we send a sign prompt to any player which send any message in the chat. We simulate a config prompt to edit the price of an item inside the default configuration. At first the callback to get the player's response is set, we'll come back to it later. Then we define the lines color (You can not set a different color for each line) on the sign. After that we set a string which will serve as the cancel command. If you set it to "exit" then the player can cancel the prompt by writing "exit" on the first line of the sign. Finally we send the prompt to the player. The four last string parameters are the initial lines put on the sign before it's opened to the player. So now lets see what the player will see!

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/b3a5b552-5ad0-41e2-a1bc-02eda4c10f89)

We can see our initial lines! We can also see a part of the simulated sign in the bottom right side of the screen. It's because the Minecraft client ignores any open sign interface packet if it's not actually linked to a real sign in the world! But no worries, PromptApi handles that for you! First, this sign is client side only, it means only the involved player will see the sign and secondly the sign will be removed (or replaced by any block which was placed here initially) after the player left the sign interface!

Let's edit the price now and then click on the done button (or hit the ESC keybind)

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/9a35a764-bc73-4160-bc5e-5ab024081b4a)

We can see that something happened, it's the callback, which we skipped before who did that. So we can come back to it and check why it happened. \
You have 3 parameters in the callback, cancelled, lines and line.
- __cancelled__ is a boolean which tells you if the player wrote the cancel command on the first line or not. In most cases you want to leave and do nothing if the player cancelled the prompt.
- __lines__ is a string array of length 4. This array contains the text of each separate row of the sign. In our example `lines[0]` would be `Item1 price`, etc.. And it does not contain any line feed nor carriage return at the end. (always of length 4, if the last line is empty on the sign, then `string[3]` is `""`)
- __line__ is a somewhat special string. It contains the four rows appended together but without space or any special character between rows. It means you can not tell which part of the string was on which row. It's for sentence/long words purposes. In our example it would be `Item1 pricePrice:130.3'exit' on the firstline to cancel`

In our case, we return if the prompt is cancelled (player wrote "exit" on the first line), then we get the float value on the second line by splitting at ":" and we set it inside the config if its a "valid" price (positive and not free). We then send the message you can see on the image above which confirm to the player that his change has been successful. 

It's a very basic example to showcase this API, in a real situation you would check if the second line actually contains ":" before splitting, put the float parsing inside try catch, etc... Do not trust user input!

### NBTTagApi 
