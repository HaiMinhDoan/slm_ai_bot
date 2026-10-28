package vn.slmglobal.slmai.services.sizing;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Bảng PV out theo năm: nl_bang_pvout + nl_pvout_tinh (V7). */
@Repository
@RequiredArgsConstructor
public class PvoutRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /** Thông tin một bảng (không gồm số liệu) */
    public record TableInfo(int year, String source, String note, String importedBy, OffsetDateTime importedAt,
                            int provinceCount) {
    }

    /** Một bảng đầy đủ: mã tỉnh → 12 tháng (kWh/kWp) */
    public record Table(int year, String source, Map<String, double[]> monthly) {

        public Optional<double[]> of(String provinceCode) {
            return Optional.ofNullable(monthly.get(provinceCode));
        }

        public static double annual(double[] months) {
            double sum = 0;
            for (double m : months) {
                sum += m;
            }
            return sum;
        }
    }

    public List<TableInfo> list() {
        return jdbc.query("""
                SELECT b.nam, b.nguon, b.ghi_chu, b.nap_boi, b.nap_luc,
                       (SELECT count(*) FROM nl_pvout_tinh t WHERE t.nam = b.nam) AS so_tinh
                FROM nl_bang_pvout b ORDER BY b.nam DESC""", (rs, i) -> new TableInfo(rs.getInt("nam"),
                rs.getString("nguon"), rs.getString("ghi_chu"), rs.getString("nap_boi"),
                rs.getObject("nap_luc", OffsetDateTime.class), rs.getInt("so_tinh")));
    }

    public Optional<Integer> latestYearUpTo(int year) {
        List<Integer> years = jdbc.queryForList("SELECT max(nam) FROM nl_bang_pvout WHERE nam <= :y",
                Map.of("y", year), Integer.class);
        Integer y = years.isEmpty() ? null : years.get(0);
        if (y == null) {
            years = jdbc.queryForList("SELECT min(nam) FROM nl_bang_pvout", Map.of(), Integer.class);
            y = years.isEmpty() ? null : years.get(0);
        }
        return Optional.ofNullable(y);
    }

    public boolean exists(int year) {
        Boolean b = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM nl_bang_pvout WHERE nam = :y)",
                Map.of("y", year), Boolean.class);
        return Boolean.TRUE.equals(b);
    }

    public Optional<Table> load(int year) {
        List<String> sources = jdbc.queryForList("SELECT nguon FROM nl_bang_pvout WHERE nam = :y",
                Map.of("y", year), String.class);
        if (sources.isEmpty()) {
            return Optional.empty();
        }
        Map<String, double[]> monthly = new LinkedHashMap<>();
        jdbc.query("SELECT ma_tinh, theo_thang FROM nl_pvout_tinh WHERE nam = :y ORDER BY ma_tinh",
                Map.of("y", year), rs -> {
                    monthly.put(rs.getString("ma_tinh"), toDoubles(rs.getArray("theo_thang")));
                });
        return Optional.of(new Table(year, sources.get(0), Map.copyOf(monthly)));
    }

    /** Ghi (thay) bảng của một năm. Gọi trong transaction. */
    public void replace(int year, String source, String note, String importedBy, Map<String, double[]> monthly) {
        jdbc.update("DELETE FROM nl_bang_pvout WHERE nam = :y", Map.of("y", year));
        jdbc.update("INSERT INTO nl_bang_pvout (nam, nguon, ghi_chu, nap_boi) VALUES (:y, :nguon, :ghiChu, :napBoi)",
                new MapSqlParameterSource().addValue("y", year).addValue("nguon", source)
                        .addValue("ghiChu", note).addValue("napBoi", importedBy));
        MapSqlParameterSource[] rows = monthly.entrySet().stream()
                .map(e -> new MapSqlParameterSource().addValue("y", year).addValue("ma", e.getKey())
                        .addValue("thang", toLiteral(e.getValue())))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate("INSERT INTO nl_pvout_tinh (nam, ma_tinh, theo_thang) "
                + "VALUES (:y, :ma, CAST(:thang AS numeric[]))", rows);
    }

    private static double[] toDoubles(Array array) throws SQLException {
        Object[] values = (Object[]) array.getArray();
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = ((BigDecimal) values[i]).doubleValue();
        }
        return out;
    }

    private static String toLiteral(double[] values) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(BigDecimal.valueOf(values[i]).stripTrailingZeros().toPlainString());
        }
        return sb.append('}').toString();
    }
}
