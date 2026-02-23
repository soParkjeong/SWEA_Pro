package callTaxi;

import java.util.HashSet;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int M;
	int L;
	int S;//버킷 사이즈
	int W;//도시 한 변을 버킷으로 쪼갤 때 몇 버킷 나오는지 
	
	int[] tx,ty;//택시 좌표
	
	long[] moveSum;//손님 태우고 총 이동 거리
	long[] totalSum;//총 운행거리
	
	HashSet<Integer>[][] bucket;
	
	TreeSet<Integer> bestCand;//moveSum 상위 5대 택시를 뽑기 위한 택시 저장소- 총 운행거리는 따로 저장해두었다가 getBest함수 호출하면 그 때 이 treeSet에 저장하고 상위 5개 뽑자 
	//어차피 택시 많아봤자 2,000개니까 getBest함수 호출될 때 최대 2,000번 돌면서 여기에 넣고 정렬해서 뽑는게 맞을 것 같음
	

	//버킷 인덱싱 함수
	int bx(int x) return x/S;
	int by(int y) return y/S;
	
	public void init(int N, int M, int L, int[] mXs, int[] mYs)
	{
		this.N=N;
		this.M=M;
		this.L=L;
		
		this.S=L;//버킷 사이즈
		this.W=N/S;
		
		//택시 좌표
		tx=new int[M+1];
		ty=new int[M+1];
		
		moveSum=new long[M+1];
		totalSum=new long[M+1];
		
		bucket=new HashSet[W][W];
		for(int i=0;i<W;i++) {
			for(int j=0;j<W;j++) {
				bucket[i][j]=new HashSet<>();
			}
		}
		
		bestCand=new TreeSet<>((a,b)->{
			if(a==b) return 0;
			long ta=totalSum[a];
			long tb=totalSum[b];
			if(ta!=tb) return Long.compare(tb,ta);
			return Long.compare(a, b);
		});
		
		for(int id=1;id<=M;id++) {
			tx[id]=mXs[id-1];//0 base라 -1 해주기
			ty[id]=mYs[id-1];
			moveSum[id]=0;
			totalSum[id]=0;
			
			bucket[bx(tx[id])][by(ty[id])].add(id);
			bestCand.add(id);//처음에 택시 쫙 넣어놓기
		}
		return;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY)
	{
		return -1;
	}

	public Solution.Result reset(int mNo)
	{
		Solution.Result res = new Solution.Result();

		return res;
	}

	public void getBest(int[] mNos)
	{
		return;
	}
}