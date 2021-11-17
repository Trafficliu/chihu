package com.chihu.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ApiServerAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${whitelist.ip.enable:false}")
    private boolean enableWhitelistIp;

    @Value("${whitelist.ip.list")
    private String whitelistIpList;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors()
            .and()
            .csrf().disable()
            .exceptionHandling()
            // TODO: re-enable this when you have a login page
            .authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // Users need admin role to access admin pages
            .antMatchers("/admin/**").hasAuthority("ADMIN")
            .antMatchers("/eater/**").hasAuthority("EATER")
            .antMatchers("/owner/**").hasAuthority("OWNER")
            .antMatchers(
                    "/chihu/sign_in",
                    "/auth/sign_in",
                    "/auth/activate",
                    // TODO: find how to filter by ip address and move this to only internal ip
                    "/auth/request_activation_token", // This should only be enabled for integration test
                    "/auth/email_activation_token",
                    // TODO: find how to filter by ip address and move this to only internal ip
                    "/auth/request_pwd_reset_token", // This should only be enabled for integration test
                    "/auth/email_pwd_reset_token",
                    "/auth/reset_password_with_token",
                    "/chihu/sign_out",
                    "/chihu/register",
                    "/auth/register",
                    "/chihu/home",
                    "/chihu/server/auth/welcome",
                    "/")
                .permitAll()
            .anyRequest().authenticated()
            .and().formLogin()
            .and()
            .rememberMe()
            .tokenRepository(persistentTokenRepository())
            .tokenValiditySeconds(60*60*24*14);
        http.addFilterBefore(
            authenticationFilter(),
            UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
        auth.jdbcAuthentication()
                .dataSource(dataSource);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public ApiServerAuthenticationFilter authenticationFilter() {
        return new ApiServerAuthenticationFilter();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(10, new SecureRandom());
    }
}
