package com.nhonguoiyeucu.openlinkedhub.service;

import com.nhonguoiyeucu.openlinkedhub.model.Business;
import com.nhonguoiyeucu.openlinkedhub.model.DistrictProfile;
import com.nhonguoiyeucu.openlinkedhub.repository.BusinessRepository;
import com.nhonguoiyeucu.openlinkedhub.repository.DistrictProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DataQueryService {
    private final DistrictProfileRepository districtRepo;
    private final BusinessRepository businessRepo;
    public List<DistrictProfile> getAllDistricts() { return districtRepo.findAll(); }
    public Optional<DistrictProfile> getDistrictById(Long id) { return districtRepo.findById(id); }
    public List<Business> getBusinessesByDistrictId(Long districtId) { return businessRepo.findByDistrictProfile_Id(districtId); }
}
