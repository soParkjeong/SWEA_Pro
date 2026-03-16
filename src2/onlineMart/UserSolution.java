package onlineMart;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

class UserSolution
{
	HashMap<Integer,mInfo> mMap;//물품 정보
	
	static class mInfo{
		int id;//물품별 id(mID)
		int cat;//물품별 카테고리(mCategory)
		int com;//물품별 제조사(mCompany)
		int price;//물품별 가격(mPrice)
		boolean closed;//판매 여부 
		
		mInfo(int id,int cat,int com,int price){
			this.id=id;
			this.cat=cat;
			this.com=com;
			this.price=price;
			this.closed=false;
		}
	}
	
	int[][]offSet;//품목,제조사 조합별 lazy 누적할인값 저장용
	
	TreeSet<mInfo>[][] set;//품목,제조사 조합별 판매중 상품 정렬용 TreeSet
	
	Comparator<mInfo>comp=(a,b)->{
		if(a.price==b.price) return Integer.compare(a.id,b.id);//작은 id 먼저
		return Integer.compare(a.price,b.price);//작은 값 먼저 
	};
	
    public void init()
    {
    	mMap=new HashMap<>();
    	offSet=new int[6][6];//품목,제조사가 각각 1~5까지니까
    	set=new TreeSet[6][6];
    	for(int i=0;i<6;i++) {
    		for(int j=0;j<6;j++) {
    			set[i][j]=new TreeSet<>(comp);
    		}
    	}//25개 조합의 트리셋 생성 완 
    	
        return;
    }

    public int sell(int mID, int mCategory, int mCompany, int mPrice)
    {
    	mInfo info=new mInfo(mID,mCategory,mCompany,mPrice+offSet[mCategory][mCompany]);
    	mMap.put(mID, info);//맵에 추가
    	
    	set[mCategory][mCompany].add(info);//판매 중 셋에도 추가 
    	
//    	printMap();
    	
//    	System.out.println("**************************");
//    	System.out.println("mCategory : " + mCategory + " , mCompany : " + mCompany);
//    	for(mInfo m : set[mCategory][mCompany]) {
//    		System.out.print("(" + m.id + "," + m.price + ")" + " ");
//    	}
//    	System.out.println();
    	
    	return set[mCategory][mCompany].size();
    }

    public int closeSale(int mID)
    {
    	if(mMap.containsKey(mID)) {
    		if(mMap.get(mID).closed) return -1;
    		mMap.get(mID).closed=true;//판매 종료 처리 
    		int cat=mMap.get(mID).cat;
    		int com=mMap.get(mID).com;
    		int price=mMap.get(mID).price;
    		
    		set[cat][com].remove(mMap.get(mID));//판매 중 상품 셋에서도 제거 
    		
    		return price-offSet[cat][com];//원래 가격에서 누적 할인가 빼주기(그게 당시까지의 모든 이벤트를 반영한 가격)
    	}else {
            return -1;
    	}
    }

    public int discount(int mCategory, int mCompany, int mAmount)
    {
    	offSet[mCategory][mCompany]+=mAmount;
    	
    	int cnt=0;
    	for(mInfo info:set[mCategory][mCompany]) {//이게 이미 낮은 것부터 들어가있으니까 
    		int price=info.price;
    		int id=info.id;
    		
    		if(price-offSet[mCategory][mCompany]<=0) {
    			cnt++;
    			mMap.get(id).closed=true;//판매 종료 처리해주기(재정렬은 아직..)
    		}else {
    			break;//커지는 순간부터 뒤는 볼 필요 없음 
    		}
    	}
    	
    	int size=set[mCategory][mCompany].size();
        return size-cnt;
    }

