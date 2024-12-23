package ch.luca008.SpigotApi.Item.Meta;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public interface Meta {

    public enum MetaType {
        POTION("Potion"),
        SKULL("Skull"),
        LEATHER_ARMOR("LeatherArmor"),
        TROPICAL_FISH("TropicalFish"),
        BOOK("Book"),
        TRIM_ARMOR("TrimArmor");

        String clazz;
        MetaType(String clazz){
            this.clazz=clazz;
        }
        public String getClazz(){return clazz;}
    }

    public ItemStack apply(ItemStack item);

    public JSONObject toJson();

    @Override
    public String toString();

    public MetaType getType();

    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player); //need ItemStack to compare some deep NBT with skulls...

    //Used to check if an item CONTAINS(not equal to anything) some meta data like skullowner, colored armor, etc...
    public static boolean hasMeta(ItemStack itemStack){
        try{
            for(MetaType type : MetaType.values()){
                Class<?> clazz = Class.forName(Meta.class.getPackage().getName()+"."+type.getClazz());
                if((boolean)clazz.getDeclaredMethod("hasMeta", ItemStack.class).invoke(clazz, itemStack))return true;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

}
