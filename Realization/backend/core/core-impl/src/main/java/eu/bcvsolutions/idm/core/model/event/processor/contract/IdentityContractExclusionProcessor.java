package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractWorkflowEventProcessor;

/**
 * HR process - identity's contract exclusion. The processes is started for
 * contracts that are both valid (meaning validFrom and validTill) and excluded.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("HR process - identity's contract exclusion. The processes is started for"
		+ " contracts that are both valid (meaning validFrom and validTill) and excluded.")
public class IdentityContractExclusionProcessor
		extends AbstractWorkflowEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-exclusion-processor";
	
	public IdentityContractExclusionProcessor() {
		super(IdentityContractEventType.UPDATE); // TODO: create too?
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected String getDefaultWorkflowDefinitionKey() {
		return "hrContractExclusion";
	}
	
	/**
	 * Identity contracts, that was valid and not excluded - is ecluded now
	 */
	@Override
	protected boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto current = event.getContent();
		IdmIdentityContractDto previous = event.getOriginalSource();
		//
		return previous.isValid() 
				&& previous.getState() != ContractState.EXCLUDED
				&& current.getState() == ContractState.EXCLUDED;
	}
	
	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 200;
	}

}