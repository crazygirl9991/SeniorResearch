package downloadCenter;

import java.io.File;
import java.util.Scanner;

import javax.swing.JFrame;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestError {

	@Test
	public void testError() {
		Exception e = new Exception("I am a exception.");
		String test0 = "test0", test1 = "test1";
		
		ErrorLogger.update(test0, e, WorkingDirectory.STUB);
		ErrorLogger.update(test1, e, WorkingDirectory.STUB);
		
		ErrorLogger.LOG();
		
		try {
			Scanner scanner = new Scanner( new File(WorkingDirectory.STUB + ErrorLogger.LOG) );
			int i = 0;
			String[] str = new String[2];
			
			while( scanner.hasNext() && i < 2 ) {
				str[i] = scanner.next();
				i++;
			}
			
			assertEquals(str[0], test0);
			assertEquals(str[1], test1);
			
			scanner.close();
		} catch (Exception e1) {
			assert(false);
		}
		
		ErrorLogger.DIALOGUE(new JFrame(), "This is a test.");
	}
}
