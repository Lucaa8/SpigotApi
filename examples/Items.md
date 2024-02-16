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
Some text

### Example
[item_sword](https://github.com/Lucaa8/SpigotApi/assets/47627900/0c894b46-9568-42b7-ad97-e425dc8639ed)


## Drop without nbt?
