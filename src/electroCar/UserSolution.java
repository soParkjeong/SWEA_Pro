package electroCar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

class UserSolution {
	int N;
	int M;
	int K;
	
	int[] charge;//시간당 충전 용량

	ArrayList<Edge>[] graph;
	HashMap<Integer,Edge> EdgeMap;//도로 정보 저장소
	
	static class Edge{
		int from;
		int to;
		int id;
		int time;
		int power;
		boolean removed;
		
		Edge(int from,int to, int id, int time, int power){
			this.from=from;
			this.to=to;
			this.id=id;
			this.time=time;
			this.power=power;
			this.removed=false;
		}
	}
	
	PriorityQueue<infectState> pq1;
	PriorityQueue<StateWithBattery> pq2;
	
	static class infectState{
		int city;
		int time;//누적 소요 시간
		
		infectState(int city,int time){
			this.city=city;
			this.time=time;
		}
	}
	
	static class StateWithBattery{
		int city;
		int time;
		int battery;
		
		StateWithBattery(int city,int time,int battery){
			this.city=city;
			this.time=time;
			this.battery=battery;
		}
	}
	
	Comparator<infectState>infectComp=(a,b)->{
		return Integer.compare(a.time,b.time);
	};
	
	Comparator<StateWithBattery>batterycomp=(a,b)->{
		return Integer.compare(a.time,b.time);
	};

	public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
		this.N=N;
		
		graph=new ArrayList[N];
		EdgeMap=new HashMap<>();
		for (int i=0;i<N;i++) graph[i] = new ArrayList<>();
		
		for(int i=0;i<K;i++) {
			Edge edge=new Edge(sCity[i],eCity[i],mId[i],mTime[i],mPower[i]);
			graph[sCity[i]].add(edge);
			EdgeMap.put(mId[i], edge);
		}
		
