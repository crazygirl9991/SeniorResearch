package downloadCenter;

public enum WorkingDirectory {
	DOWNLOADS("/downloads/"),
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
			this._workingDirectory = ce.pwd() + tempWD;
			ce.mkdir(_workingDirectory);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return _workingDirectory;
	}
}
