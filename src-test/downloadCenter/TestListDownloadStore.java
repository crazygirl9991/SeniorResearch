package downloadCenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

import org.junit.Test;

public class TestListDownloadStore {
	
	@Test
	public void testListDownloadStore() {
		String inputfile = "testclass.txt";
		DownloadStore store = new ListDownloadStore(inputfile);
		String mockWorkingDir = "";
		String outputfile = mockWorkingDir + store.getOutputfile();
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		
		if( !store.getType().equals("list") ) {
			assert(false);
		}
		
		try {
			doNothing().when(mockCE).copy(inputfile, outputfile);
			store.Write(mockCE);
			verify(mockCE).copy(inputfile, outputfile);
		} catch(Exception e) {
			assert(false);
		}
	}

}
