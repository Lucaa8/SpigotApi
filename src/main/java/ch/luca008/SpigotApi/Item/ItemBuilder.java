package ch.luca008.SpigotApi.Item;

import ch.luca008.SpigotApi.Item.Meta.Meta;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private String uid;
    private Material material = Material.STONE;
    private String name;
    private List<String> lore = new ArrayList<>();
    private List<Enchant> enchantList = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private List<ItemAttribute> attributes = new ArrayList<>();
    private boolean glowing = false;
    private Meta meta = null;
    private int repairCost = 0;
    private int customData = 0;
    private int damage = 0;
    private boolean invulnerable = false;

    public ItemBuilder setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ItemBuilder setMaterial(@Nonnull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Create a List of String with the single String given.
     * @param lore A String with a LF for every new line of the lore. E.g: "Line 1\nLine 2" will create ["Line 1", "Line 2"]
     */
    public ItemBuilder setLore(@Nonnull String lore){
        this.lore = ch.luca008.SpigotApi.Utils.StringUtils.asLore(lore);
        return this;
    }

    public ItemBuilder setLore(@Nonnull List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setEnchantList(@Nonnull List<Enchant> enchantList) {
        this.enchantList = enchantList;
        return this;
    }

    public ItemBuilder addEnchant(@Nonnull Enchant enchantment) {
        if(this.enchantList.contains(enchantment)){
            Logger.warn("Cannot add the given enchantment \"" + enchantment + "\" because it already exists on this item.", ItemBuilder.class.getName());
        } else {
            this.enchantList.add(enchantment);
        }
        return this;
    }

    public ItemBuilder addEnchant(@Nonnull Enchantment enchantment, int level) {
        return addEnchant(new Enchant(enchantment, level));
    }

    public ItemBuilder setFlags(@Nonnull List<ItemFlag> flags) {
        this.flags = flags;
        return this;
    }

    public ItemBuilder addFlag(@Nonnull ItemFlag flag) {
        if(this.flags.contains(flag)){
            Logger.warn("Cannot add the given item flag \"" + flag.name() + "\" because it already exists on this item.", ItemBuilder.class.getName());
        } else {
            this.flags.add(flag);
        }
        return this;
    }

    public ItemBuilder setAttributes(@Nonnull List<ItemAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ItemBuilder addAttribute(@Nonnull ItemAttribute attribute) {
        if(this.attributes.contains(attribute)){
            Logger.warn("Cannot add the given attribute \"" + attribute + "\" because it already exists on this item.", ItemBuilder.class.getName());
        } else {
            this.attributes.add(attribute);
        }
        return this;
    }

    public ItemBuilder addAttribute(@Nonnull Attribute attribute, @Nonnull String name, double value, @Nullable AttributeModifier.Operation operation, @Nullable EquipmentSlot slot) {
        return addAttribute(new ItemAttribute(attribute, name, value, operation, slot));
    }

    /**
     * The Operation will be by default ADD_NUMBER and the slot null so the attribute will be effective on any slot.
     */
    public ItemBuilder addAttribute(@Nonnull Attribute attribute, @Nonnull String name, double value) {
        return addAttribute(attribute, name, value, AttributeModifier.Operation.ADD_NUMBER, null);
    }

    public ItemBuilder setMeta(Meta meta) {
        this.meta = meta;
        return this;
    }

    public ItemBuilder setRepairCost(int repairCost) {
        this.repairCost = repairCost;
        return this;
    }

    public ItemBuilder setCustomData(int customData) {
        this.customData = customData;
        return this;
    }

    public ItemBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public ItemBuilder setIsInvulnerable(boolean invulnerable){
        this.invulnerable = invulnerable;
        return this;
    }

    /**
     * Add a luck enchant on the item and then use the flag HIDE_ENCHANTS to hide the enchantment. The principal use of this is for interactive inventories, maybe if the player already bought this item.
     */
    public ItemBuilder setGlowing(boolean glowing){
        this.glowing = glowing;
        return this;
    }

    public Item createItem() {
        Item i = new Item(uid, material, name, lore, enchantList, flags, attributes, meta, repairCost, customData, damage, invulnerable);
        if(glowing){
            i.glow();
        }
        return i;
    }
}
