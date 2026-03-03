package smartFarm;

class UserSolution {
	int N;
	
	int gt0,gt1,gt2;
	
	int[][][] farm;//{mTime,mCategory,gt,gw}
	
	int curTime;//현재 시각(전역)
	
	void init(int N, int mGrowthTime[])	{
		this.N=N;
		
		farm=new int[N][N][4];
		
		for (int r = 0; r < N; r++) {
		    for (int c = 0; c < N; c++) {
		        farm[r][c][0] = -1;//심은 시간 
		        farm[r][c][1] = -1;//품종
		        farm[r][c][2] = 0;//시간에 따른 작물 크기
		        farm[r][c][3] = 0;//급수 횟수 
		    }
		}
		
		this.gt0=mGrowthTime[0];//mCategory==0
		this.gt1=mGrowthTime[1];//1
		this.gt2=mGrowthTime[2];//2
		
		curTime=0;
	}

	int sow(int mTime, int mRow, int mCol, int mCategory) {
		if(farm[mRow][mCol][0]!=-1) return 0;//안 심어져있으면 0
		
		farm[mRow][mCol][0]=mTime;//심은 시간 저장 
		farm[mRow][mCol][1]=mCategory;
	    farm[mRow][mCol][3]=0;
	    
		if(mCategory==0) farm[mRow][mCol][2]=gt0;
		else if(mCategory==1) farm[mRow][mCol][2]=gt1;
		else farm[mRow][mCol][2]=gt2;

		return 1;//정상적으로 심었으니까 1
	}
		
	int water(int mTime, int G, int mRow, int mCol, int mHeight, int mWidth) {
		int cnt=0;
		
		for(int r=0;r<mHeight;r++) {
			for(int c=0;c<mWidth;c++) {
				int nr=mRow+r;
				int nc=mCol+c;
				if(nr>=0&&nr<N&&nc>=0&&nc<N) {//범위 안이면 
					if(farm[nr][nc][0]!=-1) {//빈칸이 아니면
						farm[nr][nc][3]+=G;//급수 빔 
						cnt++;
					}
				}
			}
		}
		return cnt;
	}
	

	int harvest(int mTime, int L, int mRow, int mCol, int mHeight, int mWidth) {
	    int cnt = 0;

	    for (int r = 0; r < mHeight; r++) {
	        for (int c = 0; c < mWidth; c++) {
	            int nr = mRow + r;
	            int nc = mCol + c;

	            int sowTime = farm[nr][nc][0];
	            if (sowTime == -1) continue; // 빈칸 무시

	            int gt = farm[nr][nc][2];
	            int gw = farm[nr][nc][3];

	            int size = (mTime - sowTime) / gt + gw;
	            if (size < L) return 0;

	            cnt++;
	        }
	    }

	    //수확했으니 삭제 
	    for (int r = 0; r < mHeight; r++) {
	        for (int c = 0; c < mWidth; c++) {
	            int nr = mRow + r;
	            int nc = mCol + c;

	            if (farm[nr][nc][0] == -1) continue;

	            farm[nr][nc][0] = -1;
	            farm[nr][nc][1] = -1;
	            farm[nr][nc][2] = 0;
	            farm[nr][nc][3] = 0;
	        }
	    }
	    return cnt;
	}
}
