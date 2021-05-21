package com.ecar.servciestation.modules.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Account implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "account_id")
    private Long id;

    // 유저 기본 정보 //

    @Column(nullable = false, length = 100)
    private String userName;

    @Column(nullable = false, length = 100)
    @JsonProperty(access = WRITE_ONLY)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    private LocalDateTime joinedAt;

    // 유저 인증 정보 //

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    private boolean emailAuthVerified;

    @JsonProperty(access = WRITE_ONLY)
    private String emailAuthToken;

    @JsonProperty(access = WRITE_ONLY)
    private LocalDateTime emailAuthTokenGeneratedAt;

    // 개인화 서비스 //

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<History> histories = new ArrayList<>();

    // Security UserDetails 메서드 재정의 //

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    @JsonProperty(access = WRITE_ONLY)
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonProperty(access = WRITE_ONLY)
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonProperty(access = WRITE_ONLY)
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonProperty(access = WRITE_ONLY)
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonProperty(access = WRITE_ONLY)
    public boolean isEnabled() {
        return true;
    }

    // 연관 관계 설정 메서드 //

    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.add(bookmark);
        bookmark.setAccount(this);
    }

    public void addHistory(History history) {
        this.histories.add(history);
        history.setAccount(this);
    }

    // 비지니스 메서드 //

    public void generateEmailAuthToken() {
        this.emailAuthToken = UUID.randomUUID().toString();
        this.emailAuthTokenGeneratedAt = LocalDateTime.now();
    }

    public void successEmailAuthentication() {
        this.emailAuthVerified = true;
        this.joinedAt = LocalDateTime.now();
    }
}
