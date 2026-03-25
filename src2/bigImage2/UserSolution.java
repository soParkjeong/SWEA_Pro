package bigImage2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class UserSolution {
	int N;
	HashMap<Integer,smallInfo>smallMap;
	
	static class smallInfo{
		int id;
		int size;
//		ArrayList<int[]> pixelMap;
		int[][] pixelSum;//소형이미지 id별 누적합 
//		int pixelSum;
		smallInfo(int id,int size,int[][]pixelSum){
			this.id=id;
			this.size=size;
			this.pixelSum=pixelSum;
//			this.pixelSum=pixelSum;
		}
	}
	
//	ArrayList<PrintInfo>[][]bucket;	
	
	static class PrintInfo{//프린트하는 소형이미지
		int id;//소형이미지 id
		int r;//소형이미지가 프린트될 대형이미지 내 r
		int c;//소형이미지가 프린트될 대형이미지 내 c
		int dir;
		
		PrintInfo(int id,int r,int c,int dir){
			this.id=id;
			this.r=r;
			this.c=c;
			this.dir=dir;
		}
	}
	
	ArrayList<PrintInfo> printed;//인쇄된 소형이미지 
//	
//	int BS;
//	int BC;
//	
//	boolean[] seen;
	
	void init(int N){
		this.N=N;
//		BS=50;
//		BC=(N+BS-1)/BS;
		
		smallMap=new HashMap<>();
		printed=new ArrayList<>();
	}

	void addPrint(int mID, int mSize, int mCnt, int mPixel[][]){
		int[][]print=new int[mSize][mSize];//인쇄할 소형이미지 
		
		for(int i=0;i<mCnt;i++) {
			int r=mPixel[i][0];
			int c=mPixel[i][1];
			int n=mPixel[i][2];
			print[r][c]=n;
		}
		
		int[][]ps=new int[mSize+1][mSize+1];
		
		for(int i=1;i<=mSize;i++) {
			for(int j=1;j<=mSize;j++) {
				ps[i][j]=print[i-1][j-1]+ps[i-1][j]+ps[i][j-1]-ps[i-1][j-1];
			}
		}
		
		smallMap.put(mID, new smallInfo(mID,mSize,ps));
	}

	//5000
	void pressPrint(int mID, int mRow, int mCol, int mDir){
		//여기서는 프린트되는 소형이미지들만 저장
		printed.add(new PrintInfo(mID,mRow,mCol,mDir));
	}

	int getDepth(int mRow, int mCol){
		int sum=0;
		
		for(PrintInfo p:printed) {
			smallInfo sm=smallMap.get(p.id);
			int size=sm.size;
			int[][]ps=sm.pixelSum;//소형 이미지 누적합
			
			//일단 대형이미지 내에 검색 범위에 겹치는 인쇄된 소형 이미지 영역을 찾자
			int bigsr=Math.max(mRow, p.r);
			int biger=Math.min(mRow+25, p.r+size);
			int bigsc=Math.max(mCol, p.c);
			int bigec=Math.min(mCol+25, p.c+size);
			
			if(bigsr>=biger || bigsc>=bigec) continue;//안 겹치는 부분은 패스
			
			//지금은 대형이미지 위에 찍힌 소형이미지 좌표라 대형이미지 기준 좌표지만 누적합은 소형이미지 좌표기준이니까 소형이미지 기준으로 변환
			//겹치는 부분만 소형이미지 좌표로 변환 
			int ssr=bigsr-p.r;
			int ssc=bigsc-p.c;
			int ser=biger-p.r;
			int sec=bigec-p.c;
			
			int rs,re,cs,ce;
			
			if(p.dir==0) {
				rs=ssr;
				re=ser;
				cs=ssc;
				ce=sec;
			}else if(p.dir==1) {
				rs=ssc;
				re=sec;
				cs=size-ser;
				ce=size-ssr;
			}else if(p.dir==2) {
				rs=size-ser;
				re=size-ssr;
				cs=size-sec;
				ce=size-ssc;
			}else {
				rs=size-sec;
				re=size-ssc;
				cs=ssr;
				ce=ser;
			}
			sum+=ps[re][ce]-ps[rs][ce]-ps[re][cs]+ps[rs][cs];
		}
		return sum;
	}
}