package downloadCenter;

import java.io.File;

/**
 * Enumerated type designed to locate the working directory of this
 * application and keep track of/create any and all sub-directories. 
 * @author victoria
 *
 */
public enum WorkingDirectory {
	DOWNLOADS("downloads/"),
	ERRORS("errors/"),
	UNKNOWN("./"),
	STUB("stub/");
	
	private String _workingDirectory = "";
	
	WorkingDirectory(String wd) {
		try {
			_workingDirectory = wd;
			File dir = new File(_workingDirectory);
			dir.mkdirs();
		} catch(Exception e) {
			ErrorLogger.update("ERROR: Could not make file or directory.", e);
			//TODO used logger
		}
	}
	
	public String toString() {
		return _workingDirectory;
	}
}
