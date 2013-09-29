package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * DBAccessor
 * @author Daniel J Bray
 *
 * This class acts as a bridge between the GUI and the mySQL database by sending and receiving from
 * the database and passing it along to the GUI. All SQL syntax is stored here and hard coded into
 * methods to act as a black box for the user.
 */
public class DBAccessor {
	private DBConnector connector;
	private Connection conn;
	private String table;

	/**
	 * Constructs a new DBAccessor, initializes fields, and creates a new connection to the mySQL database.
	 */
	public DBAccessor(){
		table = "";
		connector = new DBConnector();
		conn = connector.connect();
	}

	/**
	 * getDataFromRS
	 * 
	 * Get the actual data from a result set and returns it in a Vector<String[]>.
	 * 
	 * @param resSet - the result set to get the data from
	 * @return the data for the table in a Vector<String[]> data type
	 * @throws SQLException if the database encounters an error
	 */
	public static Vector<String[]> getDataFromRS(ResultSet resSet) throws SQLException{
		int cols = resSet.getMetaData().getColumnCount();
		Vector<String[]> t = new Vector<String[]>();
		while(resSet.next()){
			String[] row = new String[cols];
			for(int i = 0; i < cols; i++){
				row[i] = resSet.getString(1+i);
			}
			t.add(row);
		}
		return t;
	}

	/**
	 * getColumnNamesFromRS
	 * 
	 * Gets the column names contained in the result set. Result Sets are returned
	 * from a database query in java.sql
	 * 
	 * @param resSet - the result set
	 * @return the column names for the data
	 * @throws SQLException if the database encounters and error
	 */
	public static String[] getColNamesFromRS(ResultSet resSet) throws SQLException{
		int cols = resSet.getMetaData().getColumnCount();
		String[] colNames = new String[cols];
		for(int i=0; i<cols; i++){
			colNames[i] = resSet.getMetaData().getColumnName(1+i);
		}
		return colNames;
	}

	/**
	 * executeQuery
	 * 
	 * Allows the user to input a Query directly into the mysql database 
	 * as though a terminal/console were being used. Note that this only works
	 * for queries. Not for data modifying methods like insertions or deletions.
	 * 
	 * @param query - The SQL query (in sql syntax)
	 * @return the result set found from the query
	 * @throws SQLException if the database encounters an error
	 */
	public ResultSet executeQuery(String query) throws SQLException{
		Statement s = conn.createStatement();
		return s.executeQuery(query);
	}

	/**
	 * hasTable
	 * 
	 * Returns a boolean specifying whether or not there is a table 
	 * in the database with the name stored in the parameter 'name.'
	 * 
	 * @param name - the name of the table to be checked.
	 * @return true if there is a table by that name in the database
	 * 		   false otherwise
	 */
	public boolean hasTable(String name){
		try{
			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery("SHOW TABLES;");
			while(res.next()){
				if(name.equalsIgnoreCase(res.getString(1))){
					return true;
				}
			}
			return false;
		}
		catch(SQLException e){
			System.err.println(e.getMessage());
			return false;
		}
	}

	/**
	 * selectTable
	 * 
	 * Sets the current table to be used in the database.
	 * @param name - the table name
	 * @throws SQLException
	 */
	public void setTable(String name){
		if(name == null){
			table = "";
		}
		else{
			table = name;
		}
	}

	/**
	 * setTable
	 * 
	 * Sets the current table to a comma separated list of tables. 
	 * This allows for info to be obtained from multiple tables
	 * into one single table. However, this will not perform
	 * UNION functions.
	 * 
	 * @param name - the names of the tables
	 */
	public void setTable(List<String> name){
		table = "";
		if(name ==null)
			return;
		else{
			Iterator<String> iter = name.iterator();
			while(iter.hasNext()){
				table += iter.next();		
				if(iter.hasNext())
					table += ", ";
			}
		}
	}

	/**
	 * getTable
	 * 
	 * Gets the current table(s). If multiple tables it is returned in a 
	 * comma separated list.
	 * 
	 * @return the current table(s)
	 */
	public String getTable(){
		return table;
	}

