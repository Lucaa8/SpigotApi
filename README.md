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
- **Items** - An unique way to create, customize, compare and store items easily without YAML or NBT problems.
  
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
#### Create a Team
For example in your plugin onEnable
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
#### Add players in the Team
For example when he joins the server
```java
@EventHandler
public void OnPlayerJoinSetTeam(PlayerJoinEvent e)
{
  SpigotApi.getTeamApi().addPlayer("red", e.getPlayer().getName(), true); //Check section 4) to learn more about the 3rd param
}
```
PS: The API does not save the player's team when he leaves! 

#### Tab order of Teams
You can create a blue team, but if for some reason you want it to appear below the red team in the tab (by default the **b**lue will appear before **r**ed as the team are sorted by their ascii values). The TeamAPI handles that for you! Just set a sort order bigger than the red team and voila! (If you create a rank system, maybe keep 5-10 values between each team so you can add a new one later without the need to shift all the others)
   
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/be3d4532-b1ed-4e53-b454-3db98592662d)

#### Change or remove a player's team

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

#### Register a new scoreboard
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

#### Set the scoreboard to a player and update the placeholders
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

#### Update the placeholders later on
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
#### Remove or change the entire scoreboard
```java
Player player = e.getPlayer();
SpigotApi.getScoreboardApi().setScoreboard(player, null); //null remove any scoreboard currently displayed with immediate changes (as you call this line, the player's scoreboard is gone)
//or you can set another scoreboard's intern name instead of null and the scoreboard will switch
```

### NPCApi
This API allows you to create NPC quickly and easily. Change their position, set them a skin, track the player eyes and add a click listener on him! You can combine the TeamApi and this Api to put the NPCs on a team, to display prefixes, remove colisions, and so on..!

NPC do spawn when the player joins the world. If he quits the world the NPC is destroyed on the client, and if he comes back then the NPC is recreated. Everything is handled for you! You just need to register the NPC once and voila! Even if you decide to spawn (despawn) a NPC after a player joined, the NPC will be created (destroyed) immediatly (the player do not need to change world/reconnect). You can even show/hide an already spawned NPC with a simple method call!

#### Create a NPC
You need to store his id for later use (`npcId`)
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

#### Get back your NPC
With the id you stored, you can get back your NPC at any time and hide it or change his location for example
```java
NPCApi.NPC myNPC = SpigotApi.getNpcApi().getNpcById(npcId);
//myNPC.setActive(false); //Remove the NPC for all players
myNPC.setLocation(myNPC.getLocation().add(1.2f, 0f, 0f)); //add 1.2 block to his x coordinate
```
You can change the NPC's location even if it's not active the change takes effect immediatly if you call NPC#getLocation(), and the NPC will be spawned at the new position

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/8a184de7-9266-4244-8abe-fef05352f28d)

#### Add your NPC in a Team
You can add your NPC in a team to display prefixes/suffixes or removing the collisions with him. \
Those code blocks need to be executed only **once**, then you can move it, hide/show it and he will keep his team until he's unregistered! (Or until you remove it from his team)
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
You have 3 parameters in the callback, cancelled, lines and line; \
&nbsp;&nbsp;&nbsp;**cancelled** is a boolean which tells you if the player wrote the cancel command on the first line or not. In most cases you want to leave and do nothing if the player cancelled the prompt. \
&nbsp;&nbsp;&nbsp;**lines** is a string array of length 4. This array contains the text of each separate row of the sign. In our example `lines[0]` would be `Item1 price`, etc.. And it does not contain any line feed nor carriage return at the end. (always of length 4, if the last line is empty on the sign, then `string[3]` is `""`) \
&nbsp;&nbsp;&nbsp;**line** is a somewhat special string. It contains the four rows appended together but without space or any special character between rows. It means you can not tell which part of the string was on which row. It's for sentence/long words purposes. In our example it would be `Item1 pricePrice:130.3'exit' on the firstline to cancel`

In our case, we return if the prompt is cancelled (player wrote "exit" on the first line), then we get the float value on the second line by splitting at ":" and we set it inside the config if its a "valid" price (positive and not free). We then send the message you can see on the image above which confirm to the player that his change has been successful. 

It's a very basic example to showcase this API, in a real situation you would check if the second line actually contains ":" before splitting, put the float parsing inside try catch, etc... Do not trust user input!

### NBTTagApi 
This API does write and read any NBT value in your items. \
What's a NBT ? _Named Binary Tag_ is the way Minecraft stores informations inside an item. Have you ever considered how Minecraft stores the durability of an item ? The answer is NBT! But did you know you can take advantage of those to store whatever you want? If you want to open an interactive inventory to a player and find in no time on which item the player clicked, NBTTagAPI is for you! If you want to add any information inside an item before giving it to the player, NBTTagAPI is also for you!

We'll find out how to use it right now.
#### Create an item and stores information inside it
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent e)
{
    Player player = e.getPlayer();
    ItemStack stone = new ItemStack(Material.STONE, 1);

    NBTTagApi.NBTItem nbtStone = SpigotApi.getNBTTagApi().getNBT(stone);
    nbtStone.setTag("Owner", player.getName()).setTag("CreatedAt", System.currentTimeMillis());

    stone = nbtStone.getBukkitItem();
    player.getInventory().addItem(stone);
}
```
As you can see, the `setTag` method of `NBTItem` is a builder so you can chain statements. \
Whenever you're done adding your tags you need to assign back the itemstack `stone = nbtStone.getBukkitItem()` or it wont work. Yes it's a little bit boring and you might forget it. The builder is here for you. You can chain everything!
```java
ItemStack stone = SpigotApi.getNBTTagApi().getNBT(
        new ItemStack(Material.STONE, 1)
  )
  .setTag("Owner", player.getName())
  .setTag("CreatedAt", System.currentTimeMillis())
  .getBukkitItem();
