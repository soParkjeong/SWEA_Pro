package stopOver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
 
class UserSolution {
 
    static List<Node>[] graph;  // 간선 연결 저장
    static class Node{
        int e;
        int w;
 
        public Node(int e, int w){
            this.e = e;
            this.w = w;
        }
    }
    
    Comparator<Node>EdgeComparator=(a,b)->{
    	return Integer.compare(b.w,a.w);
    };
 
    static PriorityQueue<Node> pq;
    static int[] result;
    static int totalN;
 
    public void init(int N, int K, int sCity[], int eCity[], int mLimit[]) {
        totalN = N;
 
        graph = new ArrayList[N];
        for (int i = 0; i < N; i++) {
        	graph[i] = new ArrayList<Node>();
        }
 
        for(int i=0;i<K;i++){
            int s = sCity[i];
            int e = eCity[i];
            int w = mLimit[i];
            graph[s].add(new Node(e, w));
            graph[e].add(new Node(s, w));
        }
 
        pq = new PriorityQueue<>(EdgeComparator);
        result = new int[N];
 
        return;
    }
 
    public void add(int sCity, int eCity, int mLimit) {
    	graph[sCity].add(new Node(eCity, mLimit));
    	graph[eCity].add(new Node(sCity, mLimit));
        return;
    }
    
    static int calcSCity;
    static int calcECity;
    static int calcM;
    static int[] calcMStopover;
 
    public int calculate(int sCity, int eCity, int M, int mStopover[]) {
 
        calcSCity = sCity;
        calcECity = eCity;
        calcM = M;
        calcMStopover = mStopover;
 
        logic(calcSCity);  // 출발지에서 다익스트라 돌리기 ( 딱 1번만 전체로 )
 
        int answer = result[calcECity];      // 도착지까지 얼마나 걸려?
        if(answer == 0) return -1;           // 도착 못함
 
        for(int i=0;i<calcM;i++){
            int now = calcMStopover[i];     // 경유지
            int w = result[now];            // 경유지까지 얼마나 걸려?
            if(w == 0) return -1;            // 못가
            answer = Math.min(answer, w);
        }
 
        return answer;
    }
 
    private static void logic(int start){
        boolean visited2[] = new boolean[totalN];
        Arrays.fill(result, 0);
 
        pq.clear();
        pq.offer(new Node(start, Integer.MAX_VALUE/10));
 
        while(!pq.isEmpty()){
        	Node node = pq.poll();
 
            if(visited2[node.e]) continue;
            visited2[node.e] = true;
            result[node.e] = node.w;
 
            for(int i=0;i<graph[node.e].size();i++){
            	Node edge = graph[node.e].get(i);      // 연결되어 있는거 하나 꺼내
                int next = edge.e;                   // 다음 체크
 
                int cost = Math.min(node.w, edge.w);  // 가중치는 둘 중에 작은걸로 기억(왜냐하면 제한중량은 적은 거 기준으로 제한점이 걸리므로)
 
                if(!visited2[next] && result[next] < cost){ // 안가본데고 더 크면 넣기
                    pq.offer(new Node(next, cost));
                }
            }
 
        }
 
    }
 
}