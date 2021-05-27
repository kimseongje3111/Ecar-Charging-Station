package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.user.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
