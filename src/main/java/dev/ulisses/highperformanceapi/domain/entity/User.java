package dev.ulisses.highperformanceapi.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public @NotBlank String getUsername() {
        return this.username;
    }

    public @NotBlank String getPassword() {
        return this.password;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setUsername(@NotBlank String username) {
        this.username = username;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    // HELPER
    public boolean isLocked() {
        return lockedUntil != null
                && lockedUntil.isAfter(Instant.now());
    }
}
