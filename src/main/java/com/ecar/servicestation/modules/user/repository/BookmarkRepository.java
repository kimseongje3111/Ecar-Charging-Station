package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.repository.custom.BookmarkRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URLConnection;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

    boolean existsBookmarkByAccountAndStation(Account account, Station station);

    Bookmark findBookmarkByAccountAndStation(Account account, Station station);

}
