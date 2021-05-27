package com.ecar.servicestation.modules.ecar.repository.custom;


import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QStation.*;

public class ChargerRepositoryImpl extends Querydsl4RepositorySupport implements ChargerRepositoryCustom {

    public ChargerRepositoryImpl() {
        super(Charger.class);
    }

    @Override
    public List<Charger> findAllWithStationBySearchCondition(List<Long> ids, SearchCondition condition) {
        return selectFrom(charger)
                .join(charger.station, station).fetchJoin()
                .where(
                        charger.id.in(ids),
                        cpStatEq(condition.getCpStat()),
                        chargerTpEq(condition.getChargerTp()),
                        cpTpEq(condition.getCpTp())
                )
                .fetch();
    }

    @Override
    public List<Charger> findAllWithStation(List<Long> ids) {
        return selectFrom(charger)
                .join(charger.station, station).fetchJoin()
                .where(charger.id.in(ids))
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
