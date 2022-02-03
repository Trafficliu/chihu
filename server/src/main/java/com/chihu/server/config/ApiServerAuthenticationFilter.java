package com.chihu.server.config;

import com.chihu.server.common.ApiServerConstants;
import com.chihu.server.model.ChihuUserDetails;
import com.chihu.server.service.ChihuUserDetailsService;
import com.chihu.server.utils.JwtTokenUtil;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ApiServerAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ChihuUserDetailsService userDetailsService;

    @Autowired
    private CookieConfigurationProvider cookieConfigurationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            // TODO: Optimize user details construct here.
            //       Right now it is checking the DB too often.
            // TODO: Also need to optimize Jwt token setting here.
            if (StringUtils.hasText(jwt) && jwtTokenUtil.isTokenValid(jwt)
                && SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                String username = jwtTokenUtil.extractUsername(jwt);
                ChihuUserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(
                    authenticationToken);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication. ", e);
        }

        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith(ApiServerConstants.BEARER_HEADER_PREFIX)) {
            return bearerToken.substring(7);
        }

        if (request.getCookies() == null) {
            return null;
        }
        // TODO: clean up this part
        String origin = request.getHeader("origin");
        if (Strings.isNullOrEmpty(origin)) {
            return null;
        }
        String jwtKey = cookieConfigurationProvider.getJwtKeyFromOrigin(origin);

        for (Cookie cookie : request.getCookies()) {
            if (jwtKey.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
