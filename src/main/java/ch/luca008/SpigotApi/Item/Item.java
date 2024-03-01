package ch.luca008.SpigotApi.Item;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.Item.Meta.Book;
import ch.luca008.SpigotApi.Item.Meta.Meta;
import ch.luca008.SpigotApi.Item.Meta.MetaLoader;
import ch.luca008.SpigotApi.Item.Meta.Skull;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/*
//!\\
Meta/Skull contains NBT
This/giveOrDopWithoutNBT contains NBT
//!\\
 */

public class Item {

    private final String uid;
    private Material material;
    private String name;
    private List<String> lore;
    private List<Enchant> enchantList;
    private List<ItemFlag> flags;
    private List<ItemAttribute> attributes;
    private Meta meta;
    private int repairCost;//ItemStack editedCost = new NBTTag(i).setTag("RepairCost",34).getBukkitItem();
    private int customData;
    //misleading name, in reality this attribute stores the durability. E.g. damage=500 then the item would have 500/1561 (for a diamond sword)
    private int damage;//((Damageable)i.getItemMeta()).setDamage(i.getType().getMaxDurability()-{int:durability});
    private boolean invulnerable;

    public Item(String uid, Material material, String name, List<String> lore, List<Enchant> enchantList, List<ItemFlag> flags, List<ItemAttribute> attributes, Meta meta, int repairCost, int customData, int damage, boolean invulnerable) {
        this.uid = uid;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.enchantList = enchantList;
        this.flags = flags;
        this.attributes = attributes;
        this.meta = meta;
        this.repairCost = repairCost;
        this.customData = customData;
        this.damage = damage;
        this.invulnerable = invulnerable;
    }

    public static Item fromJson(JSONApi.JSONReader reader){
        ItemBuilder item = new ItemBuilder();
        if(reader.c("Id"))item.setUid(reader.getString("Id"));
        try{
            item.setMaterial(Material.valueOf(reader.getString("Material")));
        }catch(Exception e){
            Logger.warn("Can't load item '"+(reader.c("Id")?reader.getString("Id"):"unknown")+"' because bukkit couldn't find Material '"+reader.getString("Material")+"'.", Item.class.getName());
            Logger.warn("The present item will be replaced by a simple stone block until it gets fixed.", Item.class.getName());
            return new ItemBuilder().setMaterial(Material.STONE).createItem();
        }
        if(reader.c("Name"))item.setName(reader.getString("Name"));
        if(reader.c("RepairCost"))item.setRepairCost(reader.getInt("RepairCost"));
        if(reader.c("CustomData"))item.setCustomData(reader.getInt("CustomData"));
        if(reader.c("Lore")){
            List<String> l = reader.getArray("Lore").stream().map(Object::toString).toList();
            item.setLore(l);
        }
        if(reader.c("Enchants")){
            reader.getArray("Enchants").forEach(o->item.addEnchant(new Enchant(((JSONObject)o).toJSONString())));
        }
        if(reader.c("Attributes")){
            reader.getArray("Attributes").forEach(o->item.addAttribute(new ItemAttribute(((JSONObject)o).toJSONString())));
        }
        if(reader.c("Flags")){
            reader.getArray("Flags").forEach(o->item.addFlag(ItemFlag.valueOf((String)o)));
        }
        if(reader.c("ItemMeta")){
            item.setMeta(new MetaLoader().load(reader.getJson("ItemMeta").asJson()));
        }
        if(reader.c("Durability"))item.setDamage(reader.getInt("Durability"));
        if(reader.c("Invulnerable"))item.setIsInvulnerable(reader.getBool("Invulnerable"));
        return item.createItem();
    }

    public static Item fromJson(String json){
        try {
            JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader((JSONObject) new JSONParser().parse(json));
            return fromJson(r);
        } catch (ParseException e) {
            return null;
        }
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(uid!=null&&!uid.isEmpty())j.put("Id",uid);
        j.put("Material",material.name());
        if(name!=null&&!name.isEmpty())j.put("Name",name);
        if(repairCost>0)j.put("RepairCost", repairCost);
        if(customData>0)j.put("CustomData", customData);
        if(lore!=null&&!lore.isEmpty()){
            JSONArray jarr = new JSONArray();
            lore.forEach(l->jarr.add(l));
            j.put("Lore",jarr);
        }
        if(enchantList!=null&&!enchantList.isEmpty()){
            j.put("Enchants",Enchant.listToJson(enchantList));
        }
        if(attributes!=null&&!attributes.isEmpty()){
            j.put("Attributes",ItemAttribute.listToJson(attributes));
        }
        if(flags!=null&&!flags.isEmpty()){
            JSONArray jarr = new JSONArray();
            for(ItemFlag f : flags){
                jarr.add(f.name());
            }
            j.put("Flags",jarr);
        }
        if(meta!=null){
            j.put("ItemMeta",new MetaLoader().unload(meta));
        }
        if(damage>0){
            j.put("Durability",damage);
        }
        if(invulnerable)j.put("Invulnerable",true);
        return j;
    }

