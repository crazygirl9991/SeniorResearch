package downloadCenter;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import nom.tam.fits.*;

public abstract class DownloadStore {
	
	private String _type = "";
	protected String _inputfile = "";
	protected String _outputfilePath;
	protected List<String> _list;
	
	protected WorkingDirectory _workingDirectory = WorkingDirectory.DOWNLOADS;
	
	protected DownloadStore() {
		_workingDirectory.Instantiate();
	}
	 
	protected DownloadStore(WorkingDirectory wd) {
		_workingDirectory = wd;
	}
	
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
		
		String outputfile = basefilename + dateStr + ".lis";
		_outputfilePath = _workingDirectory + outputfile;

	    return outputfile;    
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
		ce.wget( _outputfilePath, _workingDirectory.toString() );
	}
	
	/**
	 * Imports each filename stored in outputfile one at a time and creates a
	 * table element for that fits file. Then it calls a table element function to
	 * check for matches
	 * TODO use only files that are actually downloaded and don't read in from filepath
	 * TODO log which ones never got downloaded
	 * @throws Exception 
	 */
	public void UpdateTable(CommandExecutor ce) throws Exception {
		Scanner scanner = new Scanner( new FileReader(_outputfilePath) );
		
		while( scanner.hasNext() ) {
			String next = scanner.nextLine();
			
			try {
				TableElement element = parseFitFile(next);
				element.SaveToTable(ce);
			} catch (Exception e) {
				
			}
		}
		
		scanner.close();
	}
	
	/**
	 * TODO verify that only downloaded files are in the table
	 * 		[4:38:56 PM] Leland Machen: well if you have a java File object
			[4:39:02 PM] Leland Machen: that's pointing to a directory
			[4:39:10 PM] Leland Machen: you can just say .listFiles()
			[4:39:19 PM] Leland Machen: and it'll give you File[] with files in there

	 * @return
	 */
	public TableElement parseFitFile(String uneditedFileURL) throws IOException {
		// remove the URL for WGET command from the filename //
		String spectrumFileName = "";
		int indexOfSlash = uneditedFileURL.lastIndexOf("/");
		
		if( indexOfSlash > 0 ) {
			spectrumFileName = uneditedFileURL.substring( indexOfSlash+1 );
		}
		
		// then read in the fits file and extract the plate and coordinate information from the header //
		TableElement element = new TableElement(spectrumFileName);
		
		try {
			Fits fitFileImport = new Fits(_workingDirectory + spectrumFileName);
			Header header =  fitFileImport.getHDU(0).getHeader();
			
			double[] coords = {0,0};
			int[] plateInfo = {0,0,0}; 
			
			// These header labels need to be read in this order only  //
			// so that the data can be stored in the table accurately. //
			// coords={RAOBJ,DECOBJ}; plateInfo={MJD,PLATEID,FIBERID}  // 
			coords[0] = header.getDoubleValue("RAOBJ");
			coords[1] = header.getDoubleValue("DECOBJ");
			plateInfo[0] = header.getIntValue("MJD");
			plateInfo[1] = header.getIntValue("PLATEID");
			plateInfo[2] = header.getIntValue("FIBERID");
			
			element.setCoords(coords);
			element.setPlateInfo(plateInfo);

		} catch (Exception e) {
			throw (new IOException("Could not read in data for fit file: " + spectrumFileName, e) );
		}
		
		return element;
	}
	
	public void Clean(CommandExecutor ce) throws IOException {
		ce.remove(_outputfilePath);
	}
	
	
	/* Getters and Setters */
	public String getType() { return _type; }
	protected void setType(String type) { _type = type; }
	public String getInputfile() { return _inputfile; }
	public String getOutputfile() { return _outputfilePath; }
	public WorkingDirectory getWorkingDir() { return _workingDirectory; }
	public void setWorkingDir(WorkingDirectory wd) { _workingDirectory = wd; }

}
