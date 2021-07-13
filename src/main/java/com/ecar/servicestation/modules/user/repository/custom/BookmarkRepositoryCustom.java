package com.ecar.servicestation.modules.user.repository.custom;

import com.ecar.servicestation.modules.user.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkRepositoryCustom {

    Page<Bookmark> findAllWithStationByAccountAndPaging(long accountId, Pageable pageable);

}
