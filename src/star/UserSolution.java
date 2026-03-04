package star;

import java.util.ArrayList;

class UserSolution {
	int N;
	
	int[][]city;
	//mPlane 배열에 저장해야 할 것 
	//별 유무
	StringBuilder sb=new StringBuilder();
	
	//별자리 타일에 대해 저장할 것
	//끝점(re,ce), 타일종류, 4가지 버전의 sb
	//
	
	public void init(int N, int mPlane[][])
	{
		this.N=N;
		city=new int[N][N];
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				city[i][j]=mPlane[i][j];
			}
		}//city에 별자리 타일 다 배치해놓기 
		
		
		
	}

	public int getCount(int mPiece[][])
	{   
		this.mPiece=mPiece;
		//젤 위,왼을 (0,0)기반으로 
		//쁠마,
		
		//젤 위,왼쪽 (r,c)
		for(int r=0;r<N-5;r++) {
			for(int c=0;c<N-5;c++) {
				
				}
			}
		}
		return 0;
	}

	public int getPosition(int mRow, int mCol)
	{
		return 0;
	}
}