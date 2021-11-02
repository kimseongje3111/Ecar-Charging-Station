package com.ecar.servicestation.modules.ecar.repository.custom;


import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchLocationDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QStation.*;

public class ChargerRepositoryImpl extends Querydsl4RepositorySupport implements ChargerRepositoryCustom {

    public ChargerRepositoryImpl() {
        super(Charger.class);
    }

    @Override
    public Charger findChargerWithStationById(long id) {
        return selectFrom(charger)
                .join(charger.station, station).fetchJoin()
                .where(charger.id.eq(id))
                .fetchOne();
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
    public Page<Charger> findAllWithStationPaging(List<Long> ids, Pageable pageable) {
        List<Charger> fetch =
                selectFrom(charger)
                        .join(charger.station, station).fetchJoin()
                        .where(charger.id.in(ids))
                        .fetch();

        return applyPagination(pageable, countQuery -> countQuery.selectFrom(charger).where(charger.in(fetch)));
    }

}
