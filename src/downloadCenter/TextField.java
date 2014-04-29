package downloadCenter;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public enum TextField implements FocusListener {
	RA("Right Ascension"), DEC("Declination"), MJD("MJD"), PLATE("Plate"), FIBER("Fiber");
	
	private JTextField _textField;
	
	TextField(String text) { 
		_textField = new JTextField(text);
		_textField.setName(text);
		_textField.setFont(Main.FONT);
		_textField.addFocusListener(this);
		_textField.setPreferredSize( new Dimension(150, Main.FONT.getSize()+10) );
		
		populate();
	}
	
	public Boolean hasValidTextEntry() {
		
		try {
			if( this == TextField.RA || this == TextField.DEC )
				Double.valueOf( _textField.getText() );
			else if( this == TextField.MJD || this == TextField.PLATE || this == TextField.FIBER )
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
	
	//TODO why would return null?
	public String getText() {
		if(_textField.getText().equals(_textField.getName()))
			return null;
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
