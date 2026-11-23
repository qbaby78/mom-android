# Mom (Taking Care Of My Child) — Phase 1 아키텍처 및 상세 설계

| 항목 | 내용 |
| --- | --- |
| 문서 상태 | 초안 (구현 전 설계안) |
| 범위 | Phase 1 데모 및 핵심 아키텍처 검증 |
| 앱 식별자 | `com.example.mom` (단일 APK) |
| 기준 문서 | `docs/requirements.txt`, 확정된 Phase 1 설계 기준 |

Phase 1은 부모 앱과 아이 앱을 별도 APK로 나누지 않는다. 하나의 Android 프로세스 안에서 **부모 모드 / 아이 모드**를 전환해 지오펜스 설정, 주기적 위치 검사, 위반 기록, 부모 화면의 실시간 이력 반영까지 검증한다.

Phase 2에서 앱을 분리하고 실제 서버·FCM으로 바꿀 때 Domain, UseCase, ViewModel, JUnit5가 바뀌지 않도록 **데이터 경계만** 교체 가능하게 둔다. 그 외 계층(캐시 전략 프레임워크, 이벤트 버스, 멀티 모듈 Gradle 분리)은 Phase 1에서 만들지 않는다.

---

## 1. 설계 원칙

1. **단일 앱, 역할 분리.** UI·서비스·유스케이스는 `feature/parent`와 `feature/child`로 나눈다. 둘은 서로의 Composable·ViewModel·Service를 참조하지 않고 `domain`만 공유한다.
2. **의존성 방향.** `feature` → `domain` ← `data`. `feature`는 Room, Retrofit, DataStore 구현체를 직접 알지 못한다. `domain`은 Android 프레임워크 타입(Context, Location, Compose)을 쓰지 않는다.
3. **Repository는 DataSource만 본다.** 각 Repository는 `LocalDataSource`와 `RemoteDataSource` 인터페이스 뒤에 있다. Phase 1의 Remote 구현체는 네트워크가 아니라 **서버 역할을 하는 Room DB**다.
4. **서버 DB가 진실의 원천.** 아이·관심·위반 이력의 쓰기와 부모 화면 구독은 서버 모사 DB를 기준으로 한다. 일반 Local DB는 모드 설정과 마지막 위치 스냅샷처럼 기기 안에만 있으면 되는 값만 담는다.
5. **위반 알림은 진입 시 1회.** 반경 밖으로 처음 나갔을 때 `ViolationLog` 1건을 남긴다. 같은 이탈이 이어지는 동안에는 추가 기록하지 않는다. 반경 안으로 돌아오면 해당 관심의 활성 위반을 해제하고, 다시 나가면 새 이력 1건을 남긴다.
6. **MVI.** 화면마다 `UiState`(StateFlow), `UiIntent`(단일 `onIntent`), `SideEffect`(SharedFlow)를 둔다. ViewModel은 Context와 Composable을 참조하지 않는다.
7. **Phase 2 교체 지점.** `RemoteDataSource` 구현체와 푸시 전달 담당만 바꾼다. 판정 수식, UseCase 시그니처, 화면 상태는 유지한다.

---

## 2. 패키지 구조

루트 패키지: `com.example.mom`. Phase 1은 `:app` 단일 모듈이다. 패키지 경계가 곧 Phase 2 모듈 경계다.

```text
com.example.mom
├── MomApplication
├── MainActivity
├── core
│   ├── di                  # Hilt 모듈 (Database, DataSource, Repository, Location)
│   ├── navigation          # 타입 세이프 라우트, 모드 전환 진입점
│   ├── location            # LocationProvider 인터페이스 + Fused Location 구현
│   └── util                # 순수 함수 (Haversine). Android API 비의존
├── domain
│   ├── model               # Child, Interest, ViolationLog, GeoPoint, AppMode
│   ├── repository          # ChildRepository, InterestRepository, ViolationRepository, ModeRepository
│   └── usecase
│       ├── parent          # 아이/관심 CRUD, 이력 구독
│       └── child           # 위치 1회 검사, 주기 계산
├── data
│   ├── local               # 기기 로컬 DB + DataStore
│   │   ├── db
│   │   ├── dao
│   │   ├── entity
│   │   └── LocalDataSourceImpl
│   ├── remote
│   │   ├── RemoteDataSource          # 인터페이스 (data 계층에 두고 domain repository가 의존)
│   │   ├── localserver               # Phase 1: LocalServerDbRemoteDataSource
│   │   │   ├── db
│   │   │   ├── dao
│   │   │   └── entity
│   │   └── network                   # Phase 2 자리. Phase 1에서는 빈 패키지 또는 주석만
│   ├── mapper
│   └── repository                    # Repository 구현
└── feature
    ├── mode                          # 앱 시작 시 부모/아이 모드 선택 (Phase 1 테스트 셸)
    ├── parent
    │   ├── children                  # 아이 관리
    │   ├── childform                 # 아이 등록/수정 + 관심 리스트
    │   ├── interestform              # 관심 등록/수정
    │   ├── region                    # 지도 핀 + 반경
    │   ├── alerts                    # 알람 이력
    │   └── navigation                # ModalNavigationDrawer
    └── child
        ├── service                   # ChildLocationForegroundService
        ├── monitor                   # 코루틴 루프, 수동 1회 트리거
        └── debug                     # 아이 모드 최소 디버그 화면 (설정 UI 아님)
```

### 2.1 계층 책임

| 계층 | 하는 일 | 하지 않는 일 |
| --- | --- | --- |
| `core` | DI, 내비게이션 라우트, 위치 수집 추상화, 거리 계산 | 비즈니스 규칙, 화면 상태 |
| `domain` | 모델, Repository 계약, UseCase | Room, Retrofit, Compose, Service |
| `data` | DataSource 구현, 매핑, Repository 구현 | 화면 분기, 지오펜스 정책 해석 |
| `feature/parent` | 부모 화면 MVI | 위치 서비스, DB 직접 접근 |
| `feature/child` | Foreground Service, 수집 루프, 수동 트리거 | 부모 화면, 관심 CRUD UI |

