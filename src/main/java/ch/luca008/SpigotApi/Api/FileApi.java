package ch.luca008.SpigotApi.Api;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileApi {

    /**
     * List all files inside a jar
     * @param pluginClass A class of your jar
     * @return A list of string with all files. May be empty if fails.
     */
    public static List<String> listFiles(Class<?> pluginClass){
        List<String> files = new ArrayList<>();
        try {
            CodeSource src = pluginClass.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while(true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    files.add(e.getName());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return files;
    }

    /**
     * Export a file from inside your jar to where you want outside.
     * @param parentClass A class of your jar
     * @param insidePath The destination file (can contain directories) inside your jar you want to export
     * @param outsidePath The outside jar destination where you want your file (Directories are created)
     * @return if the file has been extracted or not
     * @throws IOException if the program fails to read the bytes of the inside file
     */
    public static boolean exportFile(Class<?> parentClass, String insidePath, File outsidePath) throws IOException {
        if(!outsidePath.getParentFile().exists() && !outsidePath.getParentFile().mkdirs()){
            return false;
        }
        Path path = Paths.get(outsidePath.toURI());
        InputStream is = parentClass.getClassLoader().getResourceAsStream(insidePath);
        if(is != null){
            byte[] stream = readAllBytes(is);
            is.close();
            if(stream != null){
                Files.write(path, stream, StandardOpenOption.CREATE);
                return true;
            }
        }
        return false;
    }

    private static byte[] readAllBytes(InputStream is) {
        try {
            byte[] data = new byte[is.available()];
            DataInputStream dis = new DataInputStream(is);
            dis.readFully(data);
            dis.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}