package com.nhonguoiyeucu.openlinkedhub.repository;


import com.nhonguoiyeucu.openlinkedhub.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByDistrictProfile_Id(Long districtId);
}
