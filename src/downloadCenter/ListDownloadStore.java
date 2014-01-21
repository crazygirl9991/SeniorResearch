package downloadCenter;

import java.io.IOException;

public class ListDownloadStore extends DownloadStore {
	public ListDownloadStore(String inputFileName) {
		setType("list");
		Rename(inputFileName);
	}
	
	/**
	 * Copies the inputfile and renames it in accordance with this software's 
	 * naming convention.
	 */
	@Override
	public void Write(CommandExecutor ce) throws IOException {
		String target =  _workingDir + _outputfile;
		ce.copy(_inputfile, target);
	}
	
}
