package stopOver;

import java.util.*;

class UserSolution {

    static class Edge {
        int to;
        int limit;

        Edge(int to, int limit) {
            this.to = to;
            this.limit = limit;//제한 중량
        }
    }

    static class State {
        int city;
        int capacity;

        State(int city, int capacity) {
            this.city = city;
            this.capacity = capacity;//운송 가능한 제한 중량 
        }
    }

    static List<Edge>[] graph;
    static PriorityQueue<State> pq;
    static int[] result;
    static int totalN;

    static Comparator<State> stateComp = (a, b) -> Integer.compare(b.capacity, a.capacity);//큰 것부터 나오도록(제한 중량이 큰 것)

    public void init(int N, int K, int[] sCity, int[] eCity, int[] mLimit) {
        totalN = N;

        graph = new List[N];
        for (int i = 0; i < N; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int i = 0; i < K; i++) {
            graph[sCity[i]].add(new Edge(eCity[i], mLimit[i]));
            graph[eCity[i]].add(new Edge(sCity[i], mLimit[i]));
        }

        pq = new PriorityQueue<>(stateComp);
        result = new int[N];
    }

    public void add(int sCity, int eCity, int mLimit) {
        graph[sCity].add(new Edge(eCity, mLimit));
        graph[eCity].add(new Edge(sCity, mLimit));
    }

    public int calculate(int sCity, int eCity, int M, int[] mStopover) {
        logic(sCity);

        int answer = result[eCity];
        if (answer == 0) return -1;

        for (int i = 0; i < M; i++) {
            int stop = mStopover[i];
            if (result[stop] == 0) return -1;
            answer = Math.min(answer, result[stop]);
        }

        return answer;
    }

    private static void logic(int start) {
        Arrays.fill(result, 0);

        pq.clear();
        pq.offer(new State(start, Integer.MAX_VALUE / 10));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            // PQ에 남아 있는 오래된 상태 제거
            if (cur.capacity != result[cur.city]) continue;

            for (Edge edge : graph[cur.city]) {
                int next = edge.to;
                int nextCapacity = Math.min(cur.capacity, edge.limit);

                if (result[next] < nextCapacity) {
                    result[next] = nextCapacity;
                    pq.offer(new State(next, nextCapacity));
                }
            }
        }
    }
}