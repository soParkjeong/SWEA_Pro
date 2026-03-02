package wirelessRadio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
	int N;
	int limit;
	
	int S;//버킷 사이즈 
	int W;//도시 한 변을 버킷으로 쪼갤 때 몇 버킷 나오는지 
	
	HashMap<Integer,radioInfo> radio;
//	HashSet<Integer>[][]bucket;
	ArrayList<radioInfo>[][]bucket;
	
	static class radioInfo{
		int id;
		int y;
		int x;
		int freq;
		radioInfo(int id,int y,int x,int freq){
			this.id=id;
			this.y=y;
			this.x=x;
			this.freq=freq;
		}
	}
	
//	TreeSet<radioInfo> select;//선택된 무선통신기 담을 공간 
	PriorityQueue<radioInfo>select;
	
	static class Cand{
		int power;
		int id;
		Cand(int power,int id){
			this.power=power;
			this.id=id;
		}
	}
	
	Comparator<Cand> Comparator=(a,b)->{
		if(a.power!=b.power) {
			return Integer.compare(a.power, b.power);
		}return Integer.compare(a.id, b.id);
	};
	
	//버킷 인덱싱 함수 
	int bx(int x) {return x/S;}
	int by(int y) {return y/S;}
	
		
	void init(int N, int mLimit){
		this.N=N;
		this.limit=mLimit;
		
		this.S=limit/10;
		if(this.S<=0) this.S=1;
		
		this.W=(N+S-1)/S;
		if(this.W<=0) this.W=1;
		
		bucket=new ArrayList[W][W];
		for(int i=0;i<W;i++) {
			for(int j=0;j<W;j++) {
				bucket[i][j]=new ArrayList<>();
			}
		}
		radio=new HashMap<>();
	}

	//버킷 인덱싱 함수
	//좌표 기반으로 버킷에 add
	void addRadio(int K, int mID[], int mFreq[], int mY[], int mX[]) {
		for(int i=0;i<K;i++) {
			radioInfo info=new radioInfo(mID[i],mY[i],mX[i],mFreq[i]);
			radio.put(mID[i],info);
			
			bucket[by(mY[i])][bx(mX[i])].add(info);
		}
	}

	//파워 계산 함수
	int powercal(radioInfo r1,radioInfo r2) {
		int freq1=r1.freq;
		int freq2=r2.freq;
		
		int y1=r1.y;
		int y2=r2.y;
		
		int x1=r1.x;
		int x2=r2.x;
		
		int power=0;
		if(freq1!=freq2) {
			power=(Math.abs(y1-y2)+Math.abs(x1-x2))*10+1000;
		}else{
			power=(Math.abs(y1-y2)+Math.abs(x1-x2))*10;
		}
		
		return power;
	}
	
	//mID 무선통신기가 들어있는 버킷의 주위 버킷 내 무선통신기들 파워 계산 때리고 
	//TreeSet에 담기 
	//앞에서부터 mCount개만큼 빼서 누적합 반환 
	int getMinPower(int mID, int mCount){
		radioInfo start=radio.get(mID);
		int cy=by(start.y);
		int cx=bx(start.x);
		
//		TreeSet<Cand> cand=new TreeSet<>(Comparator);
		PriorityQueue<Cand> cand=new PriorityQueue<>(Comparator);
		
		for(int dy=-1;dy<=1;dy++) {
			int ny=cy+dy;
			if(ny<0||ny>=W) continue;
			
			for(int dx=-1;dx<=1;dx++) {
				int nx=cx+dx;
				if(nx<0||nx>=W) continue;
				
				for(radioInfo other:bucket[ny][nx]) {
					if(other.id==mID) continue;
					
					int power=powercal(start,other);
					if(power<=limit) {
						cand.add(new Cand(power,other.id));
					}
				}
			}
		}
		

		int sum=0;
		int cnt=0;
		
		while (cnt < mCount) {
		    Cand c = cand.poll();
//		    System.out.println(c.id+" "+c.power);
		    sum += c.power;
		    cnt++;
		}
		
		return sum;
	}
}