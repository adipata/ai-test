package com.example.certalert.user;

/**
 * Application roles.
 *
 * VIEWER — can list and view certificates belonging to the user's group.
 * MANAGER — everything VIEWER can do, plus add / delete certificates and edit the global threshold.
 *
 * Stored without the Spring Security "ROLE_" prefix; the prefix is added when building GrantedAuthority.
 */
public enum Role {
    VIEWER,
    MANAGER
}
