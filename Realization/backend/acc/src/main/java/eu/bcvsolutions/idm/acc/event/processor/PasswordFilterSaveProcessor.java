package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.event.PasswordFilterEvent.PasswordFilterEventType;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Save {@link AccPasswordFilterDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accPasswordFilterSaveProcessor")
@Description("Save newly created or update exists password filter definition.")
public class PasswordFilterSaveProcessor extends CoreEventProcessor<AccPasswordFilterDto> {

	private static final String PROCESSOR_NAME = "password-filter-save-processor";

	@Autowired
	private AccPasswordFilterService passwordFilterService;

	public PasswordFilterSaveProcessor() {
		super(PasswordFilterEventType.CREATE, PasswordFilterEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordFilterDto> process(EntityEvent<AccPasswordFilterDto> event) {
		AccPasswordFilterDto passwordFilterDto = event.getContent();

		passwordFilterDto = passwordFilterService.saveInternal(passwordFilterDto);
		event.setContent(passwordFilterDto);

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
