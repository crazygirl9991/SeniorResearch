package downloadCenter;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public enum WorkingDirectory {
	DOWNLOADS( "/downloads/"),
	UNKNOWN("/./"),
	STUB("/stub/");
	
	private String _workingDirectory = "";
	
	WorkingDirectory(String wd) {
		_workingDirectory = wd;
	}
	
	public void Instantiate() {
		CommandExecutor ce = new CommandExecutor();
		try {
			String tempWD = _workingDirectory;
			this._workingDirectory = Pwd(ce) + tempWD;
			findOrCreate(ce);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return _workingDirectory;
	}
	
	/**
	 * Searches for directory by piping find results to a file.
	 * If the file is empty, then nothing was found.
	 * TODO make less sloppy if possible
	 */
	private void findOrCreate(CommandExecutor ce) throws Exception {
		
		try {
			String tempFile = "doesDirectoryExist.txt";
			ce.find(_workingDirectory, tempFile);
			
			Scanner scanner = new Scanner( new FileReader(tempFile) );
			if( scanner.hasNext() ) { // if file has lines...
				Boolean found = false;
				while( scanner.hasNext() || !found ) { 
					// ...verify that one of those lines actually contains the intended directory
					String shouldContainWorkingDir = scanner.nextLine();
					found = shouldContainWorkingDir.contains(_workingDirectory);
				}
				
				if( !found ) { // if the string is not contained, find must have malfunctioned.
					scanner.close();
					throw (new Exception("ERROR: Find malfunctioned.") );
				}
			} else { // no lines => nothing was found by 'find'
				ce.mkdir(_workingDirectory);
			}
			
			scanner.close();
			
			ce.remove(tempFile);
		} catch (Exception e) {
			 throw ( new Exception("ERROR: Could not find or create directory: " + _workingDirectory, e) );
		}
	}
			
	/**
	 * Determines the current directory using command 'pwd'.
	 * @throws IOException
	 */
	public static String Pwd(CommandExecutor ce) throws Exception {
		String pwd = "";
		
		try {
			String tempFile = "pwdOutput.txt";
			ce.pwdToFile(tempFile);
			
			Scanner scanner = new Scanner( new FileReader(tempFile) );
			if( scanner.hasNext() ) {
				pwd = scanner.nextLine();
			}
			scanner.close();
			
			ce.remove(tempFile);
		} catch (Exception e) {
			 throw ( new Exception("ERROR: Can't determine working directory.", e) );
		}
		
		// removes newline if it exists (sometimes it's added automatically)
		int indexOfNewLine = pwd.indexOf("\n");
		if( indexOfNewLine > 0 )
			pwd.substring(0, indexOfNewLine-1);
		return pwd;
	}
}
