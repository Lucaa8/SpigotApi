package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Api.ReflectionApi.ClassMapping;
import ch.luca008.SpigotApi.Api.ReflectionApi.Version;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.ApiProperty;
import com.mojang.authlib.GameProfile;
import org.apache.commons.lang.ClassUtils;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class NBTTagApi {

    private static final Class<?> OBC_ITEM_STACK;
    private static final String NMS_ITEM_STACK = "NMSItemStack";
    private static final String NBT_COMPOUND = "NBTTagCompound";
    private static final String NBT_BASE = "NBTBase";
    private static final String GAME_PROFILE_SERIALIZER = "NbtUtils";

    private static final Map<Class<?>, Class<?>> NBT_TYPES = new HashMap<>();

    private static final Map<String, ClassMapping> mappings = new HashMap<>();

    static {

        Version v = ReflectionApi.SERVER_VERSION;

        OBC_ITEM_STACK = ReflectionApi.getOBCClass("inventory", "CraftItemStack");

        mappings.put(NMS_ITEM_STACK, new ClassMapping(ReflectionApi.getNMSClass("world.item", "ItemStack"), new HashMap<>(), new HashMap<>(){{ put("getOrCreateTag", "w"); put("setTag", "c"); put("removeTagKey", "c"); put("hasTag", "u"); }}));
        mappings.put(NBT_COMPOUND, new ClassMapping(ReflectionApi.getNMSClass("nbt", "NBTTagCompound"), new HashMap<>(){{ put("tags", "x"); }}, new HashMap<>(){{ put("put", "a"); put("get", "c"); }}));
        mappings.put(GAME_PROFILE_SERIALIZER, new ClassMapping(ReflectionApi.getNMSClass("nbt", "GameProfileSerializer"), new HashMap<>(), new HashMap<>(){{ put("writeGameProfile", "a"); }}));

        String getAsString = "m_"; //1.20.1
        switch (v)
        {
            case MC_1_20_2 -> getAsString = "r_";
            case MC_1_20_3 -> getAsString = "t_"; //1.20.4 same
        }

        String finalGetAsString = getAsString;
        mappings.put(NBT_BASE, new ClassMapping(ReflectionApi.getNMSClass("nbt", "NBTBase"), new HashMap<>(), new HashMap<>(){{ put("getAsString" , finalGetAsString); }}));

        NBT_TYPES.put(String.class, ReflectionApi.getNMSClass("nbt", "NBTTagString"));
        NBT_TYPES.put(int.class, ReflectionApi.getNMSClass("nbt", "NBTTagInt"));
        NBT_TYPES.put(float.class, ReflectionApi.getNMSClass("nbt", "NBTTagFloat"));
        NBT_TYPES.put(double.class, ReflectionApi.getNMSClass("nbt", "NBTTagDouble"));
        NBT_TYPES.put(short.class, ReflectionApi.getNMSClass("nbt", "NBTTagShort"));
        NBT_TYPES.put(long.class, ReflectionApi.getNMSClass("nbt", "NBTTagLong"));
        Class<?> byte_type = ReflectionApi.getNMSClass("nbt", "NBTTagByte");
        NBT_TYPES.put(byte.class, byte_type);
        NBT_TYPES.put(boolean.class, byte_type);


    }

    public NBTItem getNBT(ItemStack item) {
        return new NBTItem(item);
    }

    public Object getNMSItem(ItemStack bukkitItem) {
        return ReflectionApi.invoke(OBC_ITEM_STACK, null, "asNMSCopy", new Class[]{bukkitItem.getClass()}, bukkitItem);
    }

    public ItemStack getBukkitItem(Object nmsItem) {
        return (ItemStack) ReflectionApi.invoke(OBC_ITEM_STACK, null, "asBukkitCopy", new Class[]{nmsItem.getClass()}, nmsItem);
    }

    private final Supplier<Object> newCompound = () -> mappings.get(NBT_COMPOUND).newInstance().packet();

    private Object fromObjectToNBTBase(Object o) {

        Class<?> objType = o.getClass();
        Class<?> toPrimitive = ClassUtils.wrapperToPrimitive(objType); //String returns null because he does not have a primitive version

        if(toPrimitive != null)
            objType = toPrimitive;

        if(!NBT_TYPES.containsKey(objType))
            return null;

        return ReflectionApi.invoke(NBT_TYPES.get(objType), null, "a", new Class[]{objType}, o);

    }

    private boolean hasTag(Object nmsItem) {
        return (boolean) mappings.get(NMS_ITEM_STACK).invoke(nmsItem, "hasTag");
    }

    private Object getTagCompound(Object nmsItem) {
        return mappings.get(NMS_ITEM_STACK).invoke(nmsItem, "getOrCreateTag");
    }

    private Map<String, Object> getTags(Object nmsItem)
    {
        return (Map<String, Object>) mappings.get(NBT_COMPOUND).getFieldValue("tags", getTagCompound(nmsItem));
    }

    private boolean containsTag(Object nmsItem, String tagName) {
        return getTag(nmsItem, tagName) != null;
    }

    @Nullable
    private Object getTag(Object nmsItem, String tagName) {
        Object tagCompound = getTagCompound(nmsItem);
        return mappings.get(NBT_COMPOUND).invoke(tagCompound, "get", new Class[]{String.class}, tagName);
    }

    @Nullable
    private String getStringTag(Object nmsItem, String tagName) {
        Object nbtTag = getTag(nmsItem, tagName);
        if(nbtTag != null)
            return (String) mappings.get(NBT_BASE).invoke(nbtTag, "getAsString");
        return null;
    }

    private void setTag(Object nmsItem, String tagName, Object value) {

        if(!mappings.get(NBT_BASE).getMappedClass().isAssignableFrom(value.getClass()))
            value = fromObjectToNBTBase(value);

        if(value != null)
        {
            Object tagCompound = getTagCompound(nmsItem);
            mappings.get(NBT_COMPOUND).invoke(tagCompound, "put", new Class[]{String.class, mappings.get(NBT_BASE).getMappedClass()}, tagName, value);
            mappings.get(NMS_ITEM_STACK).invoke(nmsItem, "setTag", new Class[]{tagCompound.getClass()}, tagCompound);
        }

    }

    private void removeTag(Object nmsItem, String tagName) {
        mappings.get(NMS_ITEM_STACK).invoke(nmsItem, "removeTagKey", new Class[]{String.class}, tagName);
    }

    private void setSkullCompound(Object nmsItem, String ownerName, String textureValue)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), ownerName);
        new ApiProperty("textures", textureValue, null).addProperty(profile);

        Object emptyCompound = newCompound.get();
        Object headCompound = mappings.get(GAME_PROFILE_SERIALIZER).invoke(null, "writeGameProfile", new Class[]{emptyCompound.getClass(), profile.getClass()}, emptyCompound, profile);

        setTag(nmsItem, "SkullOwner", headCompound);
    }

    public static class NBTItem {
        private final NBTTagApi api;
        private final Object nmsItem;

        private NBTItem(ItemStack item) {
            this.api = SpigotApi.getNBTTagApi();
            this.nmsItem = api.getNMSItem(item);
        }

        public NBTItem setTag(String tag, Object value) {
            this.api.setTag(this.nmsItem, tag, value);
            return this;
        }

        public NBTItem removeTag(String tag) {
            this.api.removeTag(this.nmsItem, tag);
            return this;
        }

        public NBTItem addSkullTexture(String ownerName, String textureValue)
        {
            this.api.setSkullCompound(this.nmsItem, ownerName, textureValue);
            return this;
        }

        public Object getTag(String tag) {
            return this.api.getTag(this.nmsItem, tag);
        }

        public String getString(String tag) {
            return this.api.getStringTag(this.nmsItem, tag);
        }

        public Map<String, Object> getTags() {
            return new HashMap<>(this.api.getTags(this.nmsItem));
        }

        public boolean hasTag(String tag) {
            return this.api.containsTag(this.nmsItem, tag);
        }

        public boolean hasTags() {
            return this.api.hasTag(this.nmsItem);
        }

        public Object getTagCompound() {
            return this.api.getTagCompound(this.nmsItem);
        }

        public Object getNMSItem() {
            return this.nmsItem;
        }

        public ItemStack getBukkitItem() {
            return api.getBukkitItem(this.nmsItem);
        }
    }

}
