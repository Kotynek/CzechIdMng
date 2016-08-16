package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskDefinitionService;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Default implementation of workflow process instance service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowProcessInstanceService implements WorkflowProcessInstanceService {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private WorkflowTaskDefinitionService taskDefinitionService;

	@Autowired
	private WorkflowProcessDefinitionService processDefinitionService;

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private RepositoryService repositoryService;

	/**
	 * Start new workflow process
	 */
	@Override
	public ProcessInstance startProcess(String definitionKey, String objectType, String applicant,
			Long objectIdentifier, Map<String, Object> variables) {
		Assert.hasText(definitionKey, "Definition key cannot be null!");

		IdmIdentity applicantIdentity = null;
		if (applicant != null) {
			applicantIdentity = identityService.getByUsername(applicant);
		}

		ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
				.processDefinitionKey(definitionKey)//
				.addVariable(WorkflowProcessInstanceService.OBJECT_TYPE, objectType)
				.addVariable(WorkflowProcessInstanceService.OBJECT_IDENTIFIER, objectIdentifier)
				.addVariable(WorkflowProcessInstanceService.IMPLEMENTER_USERNAME, securityService.getUsername())
				.addVariable(WorkflowProcessInstanceService.APPLICANT_USERNAME, applicant)
				.addVariable(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER,
						applicantIdentity != null ? applicantIdentity.getId() : null);
		if (variables != null) {
			for (String key : variables.keySet()) {
				builder.addVariable(key, variables.get(key));
			}
		}

		WorkflowProcessDefinitionDto definitionDto = processDefinitionService.get(definitionKey);
		//builder.processInstanceName(definitionDto.getName());
		return builder.start();

	}

	@Override
	public ResourcesWrapper<WorkflowProcessInstanceDto> search(WorkflowFilterDto filter) {
		String processDefinitionId = filter.getProcessDefinitionId();

		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

		query.active();
		query.includeProcessVariables();

		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if (filter.getCategory() != null) {
			//Find definitions with this category (use double sided like)
			//We have to find definitions first, because process instance can't be find by category.
			ProcessDefinitionQuery queryDefinition = repositoryService.createProcessDefinitionQuery();
			queryDefinition.active();
			queryDefinition.latestVersion();
			queryDefinition.processDefinitionCategoryLike(filter.getCategory()+"%");
			List<ProcessDefinition> processDefinitions = queryDefinition.list();
			Set<String> processDefinitionKeys = new HashSet<>();
			processDefinitions.forEach(p -> processDefinitionKeys.add(p.getKey()));
			if(processDefinitionKeys.isEmpty()){
				//We don`t have any definitions ... nothing must be returned
				processDefinitionKeys.add("-1");
			}
			query.processDefinitionKeys(processDefinitionKeys);
		}
		if (equalsVariables != null) {
			for (String key : equalsVariables.keySet()) {
				query.variableValueEquals(key, equalsVariables.get(key));
			}
		}
		// check security ... only involved user or applicant or implementer can work with
		// process instance
		String loggedUser = securityService.getUsername();
		query.or();
		query.involvedUser(securityService.getUsername());
		query.variableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME, loggedUser);
		query.variableValueEquals(WorkflowProcessInstanceService.IMPLEMENTER_USERNAME, loggedUser);
		query.endOr();

		query.orderByProcessDefinitionId();
		query.desc();
		long count = query.count();
		List<ProcessInstance> processInstances = query.listPage((filter.getPageNumber()) * filter.getPageSize(),
				filter.getPageSize());
		List<WorkflowProcessInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (ProcessInstance instance : processInstances) {
				dtos.add(toResource(instance));
			}
		}
		double totalPageDouble = ((double) count / filter.getPageSize());
		double totlaPageFlorred = Math.floor(totalPageDouble);
		long totalPage = 0;
		if (totalPageDouble > totlaPageFlorred) {
			totalPage = (long) (totlaPageFlorred + 1);
		}

		ResourcesWrapper<WorkflowProcessInstanceDto> result = new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
		return result;
	}

	@Override
	public WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason) {
		if (processInstanceId == null) {
			return null;
		}
		if (deleteReason == null) {
			deleteReason = "Deleted by " + securityService.getUsername();
		}
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		query.processInstanceId(processInstanceId);
		// check security ... only applicant can delete process instance
		query.variableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME,
				securityService.getOriginalUsername());
		query.active();
		query.includeProcessVariables();
		ProcessInstance processInstance = query.singleResult();
		if (processInstance == null) {
			throw new RestApplicationException(CoreResultCode.FORBIDDEN,
					"You do not have permission for delete process instance with ID: %s !",
					ImmutableMap.of("processInstanceId",  processInstanceId ));
		}
		runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);

		return toResource(processInstance);
	}

	private WorkflowProcessInstanceDto toResource(ProcessInstance instance) {
		if (instance == null) {
			return null;
		}

		WorkflowProcessInstanceDto dto = new WorkflowProcessInstanceDto();
		dto.setId(instance.getId());
		dto.setActivityId(instance.getActivityId());
		dto.setBusinessKey(instance.getBusinessKey());
		dto.setName(instance.getName() != null ? instance.getName() : instance.getProcessDefinitionName());
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
		dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
		dto.setProcessDefinitionName(instance.getProcessDefinitionName());
		dto.setProcessVariables(instance.getProcessVariables());
		dto.setEnded(instance.isEnded());
		dto.setProcessInstanceId(instance.getProcessInstanceId());
		// Add current activity name and documentation
		BpmnModel model =  repositoryService.getBpmnModel(instance.getProcessDefinitionId());
		
		for(FlowElement element : model.getMainProcess().getFlowElements()) {
			if(element.getId().equals(instance.getActivityId())) {
				dto.setCurrentActivityName(element.getName());
				dto.setCurrentActivityDocumentation(element.getDocumentation());
			}
		}

		return dto;
	}

}
