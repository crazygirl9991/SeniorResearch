package downloadCenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CommandExecutor {
	
	Runtime _runtime;
	
	
	public CommandExecutor() {
		_runtime = Runtime.getRuntime();
	}
	
	/**
	 * TODO make portable
	 * A function designed to access a command line and download files using WGET.
	 * @param wgetFilePath - should be a ".lis" file with URLs WGET can locate and download.
	 * @param destinationDirectory - should be an already existing directory (including filepath if necessary).
	 * @throws IOException
	 */
	public void wget(String wgetFilePath, String destinationDirectory) throws UnsupportedOperationException {
		try {
			String command = "wget -nd -N -i " + wgetFilePath + " -P " + destinationDirectory;
			_runtime.exec(command);
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
	public void copy(String source, String target) throws UnsupportedOperationException {
		try {
			String command = "cp " + source + " " + target;
			_runtime.exec(command);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't convert file to WGET required formatting.", e) );
		}
	}
	
	/**
	 * TODO make portable
	 * Removes a file.
	 * @param filename - should include path if necessary.
	 * @throws IOException
	 */
	public void remove(String filename) throws UnsupportedOperationException {
		try {
			String command = "rm " + filename;
			_runtime.exec(command);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Can't remove file.", e) );
		}
	}
	
	/**
	 * Creates a directory at specified path.
	 * @param directoryName - should include path if necessary.
	 * @throws IOException
	 */
	public void mkdir(String directoryName) throws UnsupportedOperationException {
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
	public String pwd() throws UnsupportedOperationException {
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
	 * @param filename
	 * @throws UnsupportedOperationException
	 */
	public void createFile(String filename) throws UnsupportedOperationException {
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
	 * do i still need this? TODO
	 * @return
	 */
	public Runtime getRuntime() {
		return _runtime;
	}

}
