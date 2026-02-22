package MarathonCourse;

import java.util.ArrayList;
import java.util.HashSet;

class UserSolution {
	
	static class Edge{
		int to;
		int id;
		int len;
		
		Edge(int to, int id, int len){
			this.to=to;
			this.id=id;
			this.len=len;
		}
	}
	
	int n;
	ArrayList<Edge>[] graph;
	HashSet<Integer> removed;
	
	
	void init(int N)
	{
		n=N;
		graph=new ArrayList[N+1];
		for(int i=1;i<=N;i++) graph[i]=new ArrayList<>();
		removed=new HashSet<>();
		
	}

	void addRoad(int K, int mID[], int mSpotA[], int mSpotB[], int mLen[])
	{
		for(int i=0;i<K;i++) {
			int id=mID[i];
			int a=mSpotA[i];
			int b=mSpotB[i];
			int len=mLen[i];
			
			graph[a].add(new Edge(b,id,len));
			graph[b].add(new Edge(a,id,len));
		}//양방향 그래프 완
	}

	void removeRoad(int mID)
	{
		removed.add(mID);//삭제 목록에 추가 
	}

	//4개 간선 조합 클래스
	static class Path4{
		int sum;
		int e0, e1, e2, e3;
		
		Path4(int sum, int e0, int e1, int e2, int e3){
			this.sum=sum;
			this.e0=e0;
			this.e1=e1;
			this.e2=e2;
			this.e3=e3;
		}
		
		//중복 간선 여부 검사 여기서 해버리자 
		boolean isDuplicate(Path4 o) {
			int[] a= {e0,e1,e2,e3};
			int[] b= {o.e0,o.e1,o.e2,o.e3};
			
			for(int i=0;i<4;i++) {
				for(int j=0;j<4;j++) {
					if(a[i]==b[j]) return false;
				}
			}
			return true;
		}
	}
	
	int getLength(int mSpot)
	{
		//일단 출발/도착점이 mSpot인 4개 간선 조합 중 같은 반환점 목록에 들어있는 서로 다른 2개 조합 가져와서 보면서 maxLength 갱신 ㄱ
		//일단 mSpot을 출발/도착지로 4번 DFS해서 나오는 모든 조합 먼저 쳌
		//반환점이 같은 애들끼리 저장->그러고 같은 반환점 가진 조합 중 서로 다른 2개 뽑아보면서 maxLength 갱신 
		//4번 DFS해서 반환점 같은 애들끼리 저장- 이거 함수로 따로 배자 
		//그러고 반환점 같은 애들 중 서로 다른 2개 조합 가져와서 maxLength 갱신
		int se=mSpot;
		//반환점 같은 4개 간선 조합끼리 저장할 곳 만들
		ArrayList<Path4>[] tp=new ArrayList[n+1];
		for(int i=1;i<=n;i++) tp[i]=new ArrayList<>();
		
		int[] usedEdge=new int[4];
		dfs4(se,se,0,0,usedEdge,tp);
		
		int maxSum=-1;
		
		//자 이제 반환점 별로 2개 조합씩 뽑아서 합보고 max 갱신 ㄱ 
		for(int k=1;k<=n;k++) {
			ArrayList<Path4> list=tp[k];
			if(list.size()<2) continue;//8개여야 하니까 2개 조합 이상은 들어있어야지
			
			//그러고 정렬해서 조건은 충족하면서 합이 최대인 조합 찾자 
			//정렬해도 625개 정렬은 안터질듯...? 
			list.sort((a,b)-> Integer.compare(b.sum, a.sum));
			int maxLen=list.get(0).sum;//합이 젤 긴 4간선 조합 가져와
			
			for(int i=0;i<list.size();i++) {
				int maxLen1=list.get(i).sum;
				
				if(maxLen1 + maxLen <= maxSum) break;//젤 큰거 두번 더해도 갱신 안되면 걍 컷 
				
				for(int j=i+1;j<list.size();j++) {
					int maxLen2=list.get(j).sum;
					int total=maxLen1+maxLen2;
					
					if(total<=maxSum) break;//정렬 후의 합인데도 작으면 뒤에는 볼 필요도 없음 
					if(total>42195) continue;
					
					if(list.get(i).isDuplicate(list.get(j))) {
						maxSum=total;
					}
				}	
			}
		}
		
		return maxSum;
	}
	
	//mSpot에서 4번 DFS해서 나올 수 있는 모든 간선 조합 -> 반환점 같은 애들끼리 저장 함수
	void dfs4(int mSpot,int cur,int depth,int sum,int[] usedEdge,ArrayList<Path4>[] tp) {
		if(depth==4) {
			int turn=cur;
			if(turn==mSpot) return;//반환점이 mSpot이면 안됨
			//지금 반환점을 기준으로 하는 조합이 아직 없으면 만들어주
			if(tp[turn]==null) tp[turn]=new ArrayList<>();
			tp[turn].add(new Path4(sum,usedEdge[0],usedEdge[1],usedEdge[2],usedEdge[3]));
			return;
		}
		
		for(Edge e:graph[cur]) {
			if(removed.contains(e.id)) continue;//이미 삭제된거면 패스
			
			int nxt=e.to;
			if(depth>=1 &&  nxt==mSpot) continue;// mSpot 재방문 금지
			if(Used(usedEdge,depth,e.id)) continue; // 간선 중복 사용 불가
			
			usedEdge[depth]=e.id;
			dfs4(mSpot,nxt,depth+1,sum+e.len,usedEdge,tp);
		}
	}
	
	boolean Used(int[] usedEdge, int depth, int id) {
		for(int i=0;i<depth;i++) {
			if(usedEdge[i]==id) return true;
		}
		return false;
	}
}