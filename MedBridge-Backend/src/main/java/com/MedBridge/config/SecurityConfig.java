package com.MedBridge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.MedBridge.filter.JwtFilter;
import com.MedBridge.service.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService);
//    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors() // ✅ Enable CORS
                .and()
                .csrf().disable()
                .authorizeRequests()

                // ✅ Allow preflight OPTIONS requests
                .antMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // Public routes
                .antMatchers("/api/patient/register", "/api/patient/login",
                        "/api/admin/gender", "/api/patient/bloodgroup/all",
                        "/api/doctor/register", "/api/doctor/login","/api/admin/register",
                        "/api/admin/login", "/api/doctor/specialist/all","/api/doctor/by-speciality/**",
                        "/api/doctor/**","/api/doctor/image/**").permitAll()

                // Role-based protection

                .antMatchers("/api/admin/**","/api/patient/all","/api/doctor/all",
                        "/api/appointment/all","/api/appointment/id","/api/appointment/admin/assign/doctor","/api/admin/delete/id")
                        .hasAuthority("admin")

                .antMatchers("/api/appointment/id/update",
                        "/api/admin/delete/id","/api/doctor/id").hasAuthority("doctor")

                .antMatchers("/api/patient/**","/api/appointment/patient/add",
                        "/api/payment/verify","/api/appointment/patient/id","/api/payment/createOrder","/api/appointment/patient/update"
                        ,"/api/payment/createOrder","/api/doctor/all","/api/ambulance/**").hasAuthority("patient")

                .antMatchers("/api/doctor/all","/api/admin/delete/id").hasAnyAuthority("admin",  "doctor")


                .anyRequest().authenticated()

                .and()
                .exceptionHandling()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }



}
