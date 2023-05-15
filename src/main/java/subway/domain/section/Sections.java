package subway.domain.section;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.*;

import subway.domain.line.Line;
import subway.domain.station.Station;
import subway.exception.SectionNotFoundException;

public class Sections {

    private static final int UP_END_STATION_FIND_SIZE = 1;
    private static final int UP_END_STATION_FIND_INDEX = 0;

    private final List<Section> sections;

    public Sections(List<Section> sections) {
        this.sections = new ArrayList<>(sections);
    }

    public Section findSectionByDownStation(Station station) {
        Long stationId = station.getId();
        return sections.stream()
                .filter(section -> section.getDownStation().getId().equals(stationId))
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException("해당 역을 하행역으로 가지는 구간이 없습니다."));
    }

    public Section findSectionByUpStation(Station station) {
        Long stationId = station.getId();
        return sections.stream()
                .filter(section -> section.getUpStation().getId().equals(stationId))
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException("해당 역을 상행역으로 가지는 구간이 없습니다."));
    }

    public SectionCase determineSectionCaseByStationId(Long stationId) {
        Optional<Section> upSection = sections.stream()
                .filter(section -> section.getDownStation().getId().equals(stationId))
                .findFirst();

        Optional<Section> downSection = sections.stream()
                .filter(section -> section.getUpStation().getId().equals(stationId))
                .findFirst();

        boolean isUpEndStation = upSection.isEmpty() && downSection.isPresent();
        boolean isDownEndStation = upSection.isPresent() && downSection.isEmpty();

        if (isUpEndStation || isDownEndStation) {
            return SectionCase.END_SECTION;
        }
        return SectionCase.MIDDLE_SECTION;
    }

    public Optional<Section> findSectionHasDownStationNameAsDownStationByLine(String downStationName, Line line) {
        return sections.stream()
                .filter(section -> section.isSameDownStationName(downStationName) &&
                        section.isSameLineId(line.getId()))
                .findFirst();
    }

    public Optional<Section> findSectionHasUpStationNameAsUpStationByLine(String upStationName, Line line) {
        return sections.stream()
                .filter(section -> section.isSameUpStationName(upStationName) &&
                        section.isSameLineId(line.getId()))
                .findFirst();
    }

    public boolean hasSectionOnlyOne() {
        return sections.size() == 1;
    }

    public List<String> getSortedStationNames() {
        Map<Station, Station> stationConnections = generateStationConnections();

        List<String> sortedStationNames = new ArrayList<>();
        Station upEndStation = findUpEndStation(stationConnections);
        sortedStationNames.add(upEndStation.getName());
        Station tempUpStation = upEndStation;
        for (int repeatCount = 0; repeatCount < stationConnections.size(); repeatCount++) {
            Station downStation = stationConnections.get(tempUpStation);
            sortedStationNames.add(downStation.getName());
            tempUpStation = downStation;
        }
        return sortedStationNames;
    }

    private Map<Station, Station> generateStationConnections() {
        return sections.stream()
                .collect(toUnmodifiableMap(
                        Section::getUpStation,
                        Section::getDownStation));
    }

    private Station findUpEndStation(Map<Station, Station> stationConnections) {
        List<Station> upStations = new ArrayList<>(stationConnections.keySet());
        List<Station> downStations = new ArrayList<>(stationConnections.values());
        upStations.removeAll(downStations);
        if (upStations.size() != UP_END_STATION_FIND_SIZE) {
            throw new IllegalStateException("상행 종점을 찾을 수 없습니다.");
        }
        return upStations.get(UP_END_STATION_FIND_INDEX);
    }

    public List<Section> getSections() {
        return List.copyOf(sections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sections sections1 = (Sections) o;
        return Objects.equals(sections, sections1.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections);
    }

    @Override
    public String toString() {
        return "Sections{" +
                "sections=" + sections +
                '}';
    }
}