	/**
	 * getDataType
	 * 
	 * Gets the data type for the specified column. The table attribute is a comma
	 * value separated list of tables to try to search for the column. This function works for
	 * 1 or more tables. The number of skips indicates the number of tables to skip over
	 * in the comma separated list of tables. This way if a column has the same name in
	 * multiple tables but it needs to be abstracted from one of the later tables, it can
	 * be using the number of skips. (This is just a result of me being lazy while programming
	 * and not refactoring when I should have).
	 * 
	 * @param column - the column name
	 * @param table - the comma value separated list of tables to look through for the data type
	 * 			of the column.
	 * @param skips - the number of tables in the table list to skip when a match is found.
	 * @return the data type of the column
	 * @throws SQLException if the database encounters an error
	 */
	public String getDataType(String column, String table, int skips) throws SQLException{
		if(table == null)
			throw new SQLException("DBAccessor.getDataType was given a null table value.");

		StringTokenizer tk = new StringTokenizer(table, ",");
		while(tk.hasMoreTokens()){
			String t = tk.nextToken().trim();
			
			//This will get the dataType of the specified column
			String query = "SELECT DATA_TYPE FROM information_schema.columns WHERE table_name = '" + t + "' AND COLUMN_NAME = '" + column + "';";
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			
			//Makes sure result is not an empty set
			if(rs.next()){
				if(rs.getNString(1) != null){
					//Skips if 'skips' is greater than 0.
					if(skips > 0)
						skips--;
					else
						return rs.getNString(1);
				}
			}
		}
		return null;
	}

	/**
	 * getColNames
	 * 
	 * Gets the column names in the current table and returns it in a String array.
	 * 
	 * @return the column names in the current table
	 * @throws SQLException
	 */
	public String[] getColNames() throws SQLException{
		if(table == null)
			throw new SQLException("No Table Selected.");

		Statement statement = conn.createStatement();
		String s = "SELECT * FROM " + table + ";";
		ResultSet resSet = statement.executeQuery(s);
		ResultSetMetaData rsmd = resSet.getMetaData();

		String [] columnNames = new String[rsmd.getColumnCount()];
		for(int i=0; i<rsmd.getColumnCount(); i++){
			columnNames[i] = rsmd.getColumnName(i+1);
		}
		return columnNames;
	}

	/**
	 * preprocessData
	 * 
	 * Takes a tuple of data in a table stored in a Vector and turns it into SQL syntax for ease of use.
	 * For example:
	 * 	if a column has the data "Dan Bray" stored as a VARCHAR
	 * 	"Dan Bray" will turn into " 'Dan Bray' " such that it can promptly be inserted into an SQL table
	 * 
	 * @param data the data in it's raw form
	 * @return data in a processed, ready to insert form.
	 */
	public Vector<String> preprocessData(Vector<String> data){
		Vector<String> processed = new Vector<String>();
		try{
			if(table == null)
				throw new SQLException("No table selected.");

			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery("SHOW COLUMNS FROM " + table);
			ResultSetMetaData meta = res.getMetaData();
			int type;
			for(int i=0; i<meta.getColumnCount(); ++i){
				type = meta.getColumnType(i+1);
				if(type == Types.VARCHAR || type == Types.CHAR || type == Types.LONGNVARCHAR
						|| type == Types.LONGVARCHAR || type == Types.NCHAR || type == Types.NVARCHAR){
					processed.add("'"+data.get(i)+"'");
				}
				//TODO: add more if statements for each data type
				else{
					processed.add(data.get(i));
				}
			}
			return processed;
		}
		catch(SQLException e){
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * getTables
	 * 
	 * Returns all tables currently stored in the database.
	 * @return the tables currently stored in the database.
	 */
	public String[] getTables(){
		try{
			Vector<String> results = new Vector<String>();
			Statement statement = conn.createStatement();
			ResultSet res = statement.executeQuery("SHOW TABLES;");
			while(res.next()){
				results.add(res.getString(1));
			}
			String[] results_arr = new String[results.size()];
			for(int i=0; i<results_arr.length;i++){
				results_arr[i] = results.get(i);
			}
			return results_arr;
		}
		catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * getTableData
	 * 
	 * Returns all the data stored in the table as a String
	 * @return the table data
	 * @throws SQLException
	 */
	public Vector<String[]> getTableData() throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		Statement statement = conn.createStatement();
		String s = "SELECT * FROM " + table + ";";
		ResultSet resSet = statement.executeQuery(s);
		int cols = resSet.getMetaData().getColumnCount();
		Vector<String[]> t = new Vector<String[]>();
		while(resSet.next()){
			String[] row = new String[cols];
			for(int i = 0; i < cols; i++){
				row[i] = resSet.getString(1+i);
			}
			t.add(row);
		}
		return t;
	}

	/**
	 * close
	 * 
	 * Closes the connection with the database.
	 */
	public void close(){
		connector.disconnect();
	}
}