`domain.repository`는 인터페이스만 둔다. `data`의 DataSource 인터페이스를 domain이 알게 하면 계층이 뒤집히므로, **Repository 인터페이스는 domain**, **DataSource 인터페이스는 data**, **UseCase는 Repository만 주입**받는다. Phase 2에서 Remote 구현체를 Retrofit으로 바꿔도 UseCase 생성자는 그대로다.

`core.location.LocationProvider`는 `suspend fun currentLocation(): GeoPoint`만 노출한다. 구현체는 Play Services Fused Location이다. 테스트는 Fake를 넣는다. 판정 UseCase는 이 인터페이스에 의존하지 않고, 이미 얻은 `GeoPoint`를 인자로 받는다. 위치 권한·서비스 생명주기는 `feature/child`에 남긴다.

### 2.2 Phase 2로 넘기는 것

- `:app`를 `app-parent`, `app-child`로 나누고 `domain`/`data`/`core`를 라이브러리 모듈로 추출
- `LocalServerDbRemoteDataSource` → `RetrofitRemoteDataSource`
- 위반 기록 이후 부모 알림을 Room Flow 대신 FCM으로 전달
- 아이 모드의 “같은 기기에서 모드 전환” 셸 제거

Phase 1에서 Gradle 멀티 모듈은 만들지 않는다. 패키지 import 금지가 분리 준비다.

---

## 3. 도메인 모델

식별자는 `Long`이다. Room autoGenerate와 맞고, Phase 2 API가 문자열 ID를 쓰면 매퍼에서만 변환한다.

### 3.1 Child

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | Long | 0은 미저장(신규 작성 중) |
| `name` | String | 표시 이름. 공백 불가 |

학년, 특이사항 등 Phase 3 필드는 모델에 넣지 않는다. 추가 시 엔티티 컬럼과 매퍼만 늘린다.

### 3.2 Interest (GeofenceRule)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | Long | |
| `childId` | Long | Child 1 : Interest N |
| `name` | String | 관심 이름 |
| `type` | InterestType | Phase 1은 `GEOFENCE`만 |
| `latitude` | Double | 중심 위도. 미설정은 `null` 허용 (작성 중 초안) |
| `longitude` | Double | 중심 경도 |
| `radiusKm` | Double | 반경. 0보다 커야 저장 가능 |
| `intervalMinutes` | Int | 위치 확인 주기(분). 1 이상 |

저장이 완료된 Interest는 위경도·반경·주기가 모두 유효하다. 화면 초안만 `nullable` 좌표를 쓰고, Repository에 넘기는 모델은 non-null이다.

`type`은 enum으로 두어 관심 등록 화면이 타입별 하위 폼을 분기할 수 있게 한다. Phase 1 분기는 `GEOFENCE` 한 개다.

### 3.3 ViolationLog (AlertHistory)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | Long | |
| `childId` | Long | |
| `childName` | String | 기록 시점의 이름 스냅샷 |
| `interestId` | Long | |
| `interestName` | String | 기록 시점의 이름 스냅샷 |
| `occurredAt` | Long | epoch millis |
| `measuredLatitude` | Double | 판정에 쓴 측정 좌표 |
| `measuredLongitude` | Double | |
| `distanceKm` | Double | 중심까지의 거리 |

이름은 조인 대신 **기록 시점 스냅샷**으로 저장한다. 이후 아이 이름을 바꿔도 과거 알람 문구가 바뀌지 않는다. 삭제된 아이의 이력도 남긴다.

### 3.4 보조 모델

- `GeoPoint(latitude, longitude)` — 측정 위치와 중심 좌표의 공통 값.
- `GeofenceCheckResult(interestId, distanceKm, violated)` — 1개 관심에 대한 판정 결과. 영속하지 않는다.
- `AppMode` — `PARENT`, `CHILD`.
- `ActiveViolation(interestId, since)` — 현재 이탈 중인 관심. 중복 알람 방지용이며 사용자에게 목록으로 보이지 않는다.

### 3.5 관계

```text
Child 1 ──< Interest
Child 1 ──< ViolationLog
Interest 1 ──< ViolationLog
Interest 1 ──0..1 ActiveViolation
```

아이 삭제 시 관심과 해당 아이의 ActiveViolation은 함께 지운다. ViolationLog는 이력으로 남긴다.
개별 관심을 삭제하거나 관심의 위치·반경을 수정해 현재 아이 위치가 다시 반경 안쪽으로 들어온 상태일 때도 기존 ActiveViolation울 지운다.

---

## 4. 데이터베이스 설계

Room 데이터베이스를 **두 개** 둔다. 파일도 분리한다.

| DB | 파일 | 역할 | Phase 2 |
| --- | --- | --- | --- |
| `MomLocalDatabase` | `mom_local.db` | 이 기기에만 있는 상태 | 각 앱이 자기 파일 유지 |
| `MomServerDatabase` | `mom_server.db` | 서버 DB 모사. 도메인 데이터의 원천 | 제거. Remote는 HTTP로 대체 |

한 DB에 테이블만 나누면 “서버 교체”와 “로컬 캐시”가 한 트랜잭션으로 섞인다. 파일을 나누면 Phase 1 테스트는 같은 프로세스에서 둘 다 열고, Phase 2에서는 `MomServerDatabase` 모듈 바인딩만 제거한다.

### 4.1 서버 모사 DB (`MomServerDatabase`)

버전 1. `fallbackToDestructiveMigration`은 개발 중에만 허용하고, 스키마 export는 켠다.

