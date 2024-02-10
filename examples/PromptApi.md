# PromptApi
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
