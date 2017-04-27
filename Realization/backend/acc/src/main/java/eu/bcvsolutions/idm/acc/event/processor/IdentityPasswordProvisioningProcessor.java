package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.IdentityProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity's password provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Identity's and all selected systems password provisioning.")
public class IdentityPasswordProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	public static final String PROCESSOR_NAME = "identity-password-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProvisioningProcessor.class);
	private final IdentityProvisioningService provisioningService;
	
	@Autowired
	public IdentityPasswordProvisioningProcessor(IdentityProvisioningService provisioningService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(provisioningService);
		//
		this.provisioningService = provisioningService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		LOG.debug("Call provisioning for identity password [{}]", event.getContent().getUsername());
		provisioningService.changePassword(identity, passwordChangeDto);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}