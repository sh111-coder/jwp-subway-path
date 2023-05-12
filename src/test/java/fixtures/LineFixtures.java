package fixtures;

import subway.domain.line.Line;
import subway.dto.LineFindResponse;

import java.util.List;

public class LineFixtures {

    /**
     * Dummy Data
     */
    public static final String LINE2_NAME = "2호선";
    public static final String LINE7_NAME = "7호선";
    public static final long LINE2_ID = 1L;
    public static final long LINE7_ID = 2L;

    /**
     * Insert Entity
     */
    public static final Line LINE2_INSERT_ENTITY = new Line(null, LINE2_NAME);
    public static final Line LINE7_INSERT_ENTITY = new Line(null, LINE7_NAME);

    /**
     * Find Entity
     */
    public static final Line LINE2_FIND_ENTITY = new Line(LINE2_ID, LINE2_NAME);
    public static final Line LINE7_FIND_ENTITY = new Line(LINE7_ID, LINE7_NAME);

    /**
     * Response
     */
    public static final LineFindResponse LINE2_FIND_RESPONSE = new LineFindResponse(LINE2_NAME, List.of("잠실역", "강변역", "건대역"));
    public static final LineFindResponse LINE7_FIND_RESPONSE = new LineFindResponse(LINE7_NAME, List.of("온수역", "대림역", "논현역", "장암역"));

    public static final List<LineFindResponse> ALL_노선도 = List.of(LINE2_FIND_RESPONSE, LINE7_FIND_RESPONSE);
}
