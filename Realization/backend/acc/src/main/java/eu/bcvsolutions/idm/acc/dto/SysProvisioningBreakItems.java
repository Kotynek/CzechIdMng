package eu.bcvsolutions.idm.acc.dto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;

/**
 * Class for cache processed items in provisioning. There is stored only
 * timestamp
 * 
 * TODO: Linked list isn't synchronized use Collections.synchronizedList(..
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SysProvisioningBreakItems {

	private Map<ProvisioningEventType, List<Long>> executedItems;

	public SysProvisioningBreakItems() {
		this.executedItems = new HashMap<>();
		this.executedItems.put(ProvisioningEventType.CREATE, new LinkedList<Long>());
		this.executedItems.put(ProvisioningEventType.DELETE, new LinkedList<Long>());
		this.executedItems.put(ProvisioningEventType.UPDATE, new LinkedList<Long>());
	}

	/**
	 * Add new item to executed items for specific provisioning type
	 * 
	 * @param event
	 * @param timestamp
	 */
	public void addItem(ProvisioningEventType provisioningType, Long timestamp) {
		this.getExecudedItems(provisioningType).add(timestamp);
	}

	/**
	 * Return executed items for specific provisioning type
	 * 
	 * @param provisioningType
	 * @return
	 */
	public List<Long> getExecudedItems(ProvisioningEventType provisioningType) {
		return this.executedItems.get(provisioningType);
	}

	/**
	 * Get size of all processed items for specific provisioning type
	 * 
	 * @param provisioningType
	 * @return
	 */
	public int getSize(ProvisioningEventType provisioningType) {
		return this.getExecudedItems(provisioningType).size();
	}

	/**
	 * Remove all order items that given timestamp for specific provisioning
	 * type
	 * 
	 * @param provisioningType
	 * @param timestamp
	 */
	public void removeOlderRecordsThan(ProvisioningEventType provisioningType, Long timestamp) {
		this.getExecudedItems(provisioningType).
		removeIf(
				item -> item < timestamp
				);
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getDiffBetweenFirstAndLastRecord(ProvisioningEventType provisioningType) {
		if (this.getSize(provisioningType) == 0) {
			return null;
		}
		Long first = this.getExecudedItems(provisioningType).get(0);
		Long last = this.getExecudedItems(provisioningType).get(this.getSize(provisioningType) - 1);
		return last - first;
	}

	/**
	 * Return all processed items newer than timestamp for specific provisioning
	 * type
	 * 
	 * @param provisioningType
	 * @param timestamp
	 * @return
	 */
	public List<Long> getRecordsNewerThan(ProvisioningEventType provisioningType, Long timestamp) {
		return this.getExecudedItems(provisioningType).stream().filter(item -> item >= timestamp)
				.collect(Collectors.toList());
	}

	/**
	 * Return size all processed items newer than timestamp for specific
	 * provisioning type
	 * 
	 * @param provisioningType
	 * @param timestamp
	 * @return
	 */
	public int getSizeRecordsNewerThan(ProvisioningEventType provisioningType, Long timestamp) {
		return getRecordsNewerThan(provisioningType, timestamp).size();
	}
	
	/**
	 * Clear all records for given {@link ProvisioningEventType}
	 * 
	 * @param provisioningType
	 */
	public void clearRecords(ProvisioningEventType provisioningType) {
		Assert.notNull(provisioningType);
		this.getExecudedItems(provisioningType).clear();
	}
}