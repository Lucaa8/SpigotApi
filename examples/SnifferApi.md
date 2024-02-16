# SnifferApi
The SnifferAPI allow the developper to handle any packet sent by the client (the game) to the server. You can read the packet's content or even discard the packet so that Spigot/Paper doesn't even know about it.

## Disclaimer
This API is designed for internal use only but because I know some people will try to use it, I'll write a few lines of code so that you understand this API and can use it correctly. You do not want to use this API if you're not familiar with the Minecraft server package (called _NMS_ for `net.minecraft.server`). You will use low level code and can mess up your whole server if you do not use it correctly. It can also not be compatible if you change your server's version (unless you use reflection or some tools to obf/unobf spigot mappings). For these reasons, **I'm not responsible for any crash or bug related with my plugin SpigotApi**.

## Start handling packets
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

## Stop handling packets
```java
SpigotApi.getMainApi().players().stopHandling(player, "myplugin_myhandler");
```
It won't crash if there's no handler with this name, instead of that, it will be ignored silently.

## Handler callback example
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

![snifferapi](https://github.com/Lucaa8/SpigotApi/assets/47627900/09d6a5a0-d0c7-445f-b749-7decb00b956f)

In real scenarios you would open an anvil window with your own packet and ID, put an item inside the first slot, set a message in the text field, etc... But here the goal was to show how to use the SnifferAPI and not how to use _NMS_.
