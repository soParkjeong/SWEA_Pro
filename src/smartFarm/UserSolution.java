package smartFarm;

class UserSolution {
    int N;
    int[] growthTime = new int[3];//품종별 단위시간당 성장량 

    // =========================
    // ✅ "칸 단위"로 필요한 상태 (문제 필수)
    // =========================

    int[][] sowTime;     // sowTime[r][c] : 심은 시간. -1이면 빈칸(작물 없음)
    int[][] cat;         // cat[r][c]     : 품종(0~2). -1이면 빈칸
    int[][] water;       // water[r][c]   : 물로 인해 누적된 성장량 (칸 단위 값)
                         // ※ 시간에 따른 성장과는 독립적으로 단순 +G 누적

    // =========================
    // ✅ √분할(2D) 블록 설계
    // =========================

    int S;               // 블록 한 변의 길이 (대충 √N 근처)
    int BR, BC;          // 블록 개수 (행 방향 블록 수 BR, 열 방향 블록 수 BC)
                         // N=1000, S≈32면 BR≈32, BC≈32 정도

    int[][] lazyWater;   // lazyWater[br][bc] :
                         // "이 블록의 모든 작물에게" 추가로 더해져야 할 water 증가량(+G)을 미뤄서 저장
                         // ✅ 완전 블록 급수에서만 여기에 누적시키고, 실제 water[][]는 안 건드림

    boolean[][] dirty;   // dirty[br][bc] == true :
                         // 이 블록은 lazyWater가 쌓여 있는데 water[][]에 아직 반영(=push) 안 됨
                         // 즉, "블록이 더럽혀졌다(미반영 상태)" 라는 뜻
                         //
                         // dirty==false :
                         // 이 블록은 lazy가 없거나, push가 완료되어 water[][]가 최신 상태

