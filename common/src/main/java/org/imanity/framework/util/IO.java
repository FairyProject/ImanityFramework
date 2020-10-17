package org.imanity.framework.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class IO {

    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    public static String readFile(File file) throws IOException
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( file.toPath(), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }

        return contentBuilder.toString();
    }

    public static void writeFile(File file, String string) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(string);

        writer.close();
    }
}
