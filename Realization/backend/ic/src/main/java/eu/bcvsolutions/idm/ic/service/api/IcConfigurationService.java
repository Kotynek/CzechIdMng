package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;

public interface IcConfigurationService {

	/**
	 * Return key defined IC implementation
	 * @return
	 */
	String getImplementationType();

	/**
	 * Return available local connectors for this IC implementation
	 * @return
	 */
	List<IcConnectorInfo> getAvailableLocalConnectors();

	/**
	 * Return find connector default configuration by connector key
	 * @param info
	 * @return
	 */
	IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance);
	
	/**
	 * Return remote connector configuration from server by connector key
	 * @param server
	 * @return
	 */
	IcConnectorConfiguration getRemoteConnectorConfiguration(IcConnectorInstance connectorInstance);
	

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param connectorInstance - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Return available remote connectors
	 * 
	 * @param server
	 * @param framework 
	 * @return
	 */
	List<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorServer server);

	/**
	 * Check if is connector configuration valid
	 * @param connectorInstance - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Check if is connector works fine
	 * @param connectorInstance - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);


}