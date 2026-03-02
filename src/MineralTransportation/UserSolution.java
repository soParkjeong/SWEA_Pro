package MineralTransportation;

import java.util.ArrayList;
import java.util.HashMap;

class UserSolution {
	int L;
	int N;
//	HashMap<Integer,bInfo> baseCamp;
//	
//	static class bInfo{
//		int id;
//		int r;
//		int c;
//		int amount;
//		bInfo(int id,int r,int c,int amount){
//			this.id=id;
//			this.r=r;
//			this.c=c;
//			this.amount=amount;
//		}
//	}
	
	ArrayList<Integer>[][]bucket;
	int S;
	int W;
	
	int br(int r) {return r/S;}
	int bc(int c) {return c/S;}

	int campCnt;
	HashMap<Integer,Integer> idToIdx;
	
	//덩어리(채굴 가능한 최대 광물 개수가 동일한 애들끼리 한 덩어리)끼리 묶기 위한 유니온파인드
	int[] parent=new int[20001];
	int[] maxSum=new int[20001];
	
	//유니온파인드를 위해서는 어차피 인덱싱을 해야하니까 처음부터 ID기반이 아니라 인덱스형태로 저장해두기! 
	int[]rArr=new int[20001];
	int[]cArr=new int[20001];
	int[]amtArr=new int[20001];
	int[]idArr=new int[200001];
	
	int find(int x) {
		if(parent[x]==x) return x;
		return parent[x]=find(parent[x]);
	}
	
	void union(int a,int b) {
		int ra=find(a);
		int rb=find(b);
		
		if(ra==rb) return;//이미 같은 루트면 끝 
		
		parent[rb]=ra;
		maxSum[ra]+=maxSum[rb];
	}
	
	void init(int L, int N){
		this.L=L;//한번에 움직일 수 있는 에너지를 검색 반경으로 두자 
		this.N=N;
		
		this.S=L;
		this.W=(N+S-1)/S;
		
		bucket=new ArrayList[W][W];
		for(int r=0;r<W;r++) {
			for(int c=0;c<W;c++) {
				bucket[r][c]=new ArrayList<>();
			}
		}
		
//		baseCamp=new HashMap<>();
//		idToIdx=new HashMap<>();
		campCnt=0;
	}
	
	//이거 섬 한변 길이 최대 1,5000임-> 버킷 써서 탐색해야지 아니면 백퍼 터짐 
	//버킷 사이즈:L로 두고 그 베이스캠프 주위 버킷만 싹 훑기 
	//같은 덩어리(채굴 가능한 최대 광물 개수가 동일한 애들끼리 한 덩어리) 속하는 노드인지 판단- 유니온파인드? 
	int addBaseCamp(int mID, int mRow, int mCol, int mQuantity){
		int idx=campCnt++;
		idArr[idx]=mID;
//		idToIdx.put(mID, idx);
		
		rArr[idx]=mRow;
		cArr[idx]=mCol;
		amtArr[idx]=mQuantity;
		
		parent[idx]=idx;
		maxSum[idx]=mQuantity;
		
//		bInfo bc = new bInfo(mID, mRow, mCol, mQuantity);
//		baseCamp.put(mID,bc);
		
		//일단 추가된 얘가 어디 버킷에 속하노
		int cr=br(mRow);
		int cc=bc(mCol);
		
		for(int dr=-1;dr<=1;dr++) {
			int nr=cr+dr;
			if(nr<0||nr>=W) continue;
			
			for(int dc=-1;dc<=1;dc++) {
				int nc=cc+dc;
				if(nc<0||nc>=W) continue;
				
				for(int otherIdx:bucket[nr][nc]) {
//					bInfo other=baseCamp.get(otherId);
					
					int dist=(Math.abs(rArr[otherIdx]-mRow)+Math.abs(cArr[otherIdx]-mCol));
					if(dist<=L) {//L이내면 
//						int otherIdx=idToIdx.get(otherId);
						union(idx,otherIdx);//그래프 합치자(루트 연결 ㄱ)
					}
				}
			}
		}
		bucket[cr][cc].add(idx);
		
		return maxSum[find(idx)];
	}

	int findBaseCampForDropping(int K){
		int bestIdx=-1;
//		bInfo best=null;
		
		for(int i=0;i<campCnt;i++) {
//			int idx=idToIdx.get(bc.id);
			int total=maxSum[find(i)];
			
			if(total<K) continue;
			
			if (bestIdx==-1) {
//			    best = bc;
//			    bestId = bc.id;
				bestIdx=i;
			} 
			else {
			    if (amtArr[i]<amtArr[bestIdx]) {
			    	bestIdx=i;
			    }else if(amtArr[i]==amtArr[bestIdx]) {
			    	if(rArr[i]<rArr[bestIdx]) {
			    		bestIdx=i;
			    	}else if(rArr[i]==rArr[bestIdx]) {
			    		if(cArr[i]<cArr[bestIdx]) {
			    			bestIdx=i;
			    		}
			    	}
			    }
			}
		}
		return bestIdx == -1 ? -1 : idArr[bestIdx];
	}
}
