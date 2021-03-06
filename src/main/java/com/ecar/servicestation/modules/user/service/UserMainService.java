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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMainService {

    private static final String RESERVED_ITEMS_STATE_NOT_CANCEL = "0";
    private static final String RESERVED_ITEMS_STATE_COMPLETE = "1";

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
        List<ReservationTable> reservedItems = new ArrayList<>();

        if (state.equals(RESERVED_ITEMS_STATE_NOT_CANCEL)) {
            reservedItems = reservationRepository.findAllWithChargerAndCarByAccountAndNotCancel(account.getId());

        } else if (state.equals(RESERVED_ITEMS_STATE_COMPLETE)) {
            reservedItems = reservationRepository.findAllWithChargerAndCarByAccountAndComplete(account.getId());
        }

        return reservedItems.stream()
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
        statement.setReservationId(reservedItem.getId());
        statement.setChargerId(reservedItem.getCharger().getId());
        statement.setCarNumber(reservedItem.getCar().getCarNumber());
        statement.setState(reservedItem.getReserveState().name());
        statement.setPaidCash(reservedItem.getReserveFares() - reservedItem.getUsedCashPoint());
        statement.setCancellationFee(calCancellationFee(reservedItem));

        if (reservedItem.getReserveTitle() != null) {
            statement.setReserveTitle(reservedItem.getReserveTitle());
        }

        if (reservedItem.getReserveState().equals(ReservationState.STAND_BY)) {
            statement.setPaidCash(0);
            statement.setCancellationFee(0);

        } else if (reservedItem.getReserveState().equals(ReservationState.CHARGING)) {
            statement.setCancellationFee(0);
        }

        return statement;
    }

    private int calCancellationFee(ReservationTable reservedItem) {

        // ?????? ????????? ?????? //

        LocalDateTime start = reservedItem.getChargeStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        return start.minusHours(1).isAfter(now) ? 0 : (int) (reservedItem.getReserveFares() * 0.1);
    }
}