```
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/bf36d03f-c964-441b-b780-ddb274383cc4)

#### Get back the information you stored
```java
Player player = Bukkit.getPlayer("Luca008");
ItemStack retrieveItem = player.getInventory().getItemInMainHand();
NBTTagApi.NBTItem nbtItem = SpigotApi.getNBTTagApi().getNBT(retrieveItem);

if(nbtItem.hasTag("Owner"))
    player.sendMessage("§aThe owner of your held item is §b" + nbtItem.getString("Owner"));
else
    player.sendMessage("§cThis item has no owner!");
```
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/7ef9e4e9-8ade-4fb7-8c76-acf2e230a335)

#### Custom heads
With this API you can easily create custom player heads. You can get the textures from [Minecraft Heads](https://minecraft-heads.com/custom-heads) website. Just click on your favorite custom head, find the "For Developers:" section and copy the value block. Then execute this code
```java
ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);

NBTTagApi.NBTItem nbtHead = SpigotApi.getNBTTagApi().getNBT(head);
String value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0ODUwMzFiMzdmMGQ4YTRmM2I3ODE2ZWI3MTdmMDNkZTg5YTg3ZjZhNDA2MDJhZWY1MjIyMWNkZmFmNzQ4OCJ9fX0=";
nbtHead.addSkullTexture("Someone", value);

head = nbtHead.getBukkitItem();
player.getInventory().addItem(head);
```
(If you want another name for the skull, you'll need to add an item meta) \
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/6849bd1c-c652-4313-a0ff-474a446ab81b)

### SnifferApi
The SnifferAPI allow the developper to handle any packet sent by the client (the game) to the server. You can read the packet's content or even discard the packet so that Spigot/Paper doesn't even know about it.

#### Disclaimer
This API is designed for internal use only but because I know some people will try to use it, I'll write a few lines of code so that you understand this API and can use it correctly. You do not want to use this API if you're not familiar with the Minecraft server package (called _NMS_ for `net.minecraft.server`). You will use low level code and can mess up your whole server if you do not use it correctly. It can also not be compatible if you change your server's version (unless you use reflection or some tools to obf/unobf spigot mappings). For these reasons, **I'm not responsible for any crash or bug related with my plugin SpigotApi**.

#### Start handling packets
```java
@EventHandler
public void onPlayerJoin(PlayerJointEvent e)
{
    Player player = e.getPlayer();

    MainApi.PacketReceived callback = (packet, cancel) -> {};

    SpigotApi.getMainApi().players().startHandling(player, "myplugin_myhandler", callback);
}
```
If you use this feature, please put your plugin's name before the name of your handler. For example for SpigotAPI it could be "SpigotApi_customhandler". It allows to have the same handler name for multiples plugins. \
If there's already any handler for this player with the same name provided, you'll get an error log in the server's console and your handler wont be registered.

#### Stop handling packets
```java
SpigotApi.getMainApi().players().stopHandling(player, "myplugin_myhandler");
```
It won't crash if there's no handler with this name, instead of that, it will be ignored silently.

#### Handler callback example
In this quick and simple example we'll simulate an anvil input text. Whevener a player is adding/removing any character from an item's name inside an anvil, the `PacketPlayInItemName` is sent to the server with inside it, the new item's name. If I write "Hello world!" in the anvil's text field, I will get the following packets: "H", "He", ..., "Hello world!". From that, we can add a terminator character to detect when the player is done editing.
```java
MainApi.PacketReceived callback = (packet, cancel) -> {
    if(packet instanceof PacketPlayInItemName p) {
        String input = p.a();
        if(input.endsWith("\\0"))
        {
            ApiPacket.create(new PacketPlayOutCloseWindow(0)).send(player);
            player.sendMessage("§aYou entered the following message: §b" + input.substring(0, input.length()-2));
        }
    }
};
```
In this example, I'm using a NULL terminator like in C, but you can put anything you want. Then I create a `PacketPlayOutCloseWindow` and send it to the player so that the anvil window is closed. Then I send to the player the text he wrote inside the anvil, minus de NULL terminator.
![snifferapi](https://github.com/Lucaa8/SpigotApi/assets/47627900/c1d36c84-1710-451f-b66b-27304585abff) \
In real scenarios you would open an anvil window with your own packet and ID, put an item inside the first slot, set a message in the text field, etc... But here the goal was to show how to use the SnifferAPI and not how to use _NMS_.

### JSONApi
With this API you can write and read basic information inside JSON files. The API let you chain keys to write inside any deeper JSON Object without the need to create section or whatever like with the Bukkit YAML methods.
#### Writer
The Writer is used to store information inside a JSON Object and then write it in any file. **All the directories to the desired file need to exist.** You can start a writer from an existing JSONObject (JSON simple library) or an empty JSON.
```java
JSONApi.JSONWriter writer = SpigotApi.getJSONApi().getWriter(null);
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
#### Reader
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

#### Pretty Printing
If you want to print some of this JSON in the console for debbuging purposes you can use the static `JSONApi#prettyJson` method.
```java
System.out.println(JSONApi.prettyJson(writer.asJson()));
System.out.println(JSONApi.prettyJson(reader.asJson()));
```

### Items