		charge=new int[N];
		for(int i=0;i<N;i++) {
			charge[i]=mCharge[i];//도시 i의 단위 시간당 충전량 
		}
	}

	public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
		Edge edge=new Edge(sCity,eCity,mId,mTime,mPower);
		graph[sCity].add(edge);//단방향 도로 연결 완
		EdgeMap.put(mId,edge);//도로 정보도 저장 
		
		return;
	}

	public void remove(int mId) {
		EdgeMap.get(mId).removed=true;
		return;
	}
	
	//전염 다익
	int[] infectTime(int M,int[] mCity,int[] mTime) {
		pq1=new PriorityQueue<>(infectComp);
		
		int[] infect=new int[N];//도시까지 도착하는데 최소 (누적)소요시간 저장용(도시가 전염되는 가장 빠른 시각)
		
		for(int i=0;i<N;i++) Arrays.fill(infect,Integer.MAX_VALUE/10);
		
		//멀티 소스 다익스트라 
		//동시 다발적으로 M개의 도시에서 전염 시작 
		//전염 시작점이 여러 개니까, 다 PQ에 넣고 infect(dist 역할)도 초기 세팅 
		for(int i=0;i<M;i++) {
			int city=mCity[i];//i번째 도시=city
			int time=mTime[i];//city라는 도시의 전염 시작 시간
			if(time<infect[city]) {//city라는 도시의 전염 최소시각보다 빠르면 
				infect[city]=time;//city라는 도시의 최소 전염 소요시각 갱신
				pq1.add(new infectState(city,time));//PQ에 추가 
			}
		}
		
		while(!pq1.isEmpty()) {
			infectState cur=pq1.poll();
			int city=cur.city;
			int time=cur.time;
			
			if(time!=infect[city]) continue;//시간이 다르면 점검 필요 
			
			for(Edge e:graph[city]) {
				if(e.removed) continue;
				
				int next=e.to;
				int nextTime=time+e.time;
				
				if(nextTime<infect[next]) {//기존에 저장해둔 최소 전염 시각보다 더 짧게 걸리면 
					infect[next]=nextTime;//갱신
					pq1.add(new infectState(next,nextTime));//PQ에도 추가 
				}
			}
		}
		return infect;
	}
	
	boolean cut(int curCity, int curTime, int curBattery, int B, int[][] time) {
	    for (int cb = curBattery + 1; cb <= B; cb++) {
	        if (curTime>=time[curCity][cb]) return true;//잔량이 더 많이 남았는데 더 빠른 방법이 있어? 그럼 패스 
	    }
	    return false;
	}
	
	public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
		//일단 다익스트라에서 갱신할 때마다 갈 곳의 전염여부 체크해야하잖아.
		//일단 시간이 계속 지나면 다 전염은 될거자나,,,그럼 가장 빠르게 전염될 시간 갖고 있다가 그거 기준으로 전염 여부 파악하자-> 전염 다익 ㄱ
		//그러고 이제 이동 다익도!
		//지금 위치한 도시(출발도시), 지금 시간, 베터리 잔량 기반으로 다음으로 갈 간선을 정해야 함  
		//근데 그냥 최소 소요시간만 고려할건 아니잖아...베터리 잔량 대비 간선의 전력소모도 고려해야하잖아.
		//지금 도시에서 갈 간선을 선택할 때 일단 최소 소요시간인 곳을 골라
		//->그러고 그 간선의 mPower[i]가 현재 베터리 잔량보다 작으면 pass(다음 최소 소요시간 간선 보러감)/ 그러고 그 간선에 연결된 eCity의 전염 다익 시간보다 현재 시간이 뒤면 전염되었으니 pass, 앞이면 아직 전염안되었으니 거기로 ㄱ   
		
		int[] infect = infectTime(M, mCity, mTime);//전염 다익 결과 각 도시별 전염 최소 시간
		
		int[][]time=new int[N][B+1];//상태: 도시,배터리 잔량 
		for(int i=0;i<N;i++) Arrays.fill(time[i], Integer.MAX_VALUE/10);
		
		pq2=new PriorityQueue<>(batterycomp);
		
	    if (infect[sCity]==0) return -1;//출발 도시까지 전염 최소 소요시간이 0이면...출발과 동시에 이미 전염된거니까 못 가  
	    
		//일단 출발지 넣고 시작 
	    time[sCity][B]=0;
		pq2.add(new StateWithBattery(sCity,0,B));
		
		while(!pq2.isEmpty()) {
			StateWithBattery cur=pq2.poll();
			int curCity=cur.city;
			int curTime=cur.time;
			int curBattery=cur.battery;
			
			if(curTime!=time[curCity][curBattery]) continue;
			
			if(curTime>=infect[curCity]) continue;//지금까지 누적 소요 시간이 그 도시 전염 최소 소요시간보다 더 걸렸으면 이미 전염됐겠지?-> 패스
			
			if (cut(curCity, curTime, curBattery, B, time)) continue;
			
			//도착하면 누적 소요 시간 반환 
			if (curCity == eCity) return curTime;
			
			//이동이니까 간선 전체 다 점검 
			for(Edge e:graph[curCity]) {
				if(e.removed) continue;
				
				if(curBattery<e.power) continue;//잔량보다 지나갈 경로의 전력소모량 크면 패스 
				
				int next=e.to;
				int newTime=curTime+e.time;
				if(newTime>=infect[next]) continue;//전염시기보다 시간이 더 지났거나 동일하면 패스 
				
				//전력 소모
				int newb=curBattery-e.power;
				if(newTime<time[next][newb]) {
					time[next][newb]=newTime;
					pq2.add(new StateWithBattery(next,newTime,newb));
				}
			}
			
			//제자리에서 충전-> 간선 iterator로 점검 불필요 
			int newTime=curTime+1;
			if(newTime<infect[curCity]) {
				//전력 충전 
				int newb = curBattery + charge[curCity];
				if(curBattery+charge[curCity]>=B) {//충전해도 B를 넘으면 안됨
					newb=B;
				}
				if(newTime<time[curCity][newb]) {
					time[curCity][newb]=newTime;
					pq2.add(new StateWithBattery(curCity,newTime,newb));
				}
			}
		}
		
		return -1;
	}
}
