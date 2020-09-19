package dev.brighten.anticheat.classloader;

import dev.brighten.anticheat.classloader.file.JarUtil;
import dev.brighten.anticheat.utils.SystemUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * Created on 28/08/2020 Package dev.brighten.anticheat.classloader
 */
public class KauriClassLoader extends URLClassLoader {

    private Map<String, byte[]> classBytes;

    public KauriClassLoader(URL url, java.lang.ClassLoader parent) {
        super(new URL[]{url}, parent);

        File jarFile = new File(url.getPath());

        if (!jarFile.exists()) {
            return;
        }

        //Load the class bytes from the encrypted file
        classBytes = JarUtil.loadNonClassEntries(jarFile);


        Map<String, byte[]> encryptedClasses = JarUtil.loadJar(jarFile);

        if (encryptedClasses == null) {
            return;
        }

        //Decrypt the bytes using our CRC32 method
        encryptedClasses.forEach((name, bytes) -> {

            String realName = name.replaceAll("/", ".").replaceAll("\\.class", "");


            byte[] nBytes = new byte[bytes.length];

            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                b ^= SystemUtil.CRC_32.getValue();
                b ^= SystemUtil.CRC_32.getValue() / 2;
                b ^= SystemUtil.CRC_32.getValue() / 3;
                b ^= SystemUtil.CRC_32.getValue() / 4;
                b ^= SystemUtil.CRC_32.getValue() / 5;
                nBytes[i] = b;
            }

            classBytes.put(realName, nBytes);
        });

        while (!jarFile.delete()) {
        }
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Class<?> loadClass(String name) {
        Class<?> aClass;
        try {
            aClass = super.loadClass(name);
        } catch (ClassNotFoundException e) {
            return loadClassFromMemory(name);
        }
        return aClass;
    }

    @Override
    protected Class<?> findClass(String name) {
        Class<?> aClass;
        try {
            aClass = super.findClass(name);
        } catch (ClassNotFoundException e) {
            return loadClassFromMemory(name);
        }
        return aClass;
    }

    private Class<?> loadClassFromMemory(String name) {
        byte[] bytes = classBytes.get(name);
        if (bytes != null) {
            // remove the encrypted class from the pool so it cant be dumped from the class pool
            classBytes.remove(name);

            // define the class in memory
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }
}
