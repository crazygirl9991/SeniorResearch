package downloadCenter;

import java.awt.Font;

import javax.swing.JLabel;

/**
 * This is essentially the same as a JLabel, but allows for easier 
 * and more dynamic font specification.
 * @author victoria
 *
 */
@SuppressWarnings("serial")
public class Label extends JLabel {
		
	public Label(String text, Font font) {
		super(text);
		this.setFont(font);
	}
	
}
