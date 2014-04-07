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
		WORKING_DIRECTORY.Instantiate();
	}

	public FitFileStore(String inputFileName) {
		WORKING_DIRECTORY.Instantiate();
		_downloadUrls = CommandExecutor.importFile(inputFileName);
	}

	public FitFileStore(int MJD, int plate, int fiber) {
		WORKING_DIRECTORY.Instantiate();
		_downloadUrls.add(formatPlateInfoToUrl(MJD, plate, fiber));
	}

	/**
	 * The int array parameters should contain exactly 3 elements: {MJD, Plate,
	 * Fiber}.
	 * 
	 * @param plateInfo
	 */
	public FitFileStore(ArrayList<int[]> plateInfo) {
		WORKING_DIRECTORY.Instantiate();

		for (int i = 0; i < plateInfo.size(); i++) {
			int[] current = plateInfo.get(i);
			if (current.length == 3)
				_downloadUrls.add(formatPlateInfoToUrl(current[0], current[1], current[2]));
		}
	}

	/**
	 * Runs a WGET query with specified "downloads" directory (contained in
	 * WorkingDirectory.java). Requires formatted outputfile (using Rename() and
	 * then Write(), found in this class).
	 * 
	 * @throws IOException
	 */
	public void Download() throws IOException {
		CommandExecutor.wget(_downloadUrls, WORKING_DIRECTORY.toString());
	}

	/**
	 * Imports each filename stored in outputfile one at a time and creates a
	 * table element for that fits file. Then it calls a table element function
	 * to check for matches TODO use only files that are actually downloaded and
	 * don't read in from filepath TODO log which ones never got downloaded
	 * 
	 * @throws Exception
	 */
	public void UpdateTable() throws Exception {
		for (String current : _downloadUrls) {
			TableElement element = ParseFitFile(current);
			TableManager.SaveToTable(element);
		}
	}

	/**
	 * TODO verify that only downloaded files are in the table [4:38:56 PM]
	 * Leland Machen: well if you have a java File object [4:39:02 PM] Leland
	 * Machen: that's pointing to a directory [4:39:10 PM] Leland Machen: you
	 * can just say .listFiles() [4:39:19 PM] Leland Machen: and it'll give you
	 * File[] with files in there
	 * 
	 * @return
	 */
	public static TableElement ParseFitFile(String uneditedFileURL) throws IOException {
		// remove the URL for WGET command from the filename //
		String spectrumFileName = "";
		int indexOfSlash = uneditedFileURL.lastIndexOf("/");

		Boolean needSpectrum = true; //TODO this isn't supposed to be hard-coded

		if (indexOfSlash > 0)
			spectrumFileName = uneditedFileURL.substring(indexOfSlash + 1);
		else
			spectrumFileName = uneditedFileURL;

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
