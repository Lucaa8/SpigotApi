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
In the Item object there are some helpers to create the bukkit itemstack corresponding to your Item. In these methods you can specify how many of this itemstack you want to give and also to which player. You'll see later in this file that you can create items with [skull meta](#skull-meta) for example, which can be a dynamic meta which adapts to the player who will receive this item. You'll also be able to give directly the item to any online player. The method will check if this player can hold the specified amount in his inventory and if not the remaining items will drop on the ground on the player's location.

But there's a problem with the potential NBTs (Item Meta) you put inside the item (maybe display name, lore, etc...) because those are lost when a block is placed. If they are broken then picked up they won't stack with the original itemstack because their NBT now differ. To avoid that, you would keep the NBT (Item Meta) on items like swords, armors, etc... but remove it from blocks that may be placed and broke. A method let you give itemstacks without NBTs to avoid this problem.

```java
Item item = ...;
Player player = ...;
//With the Item#toItemStack(amount<, player>) the amount is max 64. (1 stack of 64)
ItemStack itemStack = item.toItemStack(32);
ItemStack itemStack1 = item.toItemStack(64, player); //for custom meta like skull
//With the Item#toItemStacks(amount<, player>) the amount can be greater than 64.
//E.g if you pass amount=129 then you would get the 2 first array entries with 1 stack of 64 each and a third entry with 1 stack of 1.
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
Item item = new ItemBuilder()
        .setUid("custom_sword")
        .setMaterial(Material.DIAMOND_SWORD)
        .setName("§bCustom Sword")
        .setDamage(500) //setDamage may lead to confusion bc in fact the sword will have 500/1561 durability. 
        //.setCustomData(3) //if you're using custom texture pack then you can change the texture of this item with this number
        .setRepairCost(15) //base repair cost
        //--Lore--
        //.setLore(List.of("§aLore line 1", "§aLore line 2"))
        .setLore("§aLore line 1\n§aLore line 2") //will produce the same as the above line
        //--ItemFlags--
        //.setFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
        .addFlag(ItemFlag.HIDE_ATTRIBUTES)
        //--Enchantments--
        //.setEnchantList(List.of(new Enchant(Enchantment.FIRE_ASPECT, 1), new Enchant(Enchantment.DAMAGE_ALL, 3)))
        .addEnchant(new Enchant(Enchantment.FIRE_ASPECT, 1))
        .addEnchant(Enchantment.DAMAGE_ALL, 3) //Without the constructor
        //--Attributes--
        .addAttribute(Attribute.GENERIC_ATTACK_SPEED, "attack_speed", 10.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
        //.addAttribute(Attribute.GENERIC_ATTACK_SPEED, "attack_speed", 10.5) //Operation would be ADD_NUMBER and slot null
        .createItem();

ItemStack itemstack = item.toItemStack(1);
//or
item.giveOrDrop(player, 1);
//you would use giveOrDrop and not giveOrDropWithoutNBT because you would lose all the enchants, durability, etc..
```

[item_sword](https://github.com/Lucaa8/SpigotApi/assets/47627900/0c894b46-9568-42b7-ad97-e425dc8639ed)

You can clearly see that the above meta is present on this sword. The item's attribute (attack speed) is hidden on the sword but when swinging it's possible to see that the timer reset way more quicker than a normal sword. The sword got the 500/1561 durability, etc...

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/2c9a8e43-134c-48ec-a225-70601715eee7)

In this picture we can see the base repair cost was indeed 15 but +1 for every diamond so it increased to 17.