**`server_children`**

| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `id` | INTEGER | PK, autoGenerate |
| `name` | TEXT | NOT NULL |

**`server_interests`**

| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `id` | INTEGER | PK, autoGenerate |
| `childId` | INTEGER | NOT NULL, index, FK → `server_children.id` ON DELETE CASCADE |
| `name` | TEXT | NOT NULL |
| `type` | TEXT | NOT NULL, Phase 1 값 `GEOFENCE` |
| `latitude` | REAL | NOT NULL |
| `longitude` | REAL | NOT NULL |
| `radiusKm` | REAL | NOT NULL |
| `intervalMinutes` | INTEGER | NOT NULL |

**`server_violation_logs`**

| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `id` | INTEGER | PK, autoGenerate |
| `childId` | INTEGER | NOT NULL, index. FK 없음 (아이 삭제 후에도 유지) |
| `childName` | TEXT | NOT NULL |
| `interestId` | INTEGER | NOT NULL, index |
| `interestName` | TEXT | NOT NULL |
| `occurredAt` | INTEGER | NOT NULL |
| `measuredLatitude` | REAL | NOT NULL |
| `measuredLongitude` | REAL | NOT NULL |
| `distanceKm` | REAL | NOT NULL |

**`server_active_violations`**

| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `interestId` | INTEGER | PK |
| `childId` | INTEGER | NOT NULL |
| `startedAt` | INTEGER | NOT NULL |

DAO는 이 DB 전용으로 둔다.

- `ServerChildDao` — `observeAll()`, `get(id)`, `insert()`, `update()`, `delete()`
- `ServerInterestDao` — `observeByChild(childId)`, `get(id)`, `listByChild(childId)`, `insert()`, `update()`, `delete()`
- `ServerViolationDao` — `observeAll()` (`occurredAt DESC`), `insert()`
- `ServerActiveViolationDao` — `find(interestId)`, `upsert()`, `delete(interestId)`

`observeAll` / `observeByChild`는 `Flow`다. Room이 테이블 변경을 알리면 부모 화면이 다시 읽는다. 아이 모드 서비스와 부모 UI가 같은 프로세스·같은 DB를 보므로 Phase 1의 “실시간 이력”은 이 Flow로 충족된다. 별도의 SharedFlow 버스는 두지 않는다.

### 4.2 로컬 DB (`MomLocalDatabase`)

Phase 1에서 도메인 엔티티를 복제 캐시하지 않는다. 서버 모사 DB가 이미 로컬 파일이라 캐시를 두면 무효화만 늘어난다. Phase 2에서 HTTP가 생기면 그때 `local_children`, `local_interests`, `local_violation_logs`를 **읽기 캐시**로 추가하고, Repository가 Remote 성공 후 Local에 upsert하는 현재 인터페이스를 그대로 쓴다.

Phase 1 로컬 DB 테이블은 하나다.

**`local_last_locations`**

| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `childId` | INTEGER | PK |
| `latitude` | REAL | NOT NULL |
| `longitude` | REAL | NOT NULL |
| `measuredAt` | INTEGER | NOT NULL |

아이 모드가 좌표를 읽을 때마다 갱신한다. 부모 UI는 Phase 1에서 이 테이블을 보여 주지 않는다. 디버그 화면과 테스트 확인용이다. 위반 판정의 입력은 이번 측정값이며, 이 테이블의 이전 값이 아니다.

모드(부모/아이)는 DB에 넣지 않고 **DataStore Preferences** (`app_mode` 문자열)에 둔다. 앱 재시작 시 마지막 모드로 복귀한다.

### 4.3 DataSource 메서드

**`ChildRemoteDataSource`**

- `observeChildren(): Flow<List<Child>>`
- `getChild(id): Child?`
- `upsertChild(child): Child` — `id == 0`이면 insert 후 발급 id 반환
- `deleteChild(id)`

**`InterestRemoteDataSource`**

- `observeInterests(childId): Flow<List<Interest>>`
- `getInterest(id): Interest?`
- `upsertInterest(interest): Interest`
- `deleteInterest(id)`

**`ViolationRemoteDataSource`**

- `observeViolations(): Flow<List<ViolationLog>>`
- `findActive(interestId): ActiveViolation?`
- `recordViolation(log): ViolationLog` — 로그 insert와 active upsert를 한 트랜잭션
- `clearActive(interestId)`

**`LocationLocalDataSource`**

- `saveLastLocation(childId, point, measuredAt)`
- `getLastLocation(childId): LastLocation?`

**`ModeLocalDataSource`**

- `observeMode(): Flow<AppMode>`
- `setMode(mode)`

Phase 1 바인딩:

```text
ChildRemoteDataSource      → LocalServerDbRemoteDataSource (child 부분)
InterestRemoteDataSource   → LocalServerDbRemoteDataSource (interest 부분)
ViolationRemoteDataSource  → LocalServerDbRemoteDataSource (violation 부분)
LocationLocalDataSource    → Room LocalDataSourceImpl
ModeLocalDataSource        → DataStore
```

구현 클래스는 하나(`LocalServerDbRemoteDataSource`)가 세 Remote 인터페이스를 구현해도 된다. Hilt `@Binds`는 인터페이스마다 둔다. Phase 2에서는 같은 인터페이스를 `RetrofitRemoteDataSource`에 다시 묶는다.

### 4.4 Repository

| Repository | 읽기 | 쓰기 |
| --- | --- | --- |
| `ChildRepository` | Remote Flow | Remote upsert/delete. Local 캐시 훅은 no-op 구현 |
| `InterestRepository` | Remote Flow | Remote upsert/delete |
| `ViolationRepository` | Remote Flow | `recordIfEntered`, `markInside` |
| `ModeRepository` | Local DataStore | Local DataStore |
| `LastLocationRepository` | Local | Local |

