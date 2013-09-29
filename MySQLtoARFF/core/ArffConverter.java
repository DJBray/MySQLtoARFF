package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import database.DBAccessor;

/**
 * ArffConverter
 * @author Daniel J Bray
 *
 * The ArffConverter class is designed to convert data formats to the ARFF extension format as 
 * taken from http://www.cs.waikato.ac.nz/ml/weka/arff.html. Use the static methods in this class
 * to convert to ARFF format. Instances of the ArffConverter are designed for .arff outputs.
 */
public class ArffConverter {
	
	private String fileName = "MysqlToArff";
	private String[] arff;

	/**
	 * ArffConverter
	 * 
	 * Constructs a new ArffConverter. The arff array contains the arff data in plain
	 * ASCII. Each row is stored in a separate index of the array (blank rows are not
	 * included).
	 * 
	 * @param arff - An array containing arff data. Each index represents a new row of the
	 * arff data not including blank rows.
	 */
	public ArffConverter(String[] arff){
		this.arff = arff;
	}

	/**
	 * sqlToARFF
	 * 
	 * Converts a SQL based table into an arff file. The database is included to find
	 * the data types for each column. In the event that columns contain the same name
	 * then multiples are to be marked <columnName>(<number>) where columnName is the name
	 * of the column and number denotes the copy. 1 copy is denoted as 1, the second copy is
	 * denoted t, etc.
	 * 
	 * Example: name, date, name(1), user, name(2)
	 * 
	 * @param database - the DBAccessor database used for the queries
	 * @param columnNames - the names of the columns in the table(s) to be converted
	 * @param data - the data in the table(s) to be converted in row-column format.
	 * @return the arff conversion in a String array with each index representing a new row in
	 * 		the arff file.
	 */
	public static String[] sqlToARFF(DBAccessor database, String[] columnNames, String[][] data){
		try{
			//The +2 marks an extra row for @relation and @data
			String[] arffFile = new String[2 + columnNames.length + data.length];
			int index = 0;

			//Define the @relation line
			arffFile[0] = "@relation ";			
			//DBAccessor allows for multi-table support. They are returned comma separated.
			StringTokenizer tk = new StringTokenizer(database.getTable(), ",");
			String relationName = "";
			//Break the table names down into one name
			while(tk.hasMoreTokens()){
				relationName += tk.nextToken().trim();
				if(tk.hasMoreTokens())
					relationName += "-";
			}
			arffFile[0] += relationName + "\n"; ;		
			index++; 
			
			//Go through each column name and parse it
			for(String col: columnNames){
				String tbl = database.getTable();
				
				int skips = 0;
				//if it is a multiple then the number denotes the number of "skips" (see DBAccessor.getDataType() )
				if(col.contains("(") && col.contains(")")){				
					int begin = col.indexOf("(")+1;
					int end = col.indexOf(")");

					skips = Integer.parseInt(col.substring(begin, end));
				}

				//Convert the column into it's actual column name in the database
				String realCol = ArffConverter.convertToRealColumn(col);				
				arffFile[index] = "@attribute " + col + " ";		
				//get the datatype
				arffFile[index] += ArffConverter.mapDataTypeToARFF(database.getDataType(realCol, tbl, skips));
				index++;
			}
			
			arffFile[index] = "\n@data"; index++;
			
			//Goes through each row of data
			for(int i=0; i<data.length; i++){
				String d = "";
				//Gets each element and appends it together into 1 comma value separated string
				for(int j=0; j<data[i].length; j++){
					//Spaced elements have to be in single quotes
					if(data[i][j].contains(" ")){
						d += "'" + data[i][j] + "'";
					}
					else{
						d += data[i][j];
					}
					
					//Adds commas
					if(j+1 < data[i].length)
						d+= ",";
				}
				arffFile[index] = d;
				index++;
			}
			
			return arffFile;
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "Arff conversion failed:\n"+e.getMessage());
		}
		return null;
	}

	/**
	 * convertToRealColumn
	 * 
	 * Helper method to convert fake columns to real columns. In
	 * essence it just takes off the "(<number>)" portion of the column
	 * name that indicates it is a multiple.
	 * 
	 * @param fakeCol - the name of the column stored in the table
	 * @return - the name of the column stored in the database
	 */
	private static String convertToRealColumn(String fakeCol){
		if(fakeCol.contains("(") && fakeCol.contains(")")){
			int index = fakeCol.indexOf("(");
			return fakeCol.substring(0, index);
		}
		else{
			return fakeCol;
		}
	}

	/**
	 * mapDataTypeToARFF
	 * 
	 * Maps each SQL dataType to the ARFF dataTypes. The four ARFF types are:
	 * string, numberic, date, and class. The default value is class so if this
	 * method becomes out of date, class will be returned. (SQL doesn't support class).
	 * 
	 * @param dataType - The sql data type
	 * @return the ARFF data type
	 */
	public static String mapDataTypeToARFF(String dataType){
		//TODO
		dataType = dataType.toLowerCase();
		if(dataType.contains("varchar") || dataType.contains("string")){
			return "string";
		}
		else if(dataType.equals("int") || dataType.contains("integer") || dataType.contains("double")){
			return "numberic";
		}
		else if(dataType.contains("date")){
			return "date";
		}
		//should never be reached with mysql
		else{
			return "class";
		}
	}

	/**
	 * writeToFile
	 * 
	 * Writes the arff array to file using fileName.
	 * The file is by default .arff and is hardcoded.
	 */
	public void writeToFile(){
		try{
			FileWriter fw = new FileWriter(fileName + ".arff");
			for(String line : arff){
				fw.write(line+"\n");
			}
			fw.close();
		}
		catch(IOException e){
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}
	
	/**
	 * showArffDialog
	 * 
	 * Shows the arff data in a Dialog window.
	 */
	public void showArffDialog(){
		JFrame frame = new JFrame();
		frame.setBounds(100, 100, 440, 560);
		frame.getContentPane().setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JScrollPane scroll = new JScrollPane();
		
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setText("");
		for(String line : arff){
			ta.setText(ta.getText() + line +"\n");
		}
		ta.setBounds(15, 15, 400, 500);
		
		scroll.add(ta);
		frame.getContentPane().add(ta);
		frame.setVisible(true);
	}
}
