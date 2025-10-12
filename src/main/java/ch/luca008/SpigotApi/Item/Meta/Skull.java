package ch.luca008.SpigotApi.Item.Meta;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.ApiProperty;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.luca008.SpigotApi.Utils.WebRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Skull implements Meta{

    private record TextureCache(String texture, long lastRequest, long cacheDuration){
        public boolean isCacheValid() {
            return (System.currentTimeMillis() - lastRequest) < cacheDuration;
        }
    }

    private static final Map<String, TextureCache> textureCache = new HashMap<>();

    public enum SkullOwnerType{
        PLAYER, //A dynamic custom player (with applyOwner(UniPlayer)) (owner = null)
        PSEUDO, //A defined player pseudo i.e "Luca008" (owner = "Luca008"), the skin will be the official online mojang servers skin for this pseudo
        MCHEADS; //A defined existing mcheads custom head i.e "King Creeper" (owner = "King Creeper's value")
    }
    private SkullOwnerType type = null;
    private String owner = null; //si player = p.ex "Luca008" et si Custom = p.ex "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZhM2JiYTJiN2EyYjRmYTQ2OTQ1YjE0NzE3NzdhYmU0NTk5Njk1NTQ1MjI5ZTc4MjI1OWFlZDQxZDYifX19"
    // Only used when type is PSEUDO:
    //   stores the texture for this current "owner" the first time a web request is done, for the next specified time in MS
    //   can be set to 0L in json config to disable caching (mojang servers are called every time Skull#apply is called with type==PSEUDO)
    private long useCache = 3600_000L;

    public Skull(JSONObject json){
        if(json.containsKey("Type")){
            type = SkullOwnerType.valueOf((String)json.get("Type"));
        }
        if(type!=null&&type!=SkullOwnerType.PLAYER&&json.containsKey("Owner")){
            owner = (String) json.get("Owner");
        }
        if(type!=null&&type==SkullOwnerType.PSEUDO&&json.containsKey("UseCache")){
            useCache = ((Long) json.get("UseCache")) * 1000;
        }
    }
    public Skull(SkullOwnerType type, String owner){
        this.type = type;
        this.owner = owner;
    }
    public Skull(SkullOwnerType type, String owner, long useCacheMS){
        this.type = type;
        this.owner = owner;
        this.useCache = useCacheMS;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof SkullMeta &&type!=null){
            if(type==SkullOwnerType.PLAYER)
                return true; //No fixed data on this skull meta instance, so nothing to check.
            else if(type==SkullOwnerType.PSEUDO){
                SkullMeta sm = (SkullMeta) item.getItemMeta();
                if(sm.hasOwner()){
                    String name = sm.getOwningPlayer().getName();
                    if(name == null && sm.getOwnerProfile() != null) name = sm.getOwnerProfile().getName();
                    return name != null && name.equals(owner);
                }
            }
            else{//MCHEADS
                NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
                if(nbt.hasTags()&&owner!=null){
                    try{
                        String value = nbt.getTagCompound().toString().split("Value:\"")[1];
                        return owner.equals(value.substring(0,value.indexOf("\"")));
                    }catch(Exception e){
                        System.err.println("Can't read and parse(trying to find 'Value:\"someValue\"') the following nbttag: " + nbt.getTags());
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getType()!= Material.PLAYER_HEAD)return false;
        return SpigotApi.getNBTTagApi().getNBT(item).hasTag("SkullOwner");
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item==null||item.getType()!=Material.PLAYER_HEAD||item.getItemMeta()==null||type==null||type==SkullOwnerType.PLAYER)return item;
        if(type==SkullOwnerType.PSEUDO){
            String texture = getTextureOrFetch();
            if(texture != null)
                return SpigotApi.getNBTTagApi().getNBT(item).addSkullTexture(owner, texture).getBukkitItem();
            Logger.warn("Cannot apply null texture to skull item. (Failed to fetch textures for name " + owner + ").", Skull.class.getName());
            return item;
        }else return SpigotApi.getNBTTagApi().getNBT(item).addSkullTexture("Custom", owner).getBukkitItem();
    }

    public ItemStack applyOwner(ItemStack item, UUID player){
        if(item==null||item.getType()!=Material.PLAYER_HEAD||item.getItemMeta()==null||type==null||type!=SkullOwnerType.PLAYER)return item;
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        item.setItemMeta(sm);
        return item;
    }

    public SkullOwnerType getOwningType(){
        return type;
    }

    public CompletableFuture<String> updateCache() {
        CompletableFuture<String> future = new CompletableFuture<>();
        if(type==null||type!=SkullOwnerType.PSEUDO||useCache<=0L){
            future.complete("");
        } else {
            textureCache.remove(owner);
            ApiProperty textures = WebRequest.getSkin(owner, false);
            if(textures==null){
                future.complete("");
            } else {
                String skin = textures.value();
                textureCache.put(owner, new TextureCache(skin, System.currentTimeMillis(), useCache));
                future.complete(skin);
            }
        }
        return future;
    }

    @Nullable
    private String getTextureOrFetch(){
        if(type==null||type!=SkullOwnerType.PSEUDO)
            return null;
        // This skull meta does not use cache
        if(useCache<=0L){
            ApiProperty textures = WebRequest.getSkin(owner, false);
            return textures == null ? null : textures.value();
        }
        if(textureCache.containsKey(owner)){
            TextureCache cache = textureCache.get(owner);
            if(cache.isCacheValid())
                return cache.texture;
        }
        try {
            // In this case we are waiting synchronously the response because this method will be called in "apply" method and
            // cannot be async. (The method calling for Item#toItemStack() should be async tho)
            String texture = updateCache().get();
            return texture.isEmpty() ? null : texture;
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(type!=null){
            j.put("Type", type.name());
        }
        if(owner!=null){
            j.put("Owner", owner);
        }
        j.put("UseCache", useCache/1000);
        return j;
    }

    @Override
    public MetaType getType(){
        return MetaType.SKULL;
    }

    @Override
    public String toString(){
        return "{MetaType:SKULL,Type:"+(type==null?"Null":type.name())+"Owner:{Value:"+(owner==null?"None":owner)+"}}";
    }

}
