package subway.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import subway.dao.SectionDao;
import subway.exception.SectionNotFoundException;

@JdbcTest
@Sql({"/test-schema.sql", "/test-data.sql"})
class SectionRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SectionRepository sectionRepository;

    @BeforeEach
    void setUp() {
        this.sectionRepository = new SectionRepository(new SectionDao(jdbcTemplate));
    }

    @Test
    @DisplayName("구간 ID로 구간 삭제 시 구간 ID에 해당하는 구간이 존재하지 않으면 예외가 발생한다.")
    void removeSectionById_throw_not_found_lineId() {
        // given
        Long dummySectionId = -1L;

        // when, then
        assertThatThrownBy(() -> sectionRepository.removeSectionById(dummySectionId))
                .isInstanceOf(SectionNotFoundException.class)
                .hasMessage("구간 ID에 해당하는 구간이 존재하지 않습니다.");
    }

}
