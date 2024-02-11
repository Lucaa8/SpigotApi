package ch.luca008.SpigotApi.Item.Meta;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Potion implements Meta{

    public enum MainEffect {
        //base potions
        UNCRAFTABLE(null, false, false),
        WATER(null, false, false),
        MUNDANE(null, false, false),
        THICK(null, false, false),
        AWKWARD(null, false, false),
        //short level I potions
        FIRE_RESISTANCE(null, false, false),
        INSTANT_DAMAGE(null, false, false),
        INSTANT_HEAL(null, false, false),
        INVISIBILITY(null, false, false),
        JUMP(null, false, false),
        LUCK(null, false, false),
        NIGHT_VISION(null, false, false),
        POISON(null, false, false),
        REGEN(null, false, false),
        SLOWNESS(null, false, false),
        SLOW_FALLING(null, false, false),
        SPEED(null, false, false),
        STRENGTH(null, false, false),
        TURTLE_MASTER(null, false, false),
        WATER_BREATHING(null, false, false),
        WEAKNESS(null, false, false),
        //short level II potions
        STRONG_HARMING("INSTANT_DAMAGE", false, true),
        STRONG_HEALING("INSTANT_HEAL", false, true),
        STRONG_LEAPING("JUMP", false, true),
        STRONG_POISON("POISON", false, true),
        STRONG_REGENERATION("REGEN", false, true),
        STRONG_SLOWNESS("SLOWNESS", false, true),
        STRONG_STRENGTH("STRENGTH", false, true),
        STRONG_SWIFTNESS("SPEED", false, true),
        STRONG_TURTLE_MASTER("TURTLE_MASTER", false, true),
        //long level I potions
        LONG_FIRE_RESISTANCE("FIRE_RESISTANCE", true, false),
        LONG_INVISIBILITY("INVISIBILITY", true, false),
        LONG_LEAPING("JUMP", true, false),
        LONG_NIGHT_VISION("NIGHT_VISION", true, false),
        LONG_POISON("POISON", true, false),
        LONG_REGENERATION("REGEN", true, false),
        LONG_SLOWNESS("SLOWNESS", true, false),
        LONG_SLOW_FALLING("SLOW_FALLING", true, false),
        LONG_STRENGTH("STRENGTH", true, false),
        LONG_SWIFTNESS("SPEED", true, false),
        LONG_TURTLE_MASTER("TURTLE_MASTER", true, false),
        LONG_WATER_BREATHING("WATER_BREATHING", true, false),
        LONG_WEAKNESS("WEAKNESS", true, false);

        private final String legacyName;
        private final boolean extended;
        private final boolean upgraded;
        private static final Class<?> PotionType = org.bukkit.potion.PotionType.class;
        private static Class<?> PotionData = null; //only for 1.20.1

        static {
            if(ReflectionApi.SERVER_VERSION == ReflectionApi.Version.MC_1_20){
                try {
                    PotionData = Class.forName("org.bukkit.potion.PotionData");
                } catch (ClassNotFoundException e) {
                    Logger.error("Cannot find the org.bukkit.potion.PotionData class. Are you sure you're running a standard build of Bukkit (Spigot, Paper, ...) ? The Potion Meta wont be enabled.", Potion.class.getName());
                }
            }
        }

        MainEffect(@Nullable String legacyName, boolean extended, boolean upgraded){
            this.legacyName = legacyName == null ? this.name() : legacyName;
            this.extended = extended;
            this.upgraded = upgraded;
        }

        //For 1.20.1
        public String getName(){
            return this.legacyName;
        }

        public boolean isExtended(){
            return this.extended;
        }

        public boolean isUpgraded(){
            return this.upgraded;
        }

        //Retrieve from JSON or PotionData
        @Nonnull
        public static MainEffect retrieve(String legacyName, boolean extended, boolean upgraded){
            for(MainEffect effect : values()){
                if(effect.getName().equals(legacyName) && extended == effect.extended && upgraded == effect.upgraded)
                    return effect;
            }
            Logger.warn("Failed to retrieve the Main Effect with such attributes: name=" + legacyName + ", extended="+extended + ", upgraded="+upgraded + ". UNCRAFTABLE is designed by default for this time.", Potion.class.getName());
            return MainEffect.UNCRAFTABLE;
        }

        //Retrieve from PotionMeta
        @Nonnull
        public static MainEffect retrieve(PotionMeta pm){
            if(ReflectionApi.SERVER_VERSION == ReflectionApi.Version.MC_1_20){
                Object data = ReflectionApi.invoke(pm.getClass(), pm, "getBasePotionData", new Class[0]);
                return retrieve(
                        ((Enum<?>)ReflectionApi.invoke(PotionData, data, "getType", new Class[0])).name(),
                        (boolean)ReflectionApi.invoke(PotionData, data, "isExtended", new Class[0]),
                        (boolean)ReflectionApi.invoke(PotionData, data, "isUpgraded", new Class[0])
                );
            }
            Object type = ReflectionApi.invoke(pm.getClass(), pm, "getBasePotionType", new Class[0]);
            return MainEffect.valueOf(((Enum<?>)type).name());
        }

        public void apply(PotionMeta pm){
            if(ReflectionApi.SERVER_VERSION == ReflectionApi.Version.MC_1_20){ //Legacy potion data (new PotionData(type, ext, up))
                Object legacyType = ReflectionApi.getEnumValue(PotionType, this.getName());
                Object data = ReflectionApi.newInstance(PotionData, new Class[]{legacyType.getClass(), boolean.class, boolean.class}, legacyType, isExtended(), isUpgraded());
                ReflectionApi.invoke(pm.getClass(), pm, "setBasePotionData", new Class[]{PotionData}, data);
                return;
            }
            Object data = ReflectionApi.getEnumValue(PotionType, this.name());
            ReflectionApi.invoke(pm.getClass(), pm, "setBasePotionType", new Class[]{data.getClass()}, data);
        }

    }

    private final MainEffect mainEffect;
    private List<PotionEffect> customsEffects;
    private Color color = null;

    public Potion(JSONObject json){
        if(json.containsKey("Color")){
            Object o = json.get("Color");
            if(o instanceof JSONObject){
                JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader((JSONObject) o);
                color = Color.fromRGB(r.getInt("r"),r.getInt("g"),r.getInt("b"));
            }
        }
        if(json.containsKey("BaseEffect")){
            JSONObject j = (JSONObject) json.get("BaseEffect");
            mainEffect = MainEffect.retrieve((String)j.get("Type"), (boolean)j.get("Extended"), (boolean)j.get("Upgraded"));
        }else mainEffect = MainEffect.UNCRAFTABLE;
        if(json.containsKey("CustomEffects")){
            JSONArray jarr = (JSONArray) json.get("CustomEffects");
            customsEffects = new ArrayList<>();
            for (Object o : jarr) {
                JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader((JSONObject) o);
                PotionEffectType type = PotionEffectType.getByName(r.getString("Type"));
                if(type!=null){
                    customsEffects.add(new PotionEffect(type, r.getInt("Duration"), r.getInt("Amplifier"), r.c("Ambient")&&r.getBool("Ambient"), r.c("Particles")&&r.getBool("Particles")));
                }
            }
        }
    }

    public Potion(@Nullable MainEffect mainEffect, List<PotionEffect> customsEffects, Color color) {
        this.mainEffect = mainEffect==null?MainEffect.UNCRAFTABLE:mainEffect;
        this.customsEffects = customsEffects;
        this.color = color;
    }

    public Potion(PotionMeta pm){
        if(pm!=null){
            this.mainEffect = MainEffect.retrieve(pm);
            if(pm.hasCustomEffects()){
                this.customsEffects = pm.getCustomEffects();
            }
            if(pm.hasColor()){
                this.color = pm.getColor();
            }
        }else{
            this.mainEffect = MainEffect.UNCRAFTABLE;
        }
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item==null||item.getItemMeta()==null)return null;
        if(item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            if(color!=null)pm.setColor(color);
            mainEffect.apply(pm);
            if(customsEffects!=null&&!customsEffects.isEmpty()){
                customsEffects.forEach(e->pm.addCustomEffect(e,true));
            }
            item.setItemMeta(pm);
            return item;
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        if(color!=null){
            JSONObject rgb = new JSONObject();
            rgb.put("r",color.getRed());
            rgb.put("g",color.getGreen());
            rgb.put("b",color.getBlue());
            j.put("Color", rgb);
        }
        JSONObject base = new JSONObject();
        base.put("Type", mainEffect.getName());
        base.put("Extended", mainEffect.isExtended());
        base.put("Upgraded", mainEffect.isUpgraded());
        j.put("BaseEffect", base);
        if(customsEffects!=null&&!customsEffects.isEmpty()){
            JSONArray jarr = new JSONArray();
            for (PotionEffect eff : customsEffects) {
                JSONObject e = new JSONObject();
                e.put("Type", eff.getType().getName());
                e.put("Duration", eff.getDuration());
                e.put("Amplifier", eff.getAmplifier());
                if(eff.isAmbient()){
                    e.put("Ambient", true);
                }
                if(eff.hasParticles()){
                    e.put("Particles", true);
                }
                jarr.add(e);
            }
            j.put("CustomEffects", jarr);
        }
        return j;
    }

    @Override
    public String toString(){
        String effects = "NULL";
        if(customsEffects!=null&&!customsEffects.isEmpty()){
            effects = "{";
            for(PotionEffect e : customsEffects){
                effects+="{Type:"+e.getType().getName()+",Duration:"+e.getDuration()+",Amplifier:"+e.getAmplifier()+",Ambient:"+e.isAmbient()+",Particles:"+e.hasParticles()+"},";
            }
            effects = effects.substring(0,effects.length()-1);
            effects += "}";
        }
        return "{MetaType:POTION,BasePotion:{Effect:"+mainEffect.getName()+",Extended:"+mainEffect.isExtended()+",Upgraded:"+mainEffect.isUpgraded()+"}," +
                (color==null?"Color:NULL":"Color:{R:"+color.getRed()+",G:"+color.getGreen()+",B:"+color.getBlue()+"}," +
                        "CustomEffects:"+effects+"}");
    }

    @Override
    public MetaType getType() {
        return MetaType.POTION;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            MainEffect metaData = MainEffect.retrieve(pm);
            if(!(color!=null?(pm.hasColor()&&pm.getColor().equals(color)):!pm.hasColor()))return false;
            if(!mainEffect.equals(metaData))return false;
            if(customsEffects!=null&&pm.hasCustomEffects()){
                if(customsEffects.size()!=pm.getCustomEffects().size())return false;
                for (PotionEffect thiz : customsEffects) {
                    boolean contains = false;
                    for(PotionEffect that : pm.getCustomEffects()){
                        if(thiz.equals(that)){
                            contains = true;
                            break;
                        }
                    }
                    if(!contains)return false;
                }
            }else{
                if((customsEffects!=null&&!pm.hasCustomEffects())||(customsEffects==null&&pm.hasCustomEffects()))return false;
            }
            return true;
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            if(MainEffect.retrieve(pm)!=MainEffect.WATER)return true;
            if(pm.hasColor())return true;
            if(pm.hasCustomEffects())return true;
        }
        return false;
    }
}
