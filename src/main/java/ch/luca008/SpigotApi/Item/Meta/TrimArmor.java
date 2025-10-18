package ch.luca008.SpigotApi.Item.Meta;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrimArmor implements Meta{

    protected TrimMaterial material = TrimMaterial.AMETHYST;
    protected TrimPattern pattern = TrimPattern.COAST;

    public TrimArmor(@Nullable JSONObject json) {
        // This check is to allow LeatherArmor meta to NOT have a TrimArmor meta
        if(json == null) return;
        JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader(json);
        if(r.c("Pattern")) {
            pattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(r.getString("Pattern")));
        } else {
            Logger.warn("Cannot find any pattern for TrimArmor meta. " + pattern.getKey().getKey() + " will be used by default.", TrimArmor.class.getName());
        }
        if(r.c("Material")) {
            material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(r.getString("Material")));
        } else {
            Logger.warn("Cannot find any material for TrimArmor meta. " + material.getKey().getKey() + " will be used by default.", TrimArmor.class.getName());
        }
    }

    public TrimArmor(TrimMaterial trimMaterial, TrimPattern trimPattern) {
        if(trimMaterial != null) {
            this.material = trimMaterial;
        }
        if(trimPattern != null) {
            this.pattern = trimPattern;
        }
    }

    public TrimArmor(@Nonnull ArmorTrim armorMeta) {
        this.material = armorMeta.getMaterial();
        this.pattern = armorMeta.getPattern();
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item.getItemMeta()!=null&& item.getItemMeta() instanceof ArmorMeta am){
            am.setTrim(new ArmorTrim(material, pattern));
            item.setItemMeta(am);
            return item;
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        return SpigotApi.getJSONApi().getWriter(null)
                .write("Pattern", this.pattern.getKey().getKey())
                .write("Material", this.material.getKey().getKey())
                .asJson();
    }

    @Override
    public String toString(){
        return "{MetaType:TRIM_ARMOR,Pattern:"+this.pattern.getKey().getKey()+",Material:"+this.material.getKey().getKey()+"}";
    }

    @Override
    public MetaType getType() {
        return MetaType.TRIM_ARMOR;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        // DO NOT REPLACE ArmorMeta by ArmorTrim because LeatherArmor meta is of type ColorableArmorMeta which DOES NOT extend ArmorTrim
        if(item != null && item.getItemMeta() instanceof ArmorMeta am && am.hasTrim()){
            return am.getTrim().getPattern().equals(this.pattern) && am.getTrim().getMaterial().equals(this.material);
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        return item.getItemMeta() instanceof ArmorMeta m && m.hasTrim();
    }

}
