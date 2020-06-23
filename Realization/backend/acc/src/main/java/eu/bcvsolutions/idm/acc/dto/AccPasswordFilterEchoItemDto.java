package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Wrapper for all stored echo in password filter.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccPasswordFilterEchoItemDto implements Serializable {

	private static final long serialVersionUID = -9658932221444943L;

	private String password;
	private ZonedDateTime created;
	private UUID accountId;

	public AccPasswordFilterEchoItemDto() {
		super();
		created = ZonedDateTime.now();
	}

	public AccPasswordFilterEchoItemDto(String password, UUID accountId) {
		super();
		this.password = password;
		this.accountId = accountId;
		created = ZonedDateTime.now();
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ZonedDateTime getCreated() {
		return created;
	}

	public void setCreated(ZonedDateTime created) {
		this.created = created;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public boolean isEcho(GuardedString password, PasswordEncoder encoder, long timeout) {
		if (encoder.matches(password.asString(), this.password)
				&& ZonedDateTime.now().isBefore(this.created.plusSeconds(timeout))) {
			return true;
		}

		return false;
	}

}
