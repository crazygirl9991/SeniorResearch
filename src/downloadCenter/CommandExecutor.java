package downloadCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Contains the logic to make changes to the user's system, e.g. by creating or removing
 * files and directories, or using wget to download files from the SDSS servers.
 * @author victoria
 *
 */
public class CommandExecutor {
	
	/**
	 * TODO make portable
	 * A function designed to access a command line and download files using WGET.
	 * @param wgetFilePath - should be a ".lis" file with URLs WGET can locate and download.
	 * @param destinationDirectory - should be an already existing directory (including filepath if necessary).
	 * @throws IOException
	 */
	public static void wget(ArrayList<String> getUrls, String destinationDirectory) throws UnsupportedOperationException {
		try { 
//			[6:25:32 PM] Leland Machen: import java.io.File;
//			import java.net.HttpURLConnection;
//			import java.net.URL;
//			import java.nio.file.Files;
//			import java.util.Arrays;
//			import java.util.List;
//
//			public class ReadFiles {
//
//			 public static void main(String[] args) throws Exception {
//			  List<String> names = Arrays.asList("http://i.imgur.com/LxtERwz.jpg");
//			  for(String name : names)
//			  {
//			   URL url = new URL(name);
//			   HttpURLConnection connect = (HttpURLConnection) url.openConnection();
//			   Files.copy(connect.getInputStream(),new File("output.jpg").toPath());
//			  }
//			 }
//			}
			//TODO implement it this way instead of other way
//			for( String current : getUrls ) {
//				URL url = new URL(current);
//				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//				connection.setRequestMethod("GET");
//			
//				InputStream data = connection.getInputStream();
//			}
			// "-i" => from specified file, "-N" => turn on time stamping, "-nd" => store all downloaded //
			// files in current or specified directory with no hierarchies, "-P" => specifies a destination directory //
			String wgetFilePath = "wgetDownloadUrls.lis";
			writeFile(wgetFilePath, getUrls);
			
			String command = "wget -nd -N -i " + wgetFilePath + " -P " + destinationDirectory;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
			
			remove(wgetFilePath);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't convert file to WGET required formatting.", e) );
		}
	}
	
	/**
	 * TODO make portable
	 * Copies and renames a source file from any path to any destination (so long as administrative
	 * access is not required).
	 * @param source - source file, including path if necessary
	 * @param target - target file name and directory path
	 * @throws IOException
	 */
	public static void copy(String source, String target) throws UnsupportedOperationException {
		try {
			File file1 = new File(source), file2 = new File(target);
			file1.renameTo(file2);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't copy file " + source + " as file " + target, e) );
		}
	}
	
	/**
	 * Removes a file.
	 * @param filename - should include path if necessary.
	 * @throws IOException
	 */
	public static void remove(String filename) throws UnsupportedOperationException {
		try {
			File file = new File(filename);
			file.delete();
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't remove file " + filename, e) );
		}
	}
	
	/**
	 * Creates a directory at specified path.
	 * @param directoryName - should include path if necessary.
	 * @throws IOException
	 */
	public static void mkdir(String directoryName) throws UnsupportedOperationException {
		try {
			File dir = new File(directoryName);
			dir.mkdirs();
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Could not make file or directory.", e) );
		}
	}
	
	/**
	 * Returns the current working directory.
	 * @throws IOException
	 */
	public static String pwd() throws UnsupportedOperationException {
		String pwd = "";
		try {
			pwd = System.getProperty("user.dir");
		}  catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: PWD command failed.", e) );
		}
		return pwd;
	}
	
	/**
	 * TODO document
	 */
	public static ArrayList<String> importFile(String filename) throws UnsupportedOperationException {
		ArrayList<String> strings = new ArrayList<String>();
		
		try {
			Scanner scanner = new Scanner( new FileReader(filename) );
			
			while( scanner.hasNextLine() )
				strings.add( scanner.nextLine() );
			
			scanner.close();	
		} catch (FileNotFoundException e) {
			throw ( new UnsupportedOperationException("ERROR: Can't import file: " + filename, e) );
		}
		
		return strings;
	}
	
	/**
	 * TODO document
	 * @param filename
	 * @throws UnsupportedOperationException
	 */
	public static void createFile(String filename) throws UnsupportedOperationException {
		try {
			String output = "## File has not yet been written ##";
			BufferedWriter writer = new BufferedWriter( new FileWriter(filename) );
			writer.write(output);
			writer.close();
		}  catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't create file: " + filename, e) );
		}
	}
	
	/**
	 * TODO
	 * @param filename
	 * @param data
	 */
	public static void writeFile(String filename, ArrayList<String> data) {
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(filename) );
			
			for( String current : data )
				writer.write(current);
			
			writer.close();
		}  catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't write file: " + filename, e) );
		}
	}
}
