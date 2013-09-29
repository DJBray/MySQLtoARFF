package gui;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import database.DBAccessor;


/**
 * ShowTable_Panel
 * @author Daniel J Bray
 *
 * This class is a GUI for displaying a table taken from a mySQL database. The table is displayed
 * in a Panel.
 */
public class ShowTable_Panel extends Panel{

	/**
	 * Auto generated random serialUID. Used to make the compiler shut up and stop yelling at me...
	 */
	private static final long serialVersionUID = 510261754084207513L;
	
	protected JTable table;
	protected DBAccessor database;
	protected DefaultTableModel tableModel;

	/**
	 * Create the table panel and initialize it.
	 * @param db - Database to be used.
	 * @param bounds - the bounds for the panel
	 */
	public ShowTable_Panel(DBAccessor db, Rectangle bounds) {
		super();
		database = db;
		initialize(bounds);
		this.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize(Rectangle bounds) {
		this.setLayout(new BorderLayout());
		this.setBounds(bounds);
		
		//constructs the table and adds it to the frame
		table = new JTable();
		tableModel = new DefaultTableModel(1, 1);
		table.setModel(tableModel);
		table.setBounds(bounds);
		
		//add a scroll pane
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setViewportView(table);	
		
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * addListSelectionEvent
	 * 
	 * Adds a new list selection event to the table selection model.
	 * This will catch selection events that are fired when the mouse
	 * clicks on the table.
	 * 
	 * @param list - the list selection listener
	 */
	public void addListSelectionEvent(ListSelectionListener list){
		table.getSelectionModel().addListSelectionListener(list);
	}
	
	/**
	 * getSelectedRowCount
	 * 
	 * Gets the row count in the table (excluding the column names)
	 * 
	 * @return the row count in the table.
	 */
	public int getSelectedRowCount(){
		return table.getSelectedRowCount();
	}
	
	/**
	 * excludeColumn
	 * 
	 * Excludes the column that is specified using the object identifier.
	 * The object identifier for a column can be found using the getColumn method
	 * in the table.
	 * 
	 * @param identifier - The object identifier for the column
	 */
	public void excludeColumn(Object identifier){
		TableColumn column = table.getColumn(identifier);
		table.removeColumn(column);
	}
	
	/**
	 * getColNames
	 * 
	 * Gets the column names currently in the table that were not excluded.
	 * 
	 * @return the column names for the table.
	 */
	public String[] getColNames(){
		String[] arr = new String[table.getColumnCount()];
		for(int i=0; i<table.getColumnCount(); i++){
			arr[i] = table.getColumnName(i);
		}
		return arr;
	}
	
	/**
	 * getTableData
	 * 
	 * Gets the data stored in the table that wasn't excluded. The table
	 * data is every cell in the table stored in a 2d array.
	 * 
	 * @return the data stored in the table.
	 */
	public String[][] getTableData(){
		String[][] tableData = new String[table.getRowCount()][table.getColumnCount()];
		for(int i=0; i<table.getRowCount(); i++){
			for(int j=0; j<table.getColumnCount(); j++){
				tableData[i][j] = (String)table.getValueAt(i, j);
				//System.out.print(tableData[i][j]+"\t");
			}
			//System.out.print("\n");
		}
		
		return tableData;
	}
	
	/**
	 * Updates the table info such that the table is filled with the values in data with the column names
	 * stored in colNames. 
	 * 
	 * Precondition: data always has the same amount of columns as the length of colNames.
	 * @param data is data to be stored in the table
	 * @param colNames is the list of column names
	 */
	public void updateTableInfo(Vector<String[]> data, String[] colNames){
		//sets the size of the table to never be 0
		int dataSize = data.size()==0 ? 1 : data.size();
		
		//check to make sure no columns have the same name
		for(int i=0; i<colNames.length;i++){
			int count = 1;
			for(int j=i+1; j<colNames.length; j++){
				if(colNames[i].equals(colNames[j])){
					colNames[j] += "(" + count +")";
					count++;
				}
			}
		}

		//Creates a new 2d array of the table
		String[][] n_data = new String[dataSize][colNames.length];
		for(int i=0; i<dataSize;i++){
			if(data.size() == 0)
				n_data[i] = null; //initializes the table with at lease 1 row if empty
			else
				n_data[i] = data.get(i); //otherwise fills the table with data
		}

		tableModel = new DefaultTableModel(n_data, colNames);
		table.setModel(tableModel);
	}
	
	/**
	 * removeSelectedRows
	 * 
	 * Removes the selected rows from the table.
	 */
	public void removeSelectedRows(){
		//Comes in sorted lowest to highest
		int[] rows = table.getSelectedRows();
		//Traversing backwards accounts for the shifting done when the row is removed.
		for(int i=rows.length-1; i>=0; i--){
			tableModel.removeRow(rows[i]);
		}
	}
}
