package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.exception.CStationNotFoundException;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.dto.response.UserBookmarkDto;
import com.ecar.servicestation.modules.user.dto.response.UserHistory;
import com.ecar.servicestation.modules.user.exception.CBookmarkFailedException;
import com.ecar.servicestation.modules.user.exception.CBookmarkNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
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
public class UserBasicService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final HistoryRepository historyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    public Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    public List<UserHistory> getUserHistories(Pageable pageable) {
        Account account = getUserBasicInfo();
        List<History> histories = historyRepository.findAllWithStationByAccountAndPaging(account.getId(), pageable).getContent();

        return histories.stream()
                .map(history -> {
                    UserHistory userHistory = modelMapper.map(history, UserHistory.class);
                    userHistory.setChargerCount(history.getStation().getChargers().size());

                    return userHistory;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveBookmark(long id) {
        Account account = getUserBasicInfo();
        Station station = stationRepository.findById(id).orElseThrow(CStationNotFoundException::new);

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

    public List<UserBookmarkDto> getUserBookmark(Pageable pageable) {
        Account account = getUserBasicInfo();
        List<Bookmark> bookmarks = bookmarkRepository.findAllWithStationByAccountAndPaging(account.getId(), pageable).getContent();

        return bookmarks.stream()
                .map(bookmark -> {
                    UserBookmarkDto userBookmark = modelMapper.map(bookmark, UserBookmarkDto.class);
                    userBookmark.setChargerCount(bookmark.getStation().getChargers().size());

                    return userBookmark;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBookmark(long id) {
        Account account = getUserBasicInfo();
        Station station = stationRepository.findById(id).orElseThrow(CStationNotFoundException::new);
        Bookmark bookmark = bookmarkRepository.findBookmarkByAccountAndStation(account, station);

        if (bookmark == null) {
            throw new CBookmarkNotFoundException();
        }

        bookmarkRepository.delete(account.removeBookmark(bookmark));
    }

    public List<ReservationStatementDto> getUserReservationStatements(String state) {
        Account account = getUserBasicInfo();
        List<ReservationTable> myReserveItems = reservationRepository.findAllWithChargerAndCarByAccountAndState(account.getId(), convertToReservationState(state));

        return myReserveItems.stream()
                .map(reserveItem -> convertToReservationStatement(account.getUsername(), reserveItem))
                .collect(Collectors.toList());
    }

    private ReservationStatementDto convertToReservationStatement(String userName, ReservationTable reserveItem) {
        ReservationStatementDto statement = modelMapper.map(reserveItem, ReservationStatementDto.class);
        statement.setUserName(userName);
        statement.setCarNumber(reserveItem.getCar().getCarNumber());
        statement.setCharger(reserveItem.getCharger());
        statement.setState(reserveItem.getReserveState().name());

        return statement;
    }

    private ReservationState convertToReservationState(String state) {
        if (state.equals("0")) {
            return ReservationState.PAYMENT;

        } else if (state.equals("1")) {
            return ReservationState.CHARGING;

        } else {
            return ReservationState.COMPLETE;
        }
    }
}
