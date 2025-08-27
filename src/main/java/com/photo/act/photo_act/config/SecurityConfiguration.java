package com.photo.act.photo_act.config;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private RecordService recordService;

    public SecurityConfiguration(RecordService recordService) {
        this.recordService = recordService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        //http.requiresChannel();
        setLoginView(http, LoginView.class, "/home");

    }


    @Bean
    public UserDetailsManager userDetailsManager() {

        String[] arrCols = {"userId", "username", "password", "user_rights_id", "role"};
        String strSql = " SELECT u.userId , u.username, u.password, u.user_rights_id, r.role " +
                " FROM dbuser u, dbuser_rights r " +
                " WHERE 1=1 " +
                " AND u.user_rights_id = r.id ";
        List<Record> lstUsers = recordService.findAll(strSql, arrCols);

        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        for (int r = 0; r < lstUsers.size(); r++) {
            UserDetails userDetails = User.builder()
                    .username(lstUsers.get(r).getColumnData("username"))
                    .password(lstUsers.get(r).getColumnData("password")) // encode the password
                    .roles(lstUsers.get(r).getColumnData("role"))
                    .build();
            inMemoryUserDetailsManager.createUser(userDetails);

            //     System.out.println(lstUsers.get(r).getColumnData("username") + " " + lstUsers.get(r).getColumnData("password") + "  " + passwordEncoder().encode(lstUsers.get(r).getColumnData("password")));
        }

//        UserDetails user = User.builder()
//                .username("user")
//                .password(passwordEncoder().encode("123")) // encode the password
//                .roles("USER")
//                .build();
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder().encode("123")) // encode the password
//                .roles("USER", "ADMIN")
//                .build();


        return inMemoryUserDetailsManager;
    }

    // Create a new Password Encoder
    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
