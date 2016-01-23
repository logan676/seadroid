package com.seafile.seadroid2.cipher;

import com.google.common.io.Files;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Merge those chunks into a file
 *
 * see @link{http://stackoverflow.com/a/10864346/3962551}
 */
public class FileMerge {

    public static void mergeFiles(List<File> files, File into) throws IOException {
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(new FileOutputStream(into))) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }

    public static List<File> listOfFilesToMerge(File chunk) {
        String tmpName = chunk.getName(); //{name}.{number}
        String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.')); //remove .{number}
        File[] files = chunk.getParentFile().listFiles((File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
        Arrays.sort(files); //ensuring order 001, 002, ..., 010, ...
        return Arrays.asList(files);
    }

    public static void mergeFiles(File oneOfFiles, File into)
            throws IOException {
        mergeFiles(listOfFilesToMerge(oneOfFiles), into);
    }

    public static List<File> listOfFilesToMerge(String oneOfFiles) {
        return listOfFilesToMerge(new File(oneOfFiles));
    }

    public static void mergeFiles(String oneOfFiles, String into) throws IOException{
        mergeFiles(new File(oneOfFiles), new File(into));
    }
}
