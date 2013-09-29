package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * DBConnector
 * @author Daniel J Bray
 *
 * The DBConnector class is in charge of establishing and handling the connection to the database.
 */
public class DBConnector {
	private Connection conn;
	private static final String DATABASE = "braydj";
	private static final String USERNAME = "root";
	private static final int PORT = 3306;

	/**
	 * Initializes the DBConnector
	 */
	public DBConnector(){
		try {
			Class.forName("com.mysql.jdbc.Driver"); //You need to have Connector J in your build path
		}
		catch (ClassNotFoundException e) {
			System.out.println("Cannot find driver");
		}
	}

	/**
	 * connect
	 * 
	 * Creates a new connection with the mySQL database.
	 * @return the active connection to the database.
	 */
	public Connection connect(){
		if(conn != null){
			return conn;
		}
		else{ //if(conn == null || conn.isClosed()){
			while(true){
				try{
					JPasswordField jpf = new JPasswordField();		//a relatively secure way to get a password
					int action = JOptionPane.showConfirmDialog(null, jpf,
							"Enter Password:", JOptionPane.OK_CANCEL_OPTION);

					if(action != JOptionPane.OK_OPTION)
						System.exit(0);				//exit the program if no password.

					jpf.getPassword();
					char [] pass = jpf.getPassword();
					String password = new String(pass);

					//establish a new connection using the given info
					conn = DriverManager.getConnection("jdbc:mysql://localhost:"+PORT+"/"+DATABASE,USERNAME,password);
					return conn;
				}
				catch(SQLException e){
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		}
	}

	/**
	 * disconnect
	 * 
	 * Closes the connection with the database.
	 */
	public void disconnect(){
		try{
			if(!conn.isClosed()){
				conn.close();
			}
		}
		catch(SQLException e){
			System.err.println("Error closing db.\n"+e.getMessage());
		}
	}
}
