package dev.brighten.anticheat.utils.file;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class FileDownloader {

    private URL link;
    private File realFile;

    private int downloadCounter;

    private boolean silentDownload;

    public FileDownloader() {
        //
    }

    public FileDownloader(String link) {
        try {
            this.link = new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public File download() {

        InputStream inputStream = null;
        File file = null;
        FileOutputStream fos = null;
        try {

            file = File.createTempFile(String.valueOf(ThreadLocalRandom.current().nextInt(99999999)), "");
            fos = new FileOutputStream(file);
            URLConnection urlConn = link.openConnection();
            urlConn.setConnectTimeout(1000);
            urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            inputStream = urlConn.getInputStream();

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.realFile = file;
        return file;
    }
}