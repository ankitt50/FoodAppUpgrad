package com.upgrad.FoodOrderingApp.service.businness;


import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AddressBusinessService {


    @Autowired
    private AddressDao addressDao;

    @Transactional
    public StateEntity getStateByUuid(String uuid) {
        StateEntity stateEntity =  addressDao.getStateByUuid(uuid) ;
        return stateEntity;
    }

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity){
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setActive(1);
        List<AddressEntity> customersAddressesList = customerEntity.getAddresses();
        customersAddressesList.add(addressEntity);
//        List<CustomerEntity> customerWithAddressList = new ArrayList<CustomerEntity>();
//        customerWithAddressList.add(customerEntity);

        CustomerEntity updatedCustomer = addressDao.saveAddress(customerEntity);
        boolean isAddressSaved = false;
        for (AddressEntity e:
             updatedCustomer.getAddresses()) {
            if(e.getUuid() == addressEntity.getUuid()) {
                isAddressSaved = true;
            }
        }

        if (isAddressSaved) {
            return addressEntity;
        }
        else {
            return null;
        }


    }

    @Transactional
    public List<AddressEntity> getAllSavedAddresses() {
        return addressDao.getAllSavedAddresses();
    }


    @Transactional
    public List<StateEntity> getAllStates() {
        return addressDao.getAllStates();
    }

    @Transactional
    public AddressEntity getAddressByUuid(String uuid) {
        return addressDao.getAddressByUuid(uuid);
    }

    @Transactional
    public AddressEntity deleteAddress(AddressEntity addressEntity, CustomerEntity customerEntity) {
        int index = -1;
        int currentIndex = 0;
        List<AddressEntity> customersAddressesList = customerEntity.getAddresses();
        for (AddressEntity e:
             customersAddressesList) {
            if(e.getUuid() == addressEntity.getUuid()) {
                index = currentIndex;
            }
            currentIndex++;
        }
        customersAddressesList.remove(index);
        CustomerEntity savedCustomer = addressDao.deleteAddress(customerEntity, addressEntity);
        boolean isAddressDeleted = true;
        for (AddressEntity e:
                savedCustomer.getAddresses()) {
            if(e.getUuid() == addressEntity.getUuid()) {
                isAddressDeleted = false;
            }
        }

        if (isAddressDeleted) {
            return addressEntity;
        }
        else {
            return null;
        }
    }

    public boolean isPincodeValid(String pincode) {
        if(pincode.length()!=6) {
            return false;
        }
        else {
            char[] charsInPinCode = pincode.toCharArray();
            boolean notPureNumber = false;
            for (char c: charsInPinCode) {
                if (c > 57 || c<48) {
                    notPureNumber = true;
                }
            }
            if (notPureNumber) {
                return false;
            }
            else {
                return true;
            }
        }
    }

}
