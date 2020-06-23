package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccPasswordFilterDto} filter by code
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Password filter filter - by change in IdM boolean attribute (equals)")
public class AccPasswordFilterChangeInIdmFilter extends AbstractFilterBuilder<AccPasswordFilter, AccPasswordFilterFilter> {
	
	@Autowired
	public AccPasswordFilterChangeInIdmFilter(AccPasswordFilterRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return AccPasswordFilterFilter.PARAMETER_CHANGE_IN_IDM;
	}
	
	@Override
	public Predicate getPredicate(Root<AccPasswordFilter> root, AbstractQuery<?> query, CriteriaBuilder builder, AccPasswordFilterFilter filter) {
		if (filter.isChangeInIdm() != null) {
			return null;
		}
		return builder.equal(root.get(AccPasswordFilter_.changeInIdm), filter.isChangeInIdm());
	}	
}