    Solution.RESULT show(int mHow, int mCode)
    {
//    	System.out.println("-----CMD_SHOW-----");
//    	
    	Solution.RESULT res = new Solution.RESULT();
//    	
//    	if(mHow==0) {
//    		for(int i=1;i<6;i++) {
//    			for(int j=1;j<6;j++) {
//    				if(set[i][j]!=null) {
//        				mInfo info=set[i][j].pollFirst();//제거
//        				info.price-=offSet[i][j];//값 변경
//        				if(info.price<=0) continue;//판매 중 상태가 아니게 되면 패스 
//        				set[i][j].add(info);//재투입 후 재정렬
//    				}
//    			}
//    		}
//    	}else if(mHow==1) {
//    		for(int j=1;j<6;j++) {
//    			if(set[mCode][j]!=null) {
//	    			mInfo info=set[mCode][j].pollFirst();//제거
//					info.price-=offSet[mCode][j];//값 변경
//					if(info.price<=0) continue;
//					set[mCode][j].add(info);//재투입 후 재정렬
//    			}
//    		}
//    	}else {
//    		for(int i=1;i<6;i++) {
//    			if(set[i][mCode]!=null) {
//	    			mInfo info=set[i][mCode].pollFirst();//제거
//					info.price-=offSet[i][mCode];//값 변경
//					if(info.price<=0) continue;
//					set[i][mCode].add(info);//재투입 후 재정렬
//    			}
//    		}
//    	}
 
        TreeSet<mInfo> cand=new TreeSet<>(comp);//최종 후보군 담을 트리셋 
        HashSet<mInfo> remove=new HashSet<>();
    	if(mHow==0) {
    		for(int i=1;i<6;i++) {
    			for(int j=1;j<6;j++) {
    				int cnt=0;
    				
//					System.out.println("offSet[" + i + "][" + j + "] : "  + offSet[i][j]);
    				for(mInfo info:set[i][j]) {
//    					System.out.println(info.id + " | " + info.price);

    					
    					int tempPrice = info.price;
    					tempPrice-=offSet[i][j];//값 변경
//    					info.price-=offSet[i][mCode];//값 변경
    					if(tempPrice<=0) {
    						remove.add(info);
    						continue;
    					}
    					cand.add(new mInfo(info.id, info.cat, info.com, tempPrice));
    					cnt++;
    					if(cnt==5) break;
    				}
    				for(mInfo info:remove) {
    	    			set[i][j].remove(info);
    	    		}
    			}
    		}
    		
    	}else if(mHow==1) {
    		for(int j=1;j<6;j++) {
				int cnt=0;
				for(mInfo info:set[mCode][j]) {
					cnt++;
					int tempPrice = info.price;
					tempPrice-=offSet[mCode][j];//값 변경
//					info.price-=offSet[mCode][j];//값 변경
					if(tempPrice<=0) {
						remove.add(info);
						continue;
					}
					cand.add(new mInfo(info.id, info.cat, info.com, tempPrice));
					if(cnt==5) break;
				}
				for(mInfo info:remove) {
	    			set[mCode][j].remove(info);
	    		}
    		}
    	}else {
    		for(int i=1;i<6;i++) {
				int cnt=0;
				for(mInfo info:set[i][mCode]) {
					cnt++;
					int tempPrice = info.price;
					tempPrice-=offSet[i][mCode];//값 변경
//					info.price-=offSet[i][mCode];//값 변경
					if(tempPrice<=0) {
						remove.add(info);
						continue;
					}
					cand.add(new mInfo(info.id, info.cat, info.com, tempPrice));
					if(cnt==5) break;
				}
				for(mInfo info:remove) {
	    			set[i][mCode].remove(info);
	    		}
    		}
    	}
    	
//    	System.out.println("candSize : " + cand.size());
    	
        int cnt=0;
        int size=0;
        while(!cand.isEmpty()) {
        	
        	res.IDs[cnt]=cand.pollFirst().id;
        	
//        	System.out.print(res.IDs[cnt] + " ");
        	
        	size++;
        	cnt++;
        	if(cnt==5) {
        		res.cnt=5;
        		
//        	    System.out.println();
        		
        		return res;
        	}
        }
        
//        System.out.println();
        
        res.cnt=cnt;
        return res;
    }
    
    
    
    
//    public void printMap() {
//    	for(int i=1;i<6;i++) {
//			for(int j=1;j<6;j++) {
//				System.out.println("i ; " + i + " , j : " + j);
//				for(mInfo m : set[i][j]) {
//					System.out.print(m.id + " : " + m.price);
//				}
//				System.out.println();
//			}
//		}
//    }
    
}