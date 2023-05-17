# jwp-subway-path

## API

### 1. 노선 역 등록 API

- [x] POST '/stations' uri 맵핑
    - Request
        - 노선 이름
        - 상행 역 이름
        - 하행 역 이름
        - 두 역 사이의 거리
    - Response 
      - 노선 정보
        - ID
        - 이름
      - 등록한 역 정보 리스트
        - ID
        - 이름
      - 등록한 구간 정보 리스트
        - ID
        - 상행역 이름
        - 하행역 이름
        - 거리

### 2. 노선 역 제거 API

- [x] DELETE '/stations/{id}' uri 맵핑
- Request
    - @PathVariable
- Response
    - Void

### 3. 노선 조회 API

- [x] GET '/lines/{id}'
    - Request
        - @Pathvariable 노선 ID
    - Response 
      - 역 정보 
        - String lineName 
        - List<String> stationNames
          - 역 이름은 순서대로 보여준다. (상행종점 -> 하행종점)

### 4. 노선 목록 조회 API

- [x] GET '/lines'
    - Request X
    - Response
        - 노선별 역 정보 (List)
          - String lineName
          - List<String> stationNames
            - 역 이름은 순서대로 보여준다. (상행종점 -> 하행종점)

### 5. 최단 경로 조회 API

- [ ] GET '/stations/short-path?start=&end='
    - Request : Query String
      - start (출발역 이름)
      - end (도착역 이름)
        
    - Response 
      - 경로 정보 리스트
      - 현재역 정보
        - 노선 이름
        - 역 이름
      - 다음역 정보
        - 노선 이름
        - 역 이름
      - 거리
      - 총 거리
      - 요금

---

## 비즈니스 로직

### 구현할 기능

- [x] 노선에 역을 등록한다.
    - 노선이 존재하지 않는 경우 상행역과 하행역, 노선, 구간 정보를 저장한다.
    - 노선이 존재하는 경우
        - 두 역 모두 존재하지 않으면 예외가 발생한다.
        - 두 역 모두 존재하면 예외가 발생한다.
        - 두 역 중 하나만 존재하면 존재하지 않는 역과 구간 정보를 저장한다.

- [x] 노선에서 역을 제거한다.
    - 해당하는 역이 없으면 예외가 발생한다.
    - 노선에 등록된 역이 2개인 경우 하나의 역을 제거할 때 두 역을 모두 제거한다.
    - 해당하는 역이 종점인 경우
        - 해당 역과 구간을 삭제한다.
    - 해당하는 역이 가운데 역인 경우
        - 기존 역의 양 옆에 위치한 역을 연결한다.
            - 거리가 재배정된다.

- [x] 노선 하나의 모든 역을 순서에 맞게 조회한다.
- [x] 모든 노선의 모든 역을 순서에 맞게 조회한다.

- [ ] 출발역과 도착역 사이의 최단 거리 경로를 구한다.
  - 여러 노선의 환승도 고려한다.
  - [ ] 경로 계산 시 요금도 함께 계산한다.
    - 경로의 이동 거리에 따라 요금이 계산된다.
      - 기본 운임 (10km 이내) : 1250원
      - 10km 초과 시 추가 요금이 부과된다.
        - 10~50km : 5km 단위로 100원 추가
        - 50km 초과 : 8km 단위로 100원 추가
---

## DB

![지하철 ERD](https://github.com/woowacourse-precourse/java-menu/assets/96688810/d33cfc2a-1fe9-4eb5-852d-1e586bffef8e)

### 테스트 시 DB Situation
```java
* Default Situation
`test-schema.sql` & `test-data.sql`을 적용하면 '2호선 / A역 & C역 / A-C 구간'이 등록된다.
- 이후 Case들은 TestFixtures를 이용하여 테스트한다.
        
Case 1 (가운데 역 등록) : A-B 등록 요청으로 B역이 등록된다.
    - B역 삽입 
    - A - C 구간 삭제
    - A - B 구간 삽입 
    - B - C 구간 삽입
    - 최종 구간 : A - B - C
        
Case 2 (상행 좀점 역 등록) : D-A 등록 요청으로 D역이 등록된다.
    - D역 삽입
    - D - A 구간 삽입 
    - 최종 구간 : D - A - C

Case 3 (하행 종점 역 등록) : C-E 등록 요청으로 E역이 등록된다.
    - E역 삽입 
    - C - E 구간 삽입 
    - 최종 구간 : A - C - E
```
