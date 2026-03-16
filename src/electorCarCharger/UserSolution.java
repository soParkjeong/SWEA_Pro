package electorCarCharger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
    int N;

    int[] chargeCost; //도시별 단위 거리당 충전 비용

    ArrayList<Edge>[] graph;
    HashMap<Integer, Edge> edgeMap;

    static class Edge {
        int id;
        int from;
        int to;
        int dist;
        boolean removed;

        Edge(int id, int from, int to, int dist) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.dist = dist;
            this.removed = false;
        }
    }
    
    PriorityQueue<State> pq;

    static class State {
        int city;
        int totalCost;
        int minCost; // 지금까지 지나온 경로에서의 최소 충전 단가값

        State(int city, int totalCost, int minCost) {
            this.city = city;
            this.totalCost = totalCost;
            this.minCost = minCost;
        }
    }

    Comparator<State> stateComparator = (a, b) -> Integer.compare(a.totalCost, b.totalCost);

    public void init(int N, int mCost[], int K, int mId[], int sCity[], int eCity[], int mDistance[]) {
        this.N = N;

        chargeCost = new int[N];
        for (int i = 0; i < N; i++) {
            chargeCost[i] = mCost[i];
        }

        graph = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            graph[i] = new ArrayList<>();
        }

        edgeMap = new HashMap<>();

        for (int i = 0; i < K; i++) {
            Edge edge = new Edge(mId[i], sCity[i], eCity[i], mDistance[i]);
            graph[sCity[i]].add(edge);
            edgeMap.put(mId[i], edge);
        }
    }

    public void add(int mId, int sCity, int eCity, int mDistance) {
        Edge edge = new Edge(mId, sCity, eCity, mDistance);
        graph[sCity].add(edge);
        edgeMap.put(mId, edge);
    }

    public void remove(int mId) {
        edgeMap.get(mId).removed = true;
    }

    public int cost(int sCity, int eCity) {
        // dist[도시][최소충전단가값]
        int[][] dist = new int[N][2001];
        int INF = Integer.MAX_VALUE / 10;

        for (int i = 0; i < N; i++) {
            Arrays.fill(dist[i], INF);
        }

        pq = new PriorityQueue<>(stateComparator);

        //시작점 초기화, PQ에 추가 
        dist[sCity][chargeCost[sCity]] = 0;
        pq.add(new State(sCity, 0, chargeCost[sCity]));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            if (cur.totalCost != dist[cur.city][cur.minCost]) {
                continue;
            }

            if (cur.city == eCity) {
                return cur.totalCost;
            }

            for (Edge next : graph[cur.city]) {
                if (next.removed) {
                    continue;
                }

                int nextCity = next.to;

                int addCost = cur.minCost * next.dist;
                int nextTotalCost = cur.totalCost + addCost;

                int nextMinCost = Math.min(cur.minCost, chargeCost[nextCity]);

                if (dist[nextCity][nextMinCost] > nextTotalCost) {
                    dist[nextCity][nextMinCost] = nextTotalCost;
                    pq.add(new State(nextCity, nextTotalCost, nextMinCost));
                }
            }
        }
        return -1;
    }
}