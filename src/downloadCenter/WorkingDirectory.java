package downloadCenter;

/**
 * Enumerated type designed to locate the working directory of this
 * application and keep track of/create any and all sub-directories. 
 * @author victoria
 *
 */
public enum WorkingDirectory {
	DOWNLOADS("/downloads/"),
	ERRORS("/errors/"),
	UNKNOWN("./"),
	STUB("/stub/");
	
	private String _workingDirectory = "";
	
	WorkingDirectory(String wd) {
		try {
			_workingDirectory = CommandExecutor.pwd() + wd;
			CommandExecutor.mkdir(_workingDirectory);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return _workingDirectory;
	}
}
