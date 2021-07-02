package com.ecar.servicestation.modules.user.domain;

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

    // 유저 정보 //

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    @JsonProperty(access = WRITE_ONLY)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    private String phoneNumber;

    private LocalDateTime joinedAt;

    // 인증 정보 //

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    private boolean emailAuthVerified;

    @JsonProperty(access = WRITE_ONLY)
    private String emailAuthToken;

    @JsonProperty(access = WRITE_ONLY)
    private LocalDateTime emailAuthTokenGeneratedAt;

    // 유저 서비스 //

    @Builder.Default
    @JsonProperty(access = WRITE_ONLY)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<History> histories = new ArrayList<>();

    @Builder.Default
    @JsonProperty(access = WRITE_ONLY)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Builder.Default
    @JsonProperty(access = WRITE_ONLY)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Car> myCars = new ArrayList<>();

    @Builder.Default
    @JsonProperty(access = WRITE_ONLY)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Bank> myBanks = new ArrayList<>();

    private Integer cash;

    private Integer cashPoint;

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

    public void addHistory(History history) {
        this.histories.add(history);
        history.setAccount(this);
    }

    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.add(bookmark);
        bookmark.setAccount(this);
    }

    public Bookmark removeBookmark(Bookmark bookmark) {
        this.bookmarks.remove(bookmark);

        return bookmark;
    }

    public void addCar(Car car) {
        this.myCars.add(car);
        car.setAccount(this);
    }

    public Car removeCar(Car car) {
        this.myCars.remove(car);

        return car;
    }

    public void addBank(Bank bank) {
        if (this.myBanks.size() == 0) {
            bank.setMainUsed(true);
        }

        this.myBanks.add(bank);
        bank.setAccount(this);
    }

    public Bank removeBank(Bank bank) {
        this.myBanks.remove(bank);

        if (bank.isMainUsed() && this.myBanks.size() != 0) {
            this.myBanks.get(0).setMainUsed(true);
        }

        return bank;
    }

    // 비지니스 메서드 //

    public void generateEmailAuthToken() {
        this.emailAuthToken = UUID.randomUUID().toString();
        this.emailAuthTokenGeneratedAt = LocalDateTime.now();
    }

    public void successEmailAuthentication() {
        this.emailAuthVerified = true;
        this.joinedAt = LocalDateTime.now();
        this.cash = 0;
        this.cashPoint = 5000;
    }

    public Bank getMyMainUsedBank() {
        for (Bank myBank : this.myBanks) {
            if (myBank.isMainUsed() && myBank.isBankAccountVerified()) {
                return myBank;
            }
        }

        return null;
    }

    public void chargingCash(int amount) {
        this.cash += amount;
    }

    public void paymentOrRefundCash(int amount) {
        this.cash -= amount;
    }

    public void chargingCashPoint(int amount) {
        this.cashPoint += amount;
    }

    public void paymentCashPoint(int amount) {
        this.cashPoint -= amount;
    }
}
