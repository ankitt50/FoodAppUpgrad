package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_auth")
@NamedQueries({@NamedQuery(name = "CheckAuthToken",query = "SELECT a FROM CustomerAuthEntity a WHERE a.accessToken =:accessToken")})
public class CustomerAuthEntity {

    // primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "uuid")
    private String uuid;

    // user can have multiple login sessions
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "expires_at")
    private LocalDateTime expiryTime;

    @Column(name = "login_at")
    private LocalDateTime loginTime;

    @Column(name = "logout_at")
    private LocalDateTime LogoutTime;

    public CustomerAuthEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return LogoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        LogoutTime = logoutTime;
    }

    @Override
    public String toString() {
        return "CustomerAuthEntity{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", customer=" + customer +
                ", accessToken='" + accessToken + '\'' +
                ", expiryTime=" + expiryTime +
                ", loginTime=" + loginTime +
                ", LogoutTime=" + LogoutTime +
                '}';
    }
}
