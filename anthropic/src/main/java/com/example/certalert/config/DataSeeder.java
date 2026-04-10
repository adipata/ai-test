package com.example.certalert.config;

import com.example.certalert.user.Role;
import com.example.certalert.user.User;
import com.example.certalert.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;

/**
 * Seeds demo users so the service is immediately testable. Two groups, two roles each:
 *
 * | username | password | group    | roles           |
 * |----------|----------|----------|-----------------|
 * | alice    | password | security | VIEWER, MANAGER |
 * | bob      | password | security | VIEWER          |
 * | carol    | password | platform | VIEWER, MANAGER |
 * | dave     | password | platform | VIEWER          |
 *
 * Alice and Carol can add certs; Bob and Dave can only list. Alice (security) and Carol
 * (platform) cannot see each other's certificates — that is the group isolation rule.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public ApplicationRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            List<User> seed = List.of(
                    new User("alice", encoder.encode("password"), "security",
                            EnumSet.of(Role.VIEWER, Role.MANAGER)),
                    new User("bob", encoder.encode("password"), "security",
                            EnumSet.of(Role.VIEWER)),
                    new User("carol", encoder.encode("password"), "platform",
                            EnumSet.of(Role.VIEWER, Role.MANAGER)),
                    new User("dave", encoder.encode("password"), "platform",
                            EnumSet.of(Role.VIEWER))
            );
            for (User u : seed) {
                if (users.findByUsername(u.getUsername()).isEmpty()) {
                    users.save(u);
                    log.info("seeded user {} (group={}, roles={})",
                            u.getUsername(), u.getGroup(), u.getRoles());
                }
            }
        };
    }
}
