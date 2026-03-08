package MineralTransportation;

import java.util.ArrayList;
import java.util.HashMap;
 
class UserSolution {
    int L;
    int N;
    HashMap<Integer,BaseCamp> baseCampMap;
     
    static class BaseCamp{
        int id;
        int r;
        int c;
        int amount;
        BaseCamp(int id,int r,int c,int amount){
            this.id=id;
            this.r=r;
            this.c=c;
            this.amount=amount;
        }
    }
     
    ArrayList<BaseCamp>[][]bucket;
    int BS;//버킷 사이즈
    int BCnt;//버킷이 이 맵에 몇 개까지 만들어지나?(한 변에 몇 개의 버킷이 필요한지)
    //BCnt=(N+BS-1)/BS;
     
    int br(int r) {return r/BS;}
    int bc(int c) {return c/BS;}
     
    //덩어리(채굴 가능한 최대 광물 개수가 동일한 애들끼리 한 덩어리)끼리 묶기 위한 유니온파인드
    int campCnt;
    HashMap<Integer,Integer> idToIdx;
     
    int[] parent=new int[20001];
    int[] maxSum=new int[20001];
     
    int find(int x) {
        if(parent[x]==x) return x;
        return parent[x]=find(parent[x]);
    }
     
    void union(int a,int b) {
        int ra=find(a);
        int rb=find(b);
         
        if(ra==rb) return;//이미 같은 루트면 끝 
         
        parent[rb]=ra;
        maxSum[ra]+=maxSum[rb];
    }
     
    void init(int L, int N){
        this.L=L;//한번에 움직일 수 있는 에너지를 검색 반경으로 두자 
        this.N=N;
         
        this.BS=L;
        this.BCnt=(N+BS-1)/BS;
         
        bucket=new ArrayList[BCnt][BCnt];
        for(int r=0;r<BCnt;r++) {
            for(int c=0;c<BCnt;c++) {
                bucket[r][c]=new ArrayList<>();
            }
        }
         
        baseCampMap=new HashMap<>();
        idToIdx=new HashMap<>();
        campCnt=0;
    }
     
    //버킷 사이즈:L로 두고 그 베이스캠프 주위 버킷만 싹 훑기 
    //같은 덩어리(채굴 가능한 최대 광물 개수가 동일한 애들끼리 한 덩어리) 속하는 노드인지 판단- 유니온파인드? 
    int addBaseCamp(int mID, int mRow, int mCol, int mQuantity){
        int idx=campCnt++;
        idToIdx.put(mID, idx);
         
        parent[idx]=idx;
        maxSum[idx]=mQuantity;
         
        BaseCamp bc = new BaseCamp(mID, mRow, mCol, mQuantity);
        baseCampMap.put(mID,bc);
         
        //일단 추가된 얘가 어디 버킷에 속하노
        int cr=br(mRow);
        int cc=bc(mCol);
         
        for(int dr=-1;dr<=1;dr++) {
            int nr=cr+dr;
            if(nr<0||nr>=BCnt) continue;
             
            for(int dc=-1;dc<=1;dc++) {
                int nc=cc+dc;
                if(nc<0||nc>=BCnt) continue;
                 
                for(BaseCamp other:bucket[nr][nc]) {
                    int dist=(Math.abs(other.r-mRow)+Math.abs(other.c-mCol));
                    if(dist<=L) {//L이내면 
                        int otherIdx=idToIdx.get(other.id);
                        union(idx,otherIdx);//그래프 합치기(루트 연결) 
                    }
                }
            }
        }
        bucket[cr][cc].add(bc);
         
        return maxSum[find(idx)];
    }
 
    int findBaseCampForDropping(int K){
        int bestId=-1;
        BaseCamp best=null;
         
        for(BaseCamp bc: baseCampMap.values()) {
            int idx=idToIdx.get(bc.id);
            int total=maxSum[find(idx)];
             
            if(total<K) continue;
             
            if (best == null) {
                best = bc;
                bestId = bc.id;
            } 
            else {
                if (bc.amount < best.amount) {
                    best = bc;
                    bestId = bc.id;
                }else if (bc.amount == best.amount) {
                    if (bc.r < best.r) {
                        best = bc;
                        bestId = bc.id;
                    }else if (bc.r == best.r) {
                        if (bc.c < best.c) {
                            best = bc;
                            bestId = bc.id;
                        }
                    }
                }
            }
        }
        return bestId;
    }
}