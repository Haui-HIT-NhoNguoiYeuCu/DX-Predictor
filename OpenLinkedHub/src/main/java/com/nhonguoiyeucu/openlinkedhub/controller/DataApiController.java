package com.nhonguoiyeucu.openlinkedhub.controller;

import com.nhonguoiyeucu.openlinkedhub.model.Business;
import com.nhonguoiyeucu.openlinkedhub.model.DistrictProfile;
import com.nhonguoiyeucu.openlinkedhub.service.DataQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Tag(
        name = "Public · Data",
        description = "API công khai truy vấn hồ sơ quận/huyện và doanh nghiệp"
)
public class DataApiController {

    private final DataQueryService dataQueryService;

    @GetMapping("/districts")
    @Operation(
            summary = "Danh sách quận/huyện",
            description = "Trả về toàn bộ DistrictProfile đã được ingest và chuẩn hoá.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistrictProfile.class))))
            }
    )
    public List<DistrictProfile> getAllDistricts() {
        return dataQueryService.getAllDistricts();
    }

    @GetMapping("/districts/{id}")
    @Operation(
            summary = "Chi tiết quận/huyện theo ID",
            parameters = {
                    @Parameter(name = "id", description = "ID của DistrictProfile", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = DistrictProfile.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Không tìm thấy",
                            content = @Content)
            }
    )
    public ResponseEntity<DistrictProfile> getDistrictById(@PathVariable Long id) {
        return dataQueryService.getDistrictById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/districts/{id}/businesses")
    @Operation(
            summary = "Danh sách doanh nghiệp theo quận/huyện",
            parameters = {
                    @Parameter(name = "id", description = "ID của DistrictProfile", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Business.class)))),
                    @ApiResponse(responseCode = "404",
                            description = "Không tìm thấy quận/huyện",
                            content = @Content)
            }
    )
    public List<Business> getBusinessesByDistrict(@PathVariable Long id) {
        return dataQueryService.getBusinessesByDistrictId(id);
    }
}
