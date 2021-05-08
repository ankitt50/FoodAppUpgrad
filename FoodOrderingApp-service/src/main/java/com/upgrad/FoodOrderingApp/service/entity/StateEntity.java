package com.upgrad.FoodOrderingApp.service.entity;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "state")
@NamedQueries({@NamedQuery(name = "StateUuid",query = "SELECT s FROM StateEntity s WHERE s.uuid = :uuid"),
        @NamedQuery(name = "getAllStates", query = "select s from StateEntity s")
})
public class StateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "state_name")
    private String stateName;

    @OneToMany(mappedBy = "state", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses = new ArrayList<AddressEntity>();


    public StateEntity() {
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

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return "StateEntity{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", stateName='" + stateName + '\'' +
                ", addresses=" + addresses +
                '}';
    }
}