    /**
     * Create an ItemStack from this Item. It includes UID, Damage and RepairCost as NBTTags, Display Name, Lore, etc... as ItemMeta and all extras Meta like Skull, Book, etc..
     * Similar to {@link #toItemStacks(int, OfflinePlayer)} but amount cannot exceed 64
     * @param player If this Item contains Meta like {@link Skull} or {@link Book} then we can apply the custom Meta for the given player. If null then the Meta wont be applied.
     * @param amount An amount between 1 and 64 inclusive
     * @return This Item as an Minecraft ItemStack with an amount between 1 and 64.
     */
    public ItemStack toItemStack(int amount, @Nullable OfflinePlayer player){
        if(amount>64)amount=64;
        if(amount<1)amount=1;
        ItemStack item = new ItemStack(material, amount);
        if(hasItemMeta()) item.setItemMeta(getItemMeta());
        if(meta!=null){
            ItemStack newItem = meta.apply(item);
            if(newItem == null) {
                Logger.error("Something went wrong while applying the custom item meta ("+meta.getClass().getName()+").", Item.class.getName());
            } else {
                item = newItem;
                if(player!=null){
                    if(meta instanceof Skull){
                        Skull s = (Skull)meta;
                        if(s.getOwningType()==Skull.SkullOwnerType.PLAYER){
                            item = ((Skull)meta).applyOwner(item, player.getUniqueId());
                        }
                    }
                    else if(meta instanceof Book){
                        item = ((Book)meta).applyForPlayer(item, player.getName());
                    }
                }
            }
        }
        NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
        if(repairCost>0)nbt.setTag("RepairCost",repairCost);
        if(uid!=null&&!uid.isEmpty())nbt.setTag("UUID", uid);
        return nbt.getBukkitItem();
    }

    /**
     * See {@link #toItemStack(int, OfflinePlayer)}
     */
    public ItemStack toItemStack(int amount){
        return toItemStack(amount, null);
    }

    /**
     * Similar to {@link #toItemStack(int, OfflinePlayer)}} but amount can exceed 64
     * @param amount any amount > 0 (can exceed 64)
     * @return An array of stacks. If amount is less or equal to 64 the array will contain the only stack at index 0
     */
    public ItemStack[] toItemStacks(int amount, @Nullable OfflinePlayer player){
        if(amount<=64)return new ItemStack[]{toItemStack(amount,player)};
        List<ItemStack> items = new ArrayList<>();
        ItemStack brut = toItemStack(64, player);
        while(amount>0){
            if(amount>64){
                items.add(brut.clone());
                amount-=64;
            }else{
                ItemStack lessAmount = brut.clone();
                lessAmount.setAmount(amount);
                items.add(lessAmount);
                amount=0;
            }
        }
        return items.toArray(new ItemStack[0]);
    }

    /**
     * See {@link #toItemStacks(int, OfflinePlayer)}
     */
    public ItemStack[] toItemStacks(int amount){
        return toItemStacks(amount, null);
    }

    public boolean hasItemMeta(){
        return ((name!=null&&!name.isEmpty())
                ||(lore!=null&&!lore.isEmpty())
                ||(enchantList!=null&&!enchantList.isEmpty())
                ||(flags!=null&&!flags.isEmpty())
                ||(attributes!=null&&!attributes.isEmpty())
                ||invulnerable
                ||customData>0
                ||damage>0
                ||repairCost>0
                ||(uid!=null&&!uid.isEmpty())
                ||hasMeta());
    }

