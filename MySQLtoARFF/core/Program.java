package core;


import gui.MainWindow;
import database.DBAccessor;

/**
 * Program
 * @author Daniel J Bray
 * 
 * The class specifically made to solely contain the main method. Acts as the executable
 * class.
 */
public class Program {
	public static void main(String[] args){
		new MainWindow(new DBAccessor());
	}
}
