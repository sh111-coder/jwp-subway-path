package subway.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import subway.domain.line.Line;

import java.util.List;
import java.util.Optional;

@Repository
public class LineDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Line> rowMapper =
            (rs, rowNum) ->
                    new Line(
                            rs.getLong("id"),
                            rs.getString("name")
                    );

    public LineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("line").usingGeneratedKeyColumns("id");
    }

    public Line insert(Line line) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", line.getName());

        long insertedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Line(insertedId, line.getName());
    }

    public Optional<Line> findById(Long id) {
        String sql = "select id, name from LINE WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findAny();
    }

    public Optional<Line> findByLineName(String lineName) {
        String sql = "SELECT id, name FROM line WHERE name = ?";

        return jdbcTemplate.query(sql, rowMapper, lineName).stream().findAny();
    }

    public List<Line> findAll() {
        String sql = "SELECT id, name FROM line";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void deleteById(Long id) {
        String sql = "delete from Line where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
