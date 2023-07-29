package ch.luca008.SpigotApi.Item;

import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class Enchant {

    private Enchantment enchantment;
    private int level;

    public Enchant(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchant(String json){
        try {
            JSONObject j = (JSONObject) new JSONParser().parse(json);
            enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft((String)j.get("Key")));
            level = SpigotApi.getJSONApi().getReader(j).getInt("Level");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ItemStack apply(ItemStack item){
        if(item==null)return null;
        if(!hasEnchant(item)){
            item.addUnsafeEnchantment(enchantment, level);
        }
        return item;
    }

    public ItemStack remove(ItemStack item){
        if(item==null)return null;
        if(hasEnchant(item)){
            item.getEnchantments().remove(enchantment);
        }
        return item;
    }

    public boolean hasEnchant(ItemStack item){
        if(item==null)return false;
        return item.getEnchantments().containsKey(enchantment);
    }

    public Enchantment getEnchantment(){
        return enchantment;
    }

    public int getLevel(){
        return level;
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        j.put("Key", enchantment.getKey().getKey());
        j.put("Level", level);
        return j;
    }

    public static JSONArray listToJson(List<Enchant> list){
        JSONArray jarr = new JSONArray();
        for(Enchant e : list){
            jarr.add(e.toJson());
        }
        return jarr;
    }

    @Override
    public String toString(){
        return "Enchantment{Key:"+enchantment.getKey().getKey()+",Level:"+level+"}";
    }

}
