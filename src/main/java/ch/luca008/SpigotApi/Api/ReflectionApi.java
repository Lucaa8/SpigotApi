package ch.luca008.SpigotApi.Api;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionApi {

    private static String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    public static String getServerVersion(){
        return version;
    }

    public static Class<?> getOBCClass(@Nullable String packagename, String classname){
        String name = "org.bukkit.craftbukkit." + version + "." + ((packagename == null || packagename.equals("") ? "":packagename+".")) + classname;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            System.out.println("The class named \""+name+"\" can't be found...\n");
            e.printStackTrace();
            return null;
        }
    }

    public static Object invoke(Class<?> methodClass, Object objectToInvoke, String methodName, Class<?>[] methodArgsType, Object...methodArgs) {
        Object value = null;
        try {
            Method m = (methodArgsType==null) ? methodClass.getDeclaredMethod(methodName) :
                    methodClass.getDeclaredMethod(methodName, methodArgsType);
            m.setAccessible(true);
            value = (methodArgs==null) ? m.invoke(objectToInvoke) : m.invoke(objectToInvoke, methodArgs);
            m.setAccessible(false);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Object newInstance(Class<?> clazz, Class<?>[] constructorArgsType, Object...constructorArgs){
        try {
            Constructor<?> c = clazz.getDeclaredConstructor(constructorArgsType);
            c.setAccessible(true);
            Object instance = c.newInstance(constructorArgs);
            c.setAccessible(false);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(Class<?> clazz){
        return newInstance(clazz, new Class<?>[0]);
    }

    public static void setField(Object o, String fieldname, Object value) {
        try {
            Field f = o.getClass().getDeclaredField(fieldname);
            f.setAccessible(true);
            f.set(o, value);
            f.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Object _getField(Class<?> clazz, Object object, String field){
        Object value = null;
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            value = f.get(object);
            f.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Object getField(Object o, String fieldname) {
        return _getField(o.getClass(), o, fieldname);
    }

    public static Object getStaticField(Class<?> clazz, String fieldname){
        return _getField(clazz, null, fieldname);
    }

    public static Object getEnumValue(Class<?> enumClazz, String enumValue){
        try {
            Method valueOf = enumClazz.getMethod("valueOf", String.class);
            return valueOf.invoke(enumClazz, enumValue);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            System.out.println("The enum value \""+enumValue+"\" of enum \""+enumClazz.getName()+"\" can't be found...\n");
            e.printStackTrace();
            return null;
        }
    }

}
