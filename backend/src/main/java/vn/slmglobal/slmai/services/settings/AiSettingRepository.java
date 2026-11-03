package vn.slmglobal.slmai.services.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bảng cai_dat_ai (V10): chỉ chứa giá trị bị ghi đè. */
@Repository
@RequiredArgsConstructor
public class AiSettingRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public Map<String, JsonNode> findAll() {
        Map<String, JsonNode> out = new LinkedHashMap<>();
        jdbc.query("SELECT khoa, gia_tri::text AS gia_tri FROM cai_dat_ai ORDER BY khoa", rs -> {
            try {
                out.put(rs.getString("khoa"), objectMapper.readTree(rs.getString("gia_tri")));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("cai_dat_ai." + rs.getString("khoa") + " không phải JSON", e);
            }
        });
        return out;
    }

    public void upsert(String key, JsonNode value, String updatedBy) {
        String json;
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        jdbc.update("""
                INSERT INTO cai_dat_ai (khoa, gia_tri, sua_boi, sua_luc) VALUES (:k, CAST(:v AS jsonb), :by, now())
                ON CONFLICT (khoa) DO UPDATE SET gia_tri = EXCLUDED.gia_tri, sua_boi = EXCLUDED.sua_boi, sua_luc = now()
                """, new MapSqlParameterSource().addValue("k", key).addValue("v", json).addValue("by", updatedBy));
    }

    public void delete(String key) {
        jdbc.update("DELETE FROM cai_dat_ai WHERE khoa = :k", Map.of("k", key));
    }
}
