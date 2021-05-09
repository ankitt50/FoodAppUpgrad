package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressBusinessService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AddressController {

    @Autowired
    private AddressBusinessService addressBusinessService;

    @Autowired
    private CustomerBusinessService customerBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/address",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader(name = "authorization") final String authToken,@RequestBody final SaveAddressRequest saveAddressRequest)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {


        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/address");
        AddressEntity addressEntity = new AddressEntity();

        boolean isAnyFieldEmpty = false;

        if(saveAddressRequest.getFlatBuildingName()==null || saveAddressRequest.getFlatBuildingName().equals("")){
            isAnyFieldEmpty = true;
        }else{
            addressEntity.setFlatBuildNumber(saveAddressRequest.getFlatBuildingName());
        }

        if(saveAddressRequest.getLocality()==null || saveAddressRequest.getLocality().equals("")){
            isAnyFieldEmpty = true;
        }else{
            addressEntity.setLocality(saveAddressRequest.getLocality());
        }

        if(saveAddressRequest.getCity()==null || saveAddressRequest.getCity().equals("")){
            isAnyFieldEmpty = true;
        }else{
            addressEntity.setCity(saveAddressRequest.getCity());
        }

        if(saveAddressRequest.getPincode()==null || saveAddressRequest.getPincode().equals("")){
            isAnyFieldEmpty = true;
        }else{
            addressEntity.setPincode(saveAddressRequest.getPincode());
        }

        if(saveAddressRequest.getStateUuid()==null || saveAddressRequest.getStateUuid().equals("")){
            isAnyFieldEmpty = true;
        }else{
            StateEntity state = addressBusinessService.getStateByUuid(saveAddressRequest.getStateUuid());
            addressEntity.setState(state);
        }

        if(isAnyFieldEmpty) {
            throw new SaveAddressException("SAR-001","No field can be empty");
        }
        if(!addressBusinessService.isPincodeValid(saveAddressRequest.getPincode())){
            throw new SaveAddressException("SAR-002","Invalid pincode");
        }
        if(addressEntity.getState() == null) {
            throw new AddressNotFoundException("ANF-002","No state by this id");
        }

        AddressEntity savedAddress = addressBusinessService.saveAddress(addressEntity, customerEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(savedAddress.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);

    }

    @GetMapping(path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllSavedAddresses(@RequestHeader(name = "authorization") final String authToken) throws AuthorizationFailedException {

        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/address/customer");
        List<AddressEntity> savedAddressesList = customerEntity.getAddresses();

        AddressListResponse addressListResponse = new AddressListResponse();

        if (savedAddressesList == null || savedAddressesList.isEmpty()) {
            return new ResponseEntity<>(addressListResponse.addresses(Collections.emptyList()), HttpStatus.OK);
        }

        Collections.sort(savedAddressesList, new Comparator<AddressEntity>() {
            @Override
            public int compare(AddressEntity o1, AddressEntity o2) {
                return o2.getId() - o1.getId();
            }
        });

        List<AddressList> addressList = new ArrayList<AddressList>();

        for (AddressEntity e:
             savedAddressesList) {
            AddressList address = new AddressList();
            AddressListState state = new AddressListState();
            state.id(UUID.fromString(e.getState().getUuid())).stateName(e.getState().getStateName());
            address.id(UUID.fromString(e.getUuid())).
                    flatBuildingName(e.getFlatBuildNumber()).
                    locality(e.getLocality())
                    .city(e.getCity()).pincode(e.getPincode()).state(state);
            addressList.add(address);
        }
        addressListResponse.addresses(addressList);

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }



    @GetMapping(path = "/states", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates(){

        List<StateEntity> savedStatesList = addressBusinessService.getAllStates();
        StatesListResponse statesListResponse = new StatesListResponse();

        List<StatesList> statesList = new ArrayList<StatesList>();

        for (StateEntity e:
                savedStatesList) {
            StatesList state = new StatesList();
            state.id(UUID.fromString(e.getUuid())).stateName(e.getStateName());
            statesList.add(state);
        }
        statesListResponse.states(statesList);

        return new ResponseEntity<StatesListResponse>(statesListResponse,HttpStatus.OK);
    }

    @DeleteMapping(path = "/address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@PathVariable(name = "address_id") final String addressID, @RequestHeader(name = "authorization") final String authToken) throws AuthorizationFailedException, AddressNotFoundException {
        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/address/{address_id}");

        if(addressID==null || addressID.equals("")){
            throw new AddressNotFoundException("ANF-005)","Address id can not be empty");
        }

        AddressEntity addressEntity = addressBusinessService.getAddressByUuid(addressID);

        if(addressEntity==null) {
            throw new AddressNotFoundException("ANF-003","No address by this id");
        }

        boolean anyUserMatched = false;
        AddressEntity deletedAddress = new AddressEntity();
        if(addressEntity.getCustomers()==null || addressEntity.getCustomers().isEmpty()) {
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
        }
        else {
            for (CustomerEntity e:
                 addressEntity.getCustomers()) {
                if(customerEntity.getContactNumber().equals(e.getContactNumber())) {
                    anyUserMatched = true;
                }
            }
            if (anyUserMatched) {
                deletedAddress = addressBusinessService.deleteAddress(addressEntity, customerEntity);
            }
            else {
                throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
            }
        }

        return new ResponseEntity<DeleteAddressResponse>(new DeleteAddressResponse().id(UUID.fromString(deletedAddress.getUuid())).status("ADDRESS DELETED SUCCESSFULLY"),HttpStatus.OK);
    }

    // this method extracts the token from the JWT token string sent in the Request Header
    private String getToken(String authToken) {
        String token;
        if (authToken.startsWith("Bearer ")) {
            String [] bearerToken = authToken.split("Bearer ");
            token = bearerToken[1];
        } else {
            token = authToken;
        }
        return token;
    }
}
