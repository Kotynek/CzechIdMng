package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link AccPasswordFilterSystemService}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("accPasswordFilterSystemService")
public class DefaultAccPasswordFilterSystemService
		extends AbstractEventableDtoService<AccPasswordFilterSystemDto, AccPasswordFilterSystem, AccPasswordFilterSystemFilter>
		implements AccPasswordFilterSystemService {

	@Autowired
	public DefaultAccPasswordFilterSystemService(AccPasswordFilterSystemRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.PASSWORDFILTER, getEntityClass());
	}

	@Override
	public boolean isAccountManaged(UUID accountId) {
		
		long count = this.getRepository().count(new Specification<AccPasswordFilterSystem>() {
			@Override
			public Predicate toPredicate(Root<AccPasswordFilterSystem> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				
				Subquery<AccAccount> accountSubquery = query.subquery(AccAccount.class);
				Root<AccAccount> subRootAccount = accountSubquery.from(AccAccount.class);
				accountSubquery.select(subRootAccount);
				
				Predicate and = cb.and(
						cb.equal(subRootAccount.get(AccAccount_.system), root.get(AccPasswordFilterSystem_.system)),
						cb.equal(subRootAccount.get(AccAccount_.id), accountId)
						);
				
				accountSubquery.where(and);
				
				return cb.exists(accountSubquery);
			}
		});
		
		return count > 0;
	}

	@Override
	public boolean isSystemManaged(UUID systemId) {
		AccPasswordFilterSystemFilter filter = new AccPasswordFilterSystemFilter();
		filter.setSystemId(systemId);
		return this.count(filter) > 0;
	}
}
