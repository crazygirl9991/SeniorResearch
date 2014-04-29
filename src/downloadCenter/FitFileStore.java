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

	public FitFileStore() {
	}

	public FitFileStore(String inputFileName) {
		_downloadUrls = CommandExecutor.importFile(inputFileName);
	}

	public FitFileStore(int MJD, int plate, int fiber) {
		_downloadUrls.add(formatPlateInfoToUrl(MJD, plate, fiber));
	}

	/**
	 * TODO
	 * 
	 * @param plateInfo
	 */
	public FitFileStore(int[] plateInfo) {
		if (plateInfo[2] == 0) {
			int FIBERS;

			if (plateInfo[0] <= 55000)
				FIBERS = 640; // SDSS I & II (before MJD = 55000) had 640 fibers per plate
			else
				FIBERS = 1000; // SDSS III had 1000 fibers per plate

			for (int i = 100; i <= FIBERS; i++)
				//TODO fibers start where?
				_downloadUrls.add(formatPlateInfoToUrl(plateInfo[0], plateInfo[1], i));
		} else
			_downloadUrls.add(formatPlateInfoToUrl(plateInfo[0], plateInfo[1], plateInfo[2]));
	}

	public FitFileStore(ArrayList<int[]> plateInfos) {
		for (int[] plateInfo : plateInfos) {
			if (plateInfo[2] == 0) {
				int FIBERS;

				if (plateInfo[0] <= 55000)
					FIBERS = 640; // SDSS I & II (before MJD = 55000) had 640 fibers per plate
				else
					FIBERS = 1000; // SDSS III had 1000 fibers per plate

				for (int i = 100; i <= FIBERS; i++)
					//TODO fibers start where?
					_downloadUrls.add(formatPlateInfoToUrl(plateInfo[0], plateInfo[1], i));
			} else
				_downloadUrls.add(formatPlateInfoToUrl(plateInfo[0], plateInfo[1], plateInfo[2]));
		}
	}

	/**
	 * Runs a WGET query with specified "downloads" directory (contained in
	 * WorkingDirectory.java). Requires formatted outputfile (using Rename() and
	 * then Write(), found in this class).
	 * 
	 * @throws IOException
	 */
	public Boolean Download() throws IOException {
		try {
			CommandExecutor.get(_downloadUrls, WORKING_DIRECTORY.toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * TODO 
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static TableElement ParseFitFile(String url) throws IOException
	{
		return ParseFitFile(new File(url));
	}
	
	public static TableElement ParseFitFile(File uneditedFileURL) throws IOException {
		// remove the URL for WGET command from the filename //
//		String spectrumFileName = "";
//		int indexOfSlash = uneditedFileURL.lastIndexOf("/");

		Boolean needSpectrum = true; //TODO this isn't supposed to be hard-coded

//		if (indexOfSlash > 0)
//			spectrumFileName = uneditedFileURL.substring(indexOfSlash + 1);
//		else
//			spectrumFileName = uneditedFileURL;
		String spectrumFileName = uneditedFileURL.getName();

		// then read in the fits file and extract the plate and coordinate information from the header //
		TableElement element = new TableElement(spectrumFileName);

		try {
			Fits fitFileImport = new Fits(new File(WORKING_DIRECTORY.toString(), spectrumFileName));
			Header header = fitFileImport.getHDU(0).getHeader();

			double[] coords = { 0, 0 };
			int[] plateInfo = { 0, 0, 0 };

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

			if (needSpectrum) {
				BasicHDU spectralDataHeader = fitFileImport.getHDU(0);

				// read these two coefficients from the header
				double c0 = header.getDoubleValue("COEFF0");
				double c1 = header.getDoubleValue("COEFF1");

				float[] dataX, dataY;

				// read in the flux data
				dataY = ((float[][]) spectralDataHeader.getData().getData())[0];

				// generate the wavelength data
				dataX = new float[dataY.length];
				for (int i = 0; i < dataX.length; i++)
					dataX[i] = (float) Math.pow(10, (c0 + c1 * i));

				element.setSpectrumData(dataX, dataY);
			}
			fitFileImport.getStream().close();

		} catch (Exception e) {
			throw (new IOException("Could not read in data for fit file: " + spectrumFileName, e));
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
		return "http://das.sdss.org/spectro/1d_26/" + plate + "/1d/spSpec-" + MJD + "-" + plate + "-" + fiber + ".fit";
	}
}