    public ItemMeta getItemMeta(){
        ItemMeta im = Bukkit.getItemFactory().getItemMeta(material);
        if(name!=null&&!name.isEmpty())im.setDisplayName(name.replace("&","§"));
        if(lore!=null&&!lore.isEmpty())im.setLore(lore);
        if(enchantList!=null){
            for(Enchant e : enchantList){
                im.addEnchant(e.getEnchantment(), e.getLevel(), true);
            }
        }
        if(flags!=null&&!flags.isEmpty())flags.forEach(im::addItemFlags);
        if(attributes!=null&&!attributes.isEmpty())attributes.forEach(a->im.addAttributeModifier(a.getAttribute(), a.getAttributeModifier()));
        if(invulnerable)im.setUnbreakable(true);
        if(customData>0)im.setCustomModelData(customData);
        if(damage>0)((Damageable)im).setDamage(material.getMaxDurability()-damage);
        return im;
    }

    /**
     * See {@link #isSimilar(ItemStack, OfflinePlayer)}
     */
    public boolean isSimilar(ItemStack item){//à tester lol
        return isSimilar(item,null);
    }

    /**
     * Compare the item argument with info in this item class
     * <p>
     * Material type
     * <p>
     * Display name
     * <p>
     * Lore
     * <p>
     * Item flags
     * <p>
     * Enchantments
     * <p>
     * Attributes modifiers
     * <p>
     * Unbreakable
     * <p>
     * CustomModelData (Textures pack)
     * <p>
     * Damage (Durability)
     * <p>
     * RepairCost (Initial reparation cost for the item into the anvil)
     * <p>
     * A special UUID field into the item's nbttags (Name: UUID)
     * <p>
     * Meta: Skull, Book, TropicalFishBucket, LeatherArmor and Potions(Splash and Lingering included), TrimArmor
     * @param item The itemstack to compare. Can contain meta like skull, book(even with {P} balises for playername), nbttags like custom data, repaircost, damage, etc
     * @param player A potential player who can be used in {@link Meta#hasSameMeta(ItemStack, OfflinePlayer)}. Can be null without throwing exceptions
     * @return if the itemstack match the current item
     */
    public boolean isSimilar(ItemStack item, @Nullable OfflinePlayer player){
        if(item==null||item.getType()==Material.AIR)return false;
        if(item.getType()!=material)return false;
        if(item.hasItemMeta()!=hasItemMeta())return false;
        if(item.hasItemMeta()){
            ItemMeta itemMeta1 = item.getItemMeta(), itemMeta2 = getItemMeta();
            if(!StringUtils.equals(itemMeta1.getDisplayName(), itemMeta2.getDisplayName()))return false;
            if(!ch.luca008.SpigotApi.Utils.StringUtils.equalLists(itemMeta1.getLore(), itemMeta2.getLore()))return false;
            if(!compareFlags(itemMeta1.getItemFlags(),itemMeta2.getItemFlags()))return false;
            if(!(itemMeta1.hasEnchants()?itemMeta2.hasEnchants()&&itemMeta2.getEnchants().equals(itemMeta1.getEnchants()):!itemMeta2.hasEnchants()))return false;
            if(!(itemMeta1.hasAttributeModifiers()?itemMeta2.hasAttributeModifiers()&&compareModifiers(itemMeta1.getAttributeModifiers(),itemMeta2.getAttributeModifiers()):!itemMeta2.hasAttributeModifiers()))return false;
            if(itemMeta1.isUnbreakable()!=itemMeta2.isUnbreakable())return false;
            if(!(itemMeta1.hasCustomModelData()?itemMeta2.hasCustomModelData()&&itemMeta2.getCustomModelData()==itemMeta1.getCustomModelData():!itemMeta2.hasCustomModelData()))return false;
            if(!(itemMeta1 instanceof Damageable) && itemMeta2 instanceof Damageable)return false;
            if(!(itemMeta2 instanceof Damageable) && itemMeta1 instanceof Damageable)return false;
            if(itemMeta1 instanceof Damageable){
                Damageable d1 = (Damageable) itemMeta1, d2 = (Damageable) itemMeta2;
                if(!(d1.hasDamage()?d2.hasDamage()&&d1.getDamage()==d2.getDamage():!d2.hasDamage()))return false;
            }
            NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
            int itemCost = nbt.hasTag("RepairCost")?Integer.parseInt(nbt.getTag("RepairCost").toString()):0;
            if(!(repairCost>0?repairCost==itemCost:itemCost==0))return false;
            String itemUid = nbt.hasTag("UUID")?nbt.getString("UUID"):null;
            if(!(uid!=null&&!uid.isEmpty()?itemUid!=null&&!itemUid.isEmpty()&&uid.equals(itemUid):(itemUid==null||itemUid.isEmpty())))return false;
            boolean hasMeta = Meta.hasMeta(item);
            if(!(hasMeta()?hasMeta&&meta.hasSameMeta(item,player):!hasMeta))return false;//check les meta potion, armure cuir, fish bucket, skull
        }
        return true;
    }

