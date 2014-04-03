package downloadCenter;

import java.io.File;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;

public class spectra_plotter {

	public static void main(String[] args) throws Exception {
		Fits fitFileImport = new Fits(new File("python_code", "spSpec-53166-1615-513.fit"));
		
		BasicHDU spectra = fitFileImport.getHDU(0);

		double c0 = spectra.getHeader().getDoubleValue("COEFF0");
		double c1 = spectra.getHeader().getDoubleValue("COEFF1");
		
		float[] flux = ((float[][])spectra.getData().getData())[0];
		
		float[] wavelength = new float[flux.length];
		
		for (int i = 0; i < wavelength.length; i++) {
			wavelength[i] = (float) Math.pow(10, c0 + c1 * i);
		}
		
		Fits fitFileImport2 = new Fits(new File("python_code", "spSpec-53741-2366-367.fit"));
		BasicHDU spectra2 = fitFileImport2.getHDU(0);

		double c02 = spectra2.getHeader().getDoubleValue("COEFF0");
		double c12 = spectra2.getHeader().getDoubleValue("COEFF1");
		
		float[] flux2 = ((float[][])spectra2.getData().getData())[0];
		
		float[] wavelength2 = new float[flux2.length];
		
		for (int i = 0; i < wavelength2.length; i++) {
			wavelength2[i] = (float) Math.pow(10, c02 + c12 * i);
		}
		
		int n = Math.min(flux.length,flux2.length);
		float[] ratio = new float[n];
		for (int i = 0; i < n; i++)
		{
			ratio[i] = flux[i]/flux2[i];
		}		
	}
}
