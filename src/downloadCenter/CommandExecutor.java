package downloadCenter;

import java.io.IOException;

public class CommandExecutor {
	
	Runtime _runtime;
	
	
	public CommandExecutor() {
		_runtime = Runtime.getRuntime();
	}
	
	/**
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
	 * Finds all related filenames and displays on terminal window.
	 * @param filename - expected file/directory name, which should include path if necessary.
	 * @throws IOException
	 */
	public void find(String filename) throws UnsupportedOperationException {
		try {
			String command = "find " ;
			_runtime.exec(command);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Find malfunctioned.", e) );
		}
	}
	
	/**
	 * Finds all related filenames and prints them in given pipe file.
	 * @param filename - expected file/directory location, which should include path if necessary.
	 * @param pipeFile - file to which pipe results should be directed, which should include path if necessary.
	 * @throws IOException
	 */
	public void find(String filename, String pipeFile) throws UnsupportedOperationException {
		String command = "find " + filename + " > " + pipeFile;
		find(command);
	}
	
	/**
	 * Creates a directory at specified path.
	 * @param directoryName - should include path if necessary.
	 * @throws IOException
	 */
	public void mkdir(String directoryName) throws UnsupportedOperationException {
		try {
			String command = "mkdir " + directoryName;
			_runtime.exec(command);
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: Could not make file or directory.", e) );
		}
	}
	
	/**
	 * Determines the current working directory, storing the results in given filename.
	 * @param filename - file to which pipe results should be directed, which should include path if necessary.
	 * @throws IOException
	 */
	public void pwdToFile(String filename) throws UnsupportedOperationException {
		try {
			String command = "pwd > " + filename;
			_runtime.exec(command);
		}  catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: PWD command failed.", e) );
		}
	}
	
	public Runtime getRuntime() {
		return _runtime;
	}

}
