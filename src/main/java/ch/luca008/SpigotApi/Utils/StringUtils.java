package ch.luca008.SpigotApi.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String addCap(String s){
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String enumName(String name){
        if(!name.contains("_"))return addCap(name);
        String finalName = "";
        for(String s : name.split("_")){
            finalName+=addCap(s)+" ";
        }
        return finalName.substring(0,finalName.length()-1);
    }

    public static String enumName(Enum<?> e){
        return enumName(e.name());
    }

    public static boolean equalLists(List<String> a, List<String> b){
        if (a == null && b == null) return true;
        if (((a==null)!=(b==null)) || (a.size() != b.size()))return false;
        return a.equals(b);
    }

    @Nonnull
    public static List<String> asLore(@Nullable String lore){
        if(lore==null||lore.isEmpty())return new ArrayList<>();
        lore = lore.replace("&","§");
        List<String> loreArray = new ArrayList<>();
        String lastClr = "";
        if(lore.contains("\n")){
            for(String s : lore.split("\\n")){
                String line;
                if(!lastClr.isEmpty()){
                    line=lastClr+s;
                }else line=s;
                loreArray.add(line);
                for(int i=0;i<line.length()-1;i++){
                    char id = line.charAt(i);
                    char code = line.charAt(i+1);
                    if(id=='§'&&code!='§'){
                        if(lastClr.length()>=4){
                            lastClr = ""+id+code;
                        }else{
                            lastClr+=""+id+code;
                        }
                    }
                }
            }
        }else loreArray.add(lore);
        return loreArray;
    }

}
