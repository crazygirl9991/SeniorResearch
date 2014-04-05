package downloadCenter;

/**
 * Enumerated type designed to locate the working directory of this
 * application and keep track of/create any and all sub-directories. 
 * @author victoria
 *
 */
public enum WorkingDirectory {
	DOWNLOADS("/downloads/"),
	UNKNOWN("./"),
	STUB("stub/");
	
	private String _workingDirectory = "";
	
	WorkingDirectory(String wd) {
		_workingDirectory = wd;
	}
	
	public void Instantiate() {
		try {
			String tempWD = _workingDirectory;
			this._workingDirectory = CommandExecutor.pwd() + tempWD;
			CommandExecutor.mkdir(_workingDirectory);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return _workingDirectory;
	}
}
