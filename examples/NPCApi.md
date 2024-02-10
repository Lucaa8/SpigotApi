# NPCApi
This API allows you to create NPC quickly and easily. Change their position, set them a skin, track the player eyes and add a click listener on him! You can combine the TeamApi and this Api to put the NPCs on a team, to display prefixes, remove colisions, and so on..!

NPC do spawn when the player joins the world. If he quits the world the NPC is destroyed on the client, and if he comes back then the NPC is recreated. Everything is handled for you! You just need to register the NPC once and voila! Even if you decide to spawn (despawn) a NPC after a player joined, the NPC will be created (destroyed) immediatly (the player do not need to change world/reconnect). You can even show/hide an already spawned NPC with a simple method call!

## Create a NPC
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

## Get back your NPC
With the id you stored, you can get back your NPC at any time and hide it or change his location for example
```java
NPCApi.NPC myNPC = SpigotApi.getNpcApi().getNpcById(npcId);
//myNPC.setActive(false); //Remove the NPC for all players
myNPC.setLocation(myNPC.getLocation().add(1.2f, 0f, 0f)); //add 1.2 block to his x coordinate
```
You can change the NPC's location even if it's not active the change takes effect immediatly if you call NPC#getLocation(), and the NPC will be spawned at the new position

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/8a184de7-9266-4244-8abe-fef05352f28d)

## Add your NPC in a Team
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
