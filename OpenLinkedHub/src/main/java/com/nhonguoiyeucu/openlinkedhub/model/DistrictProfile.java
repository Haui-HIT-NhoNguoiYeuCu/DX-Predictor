package com.nhonguoiyeucu.openlinkedhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Table(name = "districts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}),
                @UniqueConstraint(columnNames = {"normalized_name"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DistrictProfile {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    @NotBlank
    private String name; // Tên hiển thị (giữ nguyên dấu)


    @Column(name = "normalized_name", nullable = false)
    private String normalizedName; // Tên chuẩn hoá (không dấu, lowercase, bỏ tiền tố), dùng để idempotent merge


    private Long population; // dân số
    private Double areaKm2; // diện tích (km2)
    private Integer businessCount; // số doanh nghiệp
    private Double internetPenetration; // tỷ lệ Internet
    private Double gniPerCapita; // GNI bình quân
}
