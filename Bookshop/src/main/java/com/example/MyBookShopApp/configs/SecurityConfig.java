package com.example.MyBookShopApp.configs;

import com.example.MyBookShopApp.services.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    private Filter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
                Cookie token = null;
                if (request.getCookies() != null) {
                    token = Stream.of(request.getCookies()).filter(cookie -> cookie.getName().equals("user_token")).findFirst().orElse(null);
                }
                if (token != null && token.getValue() != null) {
                    Claims data = userService.extractUserData(token.getValue());
                    if (data != null && userService.getUserEntityByHash(data.getSubject()) != null) {
                        List<String> authorities = data.get("authorities", ArrayList.class);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(data.getSubject(), null,
                                authorities.stream().map(SimpleGrantedAuthority::new).toList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        token.setMaxAge(0);
                        response.addCookie(token);
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        tomcat.addAdditionalTomcatConnectors(connector);
        return tomcat;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] securedPaths = {"/download/*", "/my*/**", "/profile", "/editProfile", "/payment", "/successPayment",
                "/failPayment", "/transactions"};
        return http
                .requiresChannel().anyRequest().requiresSecure()
                .and()
                .portMapper().http(80).mapsTo(443).http(8080).mapsTo(8085)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(securedPaths).hasRole("USER")
                .requestMatchers("/**").permitAll()
                .and()
                .formLogin().loginPage("/signin")
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")).deleteCookies("user_token")
                .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect(request.getHeader("Referer")))
                .and()
                .build();
    }

}
