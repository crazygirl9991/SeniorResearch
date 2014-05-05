package downloadCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Contains the logic to make changes to the user's system, e.g. by creating or
 * removing files and directories, or using wget to download files from the SDSS
 * servers.
 * 
 * @author victoria
 * 
 */
public class CommandExecutor {

	/**
	 * TODO
	 * 
	 * @param wgetFilePath
	 *            - should be a ".lis" file with URLs WGET can locate and
	 *            download.
	 * @param destinationDirectory
	 *            - should be an already existing directory (including filepath
	 *            if necessary).
	 * @throws InterruptedException 
	 */
	public static void get(ArrayList<String> getUrls, final String destinationDirectory) throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(30);
		for (final String str : getUrls) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						File destination = new File(destinationDirectory, str.substring(str.lastIndexOf("/") + 1));
						if(destination.exists())
							return;
						URL url = new URL(str);
						HttpURLConnection connect = (HttpURLConnection) url.openConnection();
						Files.copy(connect.getInputStream(), destination.toPath()); //StandardCopyOption.REPLACE_EXISTING
						connect.disconnect();
					} catch (FileAlreadyExistsException f) {
						 
					} catch (Exception e) {
						ErrorLogger.update("ERROR: Can't retrieve files.", e);
					}
				}
			});
		}
		pool.shutdown();
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/**
	 * TODO make portable Copies and renames a source file from any path to any
	 * destination (so long as administrative access is not required).
	 * 
	 * @param source
	 *            - source file, including path if necessary
	 * @param target
	 *            - target file name and directory path
	 */
	public static void copy(String source, String target) {
		try {
			File file1 = new File(source), file2 = new File(target);
			file1.renameTo(file2);
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't copy file " + source + " as file " + target, e);
		}
	}

	/**
	 * Removes a file.
	 * 
	 * @param filename
	 *            - should include path if necessary.
	 */
	public static void remove(String filename) {
		try {
			File file = new File(filename);
			file.delete();
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't remove file " + filename, e);
		}
	}

	/**
	 * Creates a directory at specified path.
	 * 
	 * @param directoryName
	 *            - should include path if necessary.
	 */
	public static void mkdir(String directoryName) {
		try {
			File dir = new File(directoryName);
			dir.mkdirs();
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Could not make file or directory.", e);
		}
	}

	/**
	 * Returns the current working directory.
	 * 
	 */
	public static String pwd() {
		String pwd = "";
		try {
			pwd = System.getProperty("user.dir");
		} catch (Exception e) {
			ErrorLogger.update("ERROR: PWD command failed.", e);
		}
		
		return pwd;
	}

	/**
	 * TODO document
	 */
	public static ArrayList<String> importFile(String filename) {
		ArrayList<String> strings = new ArrayList<String>();

		try {
			Scanner scanner = new Scanner(new FileReader(filename));

			while (scanner.hasNextLine())
				strings.add(scanner.nextLine());

			scanner.close();
		} catch (FileNotFoundException e) {
			ErrorLogger.update("ERROR: Can't import file: " + filename, e);
		}

		return strings;
	}

	/**
	 * TODO document
	 * 
	 * @param filename
	 */
	public static void write(String filename, ArrayList<String> lines) {
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(filename) );
			
			for(String current : lines) {
				writer.write(current);
				writer.newLine();
			}
			
			writer.close();
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't create file: " + filename, e);
		}
	}
}
