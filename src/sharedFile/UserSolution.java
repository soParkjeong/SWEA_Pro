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
		int id;//파일 id
		int size;
		int addTime;//파일 추가 시각 
		
		File(int id,int size,int addTime){
			this.id=id;
			this.size=size;
			this.addTime=addTime;
		}
	}
	
	ArrayList<Request>[] requestComputer;//컴퓨터별 파일 요청 리스트
	
	static class Request{
		int requestId;//요청 파일 id
		int requestSize;//요청 파일 사이즈 
		int requestTime;//파일 요청 시각 
		
		Request(int requestId,int requestSize,int requestTime){
			this.requestId=requestId;
			this.requestSize=requestSize;
			this.requestTime=requestTime;
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
	
	static class Download{
		int time;//다운로드 요청 시각 
		int fileId;//다운로드 요청한 파일 id
		
		Download(int time,int fileId){
			this.time=time;
			this.fileId=fileId;
		}
	}
	
	
	//1. init
	void init(int N, int mShareFileCnt[], int mFileID[][], int mFileSize[][])
	{
		this.N=N;
		
		inComputer=new ArrayList[N+1];
		fileMap=new HashMap<>();
		
		for(int i=1;i<=N;i++) {
			inComputer[i]=new ArrayList<>();
			for(int k=0;k<mShareFileCnt[i+1];k++) {
				File file=new File(mFileID[i][k],mFileSize[i][k],0);
				inComputer[i].add(file);
				fileMap.put(mFileID[i][k], file);//맵에도 저장 완
			}
		}//컴퓨터별 파일 주입 완 
		
	}

	
	//2. makeNet
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
	
	//helper 
	//새로운 공유 파일 있는지 다익
	List<Integer> downloadDijk(int start) {
		int[] dist=new int[N+1];
		Arrays.fill(dist,Integer.MAX_VALUE/10);
		
		pq=new PriorityQueue<State>(comp);
		dist[start]=0;
		pq.add(new State(start,0));
		
		canDownload=new ArrayList<>();//5000이내의 컴퓨터 저장소 
		
		while(!pq.isEmpty()) {
			State cur=pq.poll();
			int curComp=cur.computer;
			int curdistSum=cur.distSum;
			if(curdistSum>5000) break;//컷해줘도 되겄지?
			
			if(curdistSum!=dist[curComp]) continue;
			
			canDownload.add(curComp);//누적 거리가 5000이내면 저장 
			
			for(Edge edge:graph[curComp]) {
				int next=edge.end;
				int nextDist=curdistSum + edge.dist;
				if(nextDist<=5000 && nextDist < dist[next]) {
					dist[next]=nextDist;
					pq.offer(new State(next,nextDist));
				}
			}
		}
		return canDownload;
	}

	
	//3. addLink
	void addLink(int mTime, int mComA, int mComB, int mDis)
	{
		Edge edge1=new Edge(mComA,mDis,mTime);
		Edge edge2=new Edge(mComB,mDis,mTime);
		
		graph[mComA].add(edge2);
		graph[mComB].add(edge1);//양방향 연결 완 
		
		//아마 다운로드 요청 컴퓨터 목록 저장소 생성해서 
		if(requestComputer!=null) {
			for(int i=0;i<N;i++) {
				downloadDijk(i);//5000이내에 있는 컴퓨터 목록 반환
				//그 컴퓨터들에 있는 파일들 다운로드 시작(mTime에)
				for(int d=0;d<canDownload.size();d++) {
					int compId=canDownload.get(d);
					//이 컴퓨터에 있는 파일을 요청 파일에 추가
					for(int k=0;k<inComputer[compId].size();k++) {
						int requestId=inComputer[compId].get(k).id;
						int requestSize=inComputer[compId].get(k).size;
						requestComputer[compId].add(new Request(requestId,requestSize,mTime));
					}
				}
			}
		}
	}

	
	//4. addShareFile
	void addShareFile(int mTime, int mComA, int mFileID, int mSize)
	{
		File file=new File(mFileID,mSize,mTime);
		inComputer[mComA].add(file);
		fileMap.put(mFileID, file);
		
		if(requestComputer!=null) {
			for(int i=0;i<N;i++) {
				downloadDijk(i);//5000이내에 있는 컴퓨터 목록 반환
				//그 컴퓨터들에 있는 파일들 다운로드 시작(mTime에)
				for(int d=0;d<canDownload.size();d++) {
					int compId=canDownload.get(d);
					//이 컴퓨터에 있는 파일을 요청 파일에 추가
					for(int k=0;k<inComputer[compId].size();k++) {
						int requestId=inComputer[compId].get(k).id;
						int requestSize=inComputer[compId].get(k).size;
						requestComputer[compId].add(new Request(requestId,requestSize,mTime));
					}
				}
			}
		}
	}

	
	//5. downloadFile
	int downloadFile(int mTime, int mComA, int mFileID)
	{
		int requestSize=fileMap.get(mFileID).size;
		requestComputer[mComA].add(new Request(mFileID,requestSize,mTime));
		
		int cnt=0;
		for(int i=1;i<=N;i++) {
			for(Request file:requestComputer[i]) {
				if(file.requestTime<=mTime) {
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	//다운로드가 완료되면 각 컴퓨터별 보유 파일 정보 수정 함수
	

	//6. getFileSize
	int getFileSize(int mTime, int mComA, int mFileID)
	{	
		return 0;
	}
}