package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Process catch PASSWORD event for {@link IdmIdentityDto} and set ECHO's for managed systems.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Set ECHO after password was changed for managed systems.")
public class IdentityPasswordSetEchoProcessor extends CoreEventProcessor<IdmIdentityDto> implements IdentityProcessor {

	@Autowired
	private PasswordFilterManager passwordFilterManager;
	@Autowired
	private AccPasswordFilterService passwordFilterService;
	@Autowired
	private AccPasswordFilterSystemService passwordFilterSystemService;
	
	public IdentityPasswordSetEchoProcessor() {
		super(IdentityEventType.PASSWORD);
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identityDto = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		GuardedString password = passwordChangeDto.getNewPassword();

		AccPasswordFilterFilter filter = new AccPasswordFilterFilter();
		filter.setChangeInIdm(true);
		long count = passwordFilterService.count(filter);

		if (count > 0) {
			// Set ECHO for IdM
			passwordFilterManager.setEcho(identityDto.getId(), password);
		}

		
		for (String accountIdAsString : passwordChangeDto.getAccounts()) {
			UUID accountId = UUID.fromString(accountIdAsString);
			boolean accountManaged = passwordFilterSystemService.isAccountManaged(accountId);
			if (accountManaged) {
				passwordFilterManager.setEcho(accountId, password);
			}
		}
		
		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// At the end
		return Integer.MAX_VALUE;
	}

}
