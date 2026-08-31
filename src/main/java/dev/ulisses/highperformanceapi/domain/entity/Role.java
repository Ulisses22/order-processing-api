package dev.ulisses.highperformanceapi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public @NotBlank String getName() {
        return this.name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }
}
