/**
 * @author Thabang Mamoloko
 */
package Objects;

/**
 * 
 */
public class Car {
	private String name;
	private String model;
	DealerPrice[] dealerPrices;
	/**
	 * @param name
	 * @param model
	 * @param dealerPrices
	 */
	public Car(String name, String model, DealerPrice[] dealerPrices) {
		this.name = name;
		this.model = model;
		this.dealerPrices = dealerPrices;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}
	/**
	 * @return the dealerPrices
	 */
	public DealerPrice[] getDealerPrices() {
		return dealerPrices;
	}
	/**
	 * @param dealerPrices the dealerPrices to set
	 */
	public void setDealerPrices(DealerPrice[] dealerPrices) {
		this.dealerPrices = dealerPrices;
	}

}
