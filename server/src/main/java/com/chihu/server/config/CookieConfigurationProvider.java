package com.chihu.server.config;

import com.chihu.server.common.ApiServerConstants;
import com.chihu.server.model.UserType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CookieConfigurationProvider {

    @Value("${server.cookie.domain}")
    private String domain;

    @Value("${server.cookie.eater.origin}")
    private String eaterOrigin;

    @Value("${server.cookie.owner.origin}")
    private String ownerOrigin;

    @Value("${server.cookie.admin.origin}")
    private String adminOrigin;

    @Value ("${server.cookie.path}")
    private String path;

    @Value("${server.cookie.secure}")
    private boolean secure;

    @Value("${server.cookie.httpOnly}")
    private boolean httpOnly;

    public String getJwtKeyFromOrigin(String origin) {
        if (origin.equals(eaterOrigin)) {
            return ApiServerConstants.JWT_COOKIE_NAME_EATER;
        } else if (origin.equals(ownerOrigin)) {
            return ApiServerConstants.JWT_COOKIE_NAME_OWNER;
        } else if (origin.equals(adminOrigin)) {
            return ApiServerConstants.JWT_COOKIE_NAME_ADMIN;
        }
        throw new IllegalArgumentException(String.format("Unrecognized origin %s", origin));
    }

    public String getJwtKeyFromType(int userTypeId) {
        if (userTypeId == UserType.EATER.getId()) {
            return ApiServerConstants.JWT_COOKIE_NAME_EATER;
        } else if (userTypeId == UserType.OWNER.getId()) {
            return ApiServerConstants.JWT_COOKIE_NAME_OWNER;
        } else if (userTypeId == UserType.ADMIN.getId()) {
            return ApiServerConstants.JWT_COOKIE_NAME_ADMIN;
        }
        throw new IllegalArgumentException(String.format("Unrecognized type %d", userTypeId));
    }
}
