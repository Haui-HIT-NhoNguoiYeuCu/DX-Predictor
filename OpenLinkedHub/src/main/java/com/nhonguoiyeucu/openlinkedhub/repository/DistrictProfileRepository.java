package com.nhonguoiyeucu.openlinkedhub.repository;


import com.nhonguoiyeucu.openlinkedhub.model.DistrictProfile;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface DistrictProfileRepository extends JpaRepository<DistrictProfile, Long> {
    Optional<DistrictProfile> findByName(String name);
    Optional<DistrictProfile> findByNormalizedName(String normalizedName);
}
