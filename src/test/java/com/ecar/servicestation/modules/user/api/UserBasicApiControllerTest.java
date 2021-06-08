package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.factory.BookmarkFactory;
import com.ecar.servicestation.modules.user.factory.HistoryFactory;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class UserBasicApiControllerTest {

    private static final String USER = "/user";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    ECarStationFactory eCarStationFactory;

    @Autowired
    HistoryFactory historyFactory;

    @Autowired
    BookmarkFactory bookmarkFactory;

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Test
    @DisplayName("[회원 정보 조회]정상 처리")
    public void account_inquiry_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER).header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data").isNotEmpty());
    }

    @Test
    @DisplayName("[회원 정보 조회]실패 - 인증 헤더 및 토큰 미포함")
    public void account_inquiry_failed_by_not_token() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get(USER));

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[회원 정보 조회]실패 - 올바르지 않은 인증 토큰(잘못된 형식, 만료된 토큰)")
    public void account_inquiry_failed_by_invalid_token() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER).header("X-AUTH-TOKEN", "INVALID_TOKEN")
                );

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[최근 검색 목록 조회]정상 처리")
    public void find_histories_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger();
        History history = historyFactory.createHistory(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER + "/history")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[즐겨찾기 등록]정상 처리")
    public void save_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger();

        // When
        ResultActions perform =
                mockMvc.perform(
                        put(USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNotNull();
    }

    @Test
    @DisplayName("[즐겨찾기 목록 조회]정상 처리")
    public void find_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger();
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER + "/bookmark")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[즐겨찾기 삭제]정상 처리")
    public void delete_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger();
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNull();
    }

    @Test
    @DisplayName("[즐겨찾기 삭제]실패 - 즐겨찾기 목록에 없는 삭제 요청")
    public void delete_bookmark_failed_by_not_found() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger();
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(USER + "/bookmark/-1")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1003));
    }
}