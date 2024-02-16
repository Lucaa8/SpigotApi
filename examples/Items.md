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

### Give (with or without player) or Drop (without nbt?)
How to give or drop the item (explain the difference between toitemstack(s)(amount, <player>?) and the drop and drop without nbt (why?)

### Example
Base meta item creation example and then drop/give example
[item_sword](https://github.com/Lucaa8/SpigotApi/assets/47627900/0c894b46-9568-42b7-ad97-e425dc8639ed)

## Potion Meta
just a quick description and then paste the example

## Leather Color Meta

## Skull Meta

## Book Meta

## Tropical Fish Meta
