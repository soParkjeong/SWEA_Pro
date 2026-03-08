package bigImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class UserSolution {
	int N;
	
	//전역 자료구조
	HashMap<Integer,Small> smallMap;
	
	static class Small{
		int id;
		int size;
		ArrayList<Pixel> pixelMap;
		
		Small(int id,int size){
			this.id=id;
			this.size=size;
			this.pixelMap=new ArrayList<>();
		}
	}
	
	static class Pixel{
		int r;
		int c;
		int dense;
		
		Pixel(int r,int c,int dense){
			this.r=r;
			this.c=c;
			this.dense=dense;
		}
	}
	
	//인쇄된 소형이미지 
	static class Print{
		int id;
		int r,c;
		int dir;
		
		Print(int id,int r,int c,int dir){
			this.id=id;
			this.r=r;
			this.c=c;
			this.dir=dir;
		}
	}
	
	//버킷 정의
	int bs=100;//버킷 사이즈-어차피 여러 버킷에 걸칠 것 같으니까 100으로 잡고 시간 보면서 조절해보자
	int bCnt;//한변에 버킷 몇 개 들어가? //bCnt=(N+bs-1)/bs;
	
    ArrayList<Print>[][] buckets;
    
	void init(int N){
		this.N=N;
		this.bCnt=(N+bs-1)/bs;
		
		smallMap=new HashMap<>();
		
		buckets=new ArrayList[bCnt][bCnt];
		
		for(int r=0;r<bCnt;r++) {
			for(int c=0;c<bCnt;c++) {
				buckets[r][c]=new ArrayList<>();
			}
		}
	}

	void addPrint(int mID, int mSize, int mCnt, int mPixel[][]){
		Small sm=new Small(mID,mSize);
		
		for(int k=0;k<mCnt;k++) {
			sm.pixelMap.add(new Pixel(mPixel[k][0],mPixel[k][1],mPixel[k][2]));
		}
		
		smallMap.put(mID, sm);
		
	}
	
	int rotateR(int r,int c,int size,int dir) {
		if(dir==0) return r;
		if(dir==1) return size-1-c;
		if(dir==2) return size-1-r;
		return c;
	}
	
	int rotateC(int r,int c,int size,int dir) {
		if(dir==0) return c;
		if(dir==1) return r;
		if(dir==2) return size-1-c;
		return size-1-r;
	}
	
	void pressPrint(int mID, int mRow, int mCol, int mDir){
		Small sm=smallMap.get(mID);
		int size=sm.size;
		
		Print pt=new Print(mID,mRow,mCol,mDir);
		
		int x1=mRow;
		int x2=mRow+size-1;
		int y1=mCol;
		int y2=mCol+size-1;
		
		int bx1=x1/bs;
		int bx2=x2/bs;
		int by1=y1/bs;
		int by2=y2/bs;
		
		for(int br=bx1;br<=bx2;br++) {
			for(int bc=by1;bc<=by2;bc++) {
				buckets[br][bc].add(pt);
			}
		}
	}

	int getDepth(int mRow, int mCol){
		
		//검색반경:(mRow,mCol)~(mRow+24,mCol+24)의 정사각형
		int x1=mRow;
		int x2=mRow+24;
		int y1=mCol;
		int y2=mCol+24;
		
		int bx1=x1/bs;
		int bx2=x2/bs;
		int by1=y1/bs;
		int by2=y2/bs;
		
		int result=0;
		
		HashSet<Print> seen= new HashSet<>();//이미 본 인쇄이미지인가?/이미 본 애면 패스용 
		
		for(int br=bx1; br<=bx2; br++) {
			for(int bc=by1; bc<=by2; bc++) {
				for(Print pt:buckets[br][bc]) {
					
					boolean isSeen=seen.contains(pt);//이미 본 애야?
					
					if(!isSeen) {//안 본 애면
						seen.add(pt);  
					}else {
						continue;
					}
                    
                    Small sm=smallMap.get(pt.id);
                    int size=sm.size;
                    
                    int pr1 = pt.r;
                    int pr2 = pt.r + size - 1;
                    int pc1 = pt.c;
                    int pc2 = pt.c + size - 1;

                    if (pr2 < x1 || pr1 > x2 || pc2 < y1 || pc1 > y2) continue;

                    for (Pixel px : sm.pixelMap) {
                    	//대형 이미지 인쇄할 때 인쇄했던 방향 반영해서 농도 누적 ㄱ
                        int rr = rotateR(px.r, px.c, size, pt.dir);
                        int cc = rotateC(px.r, px.c, size, pt.dir);

                        int gr = pt.r + rr;
                        int gc = pt.c + cc;

                        if (gr >= x1 && gr <= x2 && gc >= y1 && gc <= y2) {
                            result += px.dense;
                        } 
                    }
				}
			}
		}
		return result;
	}
}