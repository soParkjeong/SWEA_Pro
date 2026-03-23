package mineralTransportation2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

class UserSolution {
	int N;
	int L;
//	int[][][]map;//베이스캠프 id랑 채굴량만 저장할 맵
	
	HashMap<Integer,bcInfo> bcMap;
	HashMap<Integer,Integer> idToIdx;
	int campCnt;
	
	static class bcInfo{
		int id;
		int r;
		int c;
		int q;
		bcInfo(int id,int r,int c,int q){
			this.id=id;
			this.r=r;
			this.c=c;
			this.q=q;
		}
	}
	
	ArrayList<bcInfo>[][]bucket;
	int BS;
	int BC;
	
//	TreeSet<bcInfo>[] members;//수집 가능한 광물 최댓값이 동일한 애들끼리 그룹 
	
	Comparator<bcInfo> comp = (a, b) -> {
	    if (a.q != b.q) return Integer.compare(a.q, b.q);
	    if (a.r != b.r) return Integer.compare(a.r, b.r);
	    if (a.c != b.c) return Integer.compare(a.c, b.c);
	    return Integer.compare(a.id, b.id);
	};
	
	int[] Sum;
	int[] parent;
	
	void init(int L, int N){
		this.N=N;
		this.L=L;
		
		BS=L;
		BC=(N+BS-1)/BS;
		
//		map=new int[N][N][2];//각 맵에 id랑 채굴량만 저장 
		
		bucket=new ArrayList[BC][BC];
		for(int i=0;i<BC;i++) {
			for(int j=0;j<BC;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}
		
		bcMap=new HashMap<>();
		idToIdx=new HashMap<>();
		campCnt=0;
		
		parent=new int[20001];
		Sum=new int[20001];
		
//		members=new TreeSet[20001];
//		for(int i=0;i<20001;i++) {
//			members[i]=new TreeSet<>(comp);
//		}
	}
	
	int find(int x) {
		if(parent[x]==x) return x;
		return parent[x]=find(parent[x]);
	}
	
	void union(int x,int y) {
		x=find(x);
		y=find(y);
		
		if(x==y) return;

		parent[y]=x;
		Sum[x]+=Sum[y];
	}
	
	int addBaseCamp(int mID, int mRow, int mCol, int mQuantity){
		bcInfo base=new bcInfo(mID,mRow,mCol,mQuantity);
		bcMap.put(mID,base);
		
		int idx=campCnt++;
		idToIdx.put(mID, idx);
		
		Sum[idx]=mQuantity;
		parent[idx]=idx;
//		members[idx].add(base);
		
//		map[mRow][mCol][0]= idx;
//		map[mRow][mCol][1]=mQuantity;
		
		int br=mRow/BS;
		int bc=mCol/BS;
		bucket[br][bc].add(base);
		
		for(int r=-1;r<=1;r++) {
			for(int c=-1;c<=1;c++) {
				int nr=br+r;
				int nc=bc+c;
				if(nr<0 || nr>=BC || nc<0 || nc>=BC) continue;
				for(bcInfo baseCamp:bucket[nr][nc]) {
					int mr=baseCamp.r;
					int mc=baseCamp.c;
					if(baseCamp.id==base.id) continue;//나 자신 만나면 패스 
					int length=Math.abs(mRow-mr)+Math.abs(mCol-mc);
					if(length<=L) {
						int otherIdx=idToIdx.get(baseCamp.id);
						union(idx,otherIdx);
					}
				}
			}
		}
		return Sum[find(idx)];
	}
	
	int findBaseCampForDropping(int K){
		int bestId=-1;
//		int min=Integer.MAX_VALUE;
		bcInfo best=null;
		
		//K이상인 것중에 젤 수집 가능 광물 최댓값이 가장 작은 그룹의 First
		for(bcInfo bc:bcMap.values()) {
			int idx=idToIdx.get(bc.id);
			int sum=Sum[find(idx)];
			
			if(sum<K) continue;

			if(best==null) {
				best=bc;
				bestId=bc.id;
			}else {
				if(bc.q<best.q) {
					best=bc;
					bestId=bc.id;
				}else if(bc.q==best.q) {
					if(bc.r<best.r) {
						best=bc;
						bestId=bc.id;
					}else if(bc.r==best.r) {
						if(bc.c<best.c) {
							best=bc;
							bestId=bc.id;
						}
					}
				}
			}
		}
		return bestId;
	}
}