    private void _giveOrDrop(Player p, ItemStack[] items){
        PlayerInventory inv = p.getInventory();
        for(ItemStack item : items){
            Map<Integer, ItemStack> full = inv.addItem(item);
            if (!full.isEmpty()) {
                Location loc = p.getLocation();
                for(Map.Entry<Integer, ItemStack> extra : full.entrySet()) {
                    p.getWorld().dropItem(loc, extra.getValue());
                }
            }
        }
    }

    /**
     * This method will convert this Item to his ItemStack value and will try to give all stacks to the given player.
     * If this player's inventory is full then the method will drop this remaining items on the player's current location
     * @param p The player
     * @param amount The amount of this item to give. It can exceed 64 to takes multiple inventory slots. (If the player has 3 more free slots and you want to give 256 of this item then 3 stacks will go inside his inventory and one stack will drop on the player's location
     */
    public void giveOrDrop(Player p, int amount){
        ItemStack[] items = toItemStacks(amount, p);
        _giveOrDrop(p, items);
    }

    /**
     * Same as {@link #giveOrDrop(Player, int)} but it will remove all NBTS on the ItemStacks.
     * It should be used only on ItemStacks which are blocks like Stone, Wool, etc.. Because if you use it on a sword with durability, enchants, etc.. They will be removed.
     * I.e of use: If you give 32 cobblestone to the player with a nbt, then a place one bloc and mines it, he wont stack with 31 others blocks because the block lost his nbt
     * @param p The player
     * @param amount See {@link #giveOrDrop(Player, int)}
     */
    public void giveOrDropWithoutNBT(Player p, int amount){
        ItemStack[] items = toItemStacks(amount, p);
        ItemStack[] withoutNbtItems = new ItemStack[items.length];
        int i = 0;
        NBTTagApi api = SpigotApi.getNBTTagApi();
        for(ItemStack item : toItemStacks(amount, p)){
            NBTTagApi.NBTItem nbt = api.getNBT(item);
            for(String tag : api.getNBT(item).getTags().keySet()){
                nbt.removeTag(tag);
            }
            withoutNbtItems[i++] = nbt.getBukkitItem();
        }
        _giveOrDrop(p, withoutNbtItems);
    }

