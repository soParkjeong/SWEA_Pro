package OnlineMarket;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.TreeSet;

class UserSolution
{
	HashMap<Integer,Product> all;
	
	static class Product{
		int id;//mID
		int category;//mCategory
		int company;//mCompany
		int basePrice;//mPrice(젤 처음 가격-할인 반영 X)
		
		Product(int id,int category,int company,int basePrice){
			this.id=id;
			this.category=category;
			this.company=company;
			this.basePrice=basePrice;
		}
	}
	
	HashSet<Integer> sellClosed;//판매 종료 상품 저장용
	
	TreeSet<Product>[][] selling;//판매 중 상품 저장용(품목*제조사 조합별)
	
	int[][]offSet;
	
	Comparator<Product> prodComparator=(a,b)->{
		if(a.basePrice!=b.basePrice) return Integer.compare(a.basePrice,b.basePrice);
		return Integer.compare(a.id,b.id);
	};
	
    public void init(){
    	all=new HashMap<>();
    	sellClosed=new HashSet<>();
    	offSet=new int[6][6];
    	selling=new TreeSet[6][6];
    	for(int i=1;i<=5;i++) {
    		for(int j=1;j<=5;j++) {
    			selling[i][j]=new TreeSet<>(prodComparator);
    		}
    	}
    }

    public int sell(int mID, int mCategory, int mCompany, int mPrice){
    	int basePrice=mPrice+offSet[mCategory][mCompany];//basePrice는 바뀌면 안됨->매번 treeSet정렬하면 터짐 
    	
    	Product p=new Product(mID,mCategory,mCompany,basePrice);
    	
    	all.put(mID,p);
    	selling[mCategory][mCompany].add(p);
    	
        return selling[mCategory][mCompany].size();
    }

    public int closeSale(int mID){
    	Product p=all.get(mID);
    	if(p==null) return -1;
    	
    	int curCat=p.category;
    	int curCom=p.company;
    	
    	boolean removed=selling[curCat][curCom].remove(p);
    	if(!removed) return -1;
    	
    	sellClosed.add(mID);//판매종료처리
    	
    	int curPrice=p.basePrice-offSet[curCat][curCom];
    	return curPrice;
    }

    public int discount(int mCategory, int mCompany, int mAmount)
    {
    	offSet[mCategory][mCompany]+=mAmount;//offSet에 할인금액 누적저장
    	
    	TreeSet<Product> set=selling[mCategory][mCompany];
    	
    	while(!set.isEmpty()) {
    		Product first=set.first();
    		int curPrice=first.basePrice-offSet[mCategory][mCompany];
    		if(curPrice>0) break;
    		
    		set.pollFirst();//판매 중 상품에서 제거
    		sellClosed.add(first.id);//판매 종료 처리 
    	}
    	
    	//원래 했던 방식은 가격 순으로 정렬된 TreeSet의 장점을 살리지 못함
    	//(앞에서부터 보면서 0보다 작은지를 점검하면 정렬되어있으니까 0보다 커지는 품목부터 뒤로는 안봐도 됨)
//    	for(Product p : selling[mCategory][mCompany]) {
//    		if(p.basePrice-offSet[mCategory][mCompany]<=0) {
//    			sellClosed.add(p.id);//판매종료처리 
//    			selling[mCategory][mCompany].remove(p);//판매중 상품에서 제거 
//    		}
//    	}
    	
        return set.size();
    }

    //자 이제 show할때는 품목별로, 제조사별로 보여줘야할 수도 있으니 품목별로, 제조사별로 merge해서 상위 5개를 보는 함수 만들어주자
    
    static class Cand{
    	int cat, com;
    	Product p;
    	int finalPrice;//이건 할인가 다 반영된 가격 
    	Cand(int cat,int com,Product p,int finalPrice){
    		this.cat=cat;
    		this.com=com;
    		this.p=p;
    		this.finalPrice=finalPrice;
    	}	
    }
    
    //품목*제조사별로 짱들 쭈르륵 넣는거 5명 채울 때까지 반복 
    public void pick5(int[][] comb, Solution.RESULT res) {
    	PriorityQueue<Cand>pq=new PriorityQueue<>((a,b)->{
    		if(a.finalPrice!=b.finalPrice) return Integer.compare(a.finalPrice,b.finalPrice);
    		return Integer.compare(a.p.id,b.p.id);
    	});
    	
    	for(int[] cc: comb) {
    		int curCat=cc[0];
    		int curCom=cc[1];
    		
    		TreeSet<Product> set=selling[curCat][curCom];
    		if(set.isEmpty()) {
    			continue;
    		}
    		
    		Product first=set.first();
    		int curPrice=first.basePrice-offSet[curCat][curCom];
    		
    		pq.offer(new Cand(curCat,curCom,first,curPrice));
    	}
    	
    	int cnt=0;
    	//5개 뽑을 때까지 무한반복 ㄱ 
    	while(cnt<5 && !pq.isEmpty()) {
    		Cand cand=pq.poll();
    		res.IDs[cnt++]=cand.p.id;
    		
    		TreeSet<Product> set=selling[cand.cat][cand.com];
    		Product next=set.higher(cand.p);
    		if(next!=null) {
    			int nxtPrice=next.basePrice-offSet[cand.cat][cand.com];
    			pq.offer(new Cand(cand.cat,cand.com,next,nxtPrice));
    		}
    	}
    	res.cnt=cnt;
    }
    
    public Solution.RESULT show(int mHow, int mCode)
    {
        Solution.RESULT res = new Solution.RESULT();
        res.cnt=0;
        
        //전체 다 
        if(mHow==0) {
        	int[][]comb=new int[25][2];
        	int idx=0;
        	for(int cat=1;cat<=5;cat++) {
        		for(int com=1;com<=5;com++) {
        			comb[idx++]=new int[] {cat,com};
        		}
        	}
			pick5(comb,res);
			return res;
        }
        
        //같은 품목별로 
        if(mHow==1) {
        	int cat=mCode;
        	int[][]comb=new int[5][2];
        	for(int com=1;com<=5;com++) {
        		comb[com-1]=new int[] {cat,com};
        	}
        	pick5(comb,res);
        	return res;
        }
        
        //같은 제조사별로 
        int com=mCode;
        int[][]comb=new int[5][2];
        for(int cat=1;cat<=5;cat++) {
        	comb[cat-1]=new int[] {cat,com};
        }
        pick5(comb,res);
        return res;
    }
}