package roadDeployment;

import java.util.*;

class UserSolution {
	int N;
	int K;
	
	List<Edge>[] graph;
	PriorityQueue<State> pq;
	HashMap<Integer,Edge> EdgeMap;//도로 정보 저장용 
	HashSet<Integer> roadSet;//최단경로에 사용된 경로 모음 
	
	static class Edge{
		int id;
		int s;
		int e;
		int t;
		boolean removed;//지워진 도로야?
		
		Edge(int id,int s,int e,int t){
			this.id=id;
			this.s=s;
			this.e=e;
			this.t=t;
			this.removed=false;
		}
	}
	
	static class State{
		int city;//현재 도시
		int time;//시작점->현재 도시까지의 누적 시간
		
		State(int city,int time){
			this.city=city;
			this.time=time;
		}
	}
	
	//일단 다익용
	Comparator<State> comp=(a,b)->{ //w는 누적시간이니까 누적시간이 짧은 것부터 나오도록!
		return Integer.compare(a.time, b.time);
	};
	
    public void init(int N, int K, int[] mId, int[] sCity, int[] eCity, int[] mTime) {
        this.N=N;
        this.K=K;
        
        graph=new ArrayList[N];
        for(int n=0;n<N;n++) {
        	graph[n]=new ArrayList<>();
        }
        
        EdgeMap=new HashMap<>();
        roadSet = new HashSet<>();
        
        for(int k=0;k<K;k++) {
        	Edge edge=new Edge(mId[k],sCity[k],eCity[k],mTime[k]);
        	graph[sCity[k]].add(edge);
        	EdgeMap.put(mId[k], edge);
        }
    }

    public void add(int mId, int sCity, int eCity, int mTime) {
    	Edge edge=new Edge(mId,sCity,eCity,mTime);
        graph[sCity].add(edge);
        EdgeMap.put(mId,edge);
    }

    public void remove(int mId) {
       EdgeMap.get(mId).removed=true;
    }

    //거쳐온 경로 저장하는 다익
    public int dijkstra(int sCity,int eCity) {
    	int[]time=new int[N];
    	Arrays.fill(time, Integer.MAX_VALUE);
    	
    	int before[]= new int[N];//어디서 왔는데
    	Arrays.fill(before, -1);
    	
        pq=new PriorityQueue<>(comp);
        
        time[sCity]=0;
        pq.offer(new State(sCity,0));
        
        while(!pq.isEmpty()) {
        	State cur=pq.poll();
        	
        	int curCity=cur.city;
        	int curTime=cur.time;
        	
        	if(curTime!=time[curCity]) continue;
        	
        	if(curCity==eCity) break;
        	
        	for(Edge e: graph[curCity]) {
        		int id=e.id;
        		if(e.removed) continue;//지워진 애면 
        		
        		int next=e.e;
        		int nextTime=cur.time + e.t;
        		
        		if(nextTime < time[next]) {
        			time[next]=nextTime;
        			pq.offer(new State(next,nextTime));
        			before[next]=e.id;//next는 mId 다리타고 건너왔다
        		}
        	}
        }
        
        if(time[eCity]==Integer.MAX_VALUE/10) return -1;
        
        //거쳐온 경로 저장했으니 이제 e->s로 거슬러 오면서 최종 최단경로에 활용된 경로 fix 
        int now=eCity;//현재 위치 e에서 시작 
        while(true) {
        	if(now==sCity) break;//s까지 다 오면 끝
        	if(before[now]==-1) return -1;//이 도시로 오기 직전 도로가 없었으면 연결 끊긴 거 
        	roadSet.add(before[now]);//이 도시까지 오는데 사용한 도로를 set에 추가 
        	
        	now=EdgeMap.get(before[now]).s;//이제 방금 추가한 도로에 연결된 시작 도시(거꾸로 거슬러 오는 거니까)로 현재 위치 갱신 
        }
        return time[eCity];//이게 최종 최단경로의 소요시간 
    }
    
    //삭제된 도로 제외하고 최단거리(걍 다익)
    public int deployDijkstra(int sCity,int eCity) {
    	int[] time=new int[N];
    	Arrays.fill(time, Integer.MAX_VALUE/10);
    	
    	pq=new PriorityQueue<>(comp);
    	
    	time[sCity]=0;
    	pq.offer(new State(sCity,0));
    	
    	while(!pq.isEmpty()) {
    		State cur=pq.poll();
    		
    		int curCity=cur.city;
    		int curTime=cur.time;
    		
    		if(curTime!=time[curCity]) continue;
    		
    		if(curCity==eCity) return time[curCity];
    		
    		for(Edge e: graph[curCity]) {
    			int id=e.id;
    			if(e.removed) continue;
    			
    			int next=e.e;
    			int nextTime=cur.time+e.t;
    			
    			if(nextTime < time[next]) {
    				time[next]=nextTime;
    				pq.offer(new State(next,nextTime));
    			}
    		}
    	}
    	return -1;
    }
    
    //다익스트라를 통해 최단경로에 포함된 경로 
    public int calculate(int sCity, int eCity) {
    	roadSet.clear();
    	
    	int bestway=dijkstra(sCity,eCity);//일단 s->e까지 다익스트라 타고 최단 경로 구한 결과인 최종 최단경로의 소요시간 
    	
    	int max=-1;//도로 파괴 후 최대 소요시간 저장용 변수 
    	
    	for(Integer roadId:roadSet) {//최단 경로에 사용된 경로 하나씩 순회하면서 지우고 다익
    		EdgeMap.get(roadId).removed=true;//간선 하나 지우고
    		
    		int temp=deployDijkstra(sCity,eCity);
    		if(temp==-1) {
    			max=-1;
    			EdgeMap.get(roadId).removed = false;
    			break;
    		}else {
    			max=Math.max(max, temp);
    		}
    		EdgeMap.get(roadId).removed=false;//다시 간선 복구(다음엔 다른 거 지우고 가봐야지)
    	}
    	
    	if(max==-1) return -1;
    	
        return Math.abs(max-bestway);//지연 시간의 최대치를 알아야하니까-> 파괴 후 최대 소요 시간-원래 소요 시간
    }
}
