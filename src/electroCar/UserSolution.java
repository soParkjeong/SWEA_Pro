package electroCar;

import java.util.ArrayList;
import java.util.HashSet;

class UserSolution {
	int N;
	int M;
	int K;
	
	HashSet<Integer> removed;
	ArrayList<Edge>[] graph;
	
	static class Edge{
		int from;
		int to;
		int id;
		int time;
		int power;
		
		Edge(int from,int to, int id, int time, int power){
			this.from=from;
			this.to=to;
			this.id=id;
			this.time=time;
			this.power=power;
		}
	}
	
//	HashMap<Integer,Edge> road;
	
	int[] charge;//충전 용량 

	public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
		this.N=N;
		
		graph=new ArrayList[N];
		for (int i=0;i<N;i++) graph[i] = new ArrayList<>();
		
		for(int i=0;i<K;i++) {
			graph[sCity[i]].add(new Edge(sCity[i],eCity[i],mId[i],mTime[i],mPower[i]));
		}
		
		charge=new int[N];
		for(int i=0;i<N;i++) {
			charge[i]=mCharge[i];
		}
		
		removed=new HashSet<>();
	}

	public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
		
		graph[sCity].add(new Edge(sCity,eCity,mId,mTime,mPower));//단방향 도로 연결 완 

		return;
	}

	public void remove(int mId) {
		removed.add(mId);
		return;
	}
	
	public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
		//일단 다익스트라에서 갱신할 때마다 갈 곳의 전염여부 체크해야하잖아.
		//일단 시간이 계속 지나면 다 전염은 될거자나,,,그럼 가장 빠르게 전염될 시간 갖고 있다가 그거 기준으로 전염 여부 파악하자-> 전염 다익 ㄱ
		//지금 위치한 도시(출발도시), 지금 시간, 베터리 잔량 기반으로 다음으로 갈 간선을 정해야 함  
		//근데 그냥 최소 소요시간만 고려할건 아니잖아...베터리 잔량 대비 간선의 전력소모도 고려해야하잖아.
		//지금 도시에서 갈 간선을 선택할 때 일단 최소 소요시간인 곳을 골라
		//->그러고 그 간선의 mPower[i]가 현재 베터리 잔량보다 작으면 pass(다음 최소 소요시간 간선 보러감)/ 그러고 그 간선에 연결된 eCity의 전염 다익 시간보다 현재 시간이 뒤면 전염되었으니 pass, 앞이면 아직 전염안되었으니 거기로 ㄱ   
		
		int remain=B;//잔량 
		return 0;
	}
}
