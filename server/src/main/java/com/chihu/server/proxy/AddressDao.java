package com.chihu.server.proxy;

import com.chihu.server.model.Address;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AddressDao {
    void createAddress(@NonNull Address address);
    void updateAddress(@NonNull Address address);
    void deleteAddress(@NonNull Long addressId, @NonNull Long userId);

    Optional<Address> getAddressById(@NonNull Long addressId);
    List<Address> getUserAddresses(@NonNull Long userId);
}