## Potion Meta
This custom potion meta let you add effects on your potion material in one single line of code. It can be used with POTION, SPLASH_POTION and LINGERING_POTION material.
```java
item = new ItemBuilder()
        .setMaterial(Material.POTION)
        .setName("§cThe Witch's Potion")
        .setMeta(new Potion.PotionBuilder()
                .setMainEffect(Potion.MainEffect.STRONG_SWIFTNESS) //The main effect is optional
                //PotionEffectType, duration (ticks), amplifier(level-1), ambient, particle, icon
                //.addEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, true, true))
                .addEffect(PotionEffectType.BLINDNESS, 100, 0, false, true, true) //without the constructor but same args
                //.addEffectWithIcon(PotionEffectType.FAST_DIGGING, 100, 2, true)
                //PotionEffectType, duration, amplifier, particle. Ambient set to true by default
                .addEffectWithoutIcon(PotionEffectType.FAST_DIGGING, 100, 2, true)
                .setColor(Color.LIME)
                .getPotion())
        .setGlowing(true) //if you want the enchanted effect on the bottle
        .createItem();
```
The Main effect is one the default effect you can retrieve inside the _Food & Drinks_ tab of the creative inventory. **STRONG_** targets the level ii of the potion type (E.g. STRONG_SWIFTNESS means Speed II 1m30). The **LONG_** targets the expanded version of the potion (E.g. LONG_SWIFTNESS means Speed I 8m). Those are not very customizable so this is an optinal effect. If you do not set one, the Potion will be of type **UNCRAFTABLE** but you can still fully customize the effect displayed on it and given to the player when drank. You can combine default effect (max 1) and many secondary custom effects. The color is always customizable (with and without main effect). 

[PotionMeta](https://github.com/Lucaa8/SpigotApi/assets/47627900/6dc66421-0eb3-4201-bc60-cb8537839ca8)

As you can see, the Speed II 1m30 default effect is applied, as well as the two other secondary customizable effects. The _BLINDNESS_ effect icon is displayed on the upper right part of the screen (when the player is not in his inventory) but the _FAST_DIGGING_ (Efficiency) is not (addEffectWithoutIcon).

## Leather Color Meta
This Meta is only for Leather armor pieces and not for [trimming](#trim-armor-meta). With the Leather Color Meta you can set the default color of Minecraft or any customizable RGB value. 
```java
item = new ItemBuilder()
        .setMaterial(Material.LEATHER_CHESTPLATE)
        .setMeta(new LeatherArmor(Color.ORANGE))
        //.setMeta(new LeatherArmor(Color.fromRGB(r, g, b)))
        .createItem();
```

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/87a673e8-ecaa-409c-afd8-8cdf0d082bf5)

If you want to hide the _Color:_ description, you can add the Flag _HIDE_DYE_ to the item's base meta.
```java
...
.setMeta(new LeatherArmor(Color.ORANGE))
.addFlag(ItemFlag.HIDE_DYE)
...
```

## Trim Armor Meta
This Meta is used to create patterns on armors. It can be applied on any armor piece like _IRON_CHESTPLATE_, _DIAMOND_BOOTS_, ...
```java
item = new ItemBuilder()
        .setMaterial(Material.IRON_CHESTPLATE)
        .setMeta(new TrimArmor(TrimMaterial.EMERALD, TrimPattern.COAST))
        .createItem();
```

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/4c662cdd-bb72-48b4-a248-250296887f79)


## Skull Meta
The Skull Meta is a little bit more complex as it can be applied dynamically on any player. In fact there are three distincts types of applicable meta for the Skulls.

### HEADS-MC
The first and most funny one is the possibility to apply full customizable textures on your skull item! Just hope on [Minecraft Heads](https://minecraft-heads.com/) and choose any texture in the [Custom Heads](https://minecraft-heads.com/custom-heads) tab. When you found one, click on it and scroll down until you find the below section:
![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/190da9b1-e699-43ad-858c-97a50b5002a1)
Finally copy the Value and you are good to go!
```java
String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=";
item = new ItemBuilder()
        .setMaterial(Material.PLAYER_HEAD)
        .setName("§bThe Earth")
        .setMeta(new Skull(Skull.SkullOwnerType.MCHEADS, texture))
        .createItem();
```
The _texture_ variable contains the previous copied Value. \
The enum type name `Skull.SkullOwnerType.MCHEADS` is misleading as the website name is "Head MC".

![image](https://github.com/Lucaa8/SpigotApi/assets/47627900/a3567732-8765-4085-a3b2-7f2c33a2dce7)

### PSEUDO
Basic one, just 

## Book Meta

## Tropical Fish Meta

## Storing your items
