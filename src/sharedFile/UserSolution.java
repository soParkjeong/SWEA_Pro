package sharedFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class UserSolution {
	int N;
	int K;
	
	ArrayList<Edge>[] graph;
	static class Edge{
		int end;
		int dist;
		int time;//링크 추가 시각
		
		Edge(int end,int dist,int time){
			this.end=end;
			this.dist=dist;
			this.time=time;
		}
	}
	
	ArrayList<File>[] inComputer;//컴퓨터별 파일 리스트 
	static class File{
		int id;
		int size;
		int time;//파일 추가 시각 
		
		File(int id,int size,int time){
			this.id=id;
			this.size=size;
			this.time=time;
		}
	}
	
	HashMap<Integer,File> fileMap;
	
	//5000이내니까 작은 것 부터 싹 봐야하지 않을까? 다익스트라?
	PriorityQueue<State> pq;
	
	static class State{
		int computer;//현재 위치
		int distSum;//현재 위치까지 오는데 걸린 거리(누적)
		
		State(int computer,int distSum){
			this.computer=computer;
			this.distSum=distSum;
		}
	}
	
	Comparator<State> comp=(a,b)->{
		return Integer.compare(a.distSum,b.distSum);//누적 거리가 작은 것부터 나오게(5,000이내에 있는 공유파일만 다운로드 가능하니까 작은 것부터 보다가 distSum이 5,000이 넘는 순간부터 컷하도록)
	};
	
	List<Integer> canDownload;//5000 이내의 컴퓨터 저장소 
	
	void init(int N, int mShareFileCnt[], int mFileID[][], int mFileSize[][])
	{
		this.N=N;
		
		inComputer=new ArrayList[N+1];
		fileMap=new HashMap<>();
		
		for(int i=0;i<N;i++) {
			inComputer[i]=new ArrayList<>();
			for(int k=0;k<mShareFileCnt[i+1];k++) {
				File file=new File(mFileID[i][k],mFileSize[i][k],0);
				inComputer[i].add(file);
				fileMap.put(mFileID[i][k], file);//맵에도 저장 완
			}
		}//컴퓨터별 파일 주입 완 
		
	}

	void makeNet(int K, int mComA[], int mComB[], int mDis[])
	{
		this.K=K;
		
		for(int k=0;k<K;k++) {
			graph[k]=new ArrayList<>();
		}
		
		for(int k=0;k<K;k++) {
			graph[mComA[k]].add(new Edge(mComB[k],mDis[k],0));
			graph[mComB[k]].add(new Edge(mComA[k],mDis[k],0));
		}//양방향 그래프 완성 
	}
	
	//새로운 공유 파일 있는지 다익
	List<Integer> downloadDijk(int mTime,int start,int mFileID) {
		int[] dist=new int[N];
		canDownload=new ArrayList<>();//5000이내의 컴퓨터 저장소 
		Arrays.fill(dist,Integer.MAX_VALUE/10);
		
		pq=new PriorityQueue<State>(comp);
		pq.add(new State(start,0));
		
		while(!pq.isEmpty()) {
			State cur=pq.poll();
			int curComp=cur.computer;
			int curdistSum=cur.distSum;
			if(curdistSum<5000) {
				canDownload.add(curComp);//누적 거리가 5000이내면 저장 
				break;//컷해줘도 되겄지?
			}
			
			if(curdistSum!=dist[curComp]) continue;
			
			for(Edge edge:graph[curComp]) {
				int next=edge.end;
				int nextDist=curdistSum + edge.dist;
				if(nextDist < dist[next]) {
					dist[next]=nextDist;
					pq.offer(new State(next,nextDist));
				}
			}
		}
		return canDownload;
	}

	void addLink(int mTime, int mComA, int mComB, int mDis)
	{
		Edge edge=new Edge(mComA,mTime
		graph[mComA].add(null)
	}

	void addShareFile(int mTime, int mComA, int mFileID, int mSize)
	{
	}

	int downloadFile(int mTime, int mComA, int mFileID)
	{
		return 0;
	}

	int getFileSize(int mTime, int mComA, int mFileID)
	{
		return 0;
	}
}