# NBTTagApi
This API does write and read any NBT value in your items. \
What's a NBT ? _Named Binary Tag_ is the way Minecraft stores informations inside an item. Have you ever considered how Minecraft stores the durability of an item ? The answer is NBT! But did you know you can take advantage of those to store whatever you want? If you want to open an interactive inventory to a player and find in no time on which item the player clicked, NBTTagAPI is for you! If you want to add any information inside an item before giving it to the player, NBTTagAPI is also for you!

We'll find out how to use it right now.
## Create an item and stores information inside it
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

## Get back the information you stored
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

## Custom heads
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
