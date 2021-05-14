package com.ecar.servciestation.modules.User.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    // 유저 기본 정보 //

    private String userName;

    private String password;

    private String email;

    // 유저 인증 정보 //

    private boolean verified;

    private String checkToken;

    private LocalDateTime checkTokenGeneratedAt;

    private LocalDateTime joinedAt;

    // 개인화 서비스 //

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserBookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserHistory> histories = new ArrayList<>();

    // 연관 관계 설정 메서드 //

    public void addBookmark(UserBookmark bookmark) {
        this.bookmarks.add(bookmark);
        bookmark.setUser(this);
    }

    public void addHistory(UserHistory history) {
        this.histories.add(history);
        history.setUser(this);
    }

}
