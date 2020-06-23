package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.PasswordFilterRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
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
 * Rest controller for standard CRUD operation for password filter definition,
 * but also for change a valid method.
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/password-filters")
@Api(
		value = AccPasswordFilterController.TAG, 
		tags = AccPasswordFilterController.TAG, 
		description = "Password filter definitions and method for check a valid",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccPasswordFilterController extends AbstractReadWriteDtoController<AccPasswordFilterDto, AccPasswordFilterFilter> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccPasswordFilterController.class);

	@Autowired
	private PasswordFilterManager passwordFilterManager;

	protected static final String TAG = "Password filters";
	
	@Autowired
	public AccPasswordFilterController(AccPasswordFilterService passwordFilterService) {
		super(passwordFilterService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@ApiOperation(
			value = "Search definition for password filters (/search/quick alias)", 
			nickname = "searchPasswordFilters",
			tags = { AccPasswordFilterController.TAG }, 
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
			value = "Search definition for password filter", 
			nickname = "searchQuickPasswordFilters",
			tags = { AccPasswordFilterController.TAG }, 
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
			value = "Autocomplete password filters (selectbox usage)", 
			nickname = "autocompletePasswordFilters", 
			tags = { AccPasswordFilterController.TAG }, 
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
			value = "Password filter detail", 
			nickname = "getPasswordFilter", 
			response = AccPasswordFilterDto.class, 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Password filter's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update password filter definition", 
			nickname = "postPasswordFilter", 
			response = AccPasswordFilterDto.class, 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull AccPasswordFilterDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update password filter definition",
			nickname = "putPasswordFilter", 
			response = AccPasswordFilterDto.class, 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Password filter's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccPasswordFilterDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_UPDATE + "')")
	@ApiOperation(
			value = "Update password filter definition", 
			nickname = "patchPasswordFilter", 
			response = AccPasswordFilterDto.class, 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Password filter's uuid identifier.", required = true)
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
			value = "Delete password filter definition", 
			nickname = "deleteAccount", 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Password filter's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PASSWORD_FILTER_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnPasswordFilter", 
			tags = { AccPasswordFilterController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.PASSWORD_FILTER_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Password filter's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = "/validate", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Validate password trought password filters", 
			nickname = "validate",
			tags = { AccPasswordFilterController.TAG })
	public HttpStatus validate(
			@RequestBody @Valid PasswordFilterRequestDto request) {

		LOG.info("Validation request from resource [{}] received for identity identifier [{}]. With log identifier: [{}]",
				request.getResource(), request.getUsername(), StringUtils.defaultString(request.getLogIdentifier()));
		boolean valid = passwordFilterManager.validate(request);
		if (valid) {
			return HttpStatus.OK;
		}

		return HttpStatus.BAD_REQUEST;
	}

	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = "/change", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Change password trought password filters", 
			nickname = "change",
			tags = { AccPasswordFilterController.TAG })
	public HttpStatus change(
			@RequestBody @Valid PasswordFilterRequestDto request) {

		LOG.info("Change request from resource [{}] received for identity identifier [{}]. With log identifier: [{}]",
				request.getResource(), request.getUsername(), StringUtils.defaultString(request.getLogIdentifier()));
		passwordFilterManager.change(request);
		
		return HttpStatus.OK;
	}

	@Override
	protected AccPasswordFilterFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccPasswordFilterFilter filter = new AccPasswordFilterFilter(parameters, getParameterConverter());
		return filter;
	}
}
