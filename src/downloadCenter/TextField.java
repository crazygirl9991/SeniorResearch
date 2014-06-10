package downloadCenter;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * Similar to a JTextField, but with checks for
 * valid input and preset field types.
 * 
 * @author victoria
 *
 */
public enum TextField implements FocusListener {
	RA("Right Ascension"), DEC("Declination"), MJD("MJD"), PLATE("Plate"), FIBER("Fiber");
	
	private JTextField _textField;
	
	TextField(String text) { 
		_textField = new JTextField(text);
		_textField.setName(text);
		_textField.setFont(MainAndDownloadUI.FONT);
		_textField.addFocusListener(this);
		_textField.setPreferredSize( new Dimension(150, MainAndDownloadUI.FONT.getSize()+10) );
		
		populate();
	}
	
	/**
	 * Determines if the input can be converted into integer or double
	 * values depending on which type of textField it is. 
	 */
	public Boolean hasValidTextEntry() {
		
		try {
			if( this == TextField.RA || this == TextField.DEC )
				Double.valueOf( _textField.getText() );
			else if( this == TextField.MJD || this == TextField.PLATE || _textField.getText() != "Fiber" )
				Integer.valueOf( _textField.getText() );
			
			// If you are able to convert the value in the textfield, then it's valid
			return true;
		} catch(Exception e) {
			// Otherwise, it is not a valid entry 
			return false;
		}
	}
	
	public void setTextField(JTextField textField) { _textField = textField; }
	public JTextField getTextField() { return _textField; }
	
	public void populate() { _textField.setText( _textField.getName() ); }
	public void clear() { _textField.setText(""); }
	
	/**
	 * Either returns the text currently in each field, or null if the text equals the 
	 * value it should have normally, in order for the filters to work properly.
	 */
	public String getText() {
		if(_textField.getText().equals(_textField.getName()))
			return "";
		else
			return _textField.getText();
	}
	
	public String getString() { return _textField.getName(); }
	
	@Override
	public void focusGained(FocusEvent event) { 
		if( _textField.getText().equals(_textField.getName() ) )
			clear(); 
		}

	@Override
	public void focusLost(FocusEvent event) { 
		if( _textField.getText().equals("") )
			populate(); 
	}
}
