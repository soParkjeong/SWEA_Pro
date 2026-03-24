package bigImage2;

import java.util.ArrayList;
import java.util.HashMap;

class UserSolution {
	int N;
	HashMap<Integer,smallInfo>smallMap;
//	int[][] bigMap;//대형 이미지
	
	static class smallInfo{
		int id;
		int size;
		ArrayList<int[]> pixelMap;
		smallInfo(int id,int size,ArrayList<int[]>pixelMap){
			this.id=id;
			this.size=size;
			this.pixelMap=pixelMap;
		}
	}
	
	ArrayList<int[]>[][]bucket;
	int BS;
	int BC;
	
	void init(int N){
		this.N=N;
		BS=50;
		BC=(N+BS-1)/BS;
		
//		bigMap=new int[N][N];
		smallMap=new HashMap<>();
		
		bucket=new ArrayList[BC][BC];
		for(int i=0;i<BC;i++) {
			for(int j=0;j<BC;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}
	}

	void addPrint(int mID, int mSize, int mCnt, int mPixel[][]){
		//자 일단 smallMap에 잘 저장해보자
		ArrayList<int[]>pixelMap=new ArrayList<>();
		for(int i=0;i<mCnt;i++) {
			pixelMap.add(new int[] {mPixel[i][0],mPixel[i][1],mPixel[i][2]});
		}
		smallInfo sm=new smallInfo(mID,mSize,pixelMap);
		smallMap.put(mID, sm);//소형이미지 맵에 저장 
	}

	void pressPrint(int mID, int mRow, int mCol, int mDir){
		//여기서 버킷에 넣어야 할듯?!
		//회전을 해야한다면 회전 반영해서...
		smallInfo sm=smallMap.get(mID);
		int pixelCnt=sm.pixelMap.size();
		int ssize=sm.size;
		
		//픽셀을 대형 이미지의 버킷에 담자
		if(mDir==0) {
			for(int p=0;p<pixelCnt;p++) {
				int x=sm.pixelMap.get(p)[0];
				int y=sm.pixelMap.get(p)[1];
				int n=sm.pixelMap.get(p)[2];
//				bigMap[x][y]+=n;//대형 이미지 맵에도 농도 저장 
				int bx=x+mRow;
				int by=y+mCol;
				bucket[bx/BS][by/BS].add(new int[] {bx,by,n});//버킷에 넣기 
			}
		}else if(mDir==1) {
			for(int p=0;p<pixelCnt;p++) {
				int x=ssize-1-sm.pixelMap.get(p)[1];
				int y=sm.pixelMap.get(p)[0];
				int n=sm.pixelMap.get(p)[2];
//				bigMap[x][y]+=n;//대형 이미지 맵에도 농도 저장
				int bx=x+mRow;
				int by=y+mCol;
				bucket[bx/BS][by/BS].add(new int[] {bx,by,n});//버킷에 넣기 
			}
		}else if(mDir==2){
			for(int p=0;p<pixelCnt;p++) {
				int x=ssize-1-sm.pixelMap.get(p)[0];
				int y=ssize-1-sm.pixelMap.get(p)[1];
				int n=sm.pixelMap.get(p)[2];
//				bigMap[x][y]+=n;//대형 이미지 맵에도 농도 저장 
				int bx=x+mRow;
				int by=y+mCol;
				bucket[bx/BS][by/BS].add(new int[] {bx,by,n});//버킷에 넣기 
			}
		}else if(mDir==3) {
			for(int p=0;p<pixelCnt;p++) {
				int x=sm.pixelMap.get(p)[1];
				int y=ssize-1-sm.pixelMap.get(p)[0];
				int n=sm.pixelMap.get(p)[2];
//				bigMap[x][y]+=n;//대형 이미지 맵에도 농도 저장 
				int bx=x+mRow;
				int by=y+mCol;
				bucket[bx/BS][by/BS].add(new int[] {bx,by,n});//버킷에 넣기 
			}
		}
	}

	int getDepth(int mRow, int mCol)
	{
		//검색범위가 버킷에 어케 걸쳐지는가...보자
		//일단 검색범위 양끝이 어디 버킷인가
		int brs=mRow/BS;
		int bcs=mCol/BS;
		int bre=(mRow+25-1)/BS;
		int bce=(mCol+25-1)/BS;
		
		int sum=0;
		for(int r=brs;r<=bre;r++) {
			for(int c=bcs;c<=bce;c++) {
				for(int[] pixel:bucket[r][c]) {
					int x=pixel[0];
					int y=pixel[1];
					if(x<mRow || x>mRow+25-1 || y<mCol || y>mCol+25-1) continue;
					sum+=pixel[2];
				}
			}
		}
		return sum;
	}
}