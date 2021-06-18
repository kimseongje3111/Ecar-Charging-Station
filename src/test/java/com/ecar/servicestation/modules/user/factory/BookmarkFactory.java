package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookmarkFactory {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    @Transactional
    public Bookmark createBookmark(Account account, Station station) {
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        Bookmark bookmark = bookmarkRepository.save(
                Bookmark.builder()
                        .account(account)
                        .station(station)
                        .registeredAt(LocalDateTime.now())
                        .build()
        );

        account.addBookmark(bookmark);

        return bookmark;
    }
}
