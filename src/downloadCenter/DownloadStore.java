package downloadCenter;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public abstract class DownloadStore {
	
	private String _type = "";
	protected String _inputfile = "";
	protected String _outputfile;
	protected List<String> _list;
	
	protected String _workingDir = (WorkingDirectory.DOWNLOADS).toString();
	
	/**
	 * Takes the name of an input file and produces the name of an output file.
	 * Also sets the two member variables for these values.
	 * @param inputfile can be any extension, and if none is provided, "default" will be used.
	 * @return Returns outputfile in ".lis" format with a unique identifier added (ddMMyyyy-HHmmss).
	 */
	public String Rename() {
	    return Rename("");
	}

	/**
	 * Takes the name of an input file and produces the name of an output file.
	 * Also sets the two member variables for these values.
	 * @param inputfile can be any extension, and if none is provided, "default" will be used.
	 * @return Returns outputfile in ".lis" format with a unique identifier added (ddMMyyyy-HHmmss).
	 */
	public String Rename(String inputfile) {
		String basefilename;
		
		if( inputfile.equals("") ) {
			basefilename = "default";
		} else {
			int indexOfExt = inputfile.lastIndexOf('.');
			basefilename = inputfile.substring(0, indexOfExt);
		}

	    // use calendar to create unique file ID
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy-HHmmss"); 
		String dateStr = sdf.format(cal.getTime());
		
		_inputfile = inputfile;
		_outputfile = basefilename + dateStr + ".lis";

	    return _outputfile;    
	}

	/**
	 * Converts input (either a file, set of coordinates, or MJD,plate,fiber specification) 
	 * into a file format wget can use. 
	 * @Note Override this function in concrete classes if adding new input formats.
	 */
	public abstract void Write(CommandExecutor ce) throws IOException;
	
	
	/**
	 * Runs a WGET query with specified "downloads" directory (contained in WorkingDirectory.java).
	 * Requires formatted outputfile (using Rename() and then Write(), found in this class).
	 * @throws IOException
	 */
	public void Download(CommandExecutor ce) throws IOException {
		String path = _workingDir + _outputfile;
		ce.wget(path, _workingDir);
	}
	
	/**
	 * Imports each filename stored in outputfile one at a time and creates a
	 * table element for that fits file. Then it calls a table element function to
	 * check for matches 
	 * @throws Exception 
	 */
	public void UpdateTable() throws Exception {
		Scanner scanner = new Scanner( new FileReader(_outputfile) );
		
		while( scanner.hasNext() ) {
			TableElement element = new TableElement( _workingDir + scanner.nextLine() );
			element.SaveToTable();
		}
		
		scanner.close();
	}
	
	public void Clean(CommandExecutor ce) throws IOException {
		String outputfilePath = _workingDir + _outputfile;
		ce.remove(outputfilePath);
	}
	
	
	/* Getters and Setters */
	public String getType() { return _type; }
	protected void setType(String type) { _type = type; }
	public String getInputfile() { return _inputfile; }
	public String getOutputfile() { return _outputfile; }

}
