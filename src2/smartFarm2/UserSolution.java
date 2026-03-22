package smartFarm2;

import java.util.ArrayList;
import java.util.Arrays;

class UserSolution {
	int N;
	int[][] farm;
	int[] catGrowth;//품종별 성장 시간 
	
	static class cropInfo{
		int r;
		int c;
		int cat;//품종
		int wGrowth;//급수에 따른 성장
		int mGrowth;//시간에 따른 성장
		int sowTime;//심어진 시간
		cropInfo(int r,int c,int cat,int wGrowth,int mGrowth,int sowTime){
			this.r=r;
			this.c=c;
			this.cat=cat;
			this.wGrowth=0;//객체별로 저장하는 경우는 걸치는 애들 
			this.mGrowth=mGrowth;
			this.sowTime=sowTime;
		}
	}
	
	int[][]water;//버킷별 누적 급수 저장용  
	ArrayList<cropInfo>[][] bucket;//harvest시점에만 누적 급수 반영해서 계산 
	
	int BS;
	int BC;
	
	void init(int N, int mGrowthTime[])	{
		this.N=N;
		catGrowth=new int[3];
		for(int i=0;i<3;i++) {
			catGrowth[i]=mGrowthTime[i];
		}
		
		farm=new int[N][N];
		for(int i=0;i<N;i++) {
			Arrays.fill(farm[i], -1);
		}
		water=new int[BC][BC];
		
		BS=100;
		BC=(N+BS-1)/BS;
		
		bucket=new ArrayList[BC][BC];
		for(int i=0;i<BC;i++) {
			for(int j=0;j<BC;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}
	}

	int sow(int mTime, int mRow, int mCol, int mCategory) {
		int br=mRow/BS;
		int bc=mCol/BS;
		int cat=mCategory;
		
		cropInfo crop=new cropInfo(mRow,mCol,cat,0,catGrowth[cat],mTime);
		if(farm[mRow][mCol]==-1) {//비어있다면 
			farm[mRow][mCol]=cat;//맵에 추가 
			bucket[br][bc].add(crop);//버킷에 추가
			
			return 1;
		}		
		return 0;
	}

	int water(int mTime, int G, int mRow, int mCol, int mHeight, int mWidth) {
		//버킷 별로 급수 누적 저장
		//일단 버킷이 급수 구역에 어떻게 걸쳐진지 체크
		//일단 검색 범위 버킷 인덱스 체크
		int brs=mRow/BS;
		int bcs=mCol/BS;
		int bre=(mRow+mHeight-1)/BS;
		int bce=(mCol+mWidth-1)/BS;
		
		int cnt=0;//작물 수 반환 
		
		for(int br=brs;br<=bre;br++) {
			for(int bc=bcs;bc<=bce;bc++) {
				int rs=br*BS;
				int cs=bc*BS;
				int re=Math.min(N-1, rs+BS-1);
				int ce=Math.min(N-1, cs+BS-1);
				
				boolean full=false;
				if(rs>=mRow && re<=mRow+mHeight-1 && cs>=mCol && ce<=mCol+mWidth-1) {
					full=true;
				}
				
				if(full) {
					water[br][bc]+=G;//급수 누적 저장
					cnt+=bucket[br][bc].size();
				}else {
					for(cropInfo crop:bucket[br][bc]) {
						int r=crop.r;
						int c=crop.c;
						if(r>=mRow && r<mRow+mHeight && c>=mCol && c<mCol+mWidth) {
							crop.wGrowth+=G;
							cnt++;
						}
					}
				}
			}
		}
		return cnt;
	}

	int harvest(int mTime, int L, int mRow, int mCol, int mHeight, int mWidth) {
		int brs=mRow/BS;
		int bcs=mCol/BS;
		int bre=(mRow+mHeight-1)/BS;
		int bce=(mCol+mWidth-1)/BS;
		
		int cnt=0;
		
		for(int br=brs;br<=bre;br++) {
			for(int bc=bcs;bc<=bce;bc++) {
				int rs=br*BS;
				int cs=bc*BS;
				
				int re=Math.min(N-1, rs+BS-1);
				int ce=Math.min(N-1, cs+BS-1);
				
				boolean full=false;
				if(rs>=brs && re<=bre && cs>=bcs && ce<=bce) full=true;//쏙 들어감
				
				int length=0;
				if(full) {//쏙 들어가면
					for(cropInfo crop:bucket[br][bc]) {
						length=(mTime-crop.sowTime)/crop.mGrowth+water[br][bc];
						System.out.print("L:"+L+" | "+length+",");
						if(length<L) {
							return 0;
						}
					}
					cnt+=bucket[br][bc].size();
				}else {
					for(cropInfo crop:bucket[br][bc]) {
						int r=crop.r;
						int c=crop.c;
						if(r>=mRow && r<mRow+mHeight && c>=mCol && c<mCol+mWidth) {
							length=(mTime-crop.sowTime)/crop.mGrowth+crop.wGrowth;
							System.out.print("L:"+L+" | "+length+",");
							if(length<L) {
								return 0;
							}
							cnt++;
						}
					}
				}
				
				if(full) {//쏙 들어가면
					for(cropInfo crop:bucket[br][bc]) {
						int r=crop.r;
						int c=crop.c;
						farm[r][c]=-1;
					}
					bucket[br][bc].clear();//그 버킷 싹 비워주기 
				}else {
					int size=bucket[br][bc].size();
					for(int i=size;i>=0;i--) {
						cropInfo crop=bucket[br][bc].get(i);
						int r=crop.r;
						int c=crop.c;
						if(r>=mRow && r<mRow+mHeight && c>=mCol && c<mCol+mWidth) {
							farm[r][c]=-1;
							bucket[br][bc].remove(crop);
						}
					}
				}
			}
		}
		return cnt;
	}
}
