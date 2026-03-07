package callTaxi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int M;
	int L;
	int bucketSize;
	int W;
	
	int moveSum;//손님 태우고 총 이동 거리
	int rideSum;//총 운행거리
	
	List<Taxi>[][] bucket;//버킷 정의 
	
	TreeSet<Taxi> taxiSet;//moveSum 상위 5대 택시를 뽑기 위한 택시 저장소- 총 운행거리는 따로 저장해두었다가 getBest함수 호출하면 그 때 이 treeSet에 저장하고 상위 5개 뽑자 
	//어차피 택시 많아봤자 2,000개니까 getBest함수 호출될 때 최대 2,000번 돌면서 여기에 넣고 정렬해서 뽑는게 맞을 것 같음
	
	Comparator<Taxi> taxiComparator=(a,b)->{
		if(a.moveSum!=b.moveSum) return Integer.compare(b.moveSum,a.moveSum);
		return Integer.compare(a.id,b.id);
	};
	
	HashMap<Integer,Taxi> taxiMap;//택시 id 기반 정보 저장소 
	
	static class Taxi{
		int id;
		int x,y;
		int moveSum;
		int rideSum;
		Taxi(int id,int x,int y,int moveSum,int rideSum){
			this.id=id;
			this.x=x;
			this.y=y;
			this.moveSum=moveSum;
			this.rideSum=rideSum;
		}
	}

	//버킷 인덱싱 함수
	int bx(int x) {return x/bucketSize;}
	int by(int y) {return y/bucketSize;}
	
	public void init(int N, int M, int L, int[] mXs, int[] mYs)
	{
		this.N=N;
		this.M=M;
		this.L=L;
		
		bucketSize=L;//버킷 사이즈
		
		this.W=N/bucketSize;
		bucket=new ArrayList[W][W];
		for(int i=0;i<W;i++) {
			for(int j=0;j<W;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}
		
		taxiSet=new TreeSet<>(taxiComparator);
		
		taxiMap=new HashMap<>();
		
		for(int i=0;i<M;i++) {
			int x=mXs[i];
			int y=mYs[i];
			
			Taxi taxi=new Taxi(i+1,x,y,0,0);
			
			taxiMap.put(i+1, taxi);
			taxiSet.add(taxi);
			bucket[bx(x)][by(y)].add(taxi);
		}
		
		return;
	}

	int caldist(int tx,int ty,int cx,int cy) {
		return Math.abs(tx-cx)+Math.abs(ty-cy);
	}
	
	//택시 버킷 간 이동 함수 
	void moveTaxi(int id,int ntx,int nty) {
		//호출 당시 택시 좌표
		int otx=taxiMap.get(id).x;
		int oty=taxiMap.get(id).y;
		
		int obi=bx(otx), obj=by(oty);//호출 당시 택시 좌표가 속한 버킷 행,열
		int nbi=bx(ntx), nbj=by(nty);//도착 당시 택시 좌표가 속한 버킷 행,열
		
		if(obi!=nbi || obj!=nbj) {
			Taxi taxi=taxiMap.get(id);
			bucket[obi][obj].remove(taxi);
			bucket[nbi][nbj].add(taxi);
		}
		
		//목적지 도착 택시 좌표: ntx, nty
		taxiMap.get(id).x=ntx;
		taxiMap.get(id).y=nty;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY)
	{
		int nearId=-1;//가장 가까운 택시 id
		int nearDist=Integer.MAX_VALUE;
		
		int ci=bx(mSX);//손님 위치 
		int cj=by(mSY);
		
		//손님이 있는 버킷에서 검색 반경(L)에 속하는 버킷들 쭉 반경 검색하기 
		int r=(L+bucketSize-1)/bucketSize;

		for(int bi=ci-r;bi<=ci+r;bi++) {
			if(bi<0||bi>=W) continue;//양 끝 버킷 넘기면 안됨
			for(int bj=cj-r;bj<=cj+r;bj++) {
				if(bj<0||bj>=W) continue;
				
				for(Taxi taxi:bucket[bi][bj]) {//한 버킷 내의 모든 택시 좌표 갖고 손님 좌표와 거리 비교해서 젤 가까운 거리 찾아내기 
					int tx=taxi.x;
					int ty=taxi.y;
					int dist=caldist(tx,ty,mSX,mSY);
					if(dist>L) continue;
					
					int id=taxi.id;
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
		
		Taxi taxi=taxiMap.get(nearId);
		
		taxiSet.remove(taxi);//지우고
		taxi.moveSum+=move;//이동 거리 갱신
		taxi.rideSum+=total;//운행 거리 갱신
		taxiSet.add(taxi);//다시 재정렬 
		
		moveTaxi(nearId,mEX,mEY);//손님 목적지에 맞게 버킷에서도 택시 이동 완
		
		return nearId;
	}
	

	public Solution.Result reset(int mNo)
	{
		Solution.Result res = new Solution.Result();
		
		Taxi taxi=taxiMap.get(mNo);
		
		res.mX=taxi.x;
		res.mY=taxi.y;
		res.mMoveDistance=taxi.rideSum;
		res.mRideDistance=taxi.moveSum;
		
		taxiSet.remove(taxi);//값이 바뀌었으니 재정렬하려면 제거해야지 
		taxi.moveSum=0;
		taxi.rideSum=0;
		taxiSet.add(taxi);
		
		return res;
	}

	public void getBest(int[] mNos)
	{
		int idx=0;
		for(Taxi taxi:taxiSet) {
			mNos[idx++]=taxi.id;
			if(idx==5) break;
		}
	}
}