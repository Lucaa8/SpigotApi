package ch.luca008.SpigotApi.Item.Meta;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;

public class LeatherArmor extends TrimArmor implements Meta{

    private Color color = Bukkit.getItemFactory().getDefaultLeatherColor();
    private boolean hasTrim = false;

    public LeatherArmor(JSONObject json){
        super(json.containsKey("TrimArmor") ? (JSONObject) json.get("TrimArmor") : null);
        if(json.containsKey("Color")){
            Object o = json.get("Color");
            if(o instanceof JSONObject jo) {
                JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader(jo);
                color = Color.fromRGB(r.getInt("r"), r.getInt("g"), r.getInt("b"));
            }
        }
        if(json.containsKey("TrimArmor")) {
            this.hasTrim = true;
        }
    }

    public LeatherArmor(@Nullable Color color, @Nullable TrimPattern pattern, @Nullable TrimMaterial material){
        super(material, pattern);
        if(color!=null){
            this.color = color;
        }
        if(pattern!=null&&material!=null){
            this.hasTrim = true;
        }
    }

    public LeatherArmor(@Nullable ColorableArmorMeta meta){
        this(
                meta != null ? meta.getColor() : null,
                meta != null && meta.hasTrim() ? meta.getTrim().getPattern() : null,
                meta != null && meta.hasTrim() ? meta.getTrim().getMaterial() : null
        );
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item.getItemMeta()!=null&& item.getItemMeta() instanceof ColorableArmorMeta meta){
            meta.setColor(color);
            item.setItemMeta(meta);
            if(hasTrim)
            {
                item = super.apply(item);
            }
            return item;
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONApi.JSONWriter jw = SpigotApi.getJSONApi().getWriter(null);
        jw.write("Color.r", color.getRed());
        jw.write("Color.g", color.getGreen());
        jw.write("Color.b", color.getBlue());
        JSONObject j = jw.asJson();
        if(hasTrim) {
            j.put("TrimArmor", super.toJson());
        }
        return j;
    }

    @Override
    public String toString(){
        String trim = hasTrim ? ",TrimArmor:{PATTERN:"+ super.pattern.getKey().getKey()+",Material:"+super.material.getKey().getKey()+"}":"";
        return "{MetaType:LEATHER_ARMOR,Color:{R:"+color.getRed()+",G:"+color.getGreen()+",B:"+color.getBlue()+"}"+trim+"}";
    }

    @Override
    public MetaType getType() {
        return MetaType.LEATHER_ARMOR;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof ColorableArmorMeta meta){
            return meta.getColor().equals(color) && (!hasTrim || super.hasSameMeta(item, player));
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getItemMeta() instanceof ColorableArmorMeta meta){
            int c = meta.getColor().asRGB();
            int r = (c&0xFF0000)>>16;
            int g = (c&0xFF00)>>8;
            int b = c&0xFF;
            return !(r==160 && g==101 && b==64) || meta.hasTrim();
        }
        return false;
    }
}
