/**
 * @author Thabang Mamoloko
 */
package Classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import Objects.Dealership;

/**
 * 
 */
public class DealershipController {
	private Dealership dealership;

	/**
	 * @param dealership
	 */
	public DealershipController(Dealership dealership) {
		this.dealership = dealership;
	}
	
	public void signupDealership() {
		String sql = "INSERT INTO dealers (dealer_name, dealer_email, pwd) VALUES (?, ?, ?);";
		try {
			Connection conn = Dbh.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.dealership.getName());
			stmt.setString(2, this.dealership.getEmail());
			stmt.setString(3, this.dealership.getPassword());
			int rows = stmt.executeUpdate();
			System.out.println(rows + " Row Updated");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * @return the dealership
	 */
	public Dealership getDealership() {
		return dealership;
	}

	/**
	 * @param dealership the dealership to set
	 */
	public void setDealership(Dealership dealership) {
		this.dealership = dealership;
	}
}
