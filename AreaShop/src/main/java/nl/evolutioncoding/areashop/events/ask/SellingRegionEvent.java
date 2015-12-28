package nl.evolutioncoding.areashop.events.ask;

import nl.evolutioncoding.areashop.events.CancellableAreaShopEvent;
import nl.evolutioncoding.areashop.regions.BuyRegion;

/**
 * Broadcasted when a region is about to get sold
 */
public class SellingRegionEvent extends CancellableAreaShopEvent {

	private BuyRegion region;

	/**
	 * Constructor
	 * @param region The region that is about to get sold
	 */
	public SellingRegionEvent(BuyRegion region) {
		this.region = region;
	}

	/**
	 * Get the region that is about to be sold
	 * @return the region that is about to be sold
	 */
	public BuyRegion getRegion() {
		return region;
	}
}
