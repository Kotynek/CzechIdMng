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
 * Save {@link AccPasswordFilterSystemDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accPasswordFilterSystemSaveProcessor")
@Description("Save newly created or update exists password filter system.")
public class PasswordFilterSystemSaveProcessor extends CoreEventProcessor<AccPasswordFilterSystemDto> {

	private static final String PROCESSOR_NAME = "password-filter-save-processor";

	@Autowired
	private AccPasswordFilterSystemService passwordFilterSystemService;

	public PasswordFilterSystemSaveProcessor() {
		super(PasswordFilterEventType.CREATE, PasswordFilterEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordFilterSystemDto> process(EntityEvent<AccPasswordFilterSystemDto> event) {
		AccPasswordFilterSystemDto dto = event.getContent();

		dto = passwordFilterSystemService.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}
