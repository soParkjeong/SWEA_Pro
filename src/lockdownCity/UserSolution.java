package lockdownCity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.Queue;
import java.util.TreeMap;
//import java.util.TreeSet;

class UserSolution {
	int N;
	int M;
	String[][] city;
	
	int[] dr= {-1,1,0,0};
	int[] dc= {0,0,-1,1};

	//통행증에 사용횟수/보안등급 기반으로 이동 경로가 정해질 듯
	//사용횟수는 L, 보안등급은 최대한 낮게 제한을 두면 될듯
	static class Ticket{//통행증 보안등급 
		int limit;
		String grade;
		
		Ticket(int limit,String grade){
			this.limit=limit;
			this.grade=grade;
		}
	}
	
	TreeMap<String,Integer> gradeMap=new TreeMap<>((a,b)->a.compareTo(b));//보안 등급 사전순으로 정렬 
//	HashMap<String,Integer> gradeCnt=new HashMap<>();//보안등급 별 개수 저장용 

	static class Node{
		int r;
		int c;
		Node(int r,int c){
			this.r=r;
			this.c=c;
		}
	}
	
	void init(int N, int M, String mGrade[][]) {
		this.N=N;
		this.M=M;
		
		city=new String[N][N];
		gradeMap.clear();
//		gradeCnt.clear();
		
		for(int r=0;r<N;r++) {
			for(int c=0;c<N;c++) {
				city[r][c]=mGrade[r][c];
				addGrade(mGrade[r][c]);
			}
		}//맵에 보안등급 기재 완
	}

	//10,000번 호출 
	void change(int mRow, int mCol, int mDir, int mLength, String mGrade) {
		int dir=mDir;
		int nr=0;
		int nc=0;
		
		if(dir==0) {
			for(int r=0;r<mLength;r++) {
				nr=mRow+r;
				nc=mCol;
				removeGrade(city[nr][nc]);
				city[nr][mCol]=mGrade;
				addGrade(mGrade);
			}
		}else {
			for(int c=0;c<mLength;c++) {
				nr=mRow;
				nc=mCol+c;
				removeGrade(city[nr][nc]);
				city[mRow][nc]=mGrade;
				addGrade(mGrade);
			}
		}
	}
	
	void addGrade(String grade) {
//		int cnt=gradeCnt.getOrDefault(grade,0);//그 등급이 비어있으면 일단 0으로 초기화 
//		gradeCnt.put(grade, cnt+1);//해당 보안 등급 개수+1
		gradeMap.put(grade,gradeMap.getOrDefault(grade, 0) + 1);//TreeMap에 추가
	}
	
	void removeGrade(String grade) {
		int cnt=gradeMap.get(grade);
		
		if(cnt<=1) {//그 보안등급 개수가 1개 이하로 있으면 
//			gradeCnt.remove(grade);
			gradeMap.remove(grade);//개수 셋이랑 등급 셋에서 아예 제거
		}else {
			gradeMap.put(grade,cnt-1);//개수 셋에서 -1 해주기 
		}
	}
	
	//갈 수 있는 칸임?
	boolean canGo(String ticketGrade,String zoneGrade) {
		if(ticketGrade.compareTo(zoneGrade)<=0) {//티켓 보안등급보다 작으면 
			return true;//가능
		}return false;//불가능
	}
	
	//L이내로 도착 가능? 
	boolean canReach(Ticket ticket,int sRow,int sCol,int eRow,int eCol) {
		Queue<Node> q=new ArrayDeque<>();
		int[][]dist=new int[N][N];//(r,c)까지 오는데 누적 이동 거리 저장소 
		
		for(int i=0;i<N;i++) {
			Arrays.fill(dist[i], -1);
		}
		
		q.add(new Node(sRow,sCol));
		dist[sRow][sCol]=0;
		
		while(!q.isEmpty()) {
			Node cur=q.poll();
			
			if(dist[cur.r][cur.c]>ticket.limit) continue;
			
			if(cur.r==eRow && cur.c==eCol) return true;
			
			for(int d=0;d<4;d++) {
				int nr=cur.r+dr[d];
				int nc=cur.c+dc[d];
				
				if(nr<0 || nr>=N || nc<0 || nc>=N) continue;//경계 넘어가면 패스
				if(dist[nr][nc]!=-1) continue;//이미 방문한 곳이면 패스
				
				if(!canGo(ticket.grade,city[nr][nc])) continue;//이동 못하는 상태면 패스 
				
				dist[nr][nc]=dist[cur.r][cur.c]+1;//인접 방면으로 이동하면 이동 거리+1
				
				if(dist[nr][nc]<=ticket.limit) {//보안등급 제한 안 넘으면 ㄱ 
					q.add(new Node(nr,nc));
				}
			}
		}
		return false;
	}
	
	//200번 호출 
	String calculate(int L,int sRow,int sCol,int eRow,int eCol) {
		ArrayList<String> cand=new ArrayList<>(gradeMap.keySet());//TreeSet에는 인덱스로 접근 못하니까 
		
		//bfs로 L이내로 도착할 수 있는 경로 중 티켓 보안등급이 젤 낮은 티켓으로도 갈 수 있는 경로 
		//이미 보안등급은 사전순으로 정렬되어있으니까 이분탐색 ㄱ 
		int left=0;
		int right=cand.size()-1;
		
		int ansIdx=0;
		while(left<=right) {
			int mid=(left+right)/2;
			
			Ticket tk=new Ticket(L,cand.get(mid));
			
			if(canReach(tk,sRow,sCol,eRow,eCol)) {
				ansIdx=mid;
				left=mid+1;//티켓의 보안등급 제한 더 약하게(사전순으로 더 뒤)
			}else {
				right=mid-1;//티켓의 보안등급 제한 더 강하게(사전순으로 더 앞)
			}
		}
		return cand.get(ansIdx);
	}
}
