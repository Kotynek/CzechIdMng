package eu.bcvsolutions.idm.core.model.event.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Event processing in workflow.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public abstract class AbstractWorkflowEventProcessor <E extends Serializable> extends AbstractEntityEventProcessor<E> {
	
	public static final String PROPERTY_WF = "wf";
	
	@Autowired private WorkflowProcessInstanceService workflowService;
	@Autowired private SecurityService securityService;
	
	public AbstractWorkflowEventProcessor(EventType... type) {
		super(type);
	}
	
	@Override
	public EventResult<E> process(EntityEvent<E> event) {
		if (conditional(event)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(WorkflowProcessInstanceService.VARIABLE_DTO, event.getContent());			
			OperationResult result = process(variables);
			return new DefaultEventResult.Builder<>(event, this).setResult(result).build();
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Returns configured or default wf definition name. 
	 * If definition is not configured and default is not provided, then processor is not executed.
	 * 
	 * @return
	 */
	protected String getWorkflowDefinitionKey() {
		return getConfigurationValue(PROPERTY_WF, getDefaultWorkflowDefinitionKey());
	}
	
	/**
	 * Returns default wf definition name. Will be used, if no one is configured.
	 * 
	 * @return
	 */
	protected String getDefaultWorkflowDefinitionKey() {
		return null;
	}
	
	/**
	 * Execute processor conditionally
	 * 
	 * @param event
	 * @return
	 */
	protected boolean conditional(EntityEvent<E> event) {
		return true;
	}
	
	/**
	 * Execute wf - returns result from processed instance  processed instance
	 * 
	 * @param variables
	 * @return
	 */
	protected OperationResult process(Map<String, Object> variables) {
		return getResult(processInstance(variables));
	}
	
	/**
	 * Execute wf - returns process instance
	 * 
	 * @param variables
	 * @return
	 */
	protected ProcessInstance processInstance(Map<String, Object> variables) {
		//
		// execute process
		AbstractAuthentication authentication = securityService.getAuthentication();
		IdmIdentityDto modifier = authentication == null ? null : authentication.getCurrentIdentity();
		return workflowService.startProcess(
				getWorkflowDefinitionKey(),
				modifier == null ? null : modifier.getClass().getSimpleName(), 
				authentication == null ? null : authentication.getCurrentUsername(), 
				modifier == null || modifier.getId() == null ? null : modifier.getId().toString(), 
				variables);
	}
	
	/**
	 * Returns result variable from executed process instance
	 * 
	 * @param processInstance
	 * @return
	 */
	protected OperationResult getResult(ProcessInstance processInstance) {
		if (processInstance instanceof VariableScope) {
			VariableScope vs = (VariableScope) processInstance;
			return (OperationResult) vs.getVariable(WorkflowProcessInstanceService.VARIABLE_OPERATION_RESULT);
		}
		return null;
	}
	
	/**
	 * If wf is not given, then this processor is disabled.
	 */
	@Override
	public boolean isDisabled() {
		if (super.isDisabled()) {
			return true;
		}
		return StringUtils.isEmpty(getWorkflowDefinitionKey());		
	}
	
	/**
	 * Adds wf property
	 */
	@Override
	public List<String> getPropertyNames() {
		List<String> properties =  super.getPropertyNames();
		properties.add(PROPERTY_WF);
		return properties;		
	}
}