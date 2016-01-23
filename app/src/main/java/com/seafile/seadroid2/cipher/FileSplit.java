package com.seafile.seadroid2.cipher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Read portions of bytes to byte array and store them in chunks when buffer is full or it is end of file
 */
public class FileSplit {

    public static void splitFile(File f) {
        int partCounter = 1; //I like to name parts from 001, 002, 003

        int sizeOfChunks = 1024 * 1024 * 2; // 2MB
        byte[] buffer = new byte[sizeOfChunks];

        BufferedInputStream bis = null;
        FileOutputStream out = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            // try-with-resources to ensure closing stream
            String name = f.getName();

            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                // write each chunk of data into separate file with different number in name
                File newFile = new File(f.getParent(), name + "." + String.format("%03d", partCounter++));
                out = new FileOutputStream(newFile);
                out.write(buffer, 0, tmp);//tmp is chunk size
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
