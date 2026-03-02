package MineralTransportation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

class UserSolution {
	int L;
	int N;
	HashMap<Integer,bInfo> baseCamp;
	
	int[][] island;
	
	static class bInfo{
		int id;
		int r;
		int c;
		int amount;
		bInfo(int id,int r,int c,int amount){
			this.id=id;
			this.r=r;
			this.c=c;
			this.amount=amount;
		}
	}
	
	ArrayList<Integer>[][]bucket;
	int S;
	int W;
	
	int br(int r) {return r/S;}
	int bc(int c) {return c/S;}
	
	Comparator<bInfo> bcComparator=(a,b)->{
		if(a.amount!=b.amount) {
			return Integer.compare(a.amount,b.amount);
		}else if(a.r!=b.r) {
			return Integer.compare(a.r,b.r);
		}return Integer.compare(a.c,b.c);
	};
	
	
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
		
		baseCamp=new HashMap<>();
		island=new int[N][N];
	}
	
	//버킷 사이즈:L로 두고 그 베이스캠프 주위 버킷만 싹 훑기 
	//그러고 노드 간 맨해튼 거리가 L이하인 곳으로만 이동 가능-> 그러면 베이스 캠프간 거리가 L이하인 곳만 간선 연결해두자 /다른 노드로 이동하면 다시 L로 차량 에너지 잔량 복구
	//같은 덩어리(채굴 가능한 최대 광물 개수가 동일한 애들끼리 한 덩어리) 속하는 노드인지 판단- 유니온파인드? 
	//중복으로 지나가도 되니까 양방향 간선 연결 
	int addBaseCamp(int mID, int mRow, int mCol, int mQuantity){
		bInfo bc = new bInfo(mID, mRow, mCol, mQuantity);
		baseCamp.put(mID,bc);
		
		island[mRow][mCol]=mID;
		
		//일단 추가된 얘가 어디 버킷에 속하노
		int cr=br(mRow);
		int cc=bc(mCol);
		
		
		
		return 0;
	}

	int findBaseCampForDropping(int K){
		return 0;
	}

}
