package downloadCenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

import org.junit.Test;

public class TestListDownloadStore {
	
	@Test
	public void testListDownloadStore() {
		
		try {
			String inputfile = "testclass.txt";
			WorkingDirectory wd = WorkingDirectory.STUB;
			DownloadStore store = new ListDownloadStore(inputfile, wd);
			String outputfile = WorkingDirectory.STUB.toString() + store.getOutputfile();
			
			CommandExecutor mockCE = mock(CommandExecutor.class);
			
			if( !store.getType().equals("list") ) {
				assert(false);
			}
			
			doNothing().when(mockCE).copy(inputfile, outputfile);
			store.Write(mockCE);
			verify(mockCE).copy(inputfile, outputfile);
		} catch(Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}

}
