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
		
		int ownerComp;//이 파일이 존재하는 컴퓨터
		boolean shared;//공유 원본인지, 그냥 다운로드 완료본인지 
		
		File(int id,int size,int addTime,int ownerComp){
			this.id=id;
			this.size=size;
			this.addTime=addTime;
			this.ownerComp=ownerComp;
			this.shared=true;
		}
	}
	
	ArrayList<Request>[] requestComputer;//컴퓨터별 파일 다운로드 요청 리스트
	
	static class Request{
		int requestId;//요청 파일 id
		int requestSize;//요청 파일 사이즈 
		int requestTime;//파일 요청 시각 
		
		int compId;//요청한 컴퓨터
		long downloaded;//지금까지 다운받은 양
		int lastUpdateTime;//마지막 (업데이트) 반영 시각
		int sourceCnt;//현재 붙어있는 공유 원본 수
		boolean finished;//완료 여부 
		
		Request(int requestId,int requestSize,int requestTime,int compId,int sourceCnt){
			this.requestId=requestId;
			this.requestSize=requestSize;
			this.requestTime=requestTime;
			this.compId=compId;
			this.downloaded=0;//아직 다운로드 된 건 없음
			this.lastUpdateTime=requestTime;//초기 시각은 다운로드 요청 시각 
			this.sourceCnt=sourceCnt;
			this.finished=false;//미완료 상태 
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
		
	//1. init
	void init(int N, int mShareFileCnt[], int mFileID[][], int mFileSize[][])
	{
		this.N=N;
		
		graph=new ArrayList[N+1];
		for(int i=1;i<=N;i++) graph[i]=new ArrayList<>();
		
		inComputer=new ArrayList[N+1];
		fileMap=new HashMap<>();
		for(int i=1;i<=N;i++) {
			inComputer[i]=new ArrayList<>();
			for(int k=0;k<mShareFileCnt[i-1];k++) {
				File file=new File(mFileID[i-1][k],mFileSize[i-1][k],0,i);
				inComputer[i].add(file);
				fileMap.put(mFileID[i-1][k], file);//맵에도 저장 완
			}
		}//컴퓨터별 파일 주입 완 
		
		requestComputer=new ArrayList[N+1];
		for(int i=1; i<=N; i++) requestComputer[i]=new ArrayList<>();
	}

	
	//2. makeNet
	void makeNet(int K, int mComA[], int mComB[], int mDis[])
	{
		this.K=K;
		
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
	
	//어떤 컴퓨터가 특정 파일을 공유 원본으로 갖고 있는지 
	boolean hasSharedFile(int compId,int fileId) {
		for(File file:inComputer[compId]) {
			if(file.id==fileId && file.shared) return true;
		}
		return false;
	}
	
	//어떤 컴퓨터가 특정 파일을 갖고 있는지 여부 쳌
	File getFileInComputer(int compId,int fileId) {
		for(File file: inComputer[compId]) {
			if(file.id==fileId) return file;
		}
		return null;
	}

	//5000이하로 갈 수 있는 컴퓨터들 중 fileId의 shared=true 상태로 가진 컴퓨터 수 세기
	int countSource(int startComp, int fileId) {
		
	}
	
	//3. addLink
	void addLink(int mTime, int mComA, int mComB, int mDis)
	{
		//1. 현재 시각까지 다운로드 진행 반영(업데이트)
		
		//2. 링크 추가 
		Edge edge1=new Edge(mComA,mDis,mTime);
		Edge edge2=new Edge(mComB,mDis,mTime);
		
		graph[mComA].add(edge2);
		graph[mComB].add(edge1);//양방향 연결 완 
		
		//3. 기존 모든 요청의 sourceCnt 재계산 
		//아마 다운로드 요청 컴퓨터 목록 저장소 생성해서(->틀림: 계속 새 요청 생성)
//		if(requestComputer!=null) {
//			for(int i=1;i<=N;i++) {
//				downloadDijk(i);//5000이내에 있는 컴퓨터 목록 반환
//				//그 컴퓨터들에 있는 파일들 다운로드 시작(mTime에)
//				for(int d=0;d<canDownload.size();d++) {
//					int compId=canDownload.get(d);
//					//이 컴퓨터에 있는 파일을 요청 파일에 추가
//					for(int k=0;k<inComputer[compId].size();k++) {
//						int requestId=inComputer[compId].get(k).id;
//						int requestSize=inComputer[compId].get(k).size;
//						requestComputer[compId].add(new Request(requestId,requestSize,mTime));
//					}
//				}
//			}
//		}
	}

	
	//4. addShareFile
	void addShareFile(int mTime, int mComA, int mFileID, int mSize)
	{
		//1. 현재 시각까지 다운로드 진행 반영(업데이트)
		
		//2. 공유 파일 추가
		File file=new File(mFileID,mSize,mTime,mComA);
		inComputer[mComA].add(file);
		fileMap.put(mFileID, file);
		
		//3. 기존 모든 요청의 sourceCnt 재계산 
//		if(requestComputer!=null) {
//			for(int i=1;i<=N;i++) {
//				downloadDijk(i);//5000이내에 있는 컴퓨터 목록 반환
//				//그 컴퓨터들에 있는 파일들 다운로드 시작(mTime에)
//				for(int d=0;d<canDownload.size();d++) {
//					int compId=canDownload.get(d);
//					//이 컴퓨터에 있는 파일을 요청 파일에 추가
//					for(int k=0;k<inComputer[compId].size();k++) {
//						int requestId=inComputer[compId].get(k).id;
//						int requestSize=inComputer[compId].get(k).size;
//						requestComputer[compId].add(new Request(requestId,requestSize,mTime));
//					}
//				}
//			}
//		}
	}

	//현재 시각까지 다운로드 진행 반영용 함수
	
	
	
	//현재 그 요청에 붙을 수 있는 공유 원본 수를 세는 함수
	
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