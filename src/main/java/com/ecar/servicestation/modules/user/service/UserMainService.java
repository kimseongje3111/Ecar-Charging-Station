package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.books.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.exception.CStationNotFoundException;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.dto.response.users.UserBookmarkDto;
import com.ecar.servicestation.modules.user.dto.response.users.UserHistoryDto;
import com.ecar.servicestation.modules.user.exception.users.CBookmarkFailedException;
import com.ecar.servicestation.modules.user.exception.users.CBookmarkNotFoundException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMainService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final HistoryRepository historyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    public List<UserHistoryDto> getUserHistories(Pageable pageable) {
        Account account = getLoginUserContext();
        List<History> histories = historyRepository.findAllWithStationByAccountAndPaging(account.getId(), pageable).getContent();

        return histories.stream()
                .map(history -> {
                    UserHistoryDto userHistory = modelMapper.map(history, UserHistoryDto.class);
                    userHistory.setChargerCount(history.getStation().getChargers().size());

                    return userHistory;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerUserBookmark(long stationId) {
        Account account = getLoginUserContext();
        Station station = stationRepository.findById(stationId).orElseThrow(CStationNotFoundException::new);

        if (bookmarkRepository.findBookmarkByAccountAndStation(account, station) != null) {
            throw new CBookmarkFailedException();
        }

        account.addBookmark(
                bookmarkRepository.save(
                        Bookmark.builder()
                                .account(account)
                                .station(station)
                                .registeredAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    @Transactional
    public void deleteUserBookmark(long stationId) {
        Account account = getLoginUserContext();
        Station station = stationRepository.findById(stationId).orElseThrow(CStationNotFoundException::new);
        Bookmark bookmark = bookmarkRepository.findBookmarkByAccountAndStation(account, station);

        if (bookmark == null) {
            throw new CBookmarkNotFoundException();
        }

        bookmarkRepository.delete(account.removeBookmark(bookmark));
    }

    public List<UserBookmarkDto> getUserBookmarks(Pageable pageable) {
        Account account = getLoginUserContext();
        List<Bookmark> bookmarks = bookmarkRepository.findAllWithStationByAccountAndPaging(account.getId(), pageable).getContent();

        return bookmarks.stream()
                .map(bookmark -> {
                    UserBookmarkDto userBookmark = modelMapper.map(bookmark, UserBookmarkDto.class);
                    userBookmark.setChargerCount(bookmark.getStation().getChargers().size());

                    return userBookmark;
                })
                .collect(Collectors.toList());
    }

    public List<ReservationStatementDto> getUserReservationStatements(String state) {
        Account account = getLoginUserContext();
        List<ReservationTable> myReservedItems = reservationRepository.findAllWithChargerAndCarByAccountAndState(account.getId(), getReservationState(state));

        return myReservedItems.stream()
                .map(reservedItem -> convertToReservationStatement(account.getName(), reservedItem))
                .collect(Collectors.toList());
    }

    private Account getLoginUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    private ReservationStatementDto convertToReservationStatement(String userName, ReservationTable reservedItem) {
        ReservationStatementDto statement = modelMapper.map(reservedItem, ReservationStatementDto.class);
        statement.setUserName(userName);
        statement.setCarNumber(reservedItem.getCar().getCarNumber());
        statement.setCharger(reservedItem.getCharger());
        statement.setState(reservedItem.getReserveState().name());

        return statement;
    }

    private ReservationState getReservationState(String state) {
        if (state.equals("0")) {
            return ReservationState.PAYMENT;

        } else if (state.equals("1")) {
            return ReservationState.CHARGING;

        } else {
            return ReservationState.COMPLETE;
        }
    }

}
