package carTrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
	int N;
	int K;
	ArrayList<Edge>[]graph;
	
	HashMap<Integer,Edge> edgeMap;
	
	int[] cityPower;
	
	static class Edge{
		int id;
		int from;
		int to;
		int time;
		int power;
		Edge(int id,int from,int to,int time,int power){
			this.id=id;
			this.from=from;
			this.to=to;
			this.time=time;
			this.power=power;
		}
	}
	
	static class State{//감염 다익 상태
		int to;
		int infectTime;//감염 닿은 시각
		State(int to,int infectTime){
			this.to=to;
			this.infectTime=infectTime;
		}
	}
	
	static class StateB{//여행 다익 상태
		int to;//지금 도착해있는 도시 
		int timeSum;//지금까지 걸린 시간 
		int battery;//지금까지 충전 상태 
		StateB(int to,int timeSum,int battery){
			this.to=to;
			this.timeSum=timeSum;
			this.battery=battery;
		}
	}
	
	public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
		this.N=N;
		cityPower=new int[N];
		for(int i=0;i<N;i++) cityPower[i]=mCharge[i];
		
		graph=new ArrayList[N];
		for(int i=0;i<N;i++) {
			graph[i]=new ArrayList<>();
		}
		
		edgeMap=new HashMap<>();
		for(int i=0;i<K;i++) {
			Edge edge=new Edge(mId[i],sCity[i],eCity[i],mTime[i],mPower[i]);
			graph[sCity[i]].add(edge);//그래프 연결 완
			edgeMap.put(mId[i], edge);
		}

		return;
	}

	public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
		Edge edge=new Edge(mId,sCity,eCity,mTime,mPower);
		graph[sCity].add(edge);
		edgeMap.put(mId,edge);
		return;
	}

	public void remove(int mId) {
		Edge edge=edgeMap.get(mId);
		int s=edge.from;
		graph[s].remove(edge);//그래프에서 해당 도로 삭제
		edgeMap.remove(mId);
		return;
	}
	
	//전염 다익스트라
	/*
	 * M개의 전염병 발생 도시-mCity배열
	 * M개의 도시별 전염별 발생 시각- mTime배열
	 * mCity배열의 도시를 시작점으로 다익스트라-> 각 도시별로 전염 가장 빠른 시각 기준으로 time 배열에 저장
	 * 
	 */
	int[] time;//전염병 발생 시각
	
	public int[] infectDijk(int M,int mCity[],int mTime[]) {
		time=new int[N];// M개의 도시에서 시작해도 퍼질 가능성있는 도시는 총 N개니까 
		Arrays.fill(time,Integer.MAX_VALUE);
		
		PriorityQueue<State>pq=new PriorityQueue<>((a, b) -> Integer.compare(a.infectTime, b.infectTime));
		
		//다중 시작점 투입
		for(int i=0;i<M;i++) {
			int s=mCity[i];
			time[s]=mTime[i];
			pq.offer(new State(s,time[s]));
		}
		
		while(!pq.isEmpty()) {
			State cur=pq.poll();
			int to=cur.to;
			int infectTime=cur.infectTime;
			
			if(time[to]!=infectTime) continue;
			
			for(Edge edge:graph[to]) {
				int nxt=edge.to;
				int nxtTime=infectTime+edge.time;
				if(nxtTime<time[nxt]) {
					time[nxt]=nxtTime;
					pq.offer(new State(nxt,nxtTime));
				}
			}
		}
		return time;//각 도시별 젤 빨리 감염이 될 수 있는 시각 저장 
	}

	public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
		int[][] timeB=new int[N][B+1];
		for(int i=0;i<N;i++) Arrays.fill(timeB[i], Integer.MAX_VALUE);
		
		PriorityQueue<StateB>pq=new PriorityQueue<>((a,b)->Integer.compare(a.timeSum, b.timeSum));
		
		infectDijk(M,mCity,mTime);
		
		timeB[sCity][B]=0;
		pq.add(new StateB(sCity,0,B));//시작점,풀충상태 차로 시작 
		
		while(!pq.isEmpty()) {
			StateB cur=pq.poll();
			int to=cur.to;
			int timeSum=cur.timeSum;
			int battery=cur.battery;
			
			if(to==eCity) return timeSum;
			
			if(timeSum!=timeB[to][battery]) continue;
			if(timeSum>=time[to]) continue;
			
			for(Edge edge:graph[to]) {
				int nxt=edge.to;
				int nxtTime=timeSum+edge.time;
				
				if(battery<edge.power) continue;
				int nxtB=battery-edge.power;
				
				if(nxtTime>=time[nxt]) continue;
				if(nxtTime<timeB[nxt][nxtB]) {
					timeB[nxt][nxtB]=nxtTime;
					pq.add(new StateB(nxt,nxtTime,nxtB));
				}
			}

			//제자리에서 충전만 하는 경우도
			int power=Math.min(B, battery+cityPower[to]);
			int chargeTime=timeSum+1;
			
			if(chargeTime>=time[to] || chargeTime>timeB[to][power]) continue;
			timeB[to][power]=chargeTime;
			pq.add(new StateB(to,chargeTime,power));
		}
		return -1;
	}
}