import ch.luca008.SpigotApi.Api.ReflectionApi;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class NBTTagApiFetcher {

    public void printMappings()
    {
        Class<?> nms_itemstack = ReflectionApi.getNMSClass("world.item", "ItemStack");
        for(Method m : nms_itemstack.getDeclaredMethods())
        {
            if(m.getReturnType().getName().contains("NBTTagCompound") && m.getParameterCount() == 0)
            {
                System.out.println("-Get tags candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
            if(m.getReturnType() == void.class && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class)
            {
                System.out.println("-Remove tag candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
            if(m.getReturnType() == void.class && m.getParameterCount() == 1 && m.getParameterTypes()[0].getName().contains("NBTTagCompound"))
            {
                System.out.println("-Set tag candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
            if(m.getReturnType() == boolean.class && m.getParameterCount() == 0)
            {
                System.out.println("-Has tags candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
        }
        Class<?> nbt = ReflectionApi.getNMSClass("nbt", "NBTTagCompound");
        for(Method m : nbt.getDeclaredMethods())
        {
            if(m.getReturnType().getName().contains("NBTBase") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class)
            {
                System.out.println("-NBT Get tag candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
            if(m.getReturnType().getName().contains("NBTBase") && m.getParameterCount() == 2 && m.getParameterTypes()[0] == String.class && m.getParameterTypes()[1].getName().contains("NBTBase"))
            {
                System.out.println("-NBT Set tag candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
        }
        Class<?> nbt_base = ReflectionApi.getNMSClass("nbt", "NBTBase");
        for(Method m : nbt_base.getDeclaredMethods())
        {
            if(m.getReturnType() == String.class && m.getParameterCount() == 0)
            {
                System.out.println("-NBTBase toStringTag candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
            }
        }
        List<String> nbt_types = List.of("NBTTagString", "NBTTagInt", "NBTTagFloat", "NBTTagDouble", "NBTTagShort", "NBTTagLong", "NBTTagByte");
        for(String nbt_type : nbt_types)
        {
            Class<?> nbt_class = ReflectionApi.getNMSClass("nbt", nbt_type);
            for(Method m : nbt_class.getDeclaredMethods())
            {
                if(m.getReturnType().getName().contains(nbt_type) && m.getParameterCount() == 1)
                {
                    System.out.println("-NBTBase create candidate: " + m.getReturnType() + " " + m.getName() + Arrays.toString(m.getParameterTypes()));
                }
            }
        }
    }

}
