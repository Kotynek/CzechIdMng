package eu.bcvsolutions.idm.acc.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum AccGroupPermission implements GroupPermission {
	
	SYSTEM(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	ACCOUNT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE, IdmBasePermission.AUTOCOMPLETE),
	IDENTITYACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	TREEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLECATALOGUEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	SYNCHRONIZATION(IdmBasePermission.CREATE, IdmBasePermission.UPDATE),
	CONTRACTACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	IDENTITYROLEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	CONTRACTSLICEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	PROVISIONINGBREAK(
			IdmBasePermission.ADMIN,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	PROVISIONINGOPERATION(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	PROVISIONINGARCHIVE(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ),
	PASSWORDFILTER(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE,
			PasswordFilterBasePermission.CHANGEPASSWORD,
			PasswordFilterBasePermission.VALIDATEPASSWORD);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String SYSTEM_ADMIN = "SYSTEM" + BasePermission.SEPARATOR + "ADMIN";
	public static final String SYSTEM_COUNT = "SYSTEM" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEM_AUTOCOMPLETE = "SYSTEM" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEM_READ = "SYSTEM" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_CREATE = "SYSTEM" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEM_UPDATE = "SYSTEM" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEM_DELETE = "SYSTEM" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ACCOUNT_AUTOCOMPLETE = "ACCOUNT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String ACCOUNT_READ = "ACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNT_CREATE = "ACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ACCOUNT_UPDATE = "ACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ACCOUNT_DELETE = "ACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String IDENTITY_ACCOUNT_READ = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String IDENTITY_ACCOUNT_CREATE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_ACCOUNT_UPDATE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String IDENTITY_ACCOUNT_DELETE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_ACCOUNT_READ = "ROLEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_ACCOUNT_CREATE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_ACCOUNT_UPDATE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_ACCOUNT_DELETE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREE_ACCOUNT_READ = "TREEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String TREE_ACCOUNT_CREATE = "TREEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String TREE_ACCOUNT_UPDATE = "TREEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String TREE_ACCOUNT_DELETE = "TREEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_CATALOGUE_ACCOUNT_READ = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_CATALOGUE_ACCOUNT_CREATE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_CATALOGUE_ACCOUNT_UPDATE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_CATALOGUE_ACCOUNT_DELETE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYNCHRONIZATION_CREATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYNCHRONIZATION_UPDATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "UPDATE";
	//
	public static final String CONTRACT_ACCOUNT_READ = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String CONTRACT_ACCOUNT_CREATE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONTRACT_ACCOUNT_UPDATE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONTRACT_ACCOUNT_DELETE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONTRACT_SLICE_ACCOUNT_READ = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String CONTRACT_SLICE_ACCOUNT_CREATE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONTRACT_SLICE_ACCOUNT_UPDATE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONTRACT_SLICE_ACCOUNT_DELETE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONINGBREAK_ADMIN = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONINGBREAK_READ = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONINGBREAK_CREATE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "CREATE";
	public static final String PROVISIONINGBREAK_UPDATE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PROVISIONINGBREAK_DELETE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String IDENTITY_ROLE_ACCOUNT_READ = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String IDENTITY_ROLE_ACCOUNT_CREATE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_ROLE_ACCOUNT_UPDATE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String IDENTITY_ROLE_ACCOUNT_DELETE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONING_OPERATION_ADMIN = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONING_OPERATION_COUNT = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "COUNT";
	public static final String PROVISIONING_OPERATION_READ = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONING_OPERATION_UPDATE = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PROVISIONING_OPERATION_DELETE = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONING_ARCHIVE_ADMIN = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONING_ARCHIVE_COUNT = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "COUNT";
	public static final String PROVISIONING_ARCHIVE_READ = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONING_ARCHIVE_DELETE = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PASSWORD_FILTER_ADMIN = "PASSWORDFILTER" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PASSWORD_FILTER_COUNT = "PASSWORDFILTER" + BasePermission.SEPARATOR + "COUNT";
	public static final String PASSWORD_FILTER_AUTOCOMPLETE = "PASSWORDFILTER" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String PASSWORD_FILTER_READ = "PASSWORDFILTER" + BasePermission.SEPARATOR + "READ";
	public static final String PASSWORD_FILTER_CREATE = "PASSWORDFILTER" + BasePermission.SEPARATOR + "CREATE";
	public static final String PASSWORD_FILTER_UPDATE = "PASSWORDFILTER" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PASSWORD_FILTER_DELETE = "PASSWORDFILTER" + BasePermission.SEPARATOR + "DELETE";
	public static final String PASSWORD_FILTER_CHANGEPASSWORD = "PASSWORDFILTER" + BasePermission.SEPARATOR + "CHANGEPASSWORD";
	public static final String PASSWORD_FILTER_VALIDATEPASSWORD = "PASSWORDFILTER" + BasePermission.SEPARATOR + "VALIDATEPASSWORD";

	private final List<BasePermission> permissions;

	private AccGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {		
		return permissions;
	}
	
	@Override
	public String getName() {
		return name();
	}	
	
	@Override
	public String getModule() {
		return AccModuleDescriptor.MODULE_ID;
	}
}
