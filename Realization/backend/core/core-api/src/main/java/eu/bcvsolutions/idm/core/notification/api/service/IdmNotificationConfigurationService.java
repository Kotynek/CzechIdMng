package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;
import java.util.Set;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.BaseNotification;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationService extends ReadWriteDtoService<NotificationConfigurationDto, IdmNotificationConfigurationFilter> {

	/**
	 * Returns default notification sender. Will be used for notification's topics, which can not be found in configuration.
	 * 
	 * @return
	 */
	List<NotificationSender<?>> getDefaultSenders();
	
	/**
	 * Returns notification senders for given notification (by topic, level, etc).
	 * 
	 * @param notification
	 * @return
	 */
	List<NotificationSender<?>> getSenders(BaseNotification notification);
	
	/**
	 * Finds configured sender for given notification type
	 * 
	 * @param notificationType e.g. email, sms, custom
	 * @return
	 */
	NotificationSender<?> getSender(String notificationType);
	
	/**
	 * Method return {@link NotificationConfigurationDto} by topic, level, notification type.
	 * All parameters must exits, except notification level. 
	 * 
	 * @param topic
	 * @param level
	 * @param notificationType
	 * @return
	 */
	NotificationConfigurationDto getConfigurationByTopicLevelNotificationType(String topic, NotificationLevel level, String notificationType);
	
	/**
	 * Returns registered senders notification types.
	 * 
	 * @return
	 */
	Set<String> getSupportedNotificationTypes();
	
	/**
	 * Returns notification log type from senders notification type.
	 * 
	 * @param notificationType
	 * @return
	 */
	Class<? extends BaseEntity> toSenderType(String notificationType);
	
	/**
	 * Inits default notification configuration from all module descriptors.
	 */
	void initDefaultTopics();
	
	/**
	 * Method find all match configurations for topic and level.
	 * 
	 * @param topic
	 * @param level
	 * @return
	 */
	List<NotificationConfigurationDto> getConfigurations(String topic, NotificationLevel level);
	
	/**
	 * Method find all enabled configurations for topic and notificationType.
	 * @param topic
	 * @param notificationType
	 * @return
	 */
	List<NotificationConfigurationDto> getNotDisabledConfigurations(String topic, String notificationType);
	
	/**
	 * Method find all enabled configurations for topic and notificationType and level.
	 * @param topic
	 * @param notificationType
	 * @param level
	 * @return
	 */
	List<NotificationConfigurationDto> getNotDisabledConfigurations(String topic, String notificationType, NotificationLevel level);
}
