package parkingArea3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int M;
	int L;
	
	int[] empty;//각 구역별 빈 슬롯 개수
	PriorityQueue<Integer> emptySlot;//빈 슬롯 먼저 뽑기 위함 
	
	PriorityQueue<carInfo> carSet;//차량 주차 시각 기준으로 정렬(견인처리 위해서)
	HashMap<String,outInfo> outMap;//견인 차량 정보 저장 
	HashMap<String,carInfo> carMap;//주차 차량 정보 저장
	
	static class carInfo{
		String CarNo;
		int time;//주차 시각
		int gu;//주차 구역(견인되고 나면 여기는 -1)
		int slotNum;//슬롯 번호(견인되고 나면 여기는 -1)
		carInfo(String CarNo,int time,int gu,int slotNum){
			this.CarNo=CarNo;
			this.time=time;
			this.gu=gu;
			this.slotNum=slotNum;
		}
	}
	
	static class outInfo{
		String CarNo;
		int outTime;//견인 시각
		outInfo(String CarNo,int outTime){
			this.CarNo=CarNo;
			this.outTime=outTime;
		}
	}
	
	boolean[][] canPark;//주차 가능 여부 
	
	Comparator<carInfo>comp= (a,b)->{
		return Integer.compare(a.time,b.time);//오래된 애들부터 나오도록->걔넨 다 견인 처리 
	};
	
	//0000~9999
	TreeSet<String>[] parkTail;
	TreeSet<String>[] outTail;

	Comparator<String>compTail=(a,b)->{
		int aXX=Integer.parseInt(a.substring(0,2));
		int aY=a.charAt(2)-'A';
		int bXX=Integer.parseInt(b.substring(0,2));
		int bY=b.charAt(2)-'A';
		if(aXX==bXX) return Integer.compare(aY, bY);
		return Integer.compare(aXX, bXX);
	};
	
    public void init(int N, int M, int L)
    {
    	this.N=N;
    	this.M=M;
    	this.L=L;
    	
    	empty=new int[N];
    	Arrays.fill(empty,M);
    	
    	emptySlot=new PriorityQueue<>(
    		(a,b)->{
    			if(empty[a]==empty[b]) return a-b;
    			return Integer.compare(empty[b], empty[a]);
    		}
    	);
    	
    	for(int i=0;i<N;i++) {
    		emptySlot.offer(i);
    	}
    	
    	canPark=new boolean[N][M];//구역별 슬롯
    	for(int i=0;i<N;i++) {
    		Arrays.fill(canPark[i],true);
    	}//주차 가능 상태로 세팅
    	
    	carMap=new HashMap<>();
    	outMap=new HashMap<>();
    	
    	carSet=new PriorityQueue<>(comp);
    	
    	parkTail=new TreeSet[100001];
    	for(int i=0;i<100001;i++) {
    		parkTail[i]=new TreeSet<>(compTail);
    	}
    	outTail=new TreeSet[100001];
    	for(int i=0;i<100001;i++) {
    		outTail[i]=new TreeSet<>(compTail);
    	}
    	
        return;
    }
    
    //견인처리 함수에서 해줘야 할 것
    /**
     * car에서 poll하면서 mTime-carInfo.time<L이 될때까지 견인처리해주기
     * 
     * <견인처리>
     * carMap에서 remove
     * outMap에 추가 
     * parkTail에서 remove->outTail에 추가
     * 견인 차가 있었던 주차 자리 true로 다시 바꾸기
     * 견인 차가 있었던 주차 구역 emptySlot++
     * 
     */
    public void out(int mTime) {
    	while(true) {
    		carInfo car=carSet.peek();
    		if(car==null) break;
    		int parkTime=car.time;
    		if(mTime-parkTime>=L) {
    			car=carSet.poll();
    			String num=car.CarNo;
    			int gu=car.gu;
    			int slotNum=car.slotNum;
    			emptySlot.remove(gu);
    			empty[gu]++;//빈 자리 늘려주기
    			canPark[gu][slotNum]=true;//이제 주차 가능 
    			emptySlot.offer(gu);

    			car.gu=-1;
    			car.slotNum=-1;
    			outMap.put(num, new outInfo(num,parkTime+L));//견인 기록에 추가
    			
                int tail=Integer.parseInt(num.substring(3,7));
                parkTail[tail].remove(num);//주차꼬리에서 제거
    			outTail[tail].add(num);//견인꼬리에 추가 
    		}else {
    			break;
    		}
    	}
    }

    public Solution.RESULT_E enter(int mTime, String mCarNo){
    	out(mTime);//견인 처리
    	
    	if(outMap.containsKey(mCarNo)) outMap.remove(mCarNo);//견인맵에 있다면 견인기록에서 삭제
    	
		int tail=Integer.parseInt(mCarNo.substring(3,7));
        outTail[tail].remove(mCarNo);
    	
        Solution.RESULT_E res_e = new Solution.RESULT_E();
        
        //어디에 주차할까
        if(!emptySlot.isEmpty()) {
	        int gu=emptySlot.poll();//젤 빈자리 많은 구 뽑기
	        
	        if(empty[gu]==0) {
	        	emptySlot.offer(gu); 
	        	res_e.success=0;
	        	return res_e;
	        }else {
	            int slotNum=-1;
	            for(int i=0;i<M;i++) {
	            	if(canPark[gu][i]) {
	            		slotNum=i; 
	            		break;
	            	}
	            }
	            
	            carInfo car=new carInfo(mCarNo,mTime,gu,slotNum);
	            carMap.put(mCarNo, car);//주차맵에 추가
	            carSet.offer(car);//주차 트리셋에도 추가 
	            
	            tail=Integer.parseInt(mCarNo.substring(3,7));
	            parkTail[tail].add(mCarNo);
	            
	            empty[gu]--;//해당 구역에 빈 슬롯 개수-1
	            canPark[gu][slotNum]=false;//해당 주차 자리 주차 불가로 처리 
	            
	            res_e.success=1;
	            
	            String strSlot=String.valueOf(slotNum);
	            String strGu=String.valueOf((char) ('A' + gu));
	            int length=strSlot.length();
	            String loc="";
	            if(length==1) {
	            	loc=strGu+"00"+strSlot;
	            }else if(length==2) {
	            	loc=strGu+"0"+strSlot;
	            }else if(length==3) {
	            	loc=strGu+strSlot;
	            }
	            res_e.locname=loc;
	            
	            emptySlot.offer(gu);
	        }
        }
        return res_e;
    }

    public int pullout(int mTime, String mCarNo)
    {
    	out(mTime);
    	
    	if(outMap.containsKey(mCarNo)) {//견인 기록에 있다면 
    		int parkTime=carMap.get(mCarNo).time;//주차했던 시각 
    		int outTime=mTime-(parkTime+L);//견인되어있었던 시각 
    		int ans=(L+(outTime*5))*(-1);
    	
    		outMap.remove(mCarNo);//견인기록에서 제거
    		
    		int tail=Integer.parseInt(mCarNo.substring(3,7));
            outTail[tail].remove(mCarNo);//견인꼬리에서도 삭제 
            
            carMap.remove(mCarNo);
    		
    		return ans;
    	}
    	
    	if(!carMap.containsKey(mCarNo)) return -1;
    	
    	carInfo car=carMap.get(mCarNo);
    	
    	if(car.gu==-1) return -1;
    	
		int parkTime=carMap.get(mCarNo).time;
		int ans=mTime-parkTime;
		
		//출차 처리
		int gu=car.gu;
		int slotNum=car.slotNum;
		
		emptySlot.remove(gu);
		empty[gu]++;//빈슬롯개수+1 
		canPark[gu][slotNum]=true;//빈자리로 
		emptySlot.offer(gu);//갱신 
		
		carMap.remove(mCarNo);//출차했으니 주차목록에서 지워주기 
		
        int tail=Integer.parseInt(mCarNo.substring(3,7));
        parkTail[tail].remove(mCarNo);//주차꼬리에서도 지워주기 
		
        carSet.remove(car);//출차를 했으면 set에서도 지우기 
		return ans;
    	
    }

    public Solution.RESULT_S search(int mTime, String mStr)
    {
    	out(mTime);//견인 처리 
    	
        Solution.RESULT_S res_s = new Solution.RESULT_S();

        int tail=Integer.parseInt(mStr);
        
        int idx=0;
        int cnt=0;

    	for(String carNo:parkTail[tail]) {
    		res_s.carlist[idx++]=carNo;
    		cnt++;
    		if(cnt==5) break;
    	}
    	if(cnt<5) {
    		for(String carNo:outTail[tail]) {
    			res_s.carlist[idx++]=carNo;
    			cnt++;
    			if(cnt==5) break;
    		}
    	}
    	
    	res_s.cnt=cnt;
        return res_s;
    }
}