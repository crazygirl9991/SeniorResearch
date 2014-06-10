package downloadCenter;

/**
 * Runs the application and initializes the main menu (needs to be separate 
 * because of the way java handles static variable requirements in the main function).
 *  
 * @author victoria
 *
 */
public class Controller {
	
	public static void main(String args[]) {
		MainAndDownloadUI m = new MainAndDownloadUI();
		m.Main_Menu();
	}
}
