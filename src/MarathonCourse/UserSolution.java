package MarathonCourse;

import java.util.ArrayList;
import java.util.HashSet;

class UserSolution {
	import java.util.*;

	class UserSolution {
	    int N;
	    int M;
	    String[][] city;

	    // 상하좌우
	    int[] dr = {-1, 1, 0, 0};
	    int[] dc = {0, 0, -1, 1};

	    // 현재 도시에 존재하는 보안등급 후보들을 중복 없이 정렬해서 저장
	    TreeSet<String> gradeSet = new TreeSet<>((a, b) -> a.compareTo(b));

	    // 각 보안등급이 현재 도시에 몇 개 존재하는지 관리
	    HashMap<String, Integer> gradeCount = new HashMap<>();

	    // 통행증 객체
	    static class Pass {
	        int limit;      // 사용 가능 횟수
	        String grade;   // 통행증 보안등급

	        Pass(int limit, String grade) {
	            this.limit = limit;
	            this.grade = grade;
	        }
	    }

	    // BFS용 좌표 객체
	    static class Pos {
	        int r;
	        int c;

	        Pos(int r, int c) {
	            this.r = r;
	            this.c = c;
	        }
	    }

	    void init(int N, int M, String[][] mGrade) {
	        this.N = N;
	        this.M = M;

	        city = new String[N][N];
	        gradeSet.clear();
	        gradeCount.clear();

	        for (int r = 0; r < N; r++) {
	            for (int c = 0; c < N; c++) {
	                city[r][c] = mGrade[r][c];
	                addGrade(mGrade[r][c]);
	            }
	        }
	    }

	    void change(int mRow, int mCol, int mDir, int mLength, String mGrade) {
	        for (int i = 0; i < mLength; i++) {
	            int nr = mRow + (mDir == 0 ? i : 0);
	            int nc = mCol + (mDir == 1 ? i : 0);

	            // 기존 등급 제거
	            removeGrade(city[nr][nc]);

	            // 새 등급 반영
	            city[nr][nc] = mGrade;
	            addGrade(mGrade);
	        }
	    }

	    String calculate(int L, int sRow, int sCol, int eRow, int eCol) {
	        // 현재 존재하는 등급 후보들을 정렬된 리스트로 변환
	        ArrayList<String> candidates = new ArrayList<>(gradeSet);

	        int left = 0;
	        int right = candidates.size() - 1;

	        // 문제에서 항상 도달 가능한 L이 주어지므로,
	        // 적어도 하나의 정답은 존재한다고 보고 시작
	        int answerIndex = 0;

	        while (left <= right) {
	            int mid = (left + right) / 2;

	            // 여기서만 Pass 객체 생성
	            Pass pass = new Pass(L, candidates.get(mid));

	            // 이 통행증으로 L번 이하 이동해서 도달 가능한지 판별
	            if (canReach(pass, sRow, sCol, eRow, eCol)) {
	                answerIndex = mid;

	                // 가능하면 더 약한 등급(사전순 더 뒤쪽)도 가능한지 확인
	                left = mid + 1;
	            } else {
	                // 불가능하면 더 높은 등급(사전순 더 앞쪽)이 필요
	                right = mid - 1;
	            }
	        }

	        return candidates.get(answerIndex);
	    }

	    // 현재 등급 문자열을 도시 후보 집합에 추가
	    void addGrade(String grade) {
	        int cnt = gradeCount.getOrDefault(grade, 0);
	        gradeCount.put(grade, cnt + 1);
	        gradeSet.add(grade);
	    }

	    // 현재 등급 문자열을 도시 후보 집합에서 제거
	    void removeGrade(String grade) {
	        int cnt = gradeCount.get(grade);

	        if (cnt == 1) {
	            gradeCount.remove(grade);
	            gradeSet.remove(grade);
	        } else {
	            gradeCount.put(grade, cnt - 1);
	        }
	    }

	    // passGrade 통행증으로 zoneGrade 구역에 진입 가능한지
	    boolean canEnter(String passGrade, String zoneGrade) {
	        // 사전순이 빠를수록 더 높은 보안등급
	        // 따라서 통행증 등급이 구역 등급보다 같거나 더 높아야 진입 가능
	        // ex) C 통행증이면 C, D, E 구역 진입 가능
	        return passGrade.compareTo(zoneGrade) <= 0;
	    }

	    // 주어진 통행증으로 L번 이하에 도착 가능한지 BFS로 판별
	    boolean canReach(Pass pass, int sRow, int sCol, int eRow, int eCol) {
	        int[][] dist = new int[N][N];
	        for (int i = 0; i < N; i++) {
	            Arrays.fill(dist[i], -1);
	        }

	        ArrayDeque<Pos> q = new ArrayDeque<>();
	        q.add(new Pos(sRow, sCol));
	        dist[sRow][sCol] = 0;

	        while (!q.isEmpty()) {
	            Pos cur = q.poll();

	            // 이미 사용 횟수 초과면 더 진행할 필요 없음
	            if (dist[cur.r][cur.c] > pass.limit) continue;

	            // 도착 성공
	            if (cur.r == eRow && cur.c == eCol) return true;

	            for (int d = 0; d < 4; d++) {
	                int nr = cur.r + dr[d];
	                int nc = cur.c + dc[d];

	                if (nr < 0 || nr >= N || nc < 0 || nc >= N) continue;
	                if (dist[nr][nc] != -1) continue;

	                // 다음 칸에 진입 가능한지 확인
	                // 시작점은 BFS 시작 시 이미 들어와 있으므로,
	                // 여기서는 "다음 칸으로 이동할 때"만 검사하면 됨
	                if (!canEnter(pass.grade, city[nr][nc])) continue;

	                dist[nr][nc] = dist[cur.r][cur.c] + 1;

	                if (dist[nr][nc] <= pass.limit) {
	                    q.add(new Pos(nr, nc));
	                }
	            }
	        }

	        return false;
	    }
	}
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
				
//				if(maxLen1 + maxLen <= maxSum) break;//젤 큰거 두번 더해도 갱신 안되면 걍 컷 
				
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