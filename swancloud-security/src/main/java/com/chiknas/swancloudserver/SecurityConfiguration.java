package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.jwt.JwtSecurityConfigurerAdapter;
import com.chiknas.swancloudserver.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@Profile("production")
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfiguration(UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Security configuration for the web application. This includes all MVC endpoints
     * at the main context (starting with /).
     */
    @Bean
    @Order(3)
    public SecurityFilterChain webAppFilterChain(HttpSecurity http) throws Exception {
        return withHttpSecurity(http)
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/login", "/img/**", "/css/**", "/access-denied")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .and()
                .exceptionHandling()
                .accessDeniedPage("/access-denied")
                .and().build();
    }

    /**
     * Security configuration for the rest api part of the application. This includes all rest endpoints under
     * the api context (starting with /api)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain restApiFilterChain(HttpSecurity http) throws Exception {
        return withRestHttpSecurity(http)
                .antMatcher("/api/**")
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and().build();
    }

    /**
     * Security configuration for the authentication endpoints of the app. This includes all endpoints under
     * the authentication context (starting with /auth)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenticationFilterChain(HttpSecurity http) throws Exception {
        return withRestHttpSecurity(http)
                .antMatcher("/auth/**")
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/auth/signin")
                .permitAll()
                .anyRequest().authenticated()
                .and().build();
    }

    /**
     * Http security configuration for every single HTTP endpoint in the system.
     */
    private HttpSecurity withHttpSecurity(HttpSecurity http) throws Exception {
        return http
                .authenticationProvider(authProvider())
                .apply(new JwtSecurityConfigurerAdapter(jwtTokenProvider))
                .and();
    }

    /**
     * Http security for REST endpoints in the system. Basic http security {@link SecurityConfiguration#withHttpSecurity}
     * rules apply too.
     */
    private HttpSecurity withRestHttpSecurity(HttpSecurity http) throws Exception {
        return withHttpSecurity(http)
//              Enable for POST requests to work with postman
//                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();
    }


    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
