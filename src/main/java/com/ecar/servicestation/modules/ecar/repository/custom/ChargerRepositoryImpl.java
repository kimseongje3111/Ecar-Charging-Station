package com.ecar.servicestation.modules.ecar.repository.custom;


import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QStation.*;

public class ChargerRepositoryImpl extends Querydsl4RepositorySupport implements ChargerRepositoryCustom {

    public ChargerRepositoryImpl(EntityManager em) {
        super(Charger.class);
    }

    @Override
    public Page<Charger> findAllWithStationBySearchConditionAndPaging(List<Long> ids, SearchCondition condition, Pageable pageable) {
        List<Charger> fetch =
                selectFrom(charger)
                        .join(charger.station, station).fetchJoin()
                        .where(
                                charger.id.in(ids),
                                cpStatEq(condition.getCpStat()),
                                chargerTpEq(condition.getChargerTp()),
                                cpTpEq(condition.getCpTp())
                        )
                        .fetch();

        return applyPagination(pageable, countQuery -> countQuery.selectFrom(charger).where(charger.in(fetch)));
    }

    @Override
    public Page<Charger> findAllWithStationByPaging(List<Long> ids, Pageable pageable) {
        List<Charger> fetch =
                selectFrom(charger)
                        .join(charger.station, station).fetchJoin()
                        .where(charger.id.in(ids))
                        .fetch();

        return applyPagination(pageable, countQuery -> countQuery.selectFrom(charger).where(charger.in(fetch)));
    }

    @Override
    public List<Charger> findAllByChargerNumberAndSearchCondition(List<Long> numbers, SearchCondition condition) {
        return selectFrom(charger)
                .where(
                        charger.chargerNumber.in(numbers),
                        cpStatEq(condition.getCpStat()),
                        chargerTpEq(condition.getChargerTp()),
                        cpTpEq(condition.getCpTp())
                )
                .fetch();
    }

    @Override
    public List<Charger> findAllByChargerNumber(List<Long> numbers) {
        return selectFrom(charger)
                .where(charger.chargerNumber.in(numbers))
                .fetch();
    }

    private BooleanExpression cpStatEq(Integer cpStat) {
        return cpStat != null ? charger.state.eq(cpStat) : null;
    }

    private BooleanExpression chargerTpEq(Integer chargerTp) {
        return chargerTp != null ? charger.type.eq(chargerTp) : null;
    }

    private BooleanExpression cpTpEq(Integer cpTp) {
        return cpTp != null ? charger.mode.eq(cpTp) : null;
    }
}
