import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Item.Item;
import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.SpigotApi.Item.Meta.LeatherArmor;
import ch.luca008.SpigotApi.Item.Meta.TrimArmor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class LeatherArmorTests {

    public void _testArmorWithoutMeta()
    {
        Item i = new ItemBuilder()
                .setMaterial(Material.LEATHER_CHESTPLATE)
                .createItem();
        ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE,1);
        System.out.println(JSONApi.prettyJson(i.toJson()));
        System.out.println("testArmorWithoutMeta: " + i.isSimilar(is));
    }

    public void _testArmorWithColorMeta()
    {
        Item i = new ItemBuilder()
                .setMaterial(Material.LEATHER_CHESTPLATE)
                .setMeta(new LeatherArmor(Color.BLUE, null, null))
                .createItem();
        ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE,1);
        LeatherArmorMeta meta = (LeatherArmorMeta)is.getItemMeta();
        meta.setColor(Color.BLUE);
        is.setItemMeta(meta);
        System.out.println(JSONApi.prettyJson(i.toJson()));
        System.out.println("testArmorWithColorMeta1: " + i.isSimilar(is));
        Item i2 = Item.fromJson("{\"Material\":\"LEATHER_CHESTPLATE\",\"ItemMeta\":{\"Meta\":{\"Color\":{\"r\":0,\"b\":255,\"g\":0}},\"MetaType\":\"LeatherArmor\"}}");
        System.out.println(i2.toJson());
        System.out.println("testArmorWithColorMeta2: " + i2.isSimilar(is));
    }

    public void _testArmorWithTrimMeta()
    {
        Item i = new ItemBuilder()
                .setMaterial(Material.LEATHER_CHESTPLATE)
                .setMeta(new LeatherArmor(null, TrimPattern.EYE, TrimMaterial.AMETHYST))
                .createItem();
        ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE,1);
        ArmorMeta meta = (ArmorMeta)is.getItemMeta();
        meta.setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.EYE));
        is.setItemMeta(meta);
        System.out.println(JSONApi.prettyJson(i.toJson()));
        System.out.println("testArmorWithTrimMeta1: " + i.isSimilar(is));
        Item i2 = Item.fromJson("{\"Material\":\"LEATHER_CHESTPLATE\",\"ItemMeta\":{\"Meta\":{\"Color\":{\"r\":160,\"b\":64,\"g\":101},\"TrimArmor\":{\"Pattern\":\"eye\",\"Material\":\"amethyst\"}},\"MetaType\":\"LeatherArmor\"}}");
        System.out.println(i2.toJson());
        System.out.println("testArmorWithTrimMeta2: " + i2.isSimilar(is));
    }

    public void _testArmorWithMeta()
    {
        Item i = new ItemBuilder()
                .setMaterial(Material.LEATHER_CHESTPLATE)
                .setMeta(new LeatherArmor(Color.RED, TrimPattern.EYE, TrimMaterial.AMETHYST))
                .createItem();
        ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE,1);
        ColorableArmorMeta meta = (ColorableArmorMeta)is.getItemMeta();
        meta.setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.EYE));
        meta.setColor(Color.RED);
        is.setItemMeta(meta);
        System.out.println(JSONApi.prettyJson(i.toJson()));
        System.out.println("testArmorWithMeta1: " + i.isSimilar(is));
        Item i2 = Item.fromJson("{\"Material\":\"LEATHER_CHESTPLATE\",\"ItemMeta\":{\"Meta\":{\"Color\":{\"r\":255,\"b\":0,\"g\":0},\"TrimArmor\":{\"Pattern\":\"eye\",\"Material\":\"amethyst\"}},\"MetaType\":\"LeatherArmor\"}}");
        System.out.println(i2.toJson());
        System.out.println("testArmorWithMeta2: " + i2.isSimilar(is));
    }

    public void _testArmor()
    {
        Item i = new ItemBuilder()
                .setMaterial(Material.DIAMOND_CHESTPLATE)
                .setMeta(new TrimArmor(TrimMaterial.COPPER, TrimPattern.RIB))
                .createItem();
        ItemStack is = new ItemStack(Material.DIAMOND_CHESTPLATE,1);
        ArmorMeta meta = (ArmorMeta)is.getItemMeta();
        meta.setTrim(new ArmorTrim(TrimMaterial.COPPER, TrimPattern.RIB));
        is.setItemMeta(meta);
        System.out.println(JSONApi.prettyJson(i.toJson()));
        System.out.println("testArmor1: " + i.isSimilar(is));
        Item i2 = Item.fromJson("{\"Material\":\"DIAMOND_CHESTPLATE\",\"ItemMeta\":{\"Meta\":{\"Pattern\":\"rib\",\"Material\":\"copper\"},\"MetaType\":\"TrimArmor\"}}");
        System.out.println(i2.toJson());
        System.out.println("testArmor2: " + i2.isSimilar(is));
    }

}
