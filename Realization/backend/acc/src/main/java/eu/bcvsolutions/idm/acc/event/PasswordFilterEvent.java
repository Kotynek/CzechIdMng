package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link AccPasswordFilterDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class PasswordFilterEvent extends CoreEvent<AccPasswordFilterDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported password filter events
	 *
	 */
	public enum PasswordFilterEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE;
	}
	
	public PasswordFilterEvent(PasswordFilterEventType operation, AccPasswordFilterDto content) {
		super(operation, content);
	}
	
	public PasswordFilterEvent(PasswordFilterEventType operation, AccPasswordFilterDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}