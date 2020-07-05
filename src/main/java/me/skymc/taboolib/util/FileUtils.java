package me.skymc.taboolib.util;

import com.ilummc.tlib.util.IO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;


public class FileUtils {

	
	@SuppressWarnings("rawtypes")
	public static InputStream getResource(Class target, String filename) {
		try {
			final URL url = target.getClassLoader().getResource(filename);
			if (url == null) {
				return null;
			} else {
				final URLConnection connection = url.openConnection();
				connection.setUseCaches(false);
				return connection.getInputStream();
			}
		} catch (final IOException ignored) {
			return null;
		}
	}

	
	public static void inputStreamToFile(InputStream inputStream, File file) {
		try {
			final String text = new String(IO.readFully(inputStream), Charset.forName("utf-8"));
			final FileWriter fileWriter = new FileWriter(FileUtils.createNewFile(file));
			fileWriter.write(text);
			fileWriter.close();
		} catch (final IOException ignored) {
		}
	}

	
	public static File createNewFile(File file) {
		if (file != null && !file.exists()) {
			try {
				file.createNewFile();
			} catch (final Exception ignored) {
			}
		}
		return file;
	}

	
	public static void createNewFileAndPath(File file) {
		if (!file.exists()) {
			final String filePath = file.getPath();
			final int index = filePath.lastIndexOf(File.separator);
			File folder;
			if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
				folder.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (final IOException ignored) {
			}
		}
	}

}
