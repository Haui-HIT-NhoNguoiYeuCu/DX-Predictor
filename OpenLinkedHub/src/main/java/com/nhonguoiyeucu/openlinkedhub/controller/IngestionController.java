package com.nhonguoiyeucu.openlinkedhub.controller;

import com.nhonguoiyeucu.openlinkedhub.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(
        name = "Admin · Ingestion",
        description = "Các API quản trị để kích hoạt quá trình ingest dữ liệu"
)
public class IngestionController {

    private final DataIngestionService ingestionService;

    @PostMapping("/ingest-now")
    @Operation(
            summary = "Kích hoạt ingest ngay",
            description = "Chạy pipeline lấy dữ liệu từ World Bank, Wikidata, OpenData địa phương, chuẩn hóa và lưu vào PostgreSQL.",
            security = { @SecurityRequirement(name = "ApiKeyAuth") }
    )
    @ApiResponse(responseCode = "200", description = "Đã bắt đầu ingest")
    @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai API key", content = @Content)
    public ResponseEntity<String> ingestNow() {
        ingestionService.runIngestion();
        return ResponseEntity.ok("Ingestion started");
    }
}
