package downloadCenter;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.TableHDU;

/**
 * This class keeps track of the thresholds between data releases,
 * as well as the URL location of files for each one. This needs
 * to be updated if files located at a different location on the
 * Internet or with different formatting requirements are desired.
 * 
 * @author victoria
 *
 */
public enum SDSS {one_two, three;
	
	public String RA_HEADER;
	public String DEC_HEADER;
	public int FIBERS;
	
	public static SDSS getInstance(int[] plateInfo) {
		SDSS release;
		int mjd = plateInfo[0];
		
		if(mjd <= 55000) {
			release = SDSS.one_two;
			release.RA_HEADER = "RAOBJ";
			release.DEC_HEADER = "DECOBJ";
			release.FIBERS = 640;
		} else {
			release = SDSS.three;
			release.RA_HEADER = "PLUG_RA";
			release.DEC_HEADER = "PLUG_DEC";
			release.FIBERS = 1000;
		}
		
		return release;	
	}
	
	/**
	 * This takes an already successfully opened fit(s) file and
	 * retrieves the flux information stored there. This method
	 * depends on the release (and may need to be updated for 
	 * future releases).
	 */
	public float[] getDataY(Fits fitFileImport) throws Exception {
		float[] dataY = null;
		BasicHDU spectralDataHeader;
		
		switch(this) {
			case one_two:
				spectralDataHeader = fitFileImport.getHDU( 0 );
				dataY = ( (float[][]) spectralDataHeader.getData().getData() )[0];
				break;
			case three:
				spectralDataHeader = fitFileImport.getHDU( 1 );
				dataY = (float[]) ( (TableHDU) spectralDataHeader ).getColumn( 0 );
				break;
		}
		
		return dataY;
	}
	
	/**
	 * Takes in MJD, plate, fiber and returns the formatted URL as needed for
	 * downloading files from SDSS. This method depends on the release (and
	 * will need to be updated for future releases).
	 */
	public String formatUrl(int[] plateInfo) {
		/*
		 * Example of fully formatted URL is:
		 * http://das.sdss.org/spectro/1d_26/1615/1d/spSpec-53166-1615-513.fit
		 * Where MJD = 53166, plate = 1615, and fiber = 513
		 */
		// fiber needs to have padded 0s if less than 100 //
		String fiberStr = "", str = "";
		int mjd = plateInfo[0], plate = plateInfo[1], fiber = plateInfo[2];
		
		switch(this) {
			case one_two:
				fiberStr = String.format("%03d", fiber);
				str = "http://das.sdss.org/spectro/1d_26/" + plate + "/1d/spSpec-" + mjd + "-" + plate + "-" + fiberStr + ".fit";
				break;
			case three:
				fiberStr = String.format("%04d", fiber);
				str = "http://data.sdss3.org/sas/dr10/boss/spectro/redux/v5_5_12/spectra/" + plate + "/spec-" + plate + "-" + mjd + "-" + fiberStr + ".fits";
				break;
		}
		
		return str;
	}

}
