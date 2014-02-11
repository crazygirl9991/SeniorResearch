package downloadCenter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PlateInfoDownloadStore extends DownloadStore {
	ArrayList<String> _formattedForWGET = new ArrayList<String>();
	
	static String URL = "http://das.sdss.org/spectro/1d_26/";
	
	public PlateInfoDownloadStore(double MJD, double plate, double fiber) {
		super();
		setType("plateInfo");
		Rename();
		
		setURL(MJD, plate, fiber);
	}
	
	public PlateInfoDownloadStore(double MJD, double plate, double fiber, WorkingDirectory wd) {
		super(wd);
		setType("plateInfo");
		Rename();
		
		setURL(MJD, plate, fiber);
	}
	
	public PlateInfoDownloadStore(ArrayList< double[] > plateInfo) {
		super();
		setType("plateInfo");
		Rename();
		
		for(int i = 0; i < plateInfo.size(); i++) {
			double[] current = plateInfo.get(i);
			if( current.length == 3 )
				setURL( current[0], current[1], current[2] );
		}
	}
	
	/** 
	 * Outputs reformatted URL (which includes the MJD, plate, fiber specification
	 * to a file in accordance with this software's naming convention.
	 */
	@Override
	public void Write(CommandExecutor ce) throws IOException {
		try {
			PrintWriter writer = new PrintWriter(_outputfile, "UTF-8");
			for(int i = 0; i < _formattedForWGET.size(); i++)
				writer.println( _formattedForWGET.get(i) );
			writer.close();
		} catch (Exception e) {
			throw ( new IOException("ERROR: Can't convert input to WGET required formatting.", e) );
		}
	}
	
	/**
	 * Takes in MJD, plate, fiber and sets the URL as needed for WGET.
	 */
	public void setURL(double MJD, double plate, double fiber) {
		/* Example of fully formatted URL is: 
		 * http://das.sdss.org/spectro/1d_26/1615/1d/spSpec-53166-1615-513.fit
		 * Where MJD = 53166, plate = 1615, and fiber = 513 */
		String formatted =  URL + plate + "/1d/spSpec-" + MJD + "-" + plate + "-" + fiber + ".fit";
		_formattedForWGET.add(formatted);
		
	}
}
