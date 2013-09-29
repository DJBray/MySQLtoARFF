package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.ArffConverter;
import database.DBAccessor;

/**
 * MainWindow
 * @author Daniel J Bray
 *
 * The main window is the main GUI window for the MYSQLtoARFF program.
 * It displays data from the SQL database and has a series of tools to exclude
 * particular rows or columns so that it can be converted to ARFF format without
 * removing rows or columns from the database.
 */
public class MainWindow {

	private JFrame frame;
	private DBAccessor database;
	private ShowTable_Panel dataTable;
	private JTextArea ta_SQLConsole;
	private JList<String> tableList;
	private JTextField txtSelectedrows;
	private JComboBox<String> cb_Exclude;

	/**
	 * Create the application.
	 */
	public MainWindow(DBAccessor database) {
		this.database = database;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 687, 440);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		tableList = new JList<String>(database.getTables());
		tableList.setFont(new Font("Tahoma", Font.PLAIN, 16));
		tableList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tableList.setBounds(12, 13, 226, 111);
		frame.getContentPane().add(tableList);

		JButton btnSelectTable = new JButton("Select Table(s)");
		btnSelectTable.setBounds(12, 126, 148, 25);
		btnSelectTable.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectTables();
			}
		});
		frame.getContentPane().add(btnSelectTable);

		JButton btnConvertToArff = new JButton("Convert to ARFF");
		btnConvertToArff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				convertToARFF();
			}
		});
		btnConvertToArff.setBounds(507, 376, 162, 25);
		frame.getContentPane().add(btnConvertToArff);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(250, 0, 419, 163);
		frame.getContentPane().add(tabbedPane);

		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Tools", null, panel_1, null);
		panel_1.setLayout(null);

		JButton btnExcludeSelected = new JButton("Exclude Selected Row(s)");
		btnExcludeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataTable.removeSelectedRows();
			}
		});
		btnExcludeSelected.setBounds(190, 99, 212, 25);
		panel_1.add(btnExcludeSelected);

		cb_Exclude = new JComboBox<String>();
		cb_Exclude.setBounds(82, 14, 144, 25);
		panel_1.add(cb_Exclude);

		JButton btnExcludeColumn = new JButton("Exclude Column");
		btnExcludeColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				excludeColumn();
			}
		});
		btnExcludeColumn.setBounds(238, 14, 143, 25);
		panel_1.add(btnExcludeColumn);

		JLabel lblColumn = new JLabel("column");
		lblColumn.setBounds(14, 18, 56, 16);
		panel_1.add(lblColumn);

		JButton btnPopOutTable = new JButton("Pop out table");
		btnPopOutTable.setBounds(14, 99, 148, 25);
		btnPopOutTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popOutTable();
			}
		});
		panel_1.add(btnPopOutTable);

		JLabel lblRowsSelected = new JLabel("rows selected");
		lblRowsSelected.setBounds(12, 63, 110, 15);
		panel_1.add(lblRowsSelected);

		txtSelectedrows = new JTextField();
		txtSelectedrows.setEditable(false);
		txtSelectedrows.setBounds(190, 61, 114, 19);
		panel_1.add(txtSelectedrows);
		txtSelectedrows.setColumns(10);	

		JPanel panel = new JPanel();
		tabbedPane.addTab("SQL Console", null, panel, null);
		panel.setLayout(null);

		ta_SQLConsole = new JTextArea();
		ta_SQLConsole.setBounds(12, 37, 371, 65);
		panel.add(ta_SQLConsole);
		ta_SQLConsole.setColumns(2);
		ta_SQLConsole.setRows(2);
		ta_SQLConsole.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		ta_SQLConsole.setLineWrap(true);

		JLabel lblExecuteQuery = new JLabel("Execute Query");
		lblExecuteQuery.setBounds(12, 13, 101, 16);
		panel.add(lblExecuteQuery);

		JButton btnExecuteConsoleQuery = new JButton("Execute Query");
		btnExecuteConsoleQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeStatement();
			}
		});
		btnExecuteConsoleQuery.setBounds(12, 104, 143, 25);
		panel.add(btnExecuteConsoleQuery);

		dataTable = new ShowTable_Panel(database, new Rectangle(12, 169, 657, 201));
		dataTable.addListSelectionEvent(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				txtSelectedrows.setText(dataTable.getSelectedRowCount()+"");
			}
		});
		frame.getContentPane().add(dataTable);

		frame.setVisible(true);
	}

	/**
	 * executeStatement
	 * 
	 * Executes the statement inputted into the ta_SQLConsole by the user. This
	 * should be in proper SQL syntax.
	 */
	private void executeStatement(){
		try{
			ResultSet rs = database.executeQuery(ta_SQLConsole.getText());
			ArrayList<String> list = new ArrayList<String>();
			for(int i=0; i<rs.getMetaData().getColumnCount(); i++){
				String tableName = rs.getMetaData().getTableName(i+1);
				if(!list.contains(tableName)){
					list.add(tableName);
				}
			}
			database.setTable(list);
			dataTable.updateTableInfo(DBAccessor.getDataFromRS(rs), DBAccessor.getColNamesFromRS(rs));
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, e.getMessage());
		}
	}

	/**
	 * selectTables
	 * 
	 * Selects the table or tables from the tableList and updates the
	 * data table and comboBox used to select columns to be excluded.
	 */
	private void selectTables(){	
		try{
			if(tableList.getSelectedIndex() == -1)
				return;

			database.setTable(tableList.getSelectedValuesList());
			String[] colNames = database.getColNames();
			dataTable.updateTableInfo(database.getTableData(), colNames);

			updateComboBox();
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, e.getMessage());
		}
	}

	/**
	 * updateComboBox
	 * 
	 * Updates the combo box with the new column names
	 * stored in the data table. This should be called after any modification
	 * to the data table.
	 */
	private void updateComboBox(){
		String[] colNames = dataTable.getColNames();
		cb_Exclude.removeAllItems();
		for(String columnName: colNames){
			cb_Exclude.addItem(columnName);
		}
	}

	/**
	 * excludeColumn
	 * 
	 * Excludes the column selected from the data table that was 
	 * selected from the cb_Exclude combo box.
	 */
	private void excludeColumn(){
		if(cb_Exclude.getSelectedIndex() == -1)
			return;
		
		dataTable.excludeColumn(cb_Exclude.getSelectedItem());
		updateComboBox();
	}

	/**
	 * convertToARFF
	 * 
	 * Converts the current data table to ARFF format, writes it to file, and then
	 * displays it in a dialog.
	 */
	private void convertToARFF(){
		String[] arffFile = ArffConverter.sqlToARFF(database, dataTable.getColNames(), dataTable.getTableData());
		if(arffFile != null){
			ArffConverter convert = new ArffConverter(arffFile);
			convert.writeToFile();
			convert.showArffDialog();
		}
	}

	/**
	 * popOutTable
	 * 
	 * Pops out the data table in a separate window.
	 */
	private void popOutTable(){
		JFrame popout = new JFrame();
		popout.add(dataTable, BorderLayout.CENTER);
		popout.addWindowListener(new MainWindowExitListener());
		popout.pack();
		
		popout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		popout.setVisible(true);
	}
	
	/**
	 * MainWindowExitListener
	 * @author Daniel J Bray
	 *
	 * Used to make it so that when the popout dialog is closed the data table
	 * is restored to the main window.
	 */
	private class MainWindowExitListener implements WindowListener{

		@Override
		public void windowOpened(WindowEvent e) {}

		@Override
		public void windowClosing(WindowEvent e) {}

		@Override
		public void windowClosed(WindowEvent e) {
			dataTable.setBounds(12, 169, 657, 201);
			frame.getContentPane().add(dataTable);
			//TODO: Need to find a way to get the bounds to come out correctly on window exit
		}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowActivated(WindowEvent e) {}

		@Override
		public void windowDeactivated(WindowEvent e) {}
		
	}
}
