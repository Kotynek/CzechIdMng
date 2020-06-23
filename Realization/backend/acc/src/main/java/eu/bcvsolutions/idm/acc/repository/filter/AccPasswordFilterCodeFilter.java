package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterRepository;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccPasswordFilterDto} filter by code
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Password filter filter - by code (equals)")
public class AccPasswordFilterCodeFilter extends AbstractFilterBuilder<AccPasswordFilter, AccPasswordFilterFilter> {
	
	@Autowired
	public AccPasswordFilterCodeFilter(AccPasswordFilterRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DataFilter.PARAMETER_CODEABLE_IDENTIFIER;
	}
	
	@Override
	public Predicate getPredicate(Root<AccPasswordFilter> root, AbstractQuery<?> query, CriteriaBuilder builder, AccPasswordFilterFilter filter) {
		if (StringUtils.isEmpty(filter.getCodeableIdentifier())) {
			return null;
		}
		return builder.equal(root.get(AccPasswordFilter_.code), filter.getCodeableIdentifier());
	}	
}