package com.photo.act.photo_act.db;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.List;

public class UsersManager implements UserDetailsManager {


    private RecordService recordService;

    public UsersManager(RecordService recordService) {
        this.recordService = recordService;

        String[] arrCols = {"userId", "username", "password", "role"};
        String strSql = "SELECT userId , username, password, role FROM dbuser WHERE 1=1";
        List<Record> rec = recordService.findAll(strSql, arrCols);


        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("123")) // encode the password
                .roles("USER")
                .build();


    }

    // Create a new Password Encoder
    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
