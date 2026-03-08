package smartFarm;

import java.util.*;

class UserSolution {

    static final int BUCKET_SIZE = 100;

    int N;
    int[] growthTime;

    Crop[][] map;                  // 각 좌표에 심어진 작물
    List<Crop>[][] buckets;        // 버킷별 작물 목록
    int[][] bucketWater;           // 버킷 전체에 누적된 물

    void init(int N, int mGrowthTime[]) {
        this.N = N;

        growthTime = new int[mGrowthTime.length];
        for (int i = 0; i < mGrowthTime.length; i++) {
            growthTime[i] = mGrowthTime[i];
        }

        map = new Crop[N][N];

        // 버킷 개수
        int bucketCount = (N + BUCKET_SIZE - 1) / BUCKET_SIZE;

        buckets = new ArrayList[bucketCount][bucketCount];
        bucketWater = new int[bucketCount][bucketCount];

        for (int i = 0; i < bucketCount; i++) {
            for (int j = 0; j < bucketCount; j++) {
                buckets[i][j] = new ArrayList<>();
            }
        }
    }

    int sow(int mTime, int mRow, int mCol, int mCategory) {
        // 이미 심어진 칸이면 실패
        if (map[mRow][mCol] != null) return 0;

        int br = mRow / BUCKET_SIZE;
        int bc = mCol / BUCKET_SIZE;

        Crop crop = new Crop(mRow, mCol, mCategory, mTime);

        // 이 버킷에 지금까지 누적된 물을 빼서
        // "이 작물이 심어진 이후부터 받은 물"만 계산되도록 보정
        crop.extraWater = -bucketWater[br][bc];

        map[mRow][mCol] = crop;
        buckets[br][bc].add(crop);

        return 1;
    }

    int water(int mTime, int G, int mRow, int mCol, int mHeight, int mWidth) {
        int x1 = mRow;
        int x2 = mRow + mHeight - 1;
        int y1 = mCol;
        int y2 = mCol + mWidth - 1;

        int bx1 = x1 / BUCKET_SIZE;
        int bx2 = x2 / BUCKET_SIZE;
        int by1 = y1 / BUCKET_SIZE;
        int by2 = y2 / BUCKET_SIZE;

        int count = 0;

        for (int br = bx1; br <= bx2; br++) {
            for (int bc = by1; bc <= by2; bc++) {

                int rs = br * BUCKET_SIZE;
                int re = Math.min(N - 1, rs + BUCKET_SIZE - 1);

                int cs = bc * BUCKET_SIZE;
                int ce = Math.min(N - 1, cs + BUCKET_SIZE - 1);

                // 이 버킷이 급수 사각형 안에 완전히 포함되는지 확인
                boolean full =
                        rs >= x1 && re <= x2 &&
                        cs >= y1 && ce <= y2;

                if (full) {
                    // 버킷 전체에 한 번에 물 주기
                    bucketWater[br][bc] += G;
                    count += buckets[br][bc].size();
                } else {
                    // 일부만 겹치면 버킷 안 작물들을 직접 확인
                    for (Crop crop : buckets[br][bc]) {
                        if (crop.r >= x1 && crop.r <= x2 &&
                            crop.c >= y1 && crop.c <= y2) {
                            crop.extraWater += G;
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    int harvest(int mTime, int L, int mRow, int mCol, int mHeight, int mWidth) {
        int x1 = mRow;
        int x2 = mRow + mHeight - 1;
        int y1 = mCol;
        int y2 = mCol + mWidth - 1;

        int bx1 = x1 / BUCKET_SIZE;
        int bx2 = x2 / BUCKET_SIZE;
        int by1 = y1 / BUCKET_SIZE;
        int by2 = y2 / BUCKET_SIZE;

        List<Crop> targets = new ArrayList<>();

        // 1단계: 수확 가능 여부 먼저 전부 검사
        for (int br = bx1; br <= bx2; br++) {
            for (int bc = by1; bc <= by2; bc++) {
                for (Crop crop : buckets[br][bc]) {
                    if (crop.r >= x1 && crop.r <= x2 &&
                        crop.c >= y1 && crop.c <= y2) {

                        if (crop.getHeight(mTime, bucketWater[br][bc]) < L) {
                            return 0;
                        }

                        targets.add(crop);
                    }
                }
            }
        }

        // 2단계: 전부 가능하면 실제 삭제
        for (Crop crop : targets) {
            map[crop.r][crop.c] = null;

            int br = crop.r / BUCKET_SIZE;
            int bc = crop.c / BUCKET_SIZE;

            // posInBucket 없이 일반 remove 사용
            buckets[br][bc].remove(crop);
        }

        return targets.size();
    }

    class Crop {
        int r, c;
        int category;
        int sowTime;
        int extraWater;

        Crop(int r, int c, int category, int sowTime) {
            this.r = r;
            this.c = c;
            this.category = category;
            this.sowTime = sowTime;
            this.extraWater = 0;
        }

        int getHeight(int mTime, int bucketWaterValue) {
            return (mTime - sowTime) / growthTime[category]
                    + bucketWaterValue
                    + extraWater;
        }
    }
}