`ViolationRepository.recordIfEntered`는 아래만 수행한다.

1. `findActive(interestId)`가 이미 있으면 아무 것도 쓰지 않는다.
2. 없으면 `recordViolation`으로 로그와 active를 같이 쓴다.

`markInside`는 active가 있을 때만 `clearActive`를 호출한다.

거리 비교는 Repository에 넣지 않는다. UseCase가 판정한 뒤에 Repository의 기록 API를 호출한다.

캐시 훅을 Phase 1에 빈 구현으로 넣는 이유는, Phase 2에서 `ChildRepository` 구현부만 “Remote 성공 → Local upsert, 구독은 Local Flow”로 바꿀 때 UseCase 서명을 유지하기 위해서다. 인터페이스에 `cacheChildren()` 같은 메서드를 미리 늘리지는 않는다. 그 분기는 Repository 구현 안에만 있다.

---

## 5. UseCase

| UseCase | 입력 | 출력 | 의존 |
| --- | --- | --- | --- |
| `ObserveChildren` | 없음 | `Flow<List<Child>>` | ChildRepository |
| `SaveChild` | id?, name | `Child` | ChildRepository. 이름 trim, 빈 문자열이면 `InvalidChildName` |
| `DeleteChild` | childId | Unit | ChildRepository |
| `ObserveInterests` | childId | `Flow<List<Interest>>` | InterestRepository |
| `SaveInterest` | Interest | `Interest` | InterestRepository. 반경 > 0, 주기 ≥ 1, 좌표 범위 검증 |
| `DeleteInterest` | interestId | Unit | InterestRepository. 삭제 시 active도 제거 |
| `ObserveViolations` | 없음 | `Flow<List<ViolationLog>>` | ViolationRepository |
| `EvaluateGeofence` | center, radiusKm, measured | `GeofenceCheckResult` | 없음 (순수). `GeofenceMath.distanceKm` 사용 |
| `CheckChildLocation` | childId, measured, nowMillis | `List<GeofenceCheckResult>` | InterestRepository, ViolationRepository, LastLocationRepository |
| `ResolvePollInterval` | interests | `Duration` | 없음. 관심 목록의 `intervalMinutes` 최솟값. 비어 있으면 폴링하지 않음 |
| `ObserveAppMode` / `SetAppMode` | | | ModeRepository |

### 5.1 거리 판정

`GeofenceMath.distanceKm`는 Haversine이다. 지구 반경 6371 km. 결과는 km.

위반 조건: `distanceKm > radiusKm`. 경계 위(`==`)는 위반이 아니다.

Android `Location.distanceBetween`은 구현 검증용 참고일 뿐, domain 단위 테스트의 기준 구현은 Haversine으로 고정한다. 두 구현을 섞으면 테스트가 기기 API에 묶인다.

### 5.2 `CheckChildLocation` 순서

한 번의 위치 샘플에 대해 해당 아이의 **모든** 관심을 검사한다. 주기가 다른 관심이 있어도 샘플 1회에 전부 판정한다. 주기는 “얼마나 자주 위치를 읽을지”이지 “관심마다 별도 타이머”가 아니다. 아이 모드 루프의 sleep은 `ResolvePollInterval` 결과(분 → Duration)다.

각 관심에 대해:

1. `EvaluateGeofence`
2. 마지막 위치를 `LastLocationRepository`에 저장 (루프당 1회, 관심 반복 밖)
3. `violated == true` 이면 `recordIfEntered` (active가 있으면 로그를 추가하지 않음)
4. `violated == false` 이면 `markInside`

관심 저장 검증 실패는 `Result` 또는 sealed `SaveInterestError`(`BlankName`, `RadiusNotPositive`, `IntervalTooSmall`, `CoordinateMissing`)로 돌려준다. 예외로 흐름을 끊지 않는다.

### 5.3 수동 1회 트리거

`CheckChildLocation`은 주기 루프와 수동 버튼이 **같은 UseCase**를 호출한다. 수동 경로는 sleep을 하지 않고, 현재 위치를 한 번 읽어 즉시 검사한다. 직후 주기 루프의 다음 주기는 수동 실행 시각부터 다시 계산한다.

Phase 1에는 아이 계정이 없다. 아이 모드는 DataStore에 저장된 `monitoredChildId: Long?` 한 명을 감시한다. 부모 모드의 아이 행에 있는 “이 기기의 아이로 감시” 액션이 이 값을 쓴다. 여러 아이를 한 기기에서 동시에 감시하는 기능은 Phase 1 범위 밖이다.

---

## 6. 아이 모드 시퀀스

아이 모드는 설정 화면을 갖지 않는다. 모드 진입 시 서비스가 기동하고, 디버그 화면은 상태 표시와 수동 트리거만 제공한다.

### 6.1 모드 진입

```text
사용자: 모드 셸에서 아이 모드 선택
  → SetAppMode(CHILD)
  → monitoredChildId 확인
       없음: 디버그 화면에 "감시할 아이가 선택되지 않음"만 표시. 서비스 미기동
       있음: 위치 권한 확인
            거부: 디버그 화면에 권한 요청 버튼
            허용: ChildLocationForegroundService.start()
```

알림 채널 `child_location`에 ongoing 알림을 띄운다. 알림 본문은 “위치 확인 중”이다. 위반 푸시 알림은 Phase 1에서 만들지 않는다. 위반은 DB 이력으로만 남고, 부모 화면 Flow가 갱신된다. 같은 기기에서 부모 모드로 바꾸면 그 목록을 본다.

### 6.2 Foreground Service + 코루틴 루프

