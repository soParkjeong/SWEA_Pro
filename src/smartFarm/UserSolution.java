package smartFarm;

import java.util.*;
 
class UserSolution {
 
    int N;
    int[] growthTime = new int[3];
 
    int[] sowTime;
    int[] cat;
    int[] extraWater;
 
    // 버킷
    int S;
    int B;
    List<Integer>[][] bucket;
    int[][] bucketWater;
 
    // 버킷 인덱스 
    int[] posInBucket;
    int[] bucketR;
    int[] bucketC;
 
    int id(int r, int c) {
        return r * N + c;
    }
 
    void init(int N, int mGrowthTime[]) {
        this.N = N;
 
        for (int i = 0; i < 3; i++)
            growthTime[i] = mGrowthTime[i];
 
        int NN = N * N;
 
        sowTime = new int[NN];
        cat = new int[NN];
        extraWater = new int[NN];
 
        posInBucket = new int[NN];
        bucketR = new int[NN];
        bucketC = new int[NN];
 
        Arrays.fill(sowTime, -1);
 
        // √분할
        S = 100;
        if (S * S < N) S++;
 
        B = (N + S - 1) / S;
 
        bucket = new ArrayList[B][B];
        bucketWater = new int[B][B];
 
        for (int i = 0; i < B; i++) {
            for (int j = 0; j < B; j++) {
                bucket[i][j] = new ArrayList<>();
            }
        }
    }
 
    int sow(int mTime, int mRow, int mCol, int mCategory) {
 
        int id = id(mRow, mCol);
 
        if (sowTime[id] != -1)
            return 0;
 
        int br = mRow / S;
        int bc = mCol / S;
 
        sowTime[id] = mTime;
        cat[id] = mCategory;
 
        extraWater[id] = -bucketWater[br][bc];
 
        List<Integer> list = bucket[br][bc];
 
        posInBucket[id] = list.size();
        bucketR[id] = br;
        bucketC[id] = bc;
 
        list.add(id);
 
        return 1;
    }
    
 
    int water(int mTime, int G, int mRow, int mCol, int mHeight, int mWidth) {
 
        int x1 = mRow;
        int x2 = mRow + mHeight - 1;
        int y1 = mCol;
        int y2 = mCol + mWidth - 1;
 
        int bx1 = x1 / S;
        int bx2 = x2 / S;
        int by1 = y1 / S;
        int by2 = y2 / S;
 
        int cnt = 0;
 
        for (int br = bx1; br <= bx2; br++) {
            for (int bc = by1; bc <= by2; bc++) {
 
                int rs = br * S;
                int re = Math.min(N - 1, rs + S - 1);
 
                int cs = bc * S;
                int ce = Math.min(N - 1, cs + S - 1);
 
                boolean full =
                        rs >= x1 && re <= x2 &&
                        cs >= y1 && ce <= y2;
 
                if (full) {
 
                    bucketWater[br][bc] += G;
                    cnt += bucket[br][bc].size();
 
                } else {
 
                    for (int id : bucket[br][bc]) {
 
                        int r = id / N;
                        int c = id % N;
 
                        if (r >= x1 && r <= x2 &&
                            c >= y1 && c <= y2) {
 
                            extraWater[id] += G;
                            cnt++;
                        }
                    }
                }
            }
        }
 
        return cnt;
    }
 
    int height(int id, int mTime) {
 
        int br = bucketR[id];
        int bc = bucketC[id];
 
        return (mTime - sowTime[id]) / growthTime[cat[id]]
                + bucketWater[br][bc]
                + extraWater[id];
    }
 
    void removeFromBucket(int id) {
 
        int br = bucketR[id];
        int bc = bucketC[id];
 
        List<Integer> list = bucket[br][bc];
 
        int pos = posInBucket[id];
        int last = list.get(list.size() - 1);
 
        list.set(pos, last);
 
        posInBucket[last] = pos;
 
        list.remove(list.size() - 1);
    }
 
    int harvest(int mTime, int L, int mRow, int mCol, int mHeight, int mWidth) {
 
        int x1 = mRow;
        int x2 = mRow + mHeight - 1;
        int y1 = mCol;
        int y2 = mCol + mWidth - 1;
 
        int bx1 = x1 / S;
        int bx2 = x2 / S;
        int by1 = y1 / S;
        int by2 = y2 / S;
 
        List<Integer> target = new ArrayList<>();
 
        for (int br = bx1; br <= bx2; br++) {
            for (int bc = by1; bc <= by2; bc++) {
 
                for (int id : bucket[br][bc]) {
 
                    int r = id / N;
                    int c = id % N;
 
                    if (r >= x1 && r <= x2 &&
                        c >= y1 && c <= y2) {
 
                        if (height(id, mTime) < L)
                            return 0;
 
                        target.add(id);
                    }
                }
            }
        }
 
        for (int id : target) {
 
            sowTime[id] = -1;
 
            removeFromBucket(id);
        }
 
        return target.size();
    }
}
