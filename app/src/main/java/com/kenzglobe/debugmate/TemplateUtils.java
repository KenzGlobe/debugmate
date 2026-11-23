package com.kenzglobe.debugmate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TemplateUtils {

    public static void writeFile(File parent, String name, String content) throws IOException {
        File file = new File(parent, name);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
        } finally {
            if (writer != null) writer.close();
        }
    }

    public static File createDirectory(File parent, String name) throws IOException {
        File directory = new File(parent, name);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory: " + name);
        }
        return directory;
    }
}