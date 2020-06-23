package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterRepository;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccPasswordFilterDto} filter by {@link SysSystemDto} id.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Password filter filter - system id (equals)")
public class AccPasswordFilterSystemIdFilter extends AbstractFilterBuilder<AccPasswordFilter, AccPasswordFilterFilter> {
	
	@Autowired
	public AccPasswordFilterSystemIdFilter(AccPasswordFilterRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return AccPasswordFilterFilter.PARAMETER_SYSTEM_ID;
	}
	
	@Override
	public Predicate getPredicate(Root<AccPasswordFilter> root, AbstractQuery<?> query, CriteriaBuilder builder, AccPasswordFilterFilter filter) {
		if (filter.getSystemId() == null) {
			return null;
		}

		// guarantee for role can be defined as identity
		Subquery<AccPasswordFilterSystem> subquery = query.subquery(AccPasswordFilterSystem.class);
		Root<AccPasswordFilterSystem> subRoot = subquery.from(AccPasswordFilterSystem.class);
		subquery.select(subRoot);
		subquery.where(
			builder.equal(subRoot.get(AccPasswordFilterSystem_.passwordFilter), root), // corelation
			builder.equal(subRoot.get(AccPasswordFilterSystem_.system).get(AbstractEntity_.id), filter.getSystemId())
		);
				
				
		return builder.exists(subquery);
	}	
}