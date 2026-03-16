package Expectation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

class UserSolution {

    /*
     * [문제 해석]
     * 각 학생은 대학 1 ~ M 순서대로 지원한다고 생각하면 됨.
     *
     * add:
     *  - 새 학생이 들어오면 1번 대학부터 넣어본다.
     *  - 정원이 넘치면 그 대학의 꼴찌를 밀어낸다.
     *  - 밀려난 학생은 다음 대학으로 간다.
     *  - 이 과정을 연쇄적으로 반복한다.
     *
     * erase:
     *  - 어떤 학생이 삭제되면, 그 학생이 있던 대학에 공석이 생긴다.
     *  - 그 대학의 pool(밀려났던 후보들)에서 가장 좋은 학생을 데려와 공석을 메운다.
     *  - 만약 그 학생이 다른 대학에 이미 배정되어 있었다면,
     *    그 학생이 빠져나간 대학에 다시 공석이 생긴다.
     *  - 이 공석을 또 메우는 식으로 연쇄적으로 처리한다.
     *
     * suggest:
     *  - 현재 학생이 배정된 대학 번호 반환
     *  - 없으면 -1
     */

    // =========================
    // 전역 변수
    // =========================

    int N;                  // 대학별 정원
    int M;                  // 대학 수
    int[][] weights;        // 대학별 과목 가중치 [대학][5]

    // 학생별 원점수 저장
    HashMap<Integer, int[]> scoreMap;

    // 학생별 대학별 총점 저장
    // totalMap.get(id)[u] = id 학생의 u번 대학 총점
    HashMap<Integer, int[]> totalMap;

    // 삭제된 학생 관리
    HashSet<Integer> erased;

    // 현재 학생이 배정된 대학
    // assignedMap.get(id) = 현재 배정 대학 번호, 없으면 0
    HashMap<Integer, Integer> assignedMap;

    // 대학별 선발군
    // "좋은 학생이 앞, 나쁜 학생이 뒤" 순서
    // 따라서 last()가 꼴찌
    TreeSet<Integer>[] selected;

    // 대학별 후보군
    // 이 대학에서 밀려난 학생들을 저장
    // "좋은 학생이 앞" 순서
    // 따라서 first()가 제일 좋은 후보
    TreeSet<Integer>[] pool;

    // =========================
    // 초기화
    // =========================

    public void init(int N, int M, int[][] mWeights) {
        this.N = N;
        this.M = M;
        this.weights = mWeights;

        scoreMap = new HashMap<>();
        totalMap = new HashMap<>();
        erased = new HashSet<>();
        assignedMap = new HashMap<>();

        selected = new TreeSet[M + 1];
        pool = new TreeSet[M + 1];

        // 대학별 TreeSet 초기화
        for (int u = 1; u <= M; u++) {
            final int univ = u;

            // selected:
            // 총점 내림차순, 총점 같으면 id 오름차순
            // => 가장 좋은 학생이 앞쪽, 가장 나쁜 학생이 뒤쪽
            // => last()가 꼴찌
            selected[u] = new TreeSet<>((a, b) -> {
                int ta = getTotal(a, univ);
                int tb = getTotal(b, univ);

                if (ta != tb) return Integer.compare(tb, ta);
                return Integer.compare(a, b);
            });

            // pool도 동일한 기준으로 관리
            // => first()가 가장 좋은 후보
            pool[u] = new TreeSet<>((a, b) -> {
                int ta = getTotal(a, univ);
                int tb = getTotal(b, univ);

                if (ta != tb) return Integer.compare(tb, ta);
                return Integer.compare(a, b);
            });
        }
    }

    // =========================
    // 기본 조회 / 계산 함수
    // =========================

    // 학생 id의 특정 대학 총점 조회
    int getTotal(int id, int univ) {
        int[] arr = totalMap.get(id);
        if (arr == null) return 0;
        return arr[univ];
    }

    // 학생의 현재 배정 대학 조회
    int getAssigned(int id) {
        return assignedMap.getOrDefault(id, 0);
    }

