package lockdownCity2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Queue;
import java.util.TreeSet;

class UserSolution {
	int N;
	int M;
	String[][] map;
	
	TreeSet<String> grade;
	HashMap<String,Integer> gradeCnt;
	
	Comparator<String> comp=(a,b)->{
		return a.compareTo(b);//사전순으로 정렬 
	};
	
	static class gu{
		int r;
		int c;
		String grade;
		gu(int r,int c,String grade){
			this.r=r;
			this.c=c;
			this.grade=grade;
		}
	}
	
	void init(int N, int M, String mGrade[][]) {
		this.N=N;
		this.M=M;
		
		grade=new TreeSet<>(comp);
		gradeCnt=new HashMap<>();
		
		map=new String[N][N];
		for(int r=0;r<N;r++) {
			for(int c=0;c<N;c++) {
				map[r][c]=mGrade[r][c];
				gradeCnt.put(map[r][c], gradeCnt.getOrDefault(map[r][c],0)+1);
				grade.add(map[r][c]);
			}
		}
	}

	void change(int mRow, int mCol, int mDir, int mLength, String mGrade) {
		if(mDir==0) {
			for(int r=0;r<mLength;r++) {
				int cnt=gradeCnt.get(map[mRow+r][mCol]);
				if(cnt<=1) {
					grade.remove(map[mRow+r][mCol]);
					gradeCnt.remove(map[mRow+r][mCol]);
				}
				gradeCnt.put(map[mRow+r][mCol], cnt-1);
				map[mRow+r][mCol]=mGrade;
//				System.out.print(map[mRow+r][mCol]+",");
				grade.add(mGrade);
				gradeCnt.put(mGrade, gradeCnt.getOrDefault(mGrade,0)+1);
			}
		}else if(mDir==1) {
			for(int c=0;c<mLength;c++) {
				int cnt=gradeCnt.get(map[mRow][mCol+c]);
				if(cnt<=1) {
					grade.remove(map[mRow][mCol+c]);
					gradeCnt.remove(map[mRow][mCol+c]);
				}
				gradeCnt.put(map[mRow][mCol+c], cnt-1);
				map[mRow][mCol+c]=mGrade;
//				System.out.print(map[mRow][mCol]+",");
				grade.add(mGrade);
				gradeCnt.put(mGrade, gradeCnt.getOrDefault(mGrade,0)+1);
			}
		}
	}

	//통행증 등급 정하기 함수
	//최대 길이-26*26*...M번
	String search(int L, int sRow, int sCol, int eRow, int eCol) {
		ArrayList<String> list=new ArrayList<>(grade);
		
		int left=0;
		int right=list.size()-1;

		while(left<right) {
			int mid=(left+right+1)/2;
			String limit=list.get(mid);
			if(bfs(limit,L,sRow,sCol,eRow,eCol)) {
				left=mid;
			}else {
				right=mid-1;
			}
		}
		return list.get(left);
	}
	
	int[] dr= {-1,1,0,0};
	int[] dc= {0,0,-1,1};
	
	boolean bfs(String limit,int L, int sRow, int sCol, int eRow, int eCol) {
		int[][] dist=new int[N][N];
		boolean[][]visited=new boolean[N][N];
		for(int i=0;i<N;i++) {
			Arrays.fill(dist[i], -1);
		}
		
		Queue<gu>q=new ArrayDeque<>();
		
		q.add(new gu(sRow,sCol,map[sRow][sCol]));
		dist[sRow][sCol]=0;
		visited[sRow][sCol]=true;
		
		while(!q.isEmpty()) {
			gu cur=q.poll();
			int r=cur.r;
			int c=cur.c;
			String grade=cur.grade;
			if(r==eRow && c==eCol) return true;
			
			if(dist[r][c]==L) continue;
			
			for(int k=0;k<4;k++) {
				int nr=r+dr[k];
				int nc=c+dc[k];
				if(nr<0 || nr>=N || nc<0 || nc>=N) continue;
				if(!visited[nr][nc] && map[nr][nc].compareTo(limit)>=0) {
					visited[nr][nc]=true;
					dist[nr][nc]=dist[r][c]+1;
					q.add(new gu(nr,nc,map[nr][nc]));
				}
			}
		}
		return false;
	}
	
	String calculate(int L, int sRow, int sCol, int eRow, int eCol) {
		String ret = new String();
		
		ret=search(L,sRow,sCol,eRow,eCol);
		
		return ret;
	}
}
