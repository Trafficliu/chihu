package com.chihu.server.controller;

import com.chihu.server.model.Address;
import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.proxy.AddressDao;
import com.chihu.server.serializer.ApiServerSerializer;
import com.chihu.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressDao addressDao;

    @PostMapping("/address/create_address")
    public String createAddress(
        Authentication authentication,
        @RequestParam(value = "address_str", required = true) String addressStr
    ) throws IllegalArgumentException {

        Address address = ApiServerSerializer.toAddress(addressStr);
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        address.setUserId(userId);

        addressDao.createAddress(address);
        return "";
    }

    @PostMapping("/address/update_address")
    public String updateAddress(
        Authentication authentication,
        @RequestParam(value = "address_str", required = true) String addressStr
    ) throws IllegalArgumentException {

        Address address = ApiServerSerializer.toAddress(addressStr);
        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        address.setUserId(userId);

        addressDao.updateAddress(address);
        return "";
    }

    @PostMapping("/address/delete_address")
    public void deleteAddress(
        Authentication authentication,
        @RequestParam(value = "address_id", required = true) Long addressId
    ) throws IllegalArgumentException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        addressDao.deleteAddress(addressId, userId);
    }

    @GetMapping("/address/get_address_by_id")
    public Address getAddressById(
        Authentication authentication,
        @RequestParam(value = "address_id", required = true) Long addressId
    ) throws IllegalArgumentException {

        Optional<Address> address =
            addressDao.getAddressById(addressId);
        if (address.isEmpty()) {
            throw new IllegalArgumentException(
                "Couldn't find address with the provided ID");
        }

        return address.get();
    }

    @GetMapping("/address/get_user_addresses")
    public List<Address> getUserAddressBy(
        Authentication authentication) throws IllegalArgumentException {

        ChihuUserDetails userDetails =
            (ChihuUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        List<Address> userAddresses =
            addressDao.getUserAddresses(userId);

        return userAddresses;
    }
}
