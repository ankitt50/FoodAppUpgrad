package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "address")
@NamedQueries({@NamedQuery(name = "getAllSavedAddresses", query = "select a from AddressEntity a"),
        @NamedQuery(name = "AddressUuid",query = "SELECT a FROM AddressEntity a WHERE a.uuid = :uuid")
})
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "flat_buil_number")
    private String flatBuildNumber;

    @Column(name = "locality")
    private String locality;

    @Column(name = "city")
    private String city;

    @Column(name = "pincode")
    private String pincode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id")
    private StateEntity state;

    @Column(name = "active")
    private int active;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "address")
//    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "customer_address",
//            joinColumns = { @JoinColumn(name = "address_id") },
//            inverseJoinColumns = { @JoinColumn(name = "customer_id") }
//    )
    private List<CustomerEntity> customer;


    public AddressEntity() {
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

    public String getFlatBuildNumber() {
        return flatBuildNumber;
    }

    public void setFlatBuildNumber(String flatBuildNumber) {
        this.flatBuildNumber = flatBuildNumber;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public StateEntity getState() {
        return state;
    }

    public void setState(StateEntity state) {
        this.state = state;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public List<CustomerEntity> getCustomers() {
        return customer;
    }

    public void setCustomers(List<CustomerEntity> customers) {
        this.customer = customers;
    }

    @Override
    public String toString() {
        return "AddressEntity{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", flatBuildNumber='" + flatBuildNumber + '\'' +
                ", locality='" + locality + '\'' +
                ", city='" + city + '\'' +
                ", pincode='" + pincode + '\'' +
                ", state=" + state +
                ", active=" + active +
                ", customers=" + customer +
                '}';
    }
}
