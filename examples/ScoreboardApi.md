# ScoreboardAPI
This API registers scoreboards and display them to players. This API is powerful thanks to *placeholders*. You can create your scoreboard's lines with some default values and then update each placeholder with a different value for each player, at any time, you can re-re-update the value! You can also change a player's scoreboard with immediate effect (he does not need to reconnect)

## Register a new scoreboard
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

## Set the scoreboard to a player and update the placeholders
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

## Update the placeholders later on
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
## Remove or change the entire scoreboard
```java
Player player = e.getPlayer();
SpigotApi.getScoreboardApi().setScoreboard(player, null); //null remove any scoreboard currently displayed with immediate changes (as you call this line, the player's scoreboard is gone)
//or you can set another scoreboard's intern name instead of null and the scoreboard will switch
```