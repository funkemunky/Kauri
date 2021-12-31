package dev.brighten.anticheat.classloader.file;

import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class JarUtil {

    private JarUtil() {
    }

    public static Map<String, byte[]> loadJar(File jarFile) {
        try {
            Map<String, byte[]> classes = new HashMap<>();
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                readJar(jar, entry, classes, null);
            }
            jar.close();
            return classes;
        } catch (ZipException e) {
            MiscUtils.printToConsole("&cKauri Ara License is not valid!");
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String, byte[]> readJar(JarFile jar, JarEntry en, Map<String, byte[]> classes, List<String> ignored) {
        String name = en.getName();
        try (InputStream jis = jar.getInputStream(en)) {
            if (name.endsWith(".class")) {
                if (ignored != null) {
                    for (String s : ignored) {
                        if (name.startsWith(s)) {
                            return classes;
                        }
                    }
                }
                byte[] bytes = getBytes(jis);
                try {
                    classes.put(name, bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static Map<String, byte[]> loadNonClassEntries(File jarFile) {
        Map<String, byte[]> entries = new HashMap<>();
        try {
            ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
            ZipEntry entry;
            while ((entry = jis.getNextEntry()) != null) {
                try {
                    String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        entries.put(name, getBytes(jis));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    jis.closeEntry();
                }
            }
            jis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static byte[] getBytes(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
