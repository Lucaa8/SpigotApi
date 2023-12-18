package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.EntityPackets;
import ch.luca008.SpigotApi.Utils.Logger;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionApi {


    //Was used for NMS before 1.17, and still used for org.bukkit.craftbukkit packages. E.G: v1_20_R3
    private static final String LEGACY_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    public enum Version{
        MC_1_20(763, "(MC: 1.20.1)"),
        MC_1_20_2(764, "(MC: 1.20.2)"),
        MC_1_20_3(765, "(MC: 1.20.3)", "(MC: 1.20.4)");

        final String[] versions;
        final int protocol;

        Version(int protocol, String...versions)
        {
            this.protocol = protocol;
            this.versions = versions;
        }

        public int getProtocol(){return protocol;}

        @Nullable
        public static Version getVersion(String v)
        {
            if(v == null || v.equals(""))
                return null;
            for(Version ver : Version.values())
            {
                for(String strVer : ver.versions)
                {
                    if(v.contains(strVer))
                        return ver;
                }
            }
            return null;
        }

    }

    public final static Version SERVER_VERSION;
    private final static Map<String,ClassMapping> mappings = new HashMap<>();

    static {

        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            ReflectionApi.theUnsafe = theUnsafeField.get(null);
            ReflectionApi.alloc = ReflectionApi.theUnsafe.getClass().getMethod("allocateInstance", Class.class);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException ex) {
            Logger.error("Failed to get the unsafe field and/or the allocateInstance method from sun.misc.Unsafe class. Some of the functionalities like Team, Scoreboard, NPC, etc.. Api wont be enabled.");
            ex.printStackTrace();
        }

        SERVER_VERSION = Version.getVersion(Bukkit.getServer().getVersion());
        if(SERVER_VERSION == null)
        {
            Logger.error("Failed to find a suitable MC version for SpigotApi. Some of the functionalities like Team, Scoreboard, NPC, etc.. Api wont be enabled.");
        } else {
            Logger.info("Found Minecraft server with protocol version " + SERVER_VERSION.protocol + " which SpigotApi can use to deliver your code a lot of packets!");
            mappings.putAll(EntityPackets.getMappings());
            //add all mappings, do not forget to put the classname before the packet name for the key. E.G EntityPackets_AddEntity (to avoid dup keys if TeamsPackets also have a AddEntity packet to add an entity in a team)
        }

    }

    private static Object theUnsafe;
    private static Method alloc;

    @Nullable
    public static ClassMapping getMappingForClass(String clazz)
    {
        return mappings.get(clazz);
    }

    private static Class<?> getClazz(String pathToClass) {
        try {
            return Class.forName(pathToClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * To get more information about the params check {@link ReflectionApi#getNMSClass(String, String)}
     * @param packageName x
     * @param className y
     * @return The Class if found or else null
     */
    @Nullable
    public static Class<?> getOBCClass(@Nullable String packageName, String className){
        return getClazz("org.bukkit.craftbukkit." + LEGACY_VERSION + "." + ((packageName == null || packageName.equals("") ? "":packageName+".")) + className);
    }

    /**
     * Must be used only if your plugin runs on an old build of spigot (before 1.17 not included). For 1.17 and newer check {@link ReflectionApi#getNMSClass(String, String)}
     * @param className The class name you want to get in the old (before 1.17) net.minecraft.server.VERSION package
     * @return The Class if found or else null
     */
    @Nullable
    public static Class<?> getLegacyNMSClass(String className) {
        return getClazz("net.minecraft.server." + LEGACY_VERSION + "." + className);
    }

    /**
     * Must be used only if your plugin runs on a new build of spigot (after 1.17 included). For 1.16 and older check {@link ReflectionApi#getLegacyNMSClass(String)}
     * @param packageName a list of subpackages split with a point between them. E.g to get the Packet interface you'll need to specify "network.protocol" in this field. If set to null or empty then no subpackage will be searched
     * @param className the final class name
     * @return The Class if found or else null
     */
    @Nullable
    public static Class<?> getNMSClass(@Nullable String packageName, String className) {
        return getClazz("net.minecraft." + ((packageName == null || packageName.equals("") ? "":packageName+".")) + className);
    }

    @Nullable
    public static Class<?> getPrivateInnerClass(Class<?> outerClass, String innerClassName){
        for(Class<?> c : outerClass.getDeclaredClasses()){
            if(c.getSimpleName().equals(innerClassName)){
                return c;
            }
        }
        return null;
    }

    public static Object invoke(Class<?> methodClass, Object objectToInvoke, String methodName, @Nullable Class<?>[] methodArgsType, @Nullable Object...methodArgs) {
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

    public static Object unsafe_allocInstance(Class<?> clazz)
    {
        try {
            return alloc.invoke(theUnsafe, clazz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setField(Object o, String fieldname, Object value) {
        setField(o.getClass(), o, fieldname, value);
    }

    public static void setField(Class<?> clazz, Object o, String fieldname, Object value) {
        try {
            Field f = clazz.getDeclaredField(fieldname);
            f.setAccessible(true);
            f.set(o, value);
            f.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getField(Class<?> clazz, Object object, String field){
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
        return getField(o.getClass(), o, fieldname);
    }

    public static Object getStaticField(Class<?> clazz, String fieldname){
        return getField(clazz, null, fieldname);
    }

    public static Object getEnumValue(Class<?> enumClazz, String enumValue){
        try {
            Method valueOf = enumClazz.getDeclaredMethod("valueOf", String.class);
            valueOf.setAccessible(true);
            return valueOf.invoke(enumClazz, enumValue);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public record ApiField(String fieldName, boolean isStaticField){
        public ApiField(String fieldName) {
            this(fieldName, false);
        }
    }

    public static class ClassMapping {

        private final Map<String, ApiField> classFields;
        private final Map<String, String> classMethods;
        private final Class<?> clazz;

        public ClassMapping(Class<?> clazz, Map<String,ApiField> fields, Map<String,String> methods)
        {
            this.clazz = clazz;
            this.classFields = fields;
            this.classMethods = methods;
        }

        public ObjectMapping unsafe_newInstance()
        {
            return new ObjectMapping(this, ReflectionApi.unsafe_allocInstance(this.clazz));
        }

        public ObjectMapping newInstance()
        {
            return new ObjectMapping(this, ReflectionApi.newInstance(this.clazz));
        }

        public ObjectMapping newInstance(Class<?>[] constructorArgsType, Object...constructorArgs)
        {
            return new ObjectMapping(this, ReflectionApi.newInstance(this.clazz, constructorArgsType, constructorArgs));
        }

        @Nullable
        public Object invoke(@Nullable Object objectToInvoke, String methodName)
        {
            return this.invoke(objectToInvoke, methodName, new Class[0]);
        }

        @Nullable
        public Object invoke(@Nullable Object objectToInvoke, String methodName, @Nullable Class<?>[] methodArgsType, @Nullable Object...methodArgs)
        {
            if(!classMethods.containsKey(methodName))
                return null;
            return ReflectionApi.invoke(this.clazz, objectToInvoke, classMethods.get(methodName), methodArgsType, methodArgs);
        }

        @Nullable
        private ApiField getField(String name)
        {
            return classFields.get(name);
        }

        @Nullable
        public Object getFieldValue(String field, @Nullable Object obj)
        {
            ApiField f = getField(field);
            if(f==null)
                return null;
            if(f.isStaticField())
                return getStaticField(clazz, f.fieldName());
            if(obj == null)
                return null;
            return ReflectionApi.getField(clazz, obj, f.fieldName());
        }

        public void setFieldValue(String field, @Nullable Object obj, Object value)
        {
            ApiField f = getField(field);
            if(f==null)
                return;
            if(f.isStaticField())
                setField(clazz, null, f.fieldName(), value);
            if(obj == null)
                return;
            setField(clazz, obj, f.fieldName(), value);
        }

    }

    public record ObjectMapping(ClassMapping mapping, Object packet){

        public void set(String field, Object value){
            mapping.setFieldValue(field, packet, value);
        }

        @Nullable
        public Object get(String field){
            return mapping.getFieldValue(field, packet);
        }

        @Nullable
        public Object invoke(String methodName){
            return mapping.invoke(packet, methodName);
        }

        @Nullable
        public Object invoke(String methodName, @Nullable Class<?>[] methodArgsType, @Nullable Object...methodArgs)
        {
            return mapping.invoke(packet, methodName, methodArgsType, methodArgs);
        }

    }

}
