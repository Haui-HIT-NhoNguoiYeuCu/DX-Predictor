package com.nhonguoiyeucu.openlinkedhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhonguoiyeucu.openlinkedhub.model.Business;
import com.nhonguoiyeucu.openlinkedhub.model.DistrictProfile;
import com.nhonguoiyeucu.openlinkedhub.repository.BusinessRepository;
import com.nhonguoiyeucu.openlinkedhub.repository.DistrictProfileRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;


import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionService {


    private final RestTemplate restTemplate;
    private final DistrictProfileRepository districtRepo;
    private final BusinessRepository businessRepo;
    private final ObjectMapper mapper = new ObjectMapper();


    @Value("${openlinkedhub.api.worldbank.base}")
    private String worldBankBase;


    @Value("${openlinkedhub.api.worldbank.per_page:1000}")
    private int wbPerPage;


    @Value("${openlinkedhub.api.wikidata.sparql}")
    private String wikidataSparqlEndpoint;


    @Value("${openlinkedhub.api.danang.opendata}")
    private String danangOpenDataBase;


    @Value("${openlinkedhub.ingest.cron:}")
    private String ingestCron;

    /** World Bank: GNI per capita & Internet users */
    @Retry(name = "worldbank", fallbackMethod = "fallbackWorldBank")
    @RateLimiter(name = "worldbank")
    public Map<String, Double> fetchWorldBankData() {
// Tránh Map.of để tương thích Java < 9
        Map<String, String> indicators = new LinkedHashMap<>();
        indicators.put("gniPerCapita", "NY.GNP.PCAP.CD");
        indicators.put("internetPenetration", "IT.NET.USER.ZS");


        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, String> entry : indicators.entrySet()) {
            String key = entry.getKey();
            String code = entry.getValue();
            String url = String.format("%s/%s?format=json&per_page=%d", worldBankBase, code, wbPerPage);
            String json = restTemplate.getForObject(url, String.class);
            try {
                JsonNode root = mapper.readTree(json);
                JsonNode data = root.get(1);
                if (data != null && data.isArray()) {
                    for (JsonNode item : data) {
                        if (item.hasNonNull("value")) {
                            result.put(key, item.get("value").asDouble());
                            break;
                        }
                    }
                }
            } catch (Exception parseEx) {
                throw new RuntimeException("WorldBank parse error", parseEx);
            }
        }
        log.info("WorldBank parsed: {}", result);
        return result;
    }

    public Map<String, Double> fallbackWorldBank(Throwable t) {
        log.warn("WorldBank fetch failed after retries: {}", t.toString());
        return new HashMap<>();
    }


    /** Wikidata SPARQL: population & area for districts */
    @Retry(name = "wikidata", fallbackMethod = "fallbackWikidata")
    @RateLimiter(name = "wikidata")
    public Map<String, DistrictProfile> fetchWikidataData() {
        String sparql = String.join("\n",
                "SELECT ?districtLabel ?population ?area WHERE {",
                "  VALUES ?city { wd:Q6757 wd:Q8660 }",
                "  ?district wdt:P31/wdt:P279* wd:Q3032114;",
                "            wdt:P131* ?city.",
                "  OPTIONAL { ?district wdt:P1082 ?population. }",
                "  OPTIONAL { ?district wdt:P2046 ?area. }",
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"vi,en\". }",
                "}"
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/sparql-results+json");
            String encoded = URLEncoder.encode(sparql, StandardCharsets.UTF_8.name());
            URI uri = URI.create(wikidataSparqlEndpoint + "?query=" + encoded);

            ResponseEntity<String> resp =
                    restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode bindings = root.path("results").path("bindings");
            Map<String, DistrictProfile> map = new HashMap<>();
            for (JsonNode b : bindings) {
                String name = b.path("districtLabel").path("value").asText();
                Long pop = b.has("population") ? b.path("population").path("value").asLong() : null;
                Double area = b.has("area") ? b.path("area").path("value").asDouble() : null;
                map.put(name, DistrictProfile.builder().name(name).population(pop).areaKm2(area).build());
            }
            log.info("Wikidata districts: {}", map.size());
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Wikidata fetch error", e);
        }
    }


    public Map<String, DistrictProfile> fallbackWikidata(Throwable t) {
        log.warn("Wikidata fetch failed after retries: {}", t.toString());
        return new HashMap<>();
    }

    /** Local OpenData: districts + businesses (ví dụ) */
    @Retry(name = "localdata", fallbackMethod = "fallbackLocal")
    @RateLimiter(name = "localdata")
    public Map<String, Object> fetchLocalOpendata() {
        try {
            String url = danangOpenDataBase + "/api/example/districts-businesses.json"; // thay bằng endpoint thật
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);
            List<String> districts = new ArrayList<>();
            Map<String, List<Business>> businessByDistrict = new HashMap<>();


            if (root.has("districts")) {
                for (JsonNode d : root.get("districts")) {
                    districts.add(d.path("name").asText());
                }
            }
            if (root.has("businesses")) {
                for (JsonNode b : root.get("businesses")) {
                    String dName = b.path("district").asText();
                    Business biz = Business.builder()
                            .name(b.path("name").asText(null))
                            .address(b.path("address").asText(null))
                            .industry(b.path("industry").asText(null))
                            .build();
                    businessByDistrict.computeIfAbsent(dName, k -> new ArrayList<>()).add(biz);
                }
            }


            Map<String, Object> out = new HashMap<>();
            out.put("districtNames", districts);
            out.put("businessByDistrict", businessByDistrict);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Local OpenData fetch error", e);
        }
    }


    public Map<String, Object> fallbackLocal(Throwable t) {
        log.warn("Local OpenData fetch failed after retries: {}", t.toString());
        Map<String, Object> out = new HashMap<>();
        out.put("districtNames", Collections.emptyList());
        out.put("businessByDistrict", Collections.emptyMap());
        return out;
    }

    /** Hợp nhất dữ liệu từ 3 nguồn & lưu CSDL. */
    @Transactional
    public void runIngestion() {
        log.info("Starting ingestion (cron: {})...", ingestCron);


        Map<String, Double> wb = fetchWorldBankData();
        Map<String, DistrictProfile> wd = fetchWikidataData();
        Map<String, Object> local = fetchLocalOpendata();


        @SuppressWarnings("unchecked")
        List<String> localDistricts = (List<String>) local.getOrDefault("districtNames", Collections.emptyList());
        @SuppressWarnings("unchecked")
        Map<String, List<Business>> bizByDistrict = (Map<String, List<Business>>) local.getOrDefault("businessByDistrict", Collections.emptyMap());


        Set<String> districtNames = new HashSet<>();
        districtNames.addAll(wd.keySet());
        districtNames.addAll(localDistricts);


        List<DistrictProfile> toSaveDistricts = new ArrayList<>();
        List<Business> toSaveBusinesses = new ArrayList<>();


        for (String name : districtNames) {
            DistrictProfile merged = districtRepo.findByName(name).orElseGet(() -> DistrictProfile.builder().name(name).build());


            DistrictProfile fromWikidata = wd.get(name);
            if (fromWikidata != null) {
                if (fromWikidata.getPopulation() != null) merged.setPopulation(fromWikidata.getPopulation());
                if (fromWikidata.getAreaKm2() != null) merged.setAreaKm2(fromWikidata.getAreaKm2());
            }


            if (wb.containsKey("gniPerCapita")) merged.setGniPerCapita(wb.get("gniPerCapita"));
            if (wb.containsKey("internetPenetration")) merged.setInternetPenetration(wb.get("internetPenetration"));


            List<Business> list = bizByDistrict.getOrDefault(name, Collections.emptyList());
            merged.setBusinessCount(list.size());


            toSaveDistricts.add(merged);
        }


        List<DistrictProfile> savedDistricts = districtRepo.saveAll(toSaveDistricts);
        Map<String, DistrictProfile> byName = savedDistricts.stream().collect(Collectors.toMap(DistrictProfile::getName, d -> d));


        for (Map.Entry<String, List<Business>> e : bizByDistrict.entrySet()) {
            DistrictProfile dp = byName.get(e.getKey());
            if (dp == null) continue;
            for (Business b : e.getValue()) {
                b.setDistrictProfile(dp);
                toSaveBusinesses.add(b);
            }
        }


        if (!toSaveBusinesses.isEmpty()) {
            businessRepo.saveAll(toSaveBusinesses);
        }


        log.info("Ingestion finished. Districts: {}, Businesses: {}", savedDistricts.size(), toSaveBusinesses.size());
    }

    @Scheduled(cron = "${openlinkedhub.ingest.cron}")
    public void scheduledIngestion() {
        try { runIngestion(); } catch (Exception ex) { log.error("Scheduled ingestion error", ex); }
    }


}