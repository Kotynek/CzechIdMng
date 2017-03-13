package eu.bcvsolutions.idm.core.security.authentication.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.service.LoginService;

@Component
@Description("Default authenticator, authenticate over password saved in IdmPassword.")
public class DefaultCoreAuthenticator implements Authenticator {
	
	private static final String AUTHENTICATOR_NAME = "core-authenticator";
	
	private final LoginService loginService;
	
	@Autowired
	public DefaultCoreAuthenticator(LoginService loginService) {
		Assert.notNull(loginService);
		//
		this.loginService = loginService;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_AUTHENTICATOR_ORDER;
	}

	@Override
	public String getName() {
		return DefaultCoreAuthenticator.AUTHENTICATOR_NAME;
	}

	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		loginDto.setAuthenticationModule(this.getModule());
		loginDto = this.loginService.login(loginDto);
		return loginDto;
	}

	@Override
	public AuthenticationResponseEnum getResponse() {
		return AuthenticationResponseEnum.OPTIONAL;
	}

}
