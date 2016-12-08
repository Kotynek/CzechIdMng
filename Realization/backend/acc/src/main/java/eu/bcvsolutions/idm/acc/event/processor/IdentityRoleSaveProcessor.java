package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEventType;

/**
 * Identity role account management after save
 * 
 * @author Radek Tomiška
 *
 */
@Order(ProvisioningEvent.DEFAULT_PROVISIONING_ORDER)
@Component("accIdentityRoleSaveProcessor")
public class IdentityRoleSaveProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	private AccAccountManagementService accountManagementService;
	private final ApplicationContext applicationContext;

	@Autowired
	public IdentityRoleSaveProcessor(ApplicationContext applicationContext) {
		super(IdentityRoleEventType.SAVE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}


	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		getAccountManagementService().resolveIdentityAccounts(event.getContent().getIdentity());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * accountManagementService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private AccAccountManagementService getAccountManagementService() {
		if (accountManagementService == null) {
			accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
		}
		return accountManagementService;
	}
}