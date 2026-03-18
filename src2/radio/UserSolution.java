package radio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

class UserSolution {
	int N;
	int limit;
	int K;
	
	static class radioInfo{
		int num;
		int freq;
		int y;
		int x;
		radioInfo(int num,int freq,int y,int x){
			this.num=num;
			this.freq=freq;
			this.y=y;
			this.x=x;
		}
	}
	
	HashMap<Integer,radioInfo> radioMap;
	
	PriorityQueue<candInfo> cand;//mLimit안에 들어오는 무통들 담을 것 
	
	List<radioInfo>[][] bucket;
	int BS;
	int BC;
	
	static class candInfo{
		int power;
		int num;
		int y;
		int x;
		candInfo(int power,int num,int y,int x){
			this.power=power;
			this.num=num;
			this.y=y;
			this.x=x;
		}
	}
		
	Comparator<candInfo> comp=(a,b)->{
		if(a.power==b.power) return Integer.compare(a.num, b.num);
		return Integer.compare(a.power, b.power);
	};

	void init(int N, int mLimit){
		this.N=N;
		this.limit=mLimit;
		
		this.BS=limit/10;
		this.BC=(N+BS-1)/BS;
		
		bucket=new ArrayList[BC][BC];
		
		for(int i=0;i<BC;i++) {
			for(int j=0;j<BC;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}//버킷 생성 완 
		
		radioMap=new HashMap<>();//맵 생성
	}

	void addRadio(int K, int mID[], int mFreq[], int mY[], int mX[]){
		this.K=K;
		
		for(int k=0;k<K;k++) {
			radioInfo radio=new radioInfo(mID[k],mFreq[k],mY[k],mX[k]);
			radioMap.put(mID[k],radio);
//			System.out.print("("+"mID:"+mID[k]+", "+"mFreq:"+mFreq[k]+", "+"mY:"+mY[k]+", "+"mX:"+mX[k]+")");
			bucket[mY[k]/BS][mX[k]/BS].add(radio);//버킷에 추가
		}
//		System.out.println();
	}

	int getMinPower(int mID, int mCount){
		//트리셋 생성 
		cand=new PriorityQueue<>(comp);
		
		radioInfo radio=radioMap.get(mID);
		int freq=radio.freq;
		
		//mID 어느 버킷에?
		int bcy=radio.y/BS;
		int bcx=radio.x/BS;
		
		for(int i=-1;i<=1;i++) {
			for(int j=-1;j<=1;j++) {
				int nby=bcy+i;
				int nbx=bcx+j;
				if(nby<0||nby>=BC||nbx<0||nbx>=BC) continue;
				for(radioInfo nr:bucket[nby][nbx]) {
					if(nr.num==mID) continue;//최소니까 나 자신 패스 처리 필수 
					if(nr.freq!=freq) {
						int power=(Math.abs(radio.y-nr.y)+Math.abs(radio.x-nr.x))*10+1000;
						if(power<=limit) {
							cand.add(new candInfo(power,nr.num,nr.y,nr.x));
						}
					}else {
						int power=(Math.abs(radio.y-nr.y)+Math.abs(radio.x-nr.x))*10;
						if(power<=limit) {
							cand.add(new candInfo(power,nr.num,nr.y,nr.x));
						}
					}
				}
			}
		}
		

		int cnt=0;
		int sum=0;
		while(cnt<mCount) {
//			System.out.println(selected.power);
			sum+=cand.poll().power;
			cnt++;
		}
		return sum;
	}
}