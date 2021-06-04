package com.ecar.servicestation.modules.user.repository.custom;

import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QStation.*;
import static com.ecar.servicestation.modules.user.domain.QAccount.*;
import static com.ecar.servicestation.modules.user.domain.QBookmark.*;

public class BookmarkRepositoryImpl extends Querydsl4RepositorySupport implements BookmarkRepositoryCustom {

    public BookmarkRepositoryImpl() {
        super(Bookmark.class);
    }

    @Override
    public Page<Bookmark> findAllWithStationByAccountAndPaging(Long accountId, Pageable pageable) {
        List<Bookmark> fetch =
                selectFrom(bookmark)
                        .join(bookmark.account, account)
                        .join(bookmark.station, station).fetchJoin()
                        .where(account.id.eq(accountId))
                        .fetch();

        return applyPagination(pageable, countQuery -> countQuery.selectFrom(bookmark).where(bookmark.in(fetch)));
    }
}
