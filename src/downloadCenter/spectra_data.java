package downloadCenter;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class spectra_data {
	float[] xdata;
	float[] ydata;
	double ra;
	double dec;
	int mjd;
	int plateid;
	int fiberid;
	Color color;

	public JPanel getPanel() {
		JPanel ret_panel = new JPanel(new GridLayout(5, 2));
		JLabel lblRA = new JLabel("RA:");
		lblRA.setForeground(color);
		ret_panel.add(lblRA);
		JLabel txtRA = new JLabel(Double.toString(ra));
		txtRA.setForeground(color);
		ret_panel.add(txtRA);
		JLabel lblDEC = new JLabel("DEC:");
		lblDEC.setForeground(color);
		ret_panel.add(lblDEC);
		JLabel lbldec = new JLabel(Double.toString(dec));
		lbldec.setForeground(color);
		ret_panel.add(lbldec);
		JLabel lblMJD = new JLabel("MJD:");
		lblMJD.setForeground(color);
		ret_panel.add(lblMJD);
		JLabel lblmjd = new JLabel(Integer.toString(mjd));
		lblmjd.setForeground(color);
		ret_panel.add(lblmjd);
		JLabel lblPLATEID = new JLabel("PLATEID:");
		lblPLATEID.setForeground(color);
		ret_panel.add(lblPLATEID);
		JLabel lblplateid = new JLabel(Integer.toString(plateid));
		lblplateid.setForeground(color);
		ret_panel.add(lblplateid);
		JLabel lblFIBERID = new JLabel("FIBERID:");
		lblFIBERID.setForeground(color);
		ret_panel.add(lblFIBERID);
		JLabel lblfiberid = new JLabel(Integer.toString(fiberid));
		lblfiberid.setForeground(color);
		ret_panel.add(lblfiberid);
		return ret_panel;
	}
}
