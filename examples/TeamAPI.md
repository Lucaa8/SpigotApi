# TeamAPI
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
## Create a Team
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
## Add players in the Team
For example when he joins the server
```java
@EventHandler
public void OnPlayerJoinSetTeam(PlayerJoinEvent e)
{
  SpigotApi.getTeamApi().addPlayer("red", e.getPlayer().getName(), true); //Check section 4) to learn more about the 3rd param
}
```
PS: The API does not save the player's team when he leaves!

## Tab order of Teams
You can create a blue team, but if for some reason you want it to appear below the red team in the tab (by default the **b**lue will appear before **r**ed as the team are sorted by their ascii values). The TeamAPI handles that for you! Just set a sort order bigger than the red team and voila! (If you create a rank system, maybe keep 5-10 values between each team so you can add a new one later without the need to shift all the others)

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/be3d4532-b1ed-4e53-b454-3db98592662d)

## Change or remove a player's team

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