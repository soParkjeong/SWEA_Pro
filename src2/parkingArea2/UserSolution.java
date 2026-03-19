package parkingArea2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int M;
	int L;
	
	//일단 구역별 빈슬롯 개수 저장 배열
	int[] emptySlot;
	
	//주차 구역 선정
	boolean[][] area;
	
	//견인 기록
	TreeMap<String,carInfo> outMap;
	
	//주차 기록
	TreeMap<String,carInfo> parkMap;
	
	Comparator<carInfo>comp2=(a,b)->{
		return Integer.compare(b.time, a.time);//최신순부터 나옴
	};

	static class carInfo{
		String carNo;
		int time;//주차 시각
		int gu;//구역
		int slot;//슬롯번호

		carInfo(String carNo,int time,int gu,int slot){
			this.carNo=carNo;
			this.time=time;
			this.gu=gu;
			this.slot=slot;
		}
	}

	TreeSet<String>[] parkTail;
	TreeSet<String>[] outTail;
	
    Comparator<String>comp=(a,b)->{
    	int aXX=Integer.parseInt(a.substring(0,1));
    	int aY=Integer.parseInt(a.substring(2));
    	int bXX=Integer.parseInt(b.substring(0,1));
    	int bY=Integer.parseInt(a.substring(2));
    	if(aXX==bXX) return Integer.compare(aY,bY);
    	return Integer.compare(aXX, bXX);
    };
	
    public void init(int N, int M, int L)
    {
    	this.N=N;
    	this.M=M;
    	this.L=L;
    	
    	emptySlot=new int[26];//구역별 빈슬롯 개수 저장소
    	Arrays.fill(emptySlot, M);
    	
    	outMap=new TreeMap<>();
    	parkMap=new TreeMap<>();
    	
    	area=new boolean[N][M];
    	for(int i=0;i<N;i++) {
        	Arrays.fill(area[i], true);
    	}
    	
    	parkTail=new TreeSet[10001];
    	outTail=new TreeSet[10001];
    	for(int i=0;i<10000;i++) {
    		parkTail[i]=new TreeSet<>(comp);
    		outTail[i]=new TreeSet<>(comp);
    	}
    }

    public Solution.RESULT_E enter(int mTime, String mCarNo)
    {
        Solution.RESULT_E res_e = new Solution.RESULT_E();
       
        if(outMap.containsKey(mCarNo)) outMap.remove(mCarNo);//입차하면 주차성공여부와 상관없이 견인기록에서 삭제
        
        int tail=Integer.parseInt(mCarNo.substring(3,6));
        
        //어디에 주차할까?
        int max=0;
        int idx=0;
        boolean canPark=false;
        for(int k:emptySlot) {
        	System.out.print(k+", ");
        }
        
        System.out.println();
        
        for(int k=0;k<N;k++) {
        	if(max<emptySlot[k]) {//커야지만 갱신하면 크거나 같은 경우에는 갱신 안되니까 같은 빈 슬롯 개수일 때 작은 구역번호가 선택됨 
        		max=emptySlot[k];
        		idx=k;
        	}
        	if(emptySlot[k]!=0) canPark=true;
        }
        
        System.out.println("구역번호: "+ idx);
   
        //max가 구역 번호
        if(canPark) {
        	int slotNum=0;
        	for(int i=0;i<M;i++) {
        		if(area[idx][i]) {
        			slotNum=i;
        			break;
        		}
        	}
        	
        	carInfo car=new carInfo(mCarNo,mTime,idx,slotNum);
        	
	        parkMap.put(mCarNo,car);//주차차량 맵에 저장
	        emptySlot[idx]--;//그 구역의 빈 슬롯 개수-1
	        System.out.println("빈 슬롯 개수: "+emptySlot[idx]);
	        area[idx][slotNum]=false;//주차 불가로 바꿔주기
	        
	       //슬롯 번호 세자리로 맞추기 
	       String locnum=String.valueOf(slotNum);
	       if(locnum.length()==1) {
	    	   locnum="00"+locnum;
	       }else if(locnum.length()==2) {
	    	   locnum="0"+locnum;
	       }
	       
	       System.out.println("슬롯번호: "+locnum);
	       
	       parkTail[tail].add(mCarNo);
	        
	        res_e.success=1;
	        res_e.locname=String.valueOf((char) ('A' + idx))+locnum;
        }else {
        	res_e.success=0;
        }

        return res_e;
    }

    public int pullout(int mTime, String mCarNo)
    {
    	if(outMap.containsKey(mCarNo)) outMap.remove(mCarNo);//견인 기록에 있는 차였다면 견인기록에서 삭제
    	
    	int tail=Integer.parseInt(mCarNo.substring(3,6));
    	
    	if(parkMap.containsKey(mCarNo)) {//만약에 주자 기록에 있는 차면, 시간 비교 후 견인처리
    		carInfo p=parkMap.get(mCarNo);
    		int parkTime=p.time;
    		int gu=p.gu;
    		int slot=p.slot;
    		if(mTime-parkTime>=L) {
    			parkMap.remove(mCarNo);//견인처리
    			int outTime=parkTime+L;//견인된 시간
    			outMap.put(mCarNo, new carInfo(mCarNo,parkTime+L,-1,-1));//견인기록에 추가 
    			emptySlot[gu]++;//빈자리+1
    			area[gu][slot]=true;//주차 가능으로 전환 
    	        for(int k:emptySlot) {
    	        	System.out.print(k+", ");
    	        }
    	        
    	        System.out.println();
    	        
    	        outTail[tail].add(mCarNo);
    	        
    			return (L+(mTime-outTime)*5)*(-1);
    		}else {
    			parkMap.remove(mCarNo);//출차처리
    			emptySlot[gu]++;//빈자리+1
    			area[gu][slot]=true;//주차 가능으로 전환 
    			
    			parkTail[tail].add(mCarNo);
    	        for(int k:emptySlot) {
    	        	System.out.print(k+", ");
    	        }
    	        
    	        System.out.println();
    			return mTime-parkTime;//주차 기간 반환 
    		}
    	}
        for(int k:emptySlot) {
        	System.out.print(k+", ");
        }
        
        System.out.println();
        return -1;
    }

    public Solution.RESULT_S search(int mTime, String mStr)
    {
        Solution.RESULT_S res_s = new Solution.RESULT_S();
        
        int cnt=0;
        
       
        return res_s;
    }
    
    //견인처리 함수
    public void out(int mTime) {
    	if(mTime)
    }
}