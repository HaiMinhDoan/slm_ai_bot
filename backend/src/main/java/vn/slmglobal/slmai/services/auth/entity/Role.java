package vn.slmglobal.slmai.services.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Vai trò. Bảng vai_tro (V2), mã khớp RoleCode.java. */
@Entity
@Table(name = "vai_tro")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    private UUID id;

    @Column(name = "ma", nullable = false, unique = true)
    private String code;

    @Column(name = "ten", nullable = false)
    private String name;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "he_thong", nullable = false)
    private boolean system;
}