    /**
     * Add a luck enchant on the item and then use the flag HIDE_ENCHANTS to hide the enchantment. The principal use of this is for interactive inventories, maybe if the player already bought this item.
     */
    public void glow(){
        addFlag(ItemFlag.HIDE_ENCHANTS);
        addEnchant(new Enchant(Enchantment.LUCK, 1));
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    @Nonnull
    public Material getMaterial(){
        return material;
    }

    public void setMaterial(@Nonnull Material material) {
        this.material = material;
    }

    /**
     * If {@link Item#name} is null then the ItemStack will have the Minecraft default name for this material.
     * @return the name or null
     */
    @Nullable
    public String getName(){
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public List<String> getLore() {
        if(lore == null)
            return null;
        return new ArrayList<>(lore);
    }

    public void setLore(@Nullable List<String> lore) {
        this.lore = lore;
    }

    /**
     * @return A COPY of the Enchantments list
     */
    @Nullable
    public List<Enchant> getEnchantList() {
        if(enchantList == null)
            return null;
        return new ArrayList<>(enchantList);
    }

    /**
     * @param enchantList if set to null then clear all this item's enchantments
     */
    public void setEnchantList(@Nullable List<Enchant> enchantList) {
        this.enchantList = enchantList;
    }

    public void addEnchant(@Nonnull Enchant enchant){
        if(this.enchantList == null){
            this.enchantList = new ArrayList<>();
        } else {
            if(this.enchantList.contains(enchant) || this.enchantList.stream().anyMatch(e -> e.getEnchantment().equals(enchant.getEnchantment()))){
                Logger.warn("Cannot add the given enchantment \"" + enchant + "\" because it already exists on this item.", Item.class.getName());
                return;
            }
        }
        this.enchantList.add(enchant);
    }

    /**
     * Do not throw error if the enchantment does not exist in the list. So feel free to call this method just to be sure before adding any new enchant.
     */
    public void removeEnchant(@Nonnull Enchantment enchant){
        if(this.enchantList != null){
            this.enchantList.removeIf(e -> e.getEnchantment().equals(enchant));
        }
    }

    /**
     * @return A COPY of the ItemFlags list
     */
    @Nullable
    public List<ItemFlag> getFlags() {
        if(flags == null)
            return null;
        return new ArrayList<>(flags);
    }

    /**
     * @param flags if set to null then clear all this item's flags
     */
    public void setFlags(@Nullable List<ItemFlag> flags) {
        this.flags = flags;
    }

    public void addFlag(@Nonnull ItemFlag flag){
        if(this.flags == null){
            this.flags = new ArrayList<>();
        } else {
            if(this.flags.contains(flag)){
                Logger.warn("Cannot add the given item flag \"" + flag.name() + "\" because it already exists on this item.", Item.class.getName());
                return;
            }
        }
        this.flags.add(flag);
    }

    /**
     * Do not throw error if the item flag does not exist in the list. So feel free to call this method just to be sure before adding any new flag.
     */
    public void removeFlag(@Nonnull ItemFlag flag){
        if(this.flags != null){
            this.flags.remove(flag);
        }
    }

    /**
     * @return A COPY of the ItemAttribute list
     */
    @Nullable
    public List<ItemAttribute> getAttributes() {
        if(attributes == null)
            return null;
        return new ArrayList<>(attributes);
    }

    /**
     * @param attributes if set to null then clear all this item's attributes
     */
    public void setAttributes(@Nullable List<ItemAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(@Nonnull ItemAttribute attribute){
        if(this.attributes == null){
            this.attributes = new ArrayList<>();
        } else {
            if(this.attributes.contains(attribute)){
                Logger.warn("Cannot add the given attribute \"" + attribute + "\" because it already exists on this item.", ItemBuilder.class.getName());
                return;
            }
        }
        this.attributes.add(attribute);
    }

    /**
     * Do not throw error if the attribute does not exist in the list. So feel free to call this method just to be sure before adding any new attribute.
     */
    public void removeAttribute(@Nonnull ItemAttribute attribute){
        if(this.attributes != null){
            this.attributes.remove(attribute);
        }
    }

    public int getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
    }

    public int getCustomModelData(){
        return customData;
    }

    public void setCustomModelData(int customData) {
        this.customData = customData;
    }

    @Nullable
    public Meta getMeta(){
        return meta;
    }

    public void setMeta(@Nullable Meta meta) {
        this.meta = meta;
    }

    public boolean hasMeta(){
        return getMeta()!=null;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setIsInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return uid.equals(item.uid) && material == item.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, material);
    }

    @Override
    public String toString(){
        return "Item{" +
                "\nId:"+(uid==null||uid.isEmpty()?"null":uid)+
                ",\nMaterial:"+material.name()+
                ",\nDisplayname:"+name+
                ",\nLore:"+(lore==null||lore.isEmpty()?"null":lore.toString())+
                ",\nEnchants:"+(enchantList==null||enchantList.isEmpty()?"null":enchantList.toString())+
                ",\nAttributes:"+(attributes==null||attributes.isEmpty()?"null":attributes.toString())+
                ",\nFlags:"+(flags==null||flags.isEmpty()?"null":flags.toString())+
                ",\nItemMeta:"+(meta==null?"null":meta.toString())+
                ",\nRepairCost:"+repairCost+
                ",\nCustomData:"+customData+
                ",\nDurability:"+damage+
                ",\nInvulnerable:"+invulnerable+
                "\n}";
    }

    //compareModifiers from org/bukkit/craftbukkit/inventory/CraftMetaItem.java
    private boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null)return false;
        for (Map.Entry<Attribute, AttributeModifier> entry : first.entries()) {
            if (!second.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : second.entries()) {
            if (!first.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareFlags(Set<ItemFlag> first, Set<ItemFlag> second){
        if (first == null || second == null)return false;
        if(first.size()!=second.size())return false;
        for(ItemFlag flag : first){
            if(!second.contains(flag))return false;
        }
        return true;
    }
}
