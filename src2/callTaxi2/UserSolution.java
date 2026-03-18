package callTaxi2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

class UserSolution
{
    int N;
    int M;
    int[] tx;//택시 x좌표
    int[] ty;//택시 y좌표
    int L;
    
    int rideSum;//총 이동거리
    int moveSum;//총 운행거리
    
    static class Taxi{
        int id;
        int x;
        int y;
        int rideSum;//총 이동거리
        int moveSum;//총 운행거리 
        
        Taxi(int id,int x,int y,int rideSum,int moveSum){
            this.id=id;
            this.x=x;
            this.y=y;
            this.rideSum=rideSum;
            this.moveSum=moveSum;
        }
    }
    
    HashMap<Integer,Taxi> taxiMap;//택시 정보(id는 i)
    
    ArrayList<Taxi>[][] bucket;//정렬 기준인 손님이 매번 바뀌니까 List로..TreeSet 불가임
    int BS;//버킷 사이즈
    int BC;//도시 한변에 버킷 몇 개?
    
    TreeSet<Taxi> cand;

    Comparator<Taxi> comp=(a,b)->{
        if(a.rideSum==b.rideSum) return Integer.compare(a.id,b.id);
        return Integer.compare(b.rideSum,a.rideSum);
    };
    
    static class Call{
        int id;
        int distance;
        
        Call(int id,int distance){
            this.id=id;
            this.distance=distance;
        }
    }
    
    TreeSet<Call> selected;//L이내에 있는 택시들 
    
    Comparator<Call> compL=(a,b)->{
        if(a.distance==b.distance) return Integer.compare(a.id,b.id);
        return Integer.compare(a.distance,b.distance);
    };
    
    public void init(int N, int M, int L, int[] mXs, int[] mYs)
    {
        this.N=N;
        this.M=M;
        this.L=N/10;
        
        BS=L;
        BC=(N+BS-1)/BS;
        
        bucket=new ArrayList[BC][BC];
        for(int i=0;i<BC;i++) {
            for(int j=0;j<BC;j++) {
                bucket[i][j]=new ArrayList<>();
            }
        }//버킷 만들기
        
        tx=new int[M+1];
        ty=new int[M+1];
        taxiMap=new HashMap<>();
        cand=new TreeSet<>(comp);//후보군 넣을 곳 
        
        for(int i=1;i<=M;i++) {
            tx[i]=mXs[i-1];
            ty[i]=mYs[i-1];
            Taxi taxi=new Taxi(i,tx[i],ty[i],0,0);
            taxiMap.put(i,taxi);//맵에 저장 완 
            cand.add(taxi);
            
            bucket[tx[i]/BS][ty[i]/BS].add(taxi);//버킷에도 추가 
        }
    }

    //30,000
    public int pickup(int mSX, int mSY, int mEX, int mEY)
    {
        //지금 손님의 출발지가 속한 버킷 인덱스
        int bsx=mSX/BS;
        int bsy=mSY/BS;
        
        //손님 도착지가 속한 버킷 인덱스
        int bex=mEX/BS;
        int bey=mEY/BS;
        
        //손님 출발지 버킷으로부터 근방 L이하 탐색 
        selected=new TreeSet<>(compL);
        for(int i=-1;i<=1;i++) {
            for(int j=-1;j<=1;j++) {
                int nbx=bsx+i;
                int nby=bsy+j;
                if(nbx<0||nbx>=BC||nby<0||nby>=BC) continue;
                for(Taxi taxi:bucket[nbx][nby]) {
                    int distance=Math.abs(taxi.x-mSX)+Math.abs(taxi.y-mSY);
                    if(distance>L) continue;
                    Call call=new Call(taxi.id,distance);
                    selected.add(call);//호출될 택시 후보에 추가 
                }
            }
        }
        
//        for(Call call:selected) {
//            System.out.print(call.id+", ");
//        }
//        System.out.println();
//        
        //최종 호출될 택시
        if(selected.size()==0) {//L이내에 없으면 
            return -1;
        }else {
            Call select=selected.pollFirst();
            int id=select.id;
            Taxi taxi=taxiMap.get(id);
            
            //최종 호출될 택시 버킷 간 이동 해주기
            cand.remove(taxi);
            taxi.moveSum+=Math.abs(taxi.x-mSX)+Math.abs(taxi.y-mSY)+Math.abs(mSX-mEX)+Math.abs(mSY-mEY);
            taxi.rideSum+=Math.abs(mSX-mEX)+Math.abs(mSY-mEY);
            taxi.x=mEX;
            taxi.y=mEY;
            bucket[bsx][bsy].remove(taxi);
            bucket[bex][bey].add(taxi);
            cand.add(taxi);//최종 후보군에도 추가 
            
            return select.id;
        }
    }

    //5,000
    public Solution.Result reset(int mNo)
    {
        int id=mNo;
        Taxi taxi=taxiMap.get(id);
    
        Solution.Result res = new Solution.Result();
        
        res.mX=taxi.x;
        res.mY=taxi.y;
        res.mMoveDistance=taxi.moveSum;
        res.mRideDistance=taxi.rideSum;
        
        cand.remove(taxi);
        taxi.rideSum=0;//초기화 
        taxi.moveSum=0;//초기화 
        cand.add(taxi);

        return res;
    }

    //10,000
    public void getBest(int[] mNos)
    {
        int idx=0;
        for(Taxi taxi:cand) {
            if(idx==5) break;
            mNos[idx]=taxi.id;
            idx++;
        }
    }
}