서비스는 `LifecycleService`가 아니다. `Service` + `CoroutineScope(SupervisorJob + Dispatchers.Default)` 이다. 위치 API 호출만 `Dispatchers.IO`에 가깝게 두고, 판정·DB는 주입된 UseCase가 자신의 디스패처를 쓴다. `GlobalScope`는 쓰지 않는다. `onDestroy`에서 Job을 cancel한다.

```text
onStartCommand(START_STICKY):
  startForeground(알림)
  loop:
    childId = ModeRepository.monitoredChildId or break
    interests = InterestRepository.list(childId)   // 1회 조회. Flow 수집은 서비스에 상시 두지 않음
    if interests is empty:
        delay(1분) 후 continue          # 고정 대기. 관심이 생기면 다음 회차에 반영
    point = LocationProvider.currentLocation()
    if point == null:
        알림 문구만 "위치를 가져오지 못함"으로 갱신, delay(ResolvePollInterval) 후 continue
    CheckChildLocation(childId, point, now)
    delay(ResolvePollInterval(interests))
```

관심 목록은 매 회차 다시 읽는다. 부모가 주기를 바꾸면 다음 루프부터 반영된다. 진행 중인 delay를 중단하는 핫 리로드는 Phase 1에서 하지 않는다.

### 6.3 수동 1회 체크

디버그 화면 버튼 → `ChildMonitorController.checkOnce()`.

이 컨트롤러는 서비스와 같은 UseCase를 쓰는 싱글톤이다. 서비스가 떠 있으면 루프와 직렬화하기 위해 `Mutex` 하나로 `checkOnce`와 루프 body를 감싼다. 겹치면 수동 요청은 진행 중인 회차가 끝난 뒤 한 번 실행된다.

서비스가 아직 없으면 버튼이 서비스를 기동한 뒤 `ACTION_CHECK_ONCE`를 보낸다. 서비스는 해당 인텐트에서 루프와 별도로, 같은 Mutex 안에서 `CheckChildLocation`을 1회 실행하고 루프 타이머를 리셋한다.

에뮬레이터에서는 확장 컨트롤의 가상 위치로 `LocationProvider`가 값을 받게 한 다음 수동 버튼을 눌러 판정을 확인한다. 테스트 코드가 위치를 주입할 때는 `LocationProvider` Fake가 고정 `GeoPoint`를 반환한다.

### 6.4 위반 1회 기록

```text
측정 위치가 반경 밖
  active 없음 → ViolationLog insert + ActiveViolation upsert     # 알람 1회
  active 있음 → 기록 없음

측정 위치가 반경 안
  active 있음 → ActiveViolation delete
  active 없음 → 기록 없음
```

쿨다운 시간(예: N분에 1회)은 Phase 1 규칙으로 쓰지 않는다. 진입 1회가 요구사항의 “또는 쿨다운” 중 채택안이다. 반경 경계를 반복해서 넘나들면 넘을 때마다 1건이 생기는 것이 맞다. 그건 새 진입이다.

관심의 중심·반경이 수정되어 현재 위치가 상대적으로 밖이 되는 경우도 다음 위치 샘플에서 동일 규칙으로 처리한다. 저장 즉시 판정하지 않는다.

### 6.5 시퀀스 (한 회차)

```text
ChildLocationForegroundService
  → LocationProvider.currentLocation()
  → CheckChildLocation
       → InterestRepository.list(childId)          # Remote(서버 Room)
       → LastLocationRepository.save              # Local Room
       → EvaluateGeofence (관심별)
       → ViolationRepository.recordIfEntered | markInside
            → MomServerDatabase 트랜잭션
  → server_violation_logs 변경
  → Room Flow
  → ObserveViolations
  → AlertsViewModel.uiState
```

부모가 다른 화면을 보고 있어도 `observeViolations()`를 앱 범위에서 수집하지 않는다. 알람 이력 화면이 구독 중일 때만 Flow가 활성화된다 (`WhileSubscribed(5_000)`). 화면을 다시 열면 Room이 최신 목록을 바로 준다.

---

## 7. 내비게이션

Phase 1 최상위는 모드 셸이다.

```text
ModeGate
  ├─ ParentNavHost
  │    ├─ Children          # 시작 화면
  │    ├─ ChildForm         # childId: Long?  (null = 신규)
  │    ├─ InterestForm      # childId: Long, interestId: Long?
  │    ├─ Region            # 편집 중인 초안을 SavedStateHandle로 전달
  │    └─ Alerts
  └─ ChildDebug             # 서비스 상태 + 수동 체크. 설정 폼 없음
```

부모 그래프의 `Children`과 `Alerts`는 **같은 레벨**이다. `ModalNavigationDrawer`는 이 두 화면의 공통 셸(`ParentScaffold`)에만 둔다. `ChildForm`, `InterestForm`, `Region`은 드로어 없이 뒤로 가기로 빠진다. 편집 도중 드로어로 이탈하면 초안이 버려지기 쉽다.

라우트 (kotlinx.serialization, Phase 1 구현 시 `@Serializable`):

| 라우트 | 인자 |
| --- | --- |
| `ChildrenRoute` | 없음 |
| `ChildFormRoute` | `childId: Long = -1` (−1은 신규) |
| `InterestFormRoute` | `childId: Long`, `interestId: Long = -1` |
| `RegionRoute` | `draftKey: String` |
| `AlertsRoute` | 없음 |
| `ChildDebugRoute` | 없음 |

`Region`에서 고른 좌표·반경은 InterestForm의 `SavedStateHandle`에 `RegionDraft(latitude, longitude, radiusKm)`로 기록하고 pop한다. 지도 화면이 Repository에 직접 저장하지 않는다. 저장은 관심 화면의 저장 버튼 한 곳이다.

