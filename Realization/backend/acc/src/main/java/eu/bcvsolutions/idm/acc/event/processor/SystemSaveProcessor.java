package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Save sys system, catch event UPDATE and CREATE
 * 
 * @author svandav
 *
 */
@Component
@Description("Persists sys system.")
public class SystemSaveProcessor extends CoreEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "identity-save-processor";
	private final SysSystemService service;
	private final ConfidentialStorage confidentialStorage;

	@Autowired
	public SystemSaveProcessor(SysSystemService service, ConfidentialStorage confidentialStorage) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE);
		//
		Assert.notNull(service);
		Assert.notNull(confidentialStorage);
		//
		this.service = service;
		this.confidentialStorage = confidentialStorage;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto dto = event.getContent();
		// create default connector server
		if (dto.getConnectorServer() == null) {
			dto.setConnectorServer(new SysConnectorServerDto());
		}
		if (dto.getConnectorKey() == null) {
			dto.setConnectorKey(new SysConnectorKeyDto());
		}
		if (!service.isNew(dto)) {
			// Check if is connector changed
			SysSystemDto oldSystem = service.get(dto.getId());
			if (!dto.getConnectorKey().equals(oldSystem.getConnectorKey())) {
				// If is connector changed, we set virtual to false. (Virtual
				// connectors set this attribute on true by themselves)
				dto.setVirtual(false);
			}
		}
		SysSystemDto newSystem = service.saveInternal(dto);
		event.setContent(newSystem);
		//
		// after save entity save password to confidential storage
		// save password from remote connector server to confidential storage
		if (dto.getConnectorServer().getPassword() != null) {
			// save for newSystem
			confidentialStorage.save(newSystem.getId(), SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD,
					dto.getConnectorServer().getPassword().asString());
			//
			// set asterix
			newSystem.getConnectorServer().setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
}