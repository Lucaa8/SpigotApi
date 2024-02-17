# Items
This is not an "API" but rather a powerful tool so it's worth presenting it because it can save you a big amount of time and code to create and store Minecraft items. There is a "base" which contains all the standard item meta like _display name_, _lore_, _enchantements_, _custom data_ and more... Then there is some more specific meta like _potion_, _skull_ and more.

## Base Meta
The "Base Meta" is embed inside the SpigotApi "Item" object. Those basic itemmeta things are considered linked directly to the item compared to the standard Bukkit itemstack which contains a an "ItemMeta" interface. Here is an exhaustive list of "Base Meta" things
- [An UID](#uid)
- Material
- [Display Name](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#setDisplayName(java.lang.String))
- [Lore](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#setLore(java.util.List))
- [Enchantments](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#addEnchant(org.bukkit.enchantments.Enchantment,int,boolean))
- [Item Flags](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#addItemFlags(org.bukkit.inventory.ItemFlag...)) (HIDE_ENCHANTS, HIDE_PLACED_ON, ..)
- [Item Attributes](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#addAttributeModifier(org.bukkit.attribute.Attribute,org.bukkit.attribute.AttributeModifier)) (Attack Speed, Flying Speed, etc...)
- An anvil repair cost
- [A custom model data](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/ItemMeta.html#setCustomModelData(java.lang.Integer)) (to display different textures with a resource pack)
- A durability (for items like sword, axe, etc..)
- If invulnerable or not

### UID
The Unique ID of an Item is optionnal. You would use it if you want to reconize the itemstack later on maybe in an inventory click event. You can store a hidden unique name (string) inside the item that players cant see. It's stored as NBT and you can then get back this NBT with the [NBTagApi](https://github.com/Lucaa8/SpigotApi/blob/master/examples/NBTTagApi.md).
```java
@EventHandler
public void OnInventoryClick(InventoryClickEvent e){
    ItemStack clicked = e.getCurrentItem();
    if(clicked == null || clicked.getType() == Material.AIR)
        return;
    NBTTagApi.NBTItem item = SpigotApi.getNBTTagApi().getNBT(clicked);
    if(item.hasTag("UUID") && item.getString("UUID").equals("my_custom_item")){
        //do stuff
    }
}
```
With this, you can check the item clicked without hardcoding the Display Name or something like this.

### Enchantments and Attributes
SpigotApi has is own way to create/store attributes and enchants so they can be written as JSON with the rest of the item's meta. For this reason, you'll need to use the custom classes [Enchant.java](https://github.com/Lucaa8/SpigotApi/blob/master/src/main/java/ch/luca008/SpigotApi/Item/Enchant.java) and [ItemAttribute.java](https://github.com/Lucaa8/SpigotApi/blob/master/src/main/java/ch/luca008/SpigotApi/Item/ItemAttribute.java) of SpigotApi.

### ItemBuilder
Because there is a lot of meta attributes, it would be annoying to create each item with a constructor. It's for this reason that an ItemBuilder has been created to build any item without the nood to give every attribute. See the [Example](#example) section to learn how to use it.

### Give or Drop
In the Item object there are some helpers to create the bukkit itemstack corresponding to your Item. In these methods you can specify how many of this itemstack you want to give and also to which player. You'll see later in this file that you can create items with [skull meta](#skull-meta) for example, and you can create a dynamic meta which adapts to the player who will receive this item. You'll also be able to give directly the item to any player. The method will check if this player can hold the specified amount and if not will drop the remaining items on the ground on the player's location.

But there's a problem with the potential NBTs (Item Meta) you put inside the item (maybe display name, lore, etc...) because those are lost when a block is placed. If they are broken then picked up they won't stack with the original itemstack because their NBT now differ. To avoid that, you would keep the NBT (Item Meta) on items like swords, armors, etc... but remove it from blocks that may be placed and broke. A method let you give itemstacks without NBTs to avoid this problem.

```java
Item item = ...;
Player player = ...;
//With the Item#toItemStack(amount<, player>) the amount is max 64. (1 stack of 64)
ItemStack itemStack = item.toItemStack(32);
ItemStack itemStack1 = item.toItemStack(64, player);
//With the Item#toItemStacks(amount<, player>) the amount can be greater than 64.
//E.g if you pass amount=129 then you would get the 2 first array entries with 1 stack of 64 each and a third entry with 1 stack of 1
ItemStack[] itemStacks = item.toItemStacks(129);
ItemStack[] itemStacks1 = item.toItemStacks(128, player);
ItemStack[] itemStacks2 = item.toItemStacks(32); //ok but the array will contain only 1 entry (itemStacks2[0])
//Give until the player's inventory is full then drop on the ground at player's location
item.giveOrDrop(player, 128);
item.giveOrDropWithoutNBT(player, 128);
```

### Example
Here a small example to teach you how to use the item builder and then the conversion to itemstack.
```java
ItemAttribute attack_speed = new ItemAttribute(
        Attribute.GENERIC_ATTACK_SPEED,
        "attack_speed",
        10.5, //amount
        AttributeModifier.Operation.ADD_NUMBER,
        EquipmentSlot.HAND //optional (can be set to null, if null then attribute apply on each slot)
);

Item item = new ItemBuilder()
        .setUid("custom_sword")
        .setMaterial(Material.DIAMOND_SWORD)
        .setName("§bCustom Sword")
        .setLore(List.of("§aLore line 1", "§aLore line 2"))
        .setFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
        .setEnchantList(List.of(new Enchant(Enchantment.FIRE_ASPECT, 1), new Enchant(Enchantment.DAMAGE_ALL, 3)))
        .setAttributes(List.of(attack_speed))
        .setDamage(500) //setDamage may lead to confusion bc in fact the sword will have 500/1561 durability. 
        //.setCustomData(3) //if you're using custom texture pack then you can change the texture of this item with this number
        .setRepairCost(15) //base repair cost
        .createItem();

ItemStack itemstack = item.toItemStack(1);
//or
item.giveOrDrop(player, 1); //you would use giveOrDrop and not giveOrDropWithoutNBT because you would lose all the enchants, durability, etc..
```
For clarity and keep it "simple" to read I put the item attribute creation outside of the builder but you could put it inside.

[item_sword](https://github.com/Lucaa8/SpigotApi/assets/47627900/0c894b46-9568-42b7-ad97-e425dc8639ed)

You can clearly see that the above meta is present on this sword. The item's attribute (attack speed) is hidden on the sword but when swinging it's possible to see that the timer reset way more quicker than a normal sword. The sword got the 500/1561 durability, etc...

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/2c9a8e43-134c-48ec-a225-70601715eee7)

In this picture we can see the base repair cost was indeed 15 but +1 for every diamond so it increased to 17.


## Potion Meta
This custom potion meta let you add effects on your potion material in one single line of code. It can be used with POTION, SPLASH_POTION and LINGERING_POTION material.
```java
Item potion = new ItemBuilder()
        .setMaterial(Material.LINGERING_POTION)
        .setName("§cThe Witch's Potion")
        .setMeta(new Potion(
                Potion.MainEffect.STRONG_SWIFTNESS, //Main effect, can be null (it will be UNCRAFTABLE and have no effect)
                List.of(
                    new PotionEffect(PotionEffectType.BLINDNESS, 100, 0), //type, duration (in ticks, 100 means 5secs), amplifier(level)
                    new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 2)
                ), //A list of custom effects (you can set null to the main effect and add your(s) custom effect(s) here as main effects are not customisable)
                Color.BLACK)
        )
        .createItem();
```

## Leather Color Meta

## Skull Meta

## Book Meta

## Tropical Fish Meta

## Storing your items
