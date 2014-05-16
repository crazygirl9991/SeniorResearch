package downloadCenter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.*;

/**
 * Abstract class controlling the retrieval and storage of SDSS .FITS file
 * information.
 * 
 * @author victoria
 * 
 */
public class FitFileStore {

	protected ArrayList<String> _downloadUrls = new ArrayList<String>();
	protected static WorkingDirectory WORKING_DIRECTORY = WorkingDirectory.DOWNLOADS;

	// The cut-off MJD between SDSS I && II and SDSS III
	public static int DATA_RELEASE = 55000;

	public FitFileStore() {
	}

	public FitFileStore(String inputFileName) {
		try {
			_downloadUrls = CommandExecutor.importFile( inputFileName, "" );
		} catch(Exception e) {
			//TODO
		}
	}

	public FitFileStore(int MJD, int plate, int fiber) {
		_downloadUrls.add( formatPlateInfoToUrl( MJD, plate, fiber ) );
	}

	public FitFileStore(int[] plateInfo) {
		formatBulkDownload( plateInfo );
	}

	public FitFileStore(ArrayList<int[]> plateInfos) {
		for ( int[] plateInfo : plateInfos )
			formatBulkDownload( plateInfo );
	}

	/**
	 * Downloads valid SDSS URLs via CommandExecutor functionality.
	 * 
	 * @throws IOException
	 */
	public Boolean Download() throws IOException {
		try {
			CommandExecutor.get( _downloadUrls, WORKING_DIRECTORY.toString() );
			return true;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Opens a fits file, retrieves information regarding plate, position, and
	 * spectral data, and returns an TableElement initialized with this
	 * information.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static TableElement ParseFitFile(String url) throws IOException {
		return ParseFitFile( new File( url ), true );
	}

	/**
	 * Opens a fits file, retrieves information regarding plate, position, and
	 * spectral data, and returns an TableElement initialized with this
	 * information.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static TableElement ParseFitFile(File uneditedFileURL, Boolean needSpectrum) throws IOException {
		// remove the URL to get just the filename //
		String spectrumFileName = uneditedFileURL.getName();

		// then read in the fits file and extract the plate and coordinate information from the header //
		TableElement element = new TableElement( spectrumFileName );

		try {
			Fits fitFileImport = new Fits( new File( WORKING_DIRECTORY.toString(), spectrumFileName ) );
			Header header = fitFileImport.getHDU( 0 ).getHeader();

			double[] coords = { 0, 0 };
			int[] plateInfo = { 0, 0, 0 };

			// These header labels need to be read in this order only  //
			// so that the data can be stored in the table accurately. //
			// coords={RAOBJ,DECOBJ}; plateInfo={MJD,PLATEID,FIBERID}  //
			plateInfo[0] = header.getIntValue( "MJD" );
			plateInfo[1] = header.getIntValue( "PLATEID" );
			plateInfo[2] = header.getIntValue( "FIBERID" );
			if ( plateInfo[0] <= DATA_RELEASE ) {
				coords[0] = header.getDoubleValue( "RAOBJ" );
				coords[1] = header.getDoubleValue( "DECOBJ" );
			} else {
				coords[0] = header.getDoubleValue( "PLUG_RA" );
				coords[1] = header.getDoubleValue( "PLUG_DEC" );
			}

			element.setCoords( coords );
			element.setPlateInfo( plateInfo );

			if ( needSpectrum ) {
				

				// read these two coefficients from the header
				double c0 = header.getDoubleValue( "COEFF0" );
				double c1 = header.getDoubleValue( "COEFF1" );

				float[] dataX, dataY;

				// read in the flux data
				if ( plateInfo[0] <= DATA_RELEASE ) {
					BasicHDU spectralDataHeader = fitFileImport.getHDU( 0 );
					dataY = ( (float[][]) spectralDataHeader.getData().getData() )[0];
				} else {
					BasicHDU spectralDataHeader = fitFileImport.getHDU( 1 );
					dataY = (float[]) ( (TableHDU) spectralDataHeader ).getColumn( 0 );
				}
				// generate the wavelength data
				dataX = new float[dataY.length];
				for ( int i = 0; i < dataX.length; i++ )
					dataX[i] = (float) Math.pow( 10, ( c0 + c1 * i ) );

				element.setSpectrumData( dataX, dataY );
			}
			fitFileImport.getStream().close();

		} catch ( Exception e ) {
			ErrorLogger.update( "Could not load file: " + spectrumFileName, e );
			element = null;
		}

		return element;
	}

	public ArrayList<String> getDownloadUrls() {
		return _downloadUrls;
	}

	public WorkingDirectory getWorkingDir() {
		return WORKING_DIRECTORY;
	}

	public void setWorkingDir(WorkingDirectory wd) {
		WORKING_DIRECTORY = wd;
	}

	/**
	 * Takes in MJD, plate, fiber and returns the formatted URL as needed for
	 * downloading files from SDSS.
	 */
	private String formatPlateInfoToUrl(int MJD, int plate, int fiber) {
		/*
		 * Example of fully formatted URL is:
		 * http://das.sdss.org/spectro/1d_26/1615/1d/spSpec-53166-1615-513.fit
		 * Where MJD = 53166, plate = 1615, and fiber = 513
		 */
		// fiber needs to have padded 0s if less than 100 //
		//TODO there are too different ways to pad this omg please reorganize
		
		String str = "";
		String fiberStr = "";
		if( MJD <= DATA_RELEASE ) {
			if ( fiber < 10 )
				fiberStr = "00";
			else if ( fiber < 100 )
				fiberStr = "0";
			
			str = "http://das.sdss.org/spectro/1d_26/" + plate + "/1d/spSpec-" + MJD + "-" + plate + "-" + fiberStr + fiber + ".fit"; 
		} else {
			if ( fiber < 10 )
				fiberStr = "000";
			else if ( fiber < 100 )
				fiberStr = "00";
			else if ( fiber < 1000 )
				fiberStr = "0";
			
			str = "http://data.sdss3.org/sas/dr10/boss/spectro/redux/v5_5_12/spectra/" + plate + "/spec-" + plate + "-" + MJD + "-" + fiberStr + fiber + ".fits";
		}

		return str;
	}

	/**
	 * Contains the logic for downloading fit files in bulk.
	 */
	private void formatBulkDownload(int[] plateInfo) {
		if ( plateInfo[2] == 0 ) {
			int FIBERS;

			if ( plateInfo[0] <= DATA_RELEASE )
				FIBERS = 640; // SDSS I & II (before MJD = 55000) had 640 fibers per plate
			else
				FIBERS = 1000; // SDSS III had 1000 fibers per plate

			for ( int i = 0; i <= FIBERS; i++ )
				_downloadUrls.add( formatPlateInfoToUrl( plateInfo[0], plateInfo[1], i ) );
		} else
			_downloadUrls.add( formatPlateInfoToUrl( plateInfo[0], plateInfo[1], plateInfo[2] ) );
	}
}
