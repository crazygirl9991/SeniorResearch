package downloadCenter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDownloadStore {
	
	@Test
	public void testRename() {
		String testFilename = "TESTFILE.txt";
		DownloadStore store = new ListDownloadStore(testFilename);
		
		assertEquals( store.getInputfile(), testFilename );
		
		if( store.getOutputfile().contains(".lis") && !store.getOutputfile().equals( store.getInputfile() ) )
			assert(true);
		else
			assert(false);
	}
	
	@Test
	public void testRenameDefault() {
		DownloadStore store = new PlateInfoDownloadStore(1,1,1);
		
		assertEquals( store.getInputfile(), "" );
		
		if( store.getOutputfile().contains("default") && store.getOutputfile().contains(".lis") )
			assert(true);
		else
			assert(false);
	}
	
	//TODO 
	

}