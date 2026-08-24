/**
 * @author Thabang Mamoloko
 */
package Classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 */
public class Dbh {
	private static String URL = "jdbc:mysql://localhost:3306/wheeldesk";
	private static String USER = "root";
	private static String PASSWORD = "";
	
	public static Connection getConnection() throws SQLException{
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

}
