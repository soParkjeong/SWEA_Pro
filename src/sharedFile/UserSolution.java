package sharedFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {

    private static final int INF = 1_000_000_000;
    private static final int LIMIT = 5000;
    private static final int SPEED = 9;

    int n;

    ArrayList<Edge>[] graph;
    ArrayList<SharedFile>[] sharedFiles;
    HashMap<Integer, Integer> fileSizeMap;
    HashMap<Integer, Request>[] requests;

    static class Edge {
        int to;
        int dist;
        int openTime; // 이 링크가 생긴 시각

        Edge(int to, int dist, int openTime) {
            this.to = to;
            this.dist = dist;
            this.openTime = openTime;
        }
    }

    static class SharedFile {
        int fileId;
        int size;
        int shareTime; // 이 컴퓨터가 이 파일을 공유하기 시작한 시각

        SharedFile(int fileId, int size, int shareTime) {
            this.fileId = fileId;
            this.size = size;
            this.shareTime = shareTime;
        }
    }

    static class Request {
        int fileId;
        int requestTime;
        boolean finished;

        Request(int fileId, int requestTime) {
            this.fileId = fileId;
            this.requestTime = requestTime;
            this.finished = false;
        }
    }

    static class State implements Comparable<State> {
        int node;
        int dist;
        int pathOpenTime; // 현재 경로가 실제 사용 가능해지는 시각

        State(int node, int dist, int pathOpenTime) {
            this.node = node;
            this.dist = dist;
            this.pathOpenTime = pathOpenTime;
        }

        @Override
        public int compareTo(State o) {
            if (this.dist != o.dist) return Integer.compare(this.dist, o.dist);
            return Integer.compare(this.pathOpenTime, o.pathOpenTime);
        }
    }

    void init(int N, int[] mShareFileCnt, int[][] mFileID, int[][] mFileSize) {
        n = N;

        graph = new ArrayList[n + 1];
        sharedFiles = new ArrayList[n + 1];
        requests = new HashMap[n + 1];
        fileSizeMap = new HashMap<>();

        for (int i = 1; i <= n; i++) {
            graph[i] = new ArrayList<>();
            sharedFiles[i] = new ArrayList<>();
            requests[i] = new HashMap<>();
        }

        for (int i = 1; i <= n; i++) {
            int cnt = mShareFileCnt[i - 1];
            for (int j = 0; j < cnt; j++) {
                int fileId = mFileID[i - 1][j];
                int size = mFileSize[i - 1][j];

                sharedFiles[i].add(new SharedFile(fileId, size, 0));
                fileSizeMap.put(fileId, size);
            }
        }
    }

    void makeNet(int K, int[] mComA, int[] mComB, int[] mDis) {
        for (int i = 0; i < K; i++) {
            int a = mComA[i];
            int b = mComB[i];
            int d = mDis[i];

            graph[a].add(new Edge(b, d, 0));
            graph[b].add(new Edge(a, d, 0));
        }
    }

    void addLink(int mTime, int mComA, int mComB, int mDis) {
        graph[mComA].add(new Edge(mComB, mDis, mTime));
        graph[mComB].add(new Edge(mComA, mDis, mTime));
    }

    void addShareFile(int mTime, int mComA, int mFileID, int mSize) {
        sharedFiles[mComA].add(new SharedFile(mFileID, mSize, mTime));
        fileSizeMap.put(mFileID, mSize);
    }

    int downloadFile(int mTime, int mComA, int mFileID) {
        Request old = requests[mComA].get(mFileID);
        if (old == null) {
            requests[mComA].put(mFileID, new Request(mFileID, mTime));
        } else if (old.finished) {
            return 0;
        }

        int[] pathOpenTime = dijkstraPathOpenTime(mComA);
        return countAvailableSources(mTime, mComA, mFileID, pathOpenTime);
    }

    int getFileSize(int mTime, int mComA, int mFileID) {
        // 원래 공유 파일을 갖고 있으면 전체 크기 반환
        if (hasSharedFileAt(mComA, mFileID, mTime)) {
            return fileSizeMap.get(mFileID);
        }

        Request req = requests[mComA].get(mFileID);
        if (req == null) return 0;
        if (req.finished) return fileSizeMap.get(mFileID);

        int[] pathOpenTime = dijkstraPathOpenTime(mComA);
        long downloaded = 0;
        int totalSize = fileSizeMap.get(mFileID);

        for (int comp = 1; comp <= n; comp++) {
            if (comp == mComA) continue;
            if (pathOpenTime[comp] == INF) continue;

            SharedFile src = findSharedFile(comp, mFileID);
            if (src == null) continue;

            int startTime = Math.max(req.requestTime, Math.max(src.shareTime, pathOpenTime[comp]));
            if (mTime > startTime) {
                downloaded += 1L * (mTime - startTime) * SPEED;
            }
        }

        if (downloaded >= totalSize) {
            req.finished = true;
            return totalSize;
        }

        return (int) downloaded;
    }

    // start에서 각 컴퓨터까지
    // "거리 5000 이내인 경로들 중 경로완성시각(pathOpenTime)이 가장 빠른 값" 계산
    int[] dijkstraPathOpenTime(int start) {
        int[] bestOpenTime = new int[n + 1];
        Arrays.fill(bestOpenTime, INF);

        PriorityQueue<State> pq = new PriorityQueue<>();
        pq.offer(new State(start, 0, 0));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            if (cur.dist > LIMIT) continue;
            if (bestOpenTime[cur.node] <= cur.pathOpenTime) continue;

            bestOpenTime[cur.node] = cur.pathOpenTime;

            for (Edge next : graph[cur.node]) {
                int nextDist = cur.dist + next.dist;
                if (nextDist > LIMIT) continue;

                int nextOpenTime = Math.max(cur.pathOpenTime, next.openTime);//가장 늦게 생긴 링크 기준으로 열리므로 max
                pq.offer(new State(next.to, nextDist, nextOpenTime));
            }
        }

        return bestOpenTime;
    }

    int countAvailableSources(int mTime, int requester, int fileId, int[] pathOpenTime) {
        int count = 0;

        for (int comp = 1; comp <= n; comp++) {
            if (comp == requester) continue;
            if (pathOpenTime[comp] == INF) continue;

            SharedFile src = findSharedFile(comp, fileId);
            if (src == null) continue;

            if (Math.max(src.shareTime, pathOpenTime[comp]) <= mTime) {
                count++;
            }
        }

        return count;
    }

    SharedFile findSharedFile(int compId, int fileId) {
        for (SharedFile file : sharedFiles[compId]) {
            if (file.fileId == fileId) return file;
        }
        return null;
    }

    boolean hasSharedFileAt(int compId, int fileId, int now) {
        for (SharedFile file : sharedFiles[compId]) {
            if (file.fileId == fileId && file.shareTime <= now) return true;
        }
        return false;
    }
}