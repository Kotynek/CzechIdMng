package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.event.PasswordFilterEvent.PasswordFilterEventType;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Password filter processor for delete {@link AccPasswordFilterSystemDto} and ensures
 * referential integrity
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accPasswordFilterSystemDeleteProcessor")
@Description("Delete password filter system and ensures referential integrity. Cannot be disabled.")
public class PasswordFilterSystemDeleteProcessor extends CoreEventProcessor<AccPasswordFilterSystemDto> {

	private static final String PROCESSOR_NAME = "password-filter-system-delete-processor";

	@Autowired
	private AccPasswordFilterSystemService passwordFilterSystemService;

	public PasswordFilterSystemDeleteProcessor() {
		super(PasswordFilterEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordFilterSystemDto> process(EntityEvent<AccPasswordFilterSystemDto> event) {
		AccPasswordFilterSystemDto dto = event.getContent();

		passwordFilterSystemService.deleteInternal(dto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		// Cannot be disabled
		return false;
	}
}
