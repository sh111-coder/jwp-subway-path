package subway.application;

import static fixtures.LineFixtures.INITIAL_Line2;
import static fixtures.SectionFixtures.*;
import static fixtures.StationFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import subway.domain.line.Line;
import subway.domain.section.Distance;
import subway.domain.section.NearbyStations;
import subway.domain.section.Section;
import subway.domain.station.Station;
import subway.exception.StationNotFoundException;
import subway.repository.LineRepository;
import subway.repository.SectionRepository;
import subway.repository.StationRepository;

@SpringBootTest
@Transactional
@Sql({"/test-schema.sql", "/test-data.sql"})
class StationRemoveServiceTest {

    @Autowired
    private StationRemoveService stationRemoveService;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Nested
    @DisplayName("역 삭제 시 ")
    class deleteStationTest {

        @Test
        @DisplayName("해당하는 역이 없으면 예외가 발생한다.")
        void removeStationTest_fail_when_stationNotExist() {
            // given
            Long stationIdToRemove = -1L;

            // when, then
            assertThatThrownBy(() -> stationRemoveService.removeStationById(stationIdToRemove))
                    .isInstanceOf(StationNotFoundException.class)
                    .hasMessage("역 ID에 해당하는 역이 존재하지 않습니다.");
        }

        @Nested
        @DisplayName("해당 노선에 역이 2개 존재하는 경우")
        class onlyTwoStationExist {

            @Test
            @DisplayName("노선과 2개의 역, 구간 모두 제거된다.")
            void deleteStationsAndLineTest_when_twoStationsExist() {
                // given
                Long stationIdToRemove = INITIAL_STATION_A.ID;

                // when
                stationRemoveService.removeStationById(stationIdToRemove);
                List<Line> allLine = lineRepository.findAllLine();
                List<Station> allStation = stationRepository.findAllStation();
                List<Section> allSectionByLineId = sectionRepository.findAllSectionByLineId(INITIAL_Line2.ID);

                // then
                assertAll(
                        () -> assertThat(allStation).isEmpty(),
                        () -> assertThat(allLine).isEmpty(),
                        () -> assertThat(allSectionByLineId).isEmpty()
                );
            }

            @Nested
            @DisplayName("해당 노선에 역이 2개 이상 존재하는 경우")
            class overTwoStationExist {

                @Test
                @DisplayName("제거할 역이 상행 종점이면 역과 상행 종점 구간을 제거한다. (CASE - 제거 역 : A / 변화 : A-C-E -> C-E)")
                void remove_up_station_and_up_end_section() {
                    // given
                    Long stationIdToRemove = INITIAL_STATION_A.ID;
                    Line line2 = INITIAL_Line2.FIND_LINE;
                    Station savedDownEndStationE = stationRepository.saveStation(STATION_E.createStationToInsert(line2));
                    Station stationC = INITIAL_STATION_C.FIND_STATION;
                    Section savedDownEndSection = sectionRepository.saveSection(SECTION_C_TO_E.createSectionToInsert(
                            stationC, savedDownEndStationE, line2));

                    List<Station> expectedStations = List.of(stationC, savedDownEndStationE);
                    List<Section> expectedSections = List.of(savedDownEndSection);

                    // when
                    stationRemoveService.removeStationById(stationIdToRemove);
                    List<Station> allStation = stationRepository.findAllStation();
                    List<Section> allSectionByLineId = sectionRepository.findAllSectionByLineId(line2.getId());

                    // then

                    assertAll(
                            () -> assertThat(allStation).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).isEqualTo(expectedStations),
                            () -> assertThat(allSectionByLineId).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).isEqualTo(expectedSections)
                    );
                }

                @Test
                @DisplayName("제거할 역이 하행 종점이면 역과 하행 종점 구간을 제거한다. (CASE - 제거 역 : C / 변화 : D-A-C -> D-A)")
                void remove_down_station_and_down_end_section() {
                    // given
                    Long stationIdToRemove = INITIAL_STATION_C.ID;
                    Line line2 = INITIAL_Line2.FIND_LINE;
                    Station savedUpEndStationD = stationRepository.saveStation(STATION_D.createStationToInsert(line2));
                    Station stationA = INITIAL_STATION_A.FIND_STATION;
                    Section savedUpEndSection = sectionRepository.saveSection(SECTION_D_TO_A.createSectionToInsert(
                            savedUpEndStationD, stationA, line2));

                    List<Station> expectedStations = List.of(stationA, savedUpEndStationD);
                    List<Section> expectedSections = List.of(savedUpEndSection);

                    // when
                    stationRemoveService.removeStationById(stationIdToRemove);
                    List<Station> allStation = stationRepository.findAllStation();
                    List<Section> allSectionByLineId = sectionRepository.findAllSectionByLineId(line2.getId());

                    // then
                    assertAll(
                            () -> assertThat(allStation).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).isEqualTo(expectedStations),
                            () -> assertThat(allSectionByLineId).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).isEqualTo(expectedSections)
                    );
                }

                @Test
                @DisplayName("제거할 역이 가운데 역이면 역과 상행 구간, 하행 구간을 제거하고 새로운 구간을 추가한다. (CASE - 제거 역 : A / 변화 : D-A-C -> D-C)")
                void remove_middle_station() {
                    // given
                    Long stationIdToRemove = INITIAL_STATION_A.ID;
                    Line line2 = INITIAL_Line2.FIND_LINE;
                    Station savedUpEndStationD = stationRepository.saveStation(STATION_D.createStationToInsert(line2));
                    Station stationA = INITIAL_STATION_A.FIND_STATION;
                    sectionRepository.saveSection(SECTION_D_TO_A.createSectionToInsert(savedUpEndStationD, stationA, line2));

                    Station stationC = INITIAL_STATION_C.FIND_STATION;

                    // when
                    stationRemoveService.removeStationById(stationIdToRemove);
                    List<Station> expectedStations = List.of(savedUpEndStationD, stationC);
                    List<Section> expectedSections =
                            List.of(
                                    new Section(-1L,
                                            NearbyStations.createByUpStationAndDownStation(savedUpEndStationD, stationC),
                                            line2,
                                            new Distance(INITIAL_SECTION_A_TO_C.DISTANCE.getDistance() + SECTION_D_TO_A.DISTANCE.getDistance())
                                    )
                            );

                    List<Station> allStation = stationRepository.findAllStation();
                    List<Section> allSectionByLineId = sectionRepository.findAllSectionByLineId(line2.getId());

                    // then

                    assertAll(
                            () -> assertThat(allStation).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).ignoringCollectionOrder().isEqualTo(expectedStations),
                            () -> assertThat(allSectionByLineId).usingRecursiveComparison()
                                    .ignoringFieldsOfTypes(Long.class).ignoringCollectionOrder().isEqualTo(expectedSections)
                    );
                }
            }
        }
    }
}
