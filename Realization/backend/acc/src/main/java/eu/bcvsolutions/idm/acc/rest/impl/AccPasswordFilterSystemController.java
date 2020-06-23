package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Rest controller for standard CRUD operation for password filter conection between
 * {@link AccPasswordFilterDto} and {@link SysSystemDto}.
 * Controller has same permission as controller {@link AccPasswordFilterController}
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/password-filter-systems")
@Api(
		value = AccPasswordFilterSystemController.TAG, 
		tags = AccPasswordFilterSystemController.TAG, 
		description = "Password filter conection with systems",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccPasswordFilterSystemController extends AbstractReadWriteDtoController<AccPasswordFilterSystemDto, AccPasswordFilterSystemFilter> {
	
	protected static final String TAG = "Password filter systems";
	
	@Autowired
	public AccPasswordFilterSystemController(AccPasswordFilterSystemService passwordFilterSystemService) {
		super(passwordFilterSystemService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@ApiOperation(
			value = "Search definition for password filter and system (/search/quick alias)", 
			nickname = "searchPasswordFilterSystems",
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search password filter system", 
			nickname = "searchQuickPasswordFilterSystems",
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete password filter systems (selectbox usage)", 
			nickname = "autocompletePasswordFilterSystems", 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Password filter system detail", 
			nickname = "getPasswordFilterSystem", 
			response = AccPasswordFilterSystemDto.class, 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Password filter system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update password filter system", 
			nickname = "postPasswordFilterSystem", 
			response = AccPasswordFilterSystemDto.class, 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull AccPasswordFilterSystemDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update password filter system",
			nickname = "putPasswordFilter", 
			response = AccPasswordFilterSystemDto.class, 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Password filter system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccPasswordFilterSystemDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@ApiOperation(
			value = "Update password filter system", 
			nickname = "patchPasswordFilterSystem", 
			response = AccPasswordFilterSystemDto.class, 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Password filter system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete password filter system", 
			nickname = "deleteAccount", 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Password filter system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnPasswordFilterSystem", 
			tags = { AccPasswordFilterSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Password filter system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	@Override
	protected AccPasswordFilterSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccPasswordFilterSystemFilter filter = new AccPasswordFilterSystemFilter(parameters, getParameterConverter());
		return filter;
	}
}
