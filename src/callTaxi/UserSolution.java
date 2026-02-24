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
	int bx(int x) {return x/S;}
	int by(int y) {return y/S;}
	
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
			long ma=moveSum[a];
			long mb=moveSum[b];
			if(ma!=mb) return Long.compare(mb,ma);
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

	int caldist(int tx,int ty,int cx,int cy) {
		return Math.abs(tx-cx)+Math.abs(ty-cy);
	}
	
	//택시 버킷 간 이동 함수 
	void moveTaxi(int id,int ntx,int nty) {
		//호출 당시 택시 좌표
		int otx=tx[id];
		int oty=ty[id];
		
		//목적지 도착 택시 좌표: ntx, nty
		
		int obi=bx(otx), obj=by(oty);//호출 당시 택시 좌표가 속한 버킷 행,열
		int nbi=bx(ntx), nbj=by(nty);//도착 당시 택시 좌표가 속한 버킷 행,열
		
		if(obi!=nbi || obj!=nbj) {
			bucket[obi][obj].remove(id);
			bucket[nbi][nbj].add(id);
		}
		
		tx[id]=ntx;
		ty[id]=nty;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY)
	{
		int nearId=-1;//가장 가까운 택시 id
		int nearDist=Integer.MAX_VALUE;
		
		int ci=bx(mSX);//손님 위치 
		int cj=by(mSY);
		
		//손님이 있는 버킷에서 검색 반경(L)에 속하는 버킷들 쭉 반경 검색하기 
		int r=(L+S-1)/S;
		for(int bi=ci-r;bi<=ci+r;bi++) {
			if(bi<0||bi>=W) continue;//양 끝 버킷 넘기면 안됨
			for(int bj=cj-r;bj<=cj+r;bj++) {
				if(bj<0||bj>=W) continue;
				
				for(int id:bucket[bi][bj]) {//한 버킷 내의 모든 택시 좌표 갖고 손님 좌표와 거리 비교해서 젤 가까운 거리 찾아내기 
					int dist=caldist(tx[id],ty[id],mSX,mSY);
					if(dist>L) continue;
					
					if(dist<nearDist||(dist==nearDist && id<nearId)) {//작거나 거리는 같은데 아이디가 작으면 최적 택시로 갱신 
						nearDist=dist;
						nearId=id;
					}
				}
			}
			
		}
		if(nearId==-1) return -1;
		
		//자 호출할 택시 정했으니까 이제 택시가 얼만큼 움직였는지 계산 
		int move=caldist(mSX,mSY,mEX,mEY);//손님이 이동한 거리 
		int total=move+nearDist;//택시 총 운행 거리
		
		bestCand.remove(nearId);
		moveSum[nearId]+=move;
		totalSum[nearId]+=total;
		bestCand.add(nearId);
		
		moveTaxi(nearId,mEX,mEY);//손님 목적지로 택시 이동 완
		
		return nearId;
	}
	

	public Solution.Result reset(int mNo)
	{
		Solution.Result res = new Solution.Result();
		
		res.mX=tx[mNo];
		res.mY=ty[mNo];
		res.mMoveDistance=(int)totalSum[mNo];
		res.mRideDistance=(int)moveSum[mNo];
		
		bestCand.remove(mNo);//값이 바뀌었으니 재정렬하려면 제거해야지 
		moveSum[mNo]=0;
		totalSum[mNo]=0;
		bestCand.add(mNo);
		
		return res;
	}

	public void getBest(int[] mNos)
	{
		int idx=0;
		for(int id:bestCand) {
			mNos[idx++]=id;
			if(idx==5) break;
		}
	}
}