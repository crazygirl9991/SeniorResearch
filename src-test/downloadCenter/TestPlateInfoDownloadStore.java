package downloadCenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.FileReader;
import java.util.Scanner;

import org.junit.Test;

public class TestPlateInfoDownloadStore {
	static String URL = "http://das.sdss.org/spectro/1d_26/";
	
	@Test
	public void testPlateInfoDownloadStore() {
		double mockMJD = 1.0, mockPlate = 1.0, mockFiber = 1.0;
		DownloadStore store = new PlateInfoDownloadStore(mockMJD, mockPlate, mockFiber, WorkingDirectory.STUB);
		String formattedForWGET = URL + mockPlate + "/1d/spSpec-" + mockMJD + "-" + mockPlate + "-" + mockFiber + ".fit";
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		
		if( !store.getType().equals("plateInfo") ) {
			assert(false);
		}
		
		try {
			store.Write(mockCE);
			
			Scanner scanner = new Scanner( new FileReader(store.getOutputfile() ) );
			while( scanner.hasNext() ) {
				assertEquals(scanner.next(), formattedForWGET);
			}
			
			scanner.close();
			store.Clean( new CommandExecutor() );
		} catch(Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
}
