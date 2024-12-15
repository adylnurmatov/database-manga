package org.adyl.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.adyl.views.authentication.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(request -> request.requestMatchers("/images/**").permitAll());
//        http.authorizeHttpRequests(request -> request.requestMatchers("/welcome").permitAll());
//        http.authorizeHttpRequests(request -> request.requestMatchers("/**").authenticated());
        setLoginView(http, LoginView.class);
        super.configure(http);

//        super.configure(http);
//        setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }
}
