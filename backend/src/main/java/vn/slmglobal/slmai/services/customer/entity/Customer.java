package vn.slmglobal.slmai.services.customer.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Khách hàng (Customer 360). Bảng khach_hang (V3, mục 29). */
@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ten")
    private String name;

    /** Chuẩn hóa dạng 0xxxxxxxxx; null khi khách chưa cho số */
    @Column(name = "sdt")
    private String phone;

    @Column(name = "email")
    private String email;

    /** LEAD, CONSULTING, WON, INSTALLED, AFTER_SALES... (mục 4.1) */
    @Column(name = "giai_doan", nullable = false)
    @Builder.Default
    private String stage = "LEAD";

    @Column(name = "nhan_vien_sales_id")
    private String salesId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "so_thich", nullable = false)
    @Builder.Default
    private JsonNode preferences = JsonNodeFactory.instance.objectNode();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dong_y", nullable = false)
    @Builder.Default
    private JsonNode consent = JsonNodeFactory.instance.objectNode();

    @Column(name = "tu_choi_lien_he_luc")
    private OffsetDateTime optOutAt;

    /** Khác null: bản ghi đã được gộp vào khách này, không dùng nữa */
    @Column(name = "gop_vao_id")
    private UUID mergedIntoId;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
