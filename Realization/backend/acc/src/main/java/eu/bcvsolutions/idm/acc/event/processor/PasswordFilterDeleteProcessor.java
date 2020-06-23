package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.acc.event.PasswordFilterEvent.PasswordFilterEventType;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Password filter processor for delete {@link AccPasswordFilterDto} and ensures
 * referential integrity
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accPasswordFilterDeleteProcessor")
@Description("Delete password filter definition and ensures referential integrity. Cannot be disabled.")
public class PasswordFilterDeleteProcessor extends CoreEventProcessor<AccPasswordFilterDto> {

	private static final String PROCESSOR_NAME = "password-filter-delete-processor";

	@Autowired
	private AccPasswordFilterService passwordFilterService;
	@Autowired
	private AccPasswordFilterSystemService passwordFilterSystemService;

	public PasswordFilterDeleteProcessor() {
		super(PasswordFilterEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordFilterDto> process(EntityEvent<AccPasswordFilterDto> event) {
		AccPasswordFilterDto passwordFilter = event.getContent();

		// Delete all connections with systems
		AccPasswordFilterSystemFilter passwordFilterFilter = new AccPasswordFilterSystemFilter();
		passwordFilterFilter.setPasswordFilterId(passwordFilter.getId());
		passwordFilterSystemService.find(passwordFilterFilter, null).forEach(passwordFilterSystem -> {
			passwordFilterSystemService.delete(passwordFilterSystem);
		});
		
		passwordFilterService.deleteInternal(passwordFilter);

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
