package com.nhonguoiyeucu.openlinkedhub.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "businesses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Business {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;
    private String address;
    private String industry; // ngành nghề


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private DistrictProfile districtProfile;
}
