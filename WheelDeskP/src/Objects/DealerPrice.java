/**
 * @author Thabang Mamoloko
 */
package Objects;

/**
 * 
 */
public class DealerPrice {
	private String dealerName;
	private double price;
	/**
	 * @param dealerName
	 * @param price
	 */
	public DealerPrice(String dealerName, double price) {
		this.dealerName = dealerName;
		this.price = price;
	}
	/**
	 * @return the dealerName
	 */
	public String getDealerName() {
		return dealerName;
	}
	/**
	 * @param dealerName the dealerName to set
	 */
	public void setDealerName(String dealerName) {
		this.dealerName = dealerName;
	}
	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

}