모드 전환은 드로어 하단의 “아이 모드로 전환”이다. 전환 시 부모 백스택을 비우고 `ChildDebug`로 간다. 아이 디버그 화면의 “부모 모드로 전환”은 서비스를 `stop`한 뒤 `Children`으로 간다. 모드 값은 DataStore에 남으므로 프로세스 재생성 시 `ModeGate`가 마지막 모드로 보낸다.

---

## 8. 부모 모드 MVI

공통 규칙:

- `uiState: StateFlow<S>`, 초기값은 로딩 또는 빈 목록
- `onIntent(intent)` 단일 진입
- `effect: SharedFlow<E>`, extraBufferCapacity 1, 내비게이션·스낵바만
- 수집: `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`
- 저장 성공 시 Effect로 pop. 실패는 `uiState.errorMessage`에 두고 화면은 유지

### 8.1 아이 관리 (`Children`)

**UiState**

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `children` | `List<ChildItem>` | `ChildItem(id, name, interestCount)` |
| `isLoading` | Boolean | 최초 구독 전 |
| `errorMessage` | String? | 삭제 실패 등 |

`interestCount`는 `ObserveChildren`과 아이별 관심 Flow를 `combine`한다. 아이가 적다는 전제(데모)에서 아이 N명의 관심 Flow 결합은 허용한다. N이 커지면 서버 조회에 count 쿼리를 추가한다. Phase 1 DAO에 `countByChild`를 두어도 된다.

**UiIntent**

- `AddChild` — ChildForm 신규로 이동 Effect
- `OpenChild(id)`
- `DeleteChild(id)` — 행의 삭제. 확인은 UI에서 이미 끝난 뒤 전달
- `MonitorOnThisDevice(id)` — `monitoredChildId` 저장. 스낵바 Effect
- `Refresh` — Phase 1에선 Flow라 재구독과 동일. 풀투리프레시 자리만 확보

**SideEffect**

- `NavigateChildForm(childId: Long?)`
- `NavigateAlerts`
- `Message(text)`

화면: 목록, 빈 상태 문구, FAB(아이 추가), 상단 앱바에서 드로어 오픈. 행 클릭은 `OpenChild`. 대표 아이콘은 Phase 1에서 이름 첫 글자 아바타다. 사진 저장은 하지 않는다.

### 8.2 아이 정보 등록/수정 (`ChildForm`)

**UiState**

| 필드 | 타입 |
| --- | --- |
| `childId` | Long? |
| `name` | String |
| `interests` | `List<InterestSummary>` — id, name, type, radiusKm, intervalMinutes |
| `isSaving` | Boolean |
| `nameError` | String? |
| `isLoading` | Boolean |

신규는 `interests`가 빈 목록이고, 아이를 저장하기 전에는 관심 추가 버튼을 비활성화한다. 먼저 이름을 저장해 id를 받은 뒤에만 관심을 붙인다. 저장되지 않은 아이에 관심을 임시로 붙이는 로컬 드래프트 큐는 만들지 않는다.

**UiIntent**

- `NameChanged(value)`
- `Save` — `SaveChild`. 성공 시 신규였으면 state의 childId를 발급값으로 바꾸고 pop하지 않는다(이어서 관심을 추가하는 흐름). 수정 모드의 Save는 pop Effect
- `AddInterest` — childId 필요. `NavigateInterestForm(childId, null)`
- `OpenInterest(id)`
- `DeleteInterest(id)`
- `Back`

**SideEffect**

- `NavigateInterestForm(childId, interestId?)`
- `Pop`
- `Message`

관심 리스트는 `ObserveInterests(childId)`를 구독한다. 다른 경로에서 관심이 바뀌어도 이 화면이 살아 있으면 반영된다.

### 8.3 관심 등록/수정 (`InterestForm`)

**UiState**

| 필드 | 타입 |
| --- | --- |
| `childId` | Long |
| `interestId` | Long? |
| `name` | String |
| `type` | InterestType |
| `latitude` | Double? |
| `longitude` | Double? |
| `radiusKm` | Double? |
| `intervalMinutes` | Int |
| `isSaving` | Boolean |
| `fieldErrors` | `Set<InterestField>` |

`intervalMinutes` 기본값 15. 타입 기본값 `GEOFENCE`.

**타입 분기**

드롭다운은 `InterestType.entries`를 보여 준다. Phase 1 항목은 `GEOFENCE`뿐이다. 선택값에 따라 하위 슬롯이 바뀐다.

```text
when (state.type) {
  GEOFENCE -> GeofenceFields   # 지역 요약 + "지역 설정" 버튼 + 주기(분)
}
```

`GeofenceFields`가 쓰는 상태: 좌표·반경 요약 문구(“위도, 경도 · 반경 n km” 또는 “지역이 아직 없습니다”), 주기 입력. 좌표가 null이면 저장 시 `CoordinateMissing`.

나중에 타입이 늘면 `InterestType`과 이 `when`에 가지를 추가한다. 공통 필드(이름, 타입)는 타입 분기 밖에 둔다.

**UiIntent**

- `NameChanged`, `TypeChanged`, `IntervalChanged`
- `OpenRegion` — 현재 초안을 SavedStateHandle에 두고 Region으로 이동
- `RegionDraftApplied` — 복귀 시 핸들에서 좌표·반경을 읽어 state에 반영. 화면이 이 인텐트를 `LaunchedEffect`로 1회 보낸다
- `Save`
- `Delete` — 수정 모드에서만
- `Back`

**SideEffect**

- `NavigateRegion`
- `Pop`
- `Message`

저장 검증 순서: 이름 → 타입별 필드 → Repository upsert.

### 8.4 지역 설정 (`Region`)

지도는 Phase 1에서 **Google Maps Compose**를 쓴다. API 키가 없으면 개발 빌드가 지도를 띄우지 못하므로, 매니페스트 placeholder와 함께 위도·경도·반경을 숫자로 넣는 폴백 필드를 같은 화면에 둔다. 핀 이동과 숫자 입력은 같은 `UiState`를 갱신한다.

