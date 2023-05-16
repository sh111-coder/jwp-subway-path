package subway.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import subway.domain.line.Line;
import subway.domain.section.Distance;
import subway.domain.section.NearbyStations;
import subway.domain.section.Section;
import subway.domain.station.Station;

@Repository
public class SectionDao {

    private static final String LINE_AND_DUPLICATE_STATION_JOIN_SQL = "SELECT\n" +
            "section.id,\n" +
            "up_station.id AS up_station_id,\n" +
            "up_station.name AS up_station_name,\n" +
            "down_station.id AS down_station_id,\n" +
            "down_station.name AS down_station_name,\n" +
            "section.line_id AS line_id,\n" +
            "section.distance,\n" +
            "line.name AS line_name\n" +
            "FROM\n" +
            "subway_section section\n" +
            "INNER JOIN station up_station ON up_station.line_id = section.line_id AND up_station.id = section.up_station_id\n" +
            "INNER JOIN station down_station ON down_station.line_id = section.line_id AND down_station.id = section.down_station_id\n" +
            "INNER JOIN line ON line.id = section.line_id\n";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Section> rowMapper =
            (rs, rowNum) -> {
                long sectionId = rs.getLong("id");

                long lineId = rs.getLong("line_id");
                String lineName = rs.getString("line_name");
                Line line = new Line(lineId, lineName);

                long upStationId = rs.getLong("up_station_id");
                String upStationName = rs.getString("up_station_name");
                Station upStation = new Station(upStationId, upStationName, line);

                long downStationId = rs.getLong("down_station_id");
                String downStationName = rs.getString("down_station_name");
                Station downStation = new Station(downStationId, downStationName, line);

                NearbyStations nearbyStations = NearbyStations.createByUpStationAndDownStation(upStation, downStation);

                Distance distance = new Distance(rs.getInt("distance"));
                Section section = new Section(sectionId, nearbyStations, line, distance);
                return section;
            };

    public SectionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("subway_section").usingGeneratedKeyColumns("id");
    }

    public Section insert(Section section) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("up_station_id", section.getUpStationId())
                .addValue("down_station_id", section.getDownStationId())
                .addValue("line_id", section.getLine().getId())
                .addValue("distance", section.getDistance().getDistance());

        long insertedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        System.out.println("call");
        System.out.println("insertedId = " + insertedId);
        return new Section(insertedId, section.getNearbyStations(), section.getLine(), section.getDistance());
    }

    public Optional<Section> selectByUpStationNameAndLineName(String upStationName, String lineName) {
        String sql = LINE_AND_DUPLICATE_STATION_JOIN_SQL + "WHERE up_station.name = ? AND line.name = ?";
        return jdbcTemplate.query(sql, rowMapper, upStationName, lineName).stream().findAny();
    }

    public Optional<Section> selectByDownStationNameAndLineName(String downStationName, String lineName) {
        String sql = LINE_AND_DUPLICATE_STATION_JOIN_SQL + "WHERE down_station.name = ? AND line.name = ?";
        return jdbcTemplate.query(sql, rowMapper, downStationName, lineName).stream().findAny();
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM subway_section WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Boolean isNotExistById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM SUBWAY_SECTION WHERE id = ?)";
        return Boolean.FALSE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }

    public List<Section> selectAllSectionByLineId(Long lineId) {
        String sql = LINE_AND_DUPLICATE_STATION_JOIN_SQL + "WHERE line.id = ?";

        return jdbcTemplate.query(sql, rowMapper, lineId);
    }
}
