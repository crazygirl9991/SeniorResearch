package downloadCenter;

import java.io.IOException;
import java.io.PrintWriter;

public class PlateInfoDownloadStore extends DownloadStore {
	String _formattedForWGET;
	
	static String URL = "http://das.sdss.org/spectro/1d_26/";
	
	public PlateInfoDownloadStore(double MJD, double plate, double fiber) {
		setType("plateInfo");
		Rename();
		
		/* Example of fully formatted URL is: 
		 * http://das.sdss.org/spectro/1d_26/1615/1d/spSpec-53166-1615-513.fit
		 * Where MJD = 53166, plate = 1615, and fiber = 513 */
		_formattedForWGET = URL + plate + "/1d/spSpec-" + MJD + "-" + plate + "-" + fiber + ".fit";
	}
	
	/** 
	 * Outputs reformatted URL (which includes the MJD, plate, fiber specification
	 * to a file in accordance with this software's naming convention.
	 */
	@Override
	public void Write(CommandExecutor ce) throws IOException {
		try {
			PrintWriter writer = new PrintWriter(_outputfile, "UTF-8");
			writer.println(_formattedForWGET);
			writer.close();
		} catch (Exception e) {
			throw ( new IOException("ERROR: Can't convert input to WGET required formatting.", e) );
		}
	}
}