**UiState**

| 필드 | 타입 |
| --- | --- |
| `latitude` | Double |
| `longitude` | Double |
| `radiusKm` | Double |
| `cameraMovedByUser` | Boolean |

초기 좌표는 관심에 이미 값이 있으면 그 값, 없으면 서울시청 부근 `(37.5665, 126.9780)`, 반경 기본 `1.0` km. 기기 현재 위치로 카메라를 옮기는 버튼은 있으면 좋지만 저장 조건은 아니다. 위치 권한이 없어도 숫자를 입력해 저장할 수 있다.

지도 위 원은 `radiusKm`를 미터로 환산한 Circle 오버레이다. 반경 입력은 슬라이더 0.1–50 km와 텍스트 필드를 맞춘다. 잘못된 문자면 마지막 유효 값을 유지하고 `radiusError`를 켠다.

**UiIntent**

- `MapTapped(lat, lng)` 또는 `MarkerDragged`
- `CoordinateTyped(lat, lng)`
- `RadiusChanged(km)`
- `Apply` — 초안을 이전 화면 핸들에 쓰고 Pop
- `Back` — 변경을 버리는 Pop

**SideEffect**

- `Pop`

이 화면은 네트워크와 Repository를 호출하지 않는다.

### 8.5 알람 이력 (`Alerts`)

**UiState**

| 필드 | 타입 |
| --- | --- |
| `items` | `List<AlertItem>` |
| `isLoading` | Boolean |

`AlertItem`: `id`, `occurredAt`, `childName`, `interestName`, `distanceKm`, `radius는 이력에 없음`, `measuredLatitude`, `measuredLongitude`.

표시 문구: `{occurredAt} · {childName} · {interestName} · {distanceKm} km`. 시간 포맷은 UI 레이어(패턴 `yyyy-MM-dd HH:mm`)에서만 한다. State에는 epoch millis를 둔다.

**UiIntent**

- `OpenDrawer`
- 항목 클릭 동작 없음 (Phase 1). 상세 화면을 만들지 않는다

**SideEffect**

- `OpenDrawer`

구독:

```text
ObserveViolations()
  .map { logs -> logs.map(::toAlertItem) }
  .stateIn(...)
```

아이 모드가 `server_violation_logs`에 insert하면 이 화면이 보이는 동안 목록 맨 위(최신순)에 새 행이 나타난다. 별도 새로고침 인텐트는 없다.

빈 목록 문구: “아직 위반 이력이 없습니다.”

### 8.6 ModalNavigationDrawer

`ParentScaffold`가 drawer를 소유한다. 대상 화면은 `Children`, `Alerts`뿐이다.

| 메뉴 | 동작 |
| --- | --- |
| 아이 관리 | `Children`으로 navigate. 현재 라우트와 같으면 드로어만 닫음 |
| 알람 이력 | `Alerts`로 navigate. 단일 탑. 서로 popUpTo 시작 라우트 |
| 아이 모드로 전환 | 서비스 정책은 아이 모드 진입 시퀀스를 따름 |

드로어 상태는 Composable의 `DrawerState`로 둔다. ViewModel 상태로 넣지 않는다. 열기 요청만 Effect 또는 화면 콜백으로 받는다.

---

## 9. DI (Hilt)

| 모듈 | 바인딩 |
| --- | --- |
| `LocalDatabaseModule` | `MomLocalDatabase`, `LastLocationDao` |
| `ServerDatabaseModule` | `MomServerDatabase`, 서버 DAO 4개 |
| `DataSourceModule` | Remote 3종 → `LocalServerDbRemoteDataSource`, Local 2종 |
| `RepositoryModule` | domain Repository → data 구현 |
| `LocationModule` | `LocationProvider` → `FusedLocationProvider` |
| `UseCaseModule` | 생성자 `@Inject`면 모듈 불필요 |

ViewModel은 `@HiltViewModel`. 서비스는 `@AndroidEntryPoint`. 서비스가 UseCase를 주입받고, UseCase가 구현 DB를 모르게 한다.

Phase 2 네트워크 모듈(`Retrofit`, `OkHttp`, `kotlinx.serialization` converter)은 이 문서의 바인딩 표에 자리만 적고, Phase 1 그래프에 넣지 않는다. 의존성을 “미리 컴파일만” 해 두는 것은 구현 단계의 선택이며 설계상 필수 경로는 아니다.

---

## 10. 오류·권한·프로세스

| 상황 | 동작 |
| --- | --- |
| 위치 권한 없음 | 서비스 기동 안 함. 디버그 화면에 요청 |
| 위치 사용 중지(위치 모드 off) | 해당 회차 스킵, 알림 문구 갱신, 다음 주기 재시도 |
| 관심 0건 | 1분 대기 후 재조회 |
| DB 쓰기 실패 | 해당 회차 로그만 남기고 루프는 계속. 부모 저장 실패는 `errorMessage` |
| 아이 모드 중 프로세스 종료 | `START_STICKY`로 서비스 재시작. DataStore의 childId로 루프 재개 |
| 부모 모드로 전환 | `stopService`. 서버 DB 데이터는 유지 |

알림 권한(POST_NOTIFICATIONS)은 API 33+에서 포그라운드 알림에 필요하다. 거부 시 서비스는 시작하지 않고 디버그 화면에 이유를 보여 준다.

백그라운드 위치(ACCESS_BACKGROUND_LOCATION)는 포그라운드 서비스 타입이 `location`이면 서비스가 살아있는 동안 수집할 수 있다. Phase 1 매니페스트에 `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`를 둔다. 정밀 위치가 거부되고 대략 위치만 있으면 대략 위치로 판정한다.

---

## 11. JUnit5 테스트

