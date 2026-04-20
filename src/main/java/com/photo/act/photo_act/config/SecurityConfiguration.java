package com.photo.act.photo_act.config;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.views.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfiguration {


    @Autowired
    private RecordService recordService;

//    public SecurityConfiguration(RecordService recordService) {
//        this.recordService = recordService;
//    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);

        // Optionally customize queries if your table names/columns differ
        userDetailsManager.setUsersByUsernameQuery("SELECT username, password, active FROM dbuser WHERE username = ? ");
        userDetailsManager.setAuthoritiesByUsernameQuery("SELECT username, ur.role FROM dbuser usr, dbuser_rights ur WHERE usr.user_rights_id = ur.id " +
                " AND username = ? AND active = 1 ");

        return userDetailsManager;
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Permit public paths (OG bot controller, static assets, actuator)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/og/**",           // OG Thymeleaf bot pages
                        "/static/**",       // static images, favicon
                        "/uploads/**",      // media files
                        "/actuator/health", // load balancer health check
                        "/og/ping",
                        "/admin/cache/**"
                ).permitAll()
        );

        // VaadinSecurityConfigurer handles:
        //   - CSRF exemption for /UIDL/, /HEARTBEAT/, /PUSH/ (the key fix)
        //   - ViewAccessChecker for @PermitAll / @RolesAllowed on views
        //   - Proper logout handling
        // Configure Vaadin's security using VaadinSecurityConfigurer
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

//
//    @Bean
//    public UserDetailsManager userDetailsManager() {
//
//        String[] arrCols = {"userId", "username", "password", "user_rights_id", "role"};
//        String strSql = " SELECT u.userId , u.username, u.password, u.user_rights_id, r.role " +
//                " FROM dbuser u, dbuser_rights r " +
//                " WHERE 1=1 " +
//                " AND u.user_rights_id = r.id ";
//        List<Record> lstUsers = recordService.findAll(strSql, arrCols);
//
//        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
//
//        for (int r = 0; r < lstUsers.size(); r++) {
//            UserDetails userDetails = User.builder()
//                    .username(lstUsers.get(r).getColumnData("username"))
//                    .password(lstUsers.get(r).getColumnData("password")) // encode the password
//                    .roles(lstUsers.get(r).getColumnData("role"))
//                    .build();
//            inMemoryUserDetailsManager.createUser(userDetails);
//
//            //     System.out.println(lstUsers.get(r).getColumnData("username") + " " + lstUsers.get(r).getColumnData("password") + "  " + passwordEncoder().encode(lstUsers.get(r).getColumnData("password")));
//        }
//
//        return inMemoryUserDetailsManager;
//    }


/* chatgpt
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        // You can customize queries if needed, e.g.:
        // manager.setUsersByUsernameQuery("select username, password, enabled from my_users where username=?");
        // manager.setAuthoritiesByUsernameQuery("select username, authority from my_roles where username=?");
        return manager;
    }*/


    // Create a new Password Encoder
    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
