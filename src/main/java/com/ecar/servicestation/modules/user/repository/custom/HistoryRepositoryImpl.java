package com.ecar.servicestation.modules.user.repository.custom;

import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.user.domain.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QStation.*;
import static com.ecar.servicestation.modules.user.domain.QAccount.*;
import static com.ecar.servicestation.modules.user.domain.QHistory.*;

public class HistoryRepositoryImpl extends Querydsl4RepositorySupport implements HistoryRepositoryCustom {

    public HistoryRepositoryImpl() {
        super(History.class);
    }

    @Override
    public Page<History> findAllWithStationByAccountAndPaging(Long accountId, Pageable pageable) {
        List<History> fetch =
                selectFrom(history)
                        .join(history.account, account)
                        .join(history.station, station).fetchJoin()
                        .where(account.id.eq(accountId))
                        .fetch();

        return applyPagination(pageable, countQuery -> countQuery.selectFrom(history).where(history.in(fetch)));
    }
}
