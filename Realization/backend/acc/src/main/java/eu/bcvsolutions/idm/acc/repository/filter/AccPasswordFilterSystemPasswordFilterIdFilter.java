package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterSystemRepository;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccPasswordFilterSystemDto} filter by {@link AccPasswordFilterDto} id
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Password filter system filter - by password filter id (equals)")
public class AccPasswordFilterSystemPasswordFilterIdFilter extends AbstractFilterBuilder<AccPasswordFilterSystem, AccPasswordFilterSystemFilter> {
	
	@Autowired
	public AccPasswordFilterSystemPasswordFilterIdFilter(AccPasswordFilterSystemRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return AccPasswordFilterSystemFilter.PARAMETER_PASSWORD_FILTER_ID;
	}
	
	@Override
	public Predicate getPredicate(Root<AccPasswordFilterSystem> root, AbstractQuery<?> query, CriteriaBuilder builder, AccPasswordFilterSystemFilter filter) {
		if (filter.getPasswordFilterId() == null) {
			return null;
		}

		return builder.equal(root.get(AccPasswordFilterSystem_.passwordFilter).get(AbstractEntity_.id), filter.getPasswordFilterId());
	}	
}