    // ==========================================================
    // init : 매 테스트케이스 초기화
    // ==========================================================
    void init(int N, int mGrowthTime[]) {
        this.N = N;

        // 품종별 성장 시간 저장 (성장 시간 g면 g초마다 크기 +1)
        for (int i = 0; i < 3; i++) growthTime[i] = mGrowthTime[i];

        sowTime = new int[N][N];
        cat = new int[N][N];
        water = new int[N][N];

        // 모든 칸 "빈칸"으로 초기화
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sowTime[r][c] = -1;
                cat[r][c] = -1;
                water[r][c] = 0;
            }
        }

        // ✅ 블록 크기 S 결정 : √N 근처로 잡는 정석
        // - S를 너무 작게 잡으면 블록 수가 너무 많아져 full block 처리 이점이 줄어듦
        // - S를 너무 크게 잡으면 부분 블록 처리(직접 순회)가 느려짐
        S = (int) Math.sqrt(N);
        if (S * S < N) S++;          // ceil(sqrt(N)) 느낌으로 (예: √10=3.xx -> 4)
        if (S < 1) S = 1;

        // 블록 개수 계산
        BR = (N + S - 1) / S;        // 행 방향 블록 수
        BC = (N + S - 1) / S;        // 열 방향 블록 수

        lazyWater = new int[BR][BC];
        dirty = new boolean[BR][BC];
    }

    // ==========================================================
    // helper : br, bc 블록의 실제 row/col 범위 반환 (블록 인덱싱 함수)
    // ==========================================================
    int r0(int br) { return br * S; }
    int r1(int br) { return Math.min(N, br * S + S); } // exclusive (끝은 포함X)
    int c0(int bc) { return bc * S; }
    int c1(int bc) { return Math.min(N, bc * S + S); } // exclusive

    // ==========================================================
    // ✅ push(br, bc)
    //  - dirty 블록이면 lazyWater 값을 water[][]에 실제 반영
    //  - 부분 블록 접근 전에 꼭 필요 (water[][]가 최신이어야 하니까)
    // ==========================================================
    void push(int br, int bc) {
        if (!dirty[br][bc]) return;         // 더럽지 않으면 할 일 없음

        int add = lazyWater[br][bc];        // 블록 전체에 미뤄둔 water 증가량

        if (add != 0) {
            int rs = r0(br), re = r1(br);
            int cs = c0(bc), ce = c1(bc);

            // ✅ 블록의 모든 칸을 돌면서,
            // "작물이 심어진 칸"에만 water를 반영한다.-> 원본 배열에 lazy값 반영 
            // (빈칸에 water 반영하면 나중에 sow할 때 이상해짐)
            for (int r = rs; r < re; r++) {
                for (int c = cs; c < ce; c++) {
                    if (sowTime[r][c] != -1) {//작물이 심어진 칸이면 
                        water[r][c] += add;
                    }
                }
            }
        }

        // ✅ push 완료 -> lazy 제거, dirty 해제
        lazyWater[br][bc] = 0;
        dirty[br][bc] = false;
    }

    // ==========================================================
    // sizeAt(mTime, r, c)
    //  - (r,c)에 있는 작물의 현재 크기 계산
    //  - 시간 성장 + 물 성장
    //
    //  시간 성장: (mTime - sowTime) / growthTime[cat]
    //  물 성장: water[r][c]
    // ==========================================================
    int sizeAt(int mTime, int r, int c) {
        int st = sowTime[r][c];
        if (st == -1) return 0; // 빈칸이면 의미 없음 (호출 자체를 피하는 게 가장 안전)

        int g = growthTime[cat[r][c]];      // 품종별 성장 시간
        int grow = (mTime - st) / g;        // g초마다 +1 이므로 정수 나눗셈
        return grow + water[r][c];
    }

    // ==========================================================
    // sow : 작물 심기
    // ==========================================================
    int sow(int mTime, int mRow, int mCol, int mCategory) {
        // 이미 심어져 있으면 실패
        if (sowTime[mRow][mCol] != -1) return 0;

        int br = mRow / S;
        int bc = mCol / S;

        // ✅ 왜 push가 필요?
        // 이 칸이 속한 블록에 lazyWater가 남아있으면,
        // water[][] 값이 "실제로는 더 커야 하는데" 아직 반영 안 된 상태일 수 있음.
        // 새로 심는 칸은 water=0으로 시작해야 하므로, push로 먼저 정리해두는 게 안전.
        push(br, bc);

        sowTime[mRow][mCol] = mTime;
        cat[mRow][mCol] = mCategory;
        water[mRow][mCol] = 0;      // 문제: 심을 때 초기 크기 0, 물 누적도 0으로 시작
        return 1;
    }

    // ==========================================================
    // water : 직사각형 급수
    //  - 직사각형 안의 모든 작물 크기 +G
    //  - 직사각형 안의 "작물 개수" 반환
    // ==========================================================
    int water(int mTime, int G, int mRow, int mCol, int mHeight, int mWidth) {
        int r1 = mRow + mHeight; // exclusive
        int c1 = mCol + mWidth;  // exclusive

        // ✅ 직사각형이 걸치는 블록 범위 구하기
        // - 시작점의 블록 = mRow/S, mCol/S
        // - 끝점은 exclusive라서 (r1-1)/S로 마지막 포함 블록을 구함
        int br1 = mRow / S, br2 = (r1 - 1) / S;
        int bc1 = mCol / S, bc2 = (c1 - 1) / S;

        int cnt = 0; // 급수 영역 내 작물 수

        for (int br = br1; br <= br2; br++) {
            int rs = r0(br), re = r1(br);

            for (int bc = bc1; bc <= bc2; bc++) {
                int cs = c0(bc), ce = c1(bc);

                // ✅ (직사각형) ∩ (블록) 교집합 구하기
                int ir0 = Math.max(mRow, rs);
                int ir1 = Math.min(r1, re);
                int ic0 = Math.max(mCol, cs);
                int ic1 = Math.min(c1, ce);

                // 교집합이 블록과 완전히 같으면 "완전 블록"
                boolean full = (ir0 == rs && ir1 == re && ic0 == cs && ic1 == ce);

                if (full) {
                    // ✅ 완전 블록 처리:
                    // 블록 전체 칸에 +G 해야 하므로 lazyWater에 쌓고 dirty 표시
                    lazyWater[br][bc] += G;
                    dirty[br][bc] = true;

                    // ⚠️ 여기서의 병목:
                    // water()는 "작물 개수"를 반환해야 함.
                    // 완전 블록이라도 작물 개수를 알기 위해 결국 스캔을 하고 있음.
                    //
                    // ✅ 최적화 정석:
                    // plantedCount[br][bc] 같은 "블록 내 작물 개수"를 따로 유지하면
                    // 여기 cnt += plantedCount[br][bc] 로 O(1) 가능
                    for (int r = rs; r < re; r++) {
                        for (int c = cs; c < ce; c++) {
                            if (sowTime[r][c] != -1) cnt++;
                        }
                    }
                } else {
                    // ✅ 부분 블록 처리:
                    // 교집합 영역만 칸 단위 업데이트 해야 하므로
                    // lazy가 있으면 먼저 push해서 water[][]를 최신화
                    push(br, bc);

                    for (int r = ir0; r < ir1; r++) {
                        for (int c = ic0; c < ic1; c++) {
                            if (sowTime[r][c] != -1) {
                                water[r][c] += G;
                                cnt++;
                            }
                        }
                    }
                }
            }
        }
        return cnt;
    }

    // ==========================================================
    // harvest : 직사각형 수확
    //  - 직사각형 내 모든 작물 size >= L 이면 수확 성공
    //  - 수확 성공 시 그 작물 수 반환, 실패 시 0
    //  - 빈칸은 무시(조건 검사 대상이 아님)
    // ==========================================================
    int harvest(int mTime, int L, int mRow, int mCol, int mHeight, int mWidth) {
        int r1 = mRow + mHeight; // exclusive
        int c1 = mCol + mWidth;  // exclusive

        int br1 = mRow / S, br2 = (r1 - 1) / S;
        int bc1 = mCol / S, bc2 = (c1 - 1) / S;

        // 1) 수확 가능 여부 검사 + 수확 개수 세기
        int cnt = 0;

        for (int br = br1; br <= br2; br++) {
            int rs = r0(br), re = r1(br);

            for (int bc = bc1; bc <= bc2; bc++) {
                int cs = c0(bc), ce = c1(bc);

                int ir0 = Math.max(mRow, rs);
                int ir1i = Math.min(r1, re);
                int ic0 = Math.max(mCol, cs);
                int ic1i = Math.min(c1, ce);

                // ✅ harvest는 size 계산이 필요함
                // size = 시간성장 + 물성장인데,
                // 물성장이 lazy로 남아있으면 water[][]가 최신이 아님.
                // 그래서 여기서는 full/partial 상관없이 push 필요!
                push(br, bc);

                for (int r = ir0; r < ir1i; r++) {
                    for (int c = ic0; c < ic1i; c++) {
                        if (sowTime[r][c] == -1) continue; // 빈칸은 무시
                        int sz = sizeAt(mTime, r, c);
                        if (sz < L) return 0; // 하나라도 부족하면 전체 실패
                        cnt++;
                    }
                }
            }
        }

        // 2) 수확 성공이면 실제 삭제
        for (int br = br1; br <= br2; br++) {
            int rs = r0(br), re = r1(br);

            for (int bc = bc1; bc <= bc2; bc++) {
                int cs = c0(bc), ce = c1(bc);

                int ir0 = Math.max(mRow, rs);
                int ir1i = Math.min(r1, re);
                int ic0 = Math.max(mCol, cs);
                int ic1i = Math.min(c1, ce);

                // 삭제는 칸 단위 업데이트이므로 push 후 진행이 안전
                push(br, bc);

                for (int r = ir0; r < ir1i; r++) {
                    for (int c = ic0; c < ic1i; c++) {
                        if (sowTime[r][c] == -1) continue;
                        sowTime[r][c] = -1;
                        cat[r][c] = -1;
                        water[r][c] = 0;
                    }
                }
            }
        }

        return cnt;
    }
}
