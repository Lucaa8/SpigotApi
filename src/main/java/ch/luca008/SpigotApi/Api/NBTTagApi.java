package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.SpigotApi;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;

public class NBTTagApi {

    public NBTItem getNBT(org.bukkit.inventory.ItemStack item) {
        return new NBTItem(item);
    }

    public ItemStack getNMSItem(org.bukkit.inventory.ItemStack itemstack) {
        Class<?> CraftItemStack = ReflectionApi.getOBCClass("inventory", "CraftItemStack");
        return (ItemStack)ReflectionApi.invoke(CraftItemStack, CraftItemStack, "asNMSCopy", new Class[]{org.bukkit.inventory.ItemStack.class}, itemstack);
    }

    public org.bukkit.inventory.ItemStack getBukkitItem(ItemStack nmsCopy) {
        //inconsistency between spigot-api and paper patched jar. The spigot api's nms version doesn't know about ItemStack#asBukkitCopy but the final paper jar does.
        return (org.bukkit.inventory.ItemStack) ReflectionApi.invoke(nmsCopy.getClass(), nmsCopy, "asBukkitCopy", new Class[0]);
    }

    private NBTBase fromObjectToNBTBase(Object o) {
        //impossible to do switch case with object classes bruh
        if(o instanceof String) {
            return NBTTagString.a((String) o);
        }
        else if(o instanceof Integer) {
            return NBTTagInt.a((int)o);
        }
        else if(o instanceof Float) {
            return NBTTagFloat.a((float)o);
        }
        else if(o instanceof Double) {
            return NBTTagDouble.a((double)o);
        }
        else if(o instanceof Short) {
            return NBTTagShort.a((short)o);
        }
        else if(o instanceof Long) {
            return NBTTagLong.a((long)o);
        }
        else if(o instanceof Byte) {
            return NBTTagByte.a((byte)o);
        }
        else if(o instanceof Boolean) {
            return NBTTagByte.a((boolean)o);
        }
        return null;
    }

    private boolean hasTag(ItemStack i) {
        return i.u();
    }

    private NBTTagCompound getTags(ItemStack i) {
        return i.w();
    }

    private boolean containsTag(ItemStack i, String tagname) {
        return getTags(i).c(tagname)!=null;
    }

    private NBTBase getTag(ItemStack i, String tagname) {
        return getTags(i).c(tagname);
    }

    private String getStringTag(ItemStack i, String tagname) {
        return getTag(i, tagname).m_();
    }

    private void setTag(ItemStack i, String tagname, Object value) {
        NBTTagCompound tags = getTags(i);
        value = fromObjectToNBTBase(value);
        if(value != null)
        {
            tags.a(tagname, (NBTBase) value);
            i.c(tags);
        }
    }

    private void removeTag(ItemStack i, String tagname) {
        i.c(tagname);
    }

    public static class NBTItem {
        private final NBTTagApi api;
        private final ItemStack item;

        private NBTItem(org.bukkit.inventory.ItemStack item) {
            this.api = SpigotApi.getNBTTagApi();
            this.item = api.getNMSItem(item);
        }

        public NBTItem setTag(String tag, Object value) {
            this.api.setTag(this.item, tag, value);
            return this;
        }

        public NBTItem removeTag(String tag) {
            this.api.removeTag(this.item, tag);
            return this;
        }

        public NBTBase getTag(String tag) {
            return this.api.getTag(this.item, tag);
        }

        public String getString(String tag) {
            return this.api.getStringTag(this.item, tag);
        }

        public NBTTagCompound getTags() {
            return this.api.getTags(this.item);
        }

        public boolean hasTag(String tag) {
            return this.api.containsTag(this.item, tag);
        }

        public boolean hasTags() {
            return this.api.hasTag(this.item);
        }

        public ItemStack getNMSItem() {
            return this.item;
        }

        public org.bukkit.inventory.ItemStack getBukkitItem() {
            return api.getBukkitItem(this.item);
        }
    }

}
