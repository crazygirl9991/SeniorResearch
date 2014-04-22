package downloadCenter;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableElementModel extends AbstractTableModel {
	final String[] colnames = new String[]{"ID",/*"Filename",*/"RA","Dec","MJD","Plate","Fiber","Matches"};
	ArrayList<Integer> _filter = new ArrayList<Integer>();
	ArrayList<TableElement> _data;
	TableElementModel(ArrayList<TableElement> data)
	{
		_data = data;
		filter("","","","","",false);
	}
	
	public void filter(String RA, String DEC, String MJD, String PLATE, String FIBER, boolean matches )
	{
		_filter.clear();
		for(int i = 0; i < _data.size(); i++)
 {
			double[] coords = _data.get( i ).getCoords();
			int[] plateinfo = _data.get( i ).getPlateInfo();
			boolean filter = true;
			if ( RA != null && !Double.toString( coords[0] ).contains( RA ) )
				filter = false;
			if ( DEC != null && !Double.toString( coords[1] ).contains( DEC ) )
				filter = false;
			if ( MJD != null && !Integer.toString( plateinfo[0] ).contains( MJD ) )
				filter = false;
			if ( PLATE != null && !Integer.toString( plateinfo[1] ).contains( PLATE ) )
				filter = false;
			if ( FIBER != null && !Integer.toString( plateinfo[2] ).contains( FIBER ) )
				filter = false;
			if (matches && _data.get(i).getMatches().isEmpty())
				filter = false;
			if ( filter )
				_filter.add( i );
		}
		fireTableStructureChanged();
	}
	
	public String getRow(int r)
	{
		return _data.get(_filter.get(r)).getFilename();
	}

	@Override
	public String getColumnName(int c) {
		return colnames[c];
	}

	@Override
	public int getColumnCount() {
		return colnames.length;
	}

	@Override
	public int getRowCount() {
		return _filter.size();
	}

	@Override
	public Object getValueAt(int r, int c) {
		TableElement te = _data.get(_filter.get(r));
		switch(c)
		{
		case 0: return te.getUniqueID();
//		case 1: return te.getFilename();
		case 1: return te.getCoords()[0];
		case 2: return te.getCoords()[1];
		case 3: return te.getPlateInfo()[0];
		case 4: return te.getPlateInfo()[1];
		case 5: return te.getPlateInfo()[2];
		case 6: return te.getMatches();
		default: return "Error";
		}
	}
}
