/**
 * @author Thabang Mamoloko
 */
package Classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 */
public class userQuery {
	private String email;

	/**
	 * @param email
	 */
	public userQuery(String email) {
		this.email = email;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Get Username from the database
	 */
	
	public String username() {
		//String userName = "";
		String sql = "SELECT dealer_name FROM dealers WHERE dealer_email = ?;";
		
		try {
			Connection conn = Dbh.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, email);
			//stmt.executeQuery();
			ResultSet results = stmt.executeQuery();
			if(results.next()) {
				String username = results.getString("dealer_name");
				return username;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create post
	 */
	public void post(String name, String email, String price) {
		String sql = "INSERT INTO car_collection (dealer_name, car_name, car_price) VALUES (?, ?, ?);";
		try {
			Connection conn = Dbh.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, email);
			int p = Integer.parseInt(price);
			stmt.setInt(3, p);
			int rows = stmt.executeUpdate();
			System.out.println(rows + " Row Updated!");
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