    // 학생 원점수와 대학 가중치를 이용한 총점 계산
    int calcTotal(int[] scores, int[] weight) {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += scores[i] * weight[i];
        }
        return sum;
    }

    // 대학 univ 기준으로 a가 b보다 더 우수한가?
    boolean isBetter(int univ, int a, int b) {
        int ta = getTotal(a, univ);
        int tb = getTotal(b, univ);

        if (ta != tb) return ta > tb;
        return a < b;
    }

    // =========================
    // 대학 배정 관련 헬퍼
    // =========================

    /*
     * 학생 student를 univ 대학에 넣어본다.
     *
     * 과정:
     * 1) 이미 다른 대학에 배정되어 있었다면 거기서 제거
     * 2) univ 선발군에 넣음
     * 3) 정원 초과면 꼴찌를 밀어냄
     * 4) 밀려난 학생을 반환
     * 5) 정원 초과가 아니면 0 반환
     */
    int tryAccept(int univ, int student) {
        // 이미 삭제된 학생이면 더 진행할 필요 없음
        if (erased.contains(student)) return student;

        // 다른 대학에 배정되어 있었으면 먼저 거기서 제거
        int cur = getAssigned(student);
        if (cur != 0) {
            selected[cur].remove(student);
            assignedMap.put(student, 0);
        }

        // 현재 대학 선발군에 넣기
        selected[univ].add(student);
        assignedMap.put(student, univ);

        // 정원 안 넘치면 배정 완료
        if (selected[univ].size() <= N) {
            return 0;
        }

        // 정원 넘치면 꼴찌를 밀어냄
        int worst = selected[univ].last();
        selected[univ].remove(worst);
        assignedMap.put(worst, 0);

        // 밀려난 학생은 이 대학의 후보군(pool)에 남김
        pool[univ].add(worst);

        return worst;
    }

    /*
     * 새 학생을 1번 대학부터 차례대로 넣어보는 함수
     * 밀려나면 다음 대학으로 연쇄 이동
     */
    void processAddChain(int student) {
        int curStudent = student;

        for (int u = 1; u <= M; u++) {
            curStudent = tryAccept(u, curStudent);

            // 더 이상 밀려난 학생이 없으면 종료
            if (curStudent == 0) return;
        }

        // 끝까지 갔는데도 배정 못 받았으면 미배정
        if (curStudent != 0) {
            assignedMap.put(curStudent, 0);
        }
    }

    // =========================
    // 삭제 후 공석 메우기 관련 헬퍼
    // =========================

    /*
     * empty 대학에 공석이 생겼을 때,
     * pool[empty]에서 실제로 데려올 수 있는 학생을 찾는다.
     *
     * 스킵 조건:
     * 1) 이미 삭제된 학생
     * 2) 이미 더 좋은 대학(번호가 더 작은 대학)에 배정된 학생
     * 3) 이미 empty 대학에 배정된 학생
     */
    Integer findCandidate(int empty) {
        for (int cand : pool[empty]) {

            // 삭제된 학생이면 불가
            if (erased.contains(cand)) continue;

            int assigned = getAssigned(cand);

            // 이미 더 좋은 대학에 배정되어 있으면 못 데려옴
            if (assigned != 0 && assigned < empty) continue;

            // 이미 이 대학에 배정된 학생이면 의미 없음
            if (assigned == empty) continue;

            return cand;
        }

        return null;
    }

    /*
     * empty 대학에 생긴 공석을 연쇄적으로 메운다.
     *
     * 경우 1) 후보가 미배정 학생이면
     *        -> 그 학생을 empty에 넣고 끝
     *
     * 경우 2) 후보가 다른 대학에 배정된 학생이면
     *        -> 그 학생을 원래 대학에서 빼서 empty에 넣음
     *        -> 그 원래 대학이 새로운 공석이 됨
     *        -> 반복
     */
    void fillEmptyChain(int empty) {
        while (empty != 0 && selected[empty].size() < N) {
            Integer cand = findCandidate(empty);

            // 데려올 후보가 없으면 더 이상 못 메움
            if (cand == null) break;

            int oldUniv = getAssigned(cand);

            // 원래 다른 대학에 있었으면 그 대학에서 제거
            if (oldUniv != 0) {
                selected[oldUniv].remove(cand);
                assignedMap.put(cand, 0);
            }

            // 현재 공석 대학에 배정
            selected[empty].add(cand);
            assignedMap.put(cand, empty);

            // 만약 cand가 다른 대학에서 왔다면,
            // 그 대학이 새로운 공석이 된다.
            if (oldUniv != 0) {
                empty = oldUniv;
            } else {
                // 미배정 학생이었다면 연쇄 종료
                break;
            }
        }
    }

    // =========================
    // 1. 학생 추가
    // =========================

    public void add(int mID, int[] mScores) {
        // 학생 기본 정보 저장
        scoreMap.put(mID, mScores);
        erased.remove(mID);
        assignedMap.put(mID, 0);

        // 대학별 총점 미리 계산
        int[] totals = new int[M + 1];
        for (int u = 1; u <= M; u++) {
            totals[u] = calcTotal(mScores, weights[u - 1]);
        }
        totalMap.put(mID, totals);

        // 1번 대학부터 연쇄 배정
        processAddChain(mID);
    }

    // =========================
    // 2. 학생 삭제
    // =========================

    public void erase(int mID) {
        // 삭제 표시
        erased.add(mID);

        // 현재 배정 대학 확인
        int curUniv = getAssigned(mID);

        // 미배정이면 끝
        if (curUniv == 0) return;

        // 현재 대학 선발군에서 제거 -> 공석 발생
        selected[curUniv].remove(mID);
        assignedMap.put(mID, 0);

        // 공석 연쇄 메우기
        fillEmptyChain(curUniv);
    }

    // =========================
    // 3. 질의
    // =========================

    public int suggest(int mID) {
        // 삭제된 학생이면 무조건 -1
        if (erased.contains(mID)) return -1;

        // 현재 배정 대학 반환, 없으면 -1
        int assigned = getAssigned(mID);
        return assigned == 0 ? -1 : assigned;
    }
}