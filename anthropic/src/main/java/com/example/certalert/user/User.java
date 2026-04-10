package com.example.certalert.user;

import jakarta.persistence.*;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    /** BCrypt hash. */
    @Column(nullable = false, length = 100)
    private String password;

    /** Group tag — scopes which certificates the user can see. */
    @Column(name = "user_group", nullable = false, length = 64)
    private String group;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    protected User() {}

    public User(String username, String password, String group, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.group = group;
        this.roles = EnumSet.copyOf(roles);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getGroup() { return group; }
    public Set<Role> getRoles() { return roles; }
}
