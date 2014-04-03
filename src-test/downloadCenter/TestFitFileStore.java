package downloadCenter;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;

import org.junit.Test;

/**
 * Tests all of the functionality in downloadStore.java except those which
 * depend on instantiating a command executor (as opposed to using a mock), 
 * which is tested independently. 
 */
public class TestFitFileStore { 
	
	@Test
	public void testFormatPlateInfoToUrl() {
		int mockMJD = 1, mockPlate = 1, mockFiber = 1;
		int[] info1 = {2, 2, 3}, info2 = {4, 5, 6};
		ArrayList< int[] > mockPlateInfo = new ArrayList< int[] >();
		mockPlateInfo.add(info1);
		mockPlateInfo.add(info2);
		
		FitFileStore store1 = new FitFileStore(mockMJD, mockPlate, mockFiber);
		FitFileStore store2 = new FitFileStore(mockPlateInfo);
		
		String formattedForWGET = "http://das.sdss.org/spectro/1d_26/" + mockPlate + "/1d/spSpec-" 
				+ mockMJD + "-" + mockPlate + "-" + mockFiber + ".fit";
		assertEquals(store1.getDownloadUrls().get(0), formattedForWGET);
		
		formattedForWGET = "http://das.sdss.org/spectro/1d_26/" + info1[1] + "/1d/spSpec-" 
				+ info1[0] + "-" + info1[1] + "-" + info1[2] + ".fit";
		assertEquals(store2.getDownloadUrls().get(0), formattedForWGET);
		
		formattedForWGET = "http://das.sdss.org/spectro/1d_26/" + info2[1] + "/1d/spSpec-" 
				+ info2[0] + "-" + info2[1] + "-" + info2[2] + ".fit";
		assertEquals(store2.getDownloadUrls().get(1), formattedForWGET);
		
		if(store1.getDownloadUrls().size() != 1 || store2.getDownloadUrls().size() != 2)
			assert(false);
		
	}
	
	@Test
	public void testParseFitFile() {
		//TODO
	}
	

}