package downloadCenter;

public class Controller {
	
	public static void main(String args[]) {
		DisplayFrame frame = new DisplayFrame();
		
		try {
			frame.display();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