프레임워크: JUnit Jupiter. 코루틴은 `kotlinx-coroutines-test`. Flow는 Turbine. 대역은 MockK보다 **Fake Repository / Fake DataSource**를 우선한다. 판정 로직은 Fake만으로 상태가 보여야 한다. Android 계측 테스트는 이 목록에 넣지 않는다.

### 11.1 대상

| 대상 | 이유 |
| --- | --- |
| `GeofenceMath` | 순수 함수. 위반 경계의 기준 |
| `EvaluateGeofence` | 반경 초과·경계 일치·내부 |
| `CheckChildLocation` | 진입 1회, 지속 시 무기록, 복귀 후 재진입 |
| `SaveChild` / `SaveInterest` | 입력 검증 |
| `ResolvePollInterval` | 최솟값, 빈 목록 |
| `AlertsViewModel` | Flow 방출이 `uiState.items`에 반영 |
| `InterestFormViewModel` | 타입 분기 상태, 지역 초안 반영, 저장 검증 실패 시 화면 유지 |
| `LocalServerDbRemoteDataSource` | 선택. in-memory Room은 계측이 아니면 Robolectric이 필요하다. Phase 1 필수에서 제외하고 JVM Fake로 Repository 계약을 검증 |

Room 통합은 Phase 1 필수 단위 테스트가 아니다. DAO SQL은 이후 계측 테스트로 확인한다.

### 11.2 시나리오

**거리**

1. 동일 좌표 → 0 km
2. 위도 1도 차이 → 약 111 km (허용 오차 1 km)
3. 경도 차이만 있는 적도 부근 두 점의 대칭
4. `distance == radius` → 위반 아님
5. `distance == radius + ε` → 위반
6. 반경 0 저장은 UseCase가 거부 (판정 함수는 호출되지 않음)

**중복 억제**

1. active 없음 + 밖 → 로그 1건, active 생성
2. 같은 active + 다시 밖 → 로그 건수 변화 없음
3. 안쪽 샘플 → active 삭제, 로그 유지
4. 그 다음 밖 → 로그가 한 건 더 늘어남
5. 관심 A는 밖, 관심 B는 안 → A만 기록
6. 아이 관심 2개의 주기가 5분·30분이면 폴링 간격은 5분
7. 관심 목록이 비면 폴링 간격을 반환하지 않음 (`null`). 서비스의 1분 대기는 UseCase 밖

**저장 검증**

1. 이름 공백 → `InvalidChildName`, Repository 미호출
2. 반경 0 이하, 주기 0, 좌표 없음 → 각각 해당 오류, upsert 미호출
3. 정상 관심 → upsert 1회

**알람 화면**

1. Fake가 빈 리스트를 흘리면 `items`가 빈 목록이고 로딩이 끝남
2. 구독 중 두 번째 리스트에 새 로그가 있으면 `items` 선두가 그 로그다
3. ViewModel clear 후 Turbine이 추가 방출을 받지 않음 (`WhileSubscribed` 해제)

**관심 폼**

1. 초기 type은 `GEOFENCE`, 주기 15
2. `RegionDraftApplied` 후 요약용 좌표·반경이 state에 있다
3. 좌표 없이 `Save` → `fieldErrors`에 좌표, Effect.Pop 없음
4. 유효한 `Save` → Repository에 넘긴 `intervalMinutes`가 입력값과 같다

테스트 이름 형식: `given {조건} when {행동} then {결과}`.

---

## 12. Phase 1에서 만들지 않는 것

- 부모/아이 별도 APK, 로그인, 계정 연동
- Retrofit 호출, 서버 배포, FCM
- 위반 시스템 알림(부모 폰 푸시). 이력 목록 갱신만
- 아이 모드의 관심 편집 UI
- 다중 아이 동시 감시, 관심별 독립 타이머
- 시간 기반 쿨다운
- Phase 3 항목: 학년, 공부(Todo), 스케줄, 원격 촬영
- 지도 없이 동작하는 판정 로직의 분기 (판정은 좌표만 사용. 지도는 입력 UI)

---

## 13. 구현 순서 (참고)

설계 확정 뒤 구현은 아래 순서가 의존 방향과 같다. 이 문서 단계에서는 코드를 쓰지 않는다.

1. 도메인 모델, `GeofenceMath`, `EvaluateGeofence`와 해당 JUnit5
2. 서버 Room·로컬 Room·DataStore, DataSource, Repository
3. `CheckChildLocation`과 중복 억제 테스트
4. 부모 내비게이션과 아이·관심·지역 화면
5. 알람 이력 Flow 화면과 드로어
6. 아이 Foreground Service, 수동 트리거, 모드 셸
7. 에뮬레이터에서 가상 위치 → 수동 체크 → 부모 이력 반영을 한 경로로 확인

---

## 14. 결정 요약

| 주제 | 결정 |
| --- | --- |
| 앱 형태 | 단일 APK, 패키지로 parent/child 분리 |
| 진실 원천 | `mom_server.db` (RemoteDataSource의 Room 구현) |
| 로컬 DB | 마지막 위치만. 도메인 복제 캐시는 Phase 2 |
| 모드 저장 | DataStore |
| 감시 대상 | 기기당 아이 1명 (`monitoredChildId`) |
| 수집 주기 | 해당 아이 관심의 `intervalMinutes` 최솟값 |
| 판정 | Haversine, `distanceKm > radiusKm` |
| 알람 폭주 방지 | 이탈 진입 시 1회. 복귀하면 해제 |
| 부모 실시간 반영 | 서버 Room의 `Flow` 구독 |
| 지역 입력 | 지도 핀·원 프리뷰 + 숫자 폴백. 저장은 관심 화면 |
| 테스트 | JVM JUnit5. 판정·UseCase·ViewModel. Room 계측은 후속 |
