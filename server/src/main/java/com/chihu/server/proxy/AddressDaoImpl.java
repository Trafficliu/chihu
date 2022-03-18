package com.chihu.server.proxy;

import com.chihu.server.model.Address;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AddressDaoImpl implements AddressDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void createAddress(@NonNull Address address) {
        String sql =
            "INSERT INTO addresses " +
            "        SET user_id = :user_id, " +
            "            contact_name = :contact_name, " +
            "            phone_number = :phone_number, " +
            "            address_line1 = :address_line1, " +
            "            address_line2 = :address_line2, " +
            "            city = :city, " +
            "            state = :state, " +
            "            country = :country, " +
            "            zip_code = :zip_code, " +
            "            geo_code = :geo_code "
            ;
        // TODO: add integration with Google geo/map to properly generate the geo code
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", address.getUserId())
            .addValue("contact_name", address.getContactName())
            .addValue("phone_number", address.getPhoneNumber())
            .addValue("address_line1", address.getAddressLine1())
            .addValue("address_line2", address.getAddressLine2())
            .addValue("city", address.getCity())
            .addValue("state", address.getState())
            .addValue("country", address.getCountry())
            .addValue("zip_code", address.getZipCode())
            .addValue("geo_code", null)
            ;

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void updateAddress(@NonNull Address address) {
        String sql =
            "UPDATE addresses " +
            "   SET contact_name = :contact_name, " +
            "       phone_number = :phone_number, " +
            "       address_line1 = :address_line1, " +
            "       address_line2 = :address_line2, " +
            "       city = :city, " +
            "       state = :state, " +
            "       country = :country, " +
            "       zip_code = :zip_code, " +
            "       geo_code = :geo_code " +
            " WHERE address_id = :address_id "+
            "       AND user_id = :user_id "
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("address_id", address.getAddressId())
            .addValue("user_id", address.getUserId())
            .addValue("contact_name", address.getContactName())
            .addValue("phone_number", address.getPhoneNumber())
            .addValue("address_line1", address.getAddressLine1())
            .addValue("address_line2", address.getAddressLine2())
            .addValue("city", address.getCity())
            .addValue("state", address.getState())
            .addValue("country", address.getCountry())
            .addValue("zip_code", address.getZipCode())
            .addValue("geo_code", address.getGeoCode())
            ;

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void deleteAddress(@NonNull Long addressId, @NonNull Long userId) {
        String sql =
            "DELETE FROM addresses " +
            "      WHERE address_id = :address_id" +
            "            AND user_id = :user_id"
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("address_id", addressId)
            .addValue("user_id", userId);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public Optional<Address> getAddressById(@NonNull Long addressId) {
        String sql =
            "SELECT * " +
            "  FROM addresses " +
            " WHERE address_id = :address_id" +
            " LIMIT 1";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("address_id", addressId);

        List<Address> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Address.class));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.iterator().next());
    }

    @Override
    public List<Address> getUserAddresses(@NonNull Long userId) {
        String sql =
            "SELECT * " +
            "  FROM addresses " +
            " WHERE user_id = :user_id"
            ;
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("user_id", userId);

        List<Address> result =
            namedParameterJdbcTemplate.query(
                sql,
                parameterSource,
                new BeanPropertyRowMapper<>(Address.class));
        return result;
    }
}
