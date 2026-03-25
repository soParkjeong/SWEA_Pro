package OTT2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int time;
	
	HashMap<Integer,movieInfo> movieMap;//전체 영화 정보 저장 
	
	static class movieInfo{
		int id;
		int genre;
		int time;//등록일 
		int total;//총점
		boolean removed;//삭제 여부
		movieInfo(int id,int genre,int time,int total){
			this.id=id;
			this.genre=genre;
			this.time=time;
			this.total=total;
			this.removed=false;
		}
	}
	
	TreeSet<movieInfo> allMovie;
	TreeSet<movieInfo>[] genreMovie;//장르별 영화 저장(총점 순,총점 동일하면 최신동록일순)
	
	HashSet<Integer>[] userWatchId;//유저별 시청목록
	ArrayList<watchInfo>[] userWatch;//유저별 시청 기록(최근 시청일수록 뒷 인덱스겠지?)
	
	static class watchInfo{
		int id;
		int userScore;
		watchInfo(int id,int userScore){
			this.id=id;
			this.userScore=userScore;
		}
	}
	
	Comparator<movieInfo> comp=(a,b)->{
		if(a.total!=b.total) return Integer.compare(b.total,a.total);
		if(a.time!=b.time) return Integer.compare(b.time,a.time);//time 큰게 최근 
		return Integer.compare(a.id, b.id);
	};
	
    void init(int N){
    	this.N=N;
    	this.time=0;
    	
    	movieMap=new HashMap<>();
    	
    	userWatchId=new HashSet[N+1];
    	for(int i=0;i<=N;i++) {
    		userWatchId[i]=new HashSet<>();
    	}
    	
    	userWatch=new ArrayList[N+1];//유저별 시청목록
    	for(int i=0;i<=N;i++) {
    		userWatch[i]=new ArrayList<>();
    	}
    	
    	allMovie=new TreeSet<>(comp);
    	
    	genreMovie=new TreeSet[6];//장르별 영화목록 
    	for(int i=1;i<=5;i++) {
    		genreMovie[i]=new TreeSet<>(comp);
    	}
    }
    
    int add(int mID, int mGenre, int mTotal)
    {
    	if(movieMap.containsKey(mID)) return 0;
    	
    	movieInfo mv=new movieInfo(mID,mGenre,time++,mTotal);
    	
    	movieMap.put(mID, mv);
    	genreMovie[mGenre].add(mv);
    	allMovie.add(mv);

    	return 1;
    }
    
    int erase(int mID){
    	if(!movieMap.containsKey(mID)||movieMap.get(mID).removed){
    		return 0;
    	}
    	
		movieInfo mv=movieMap.get(mID);
		int genre=movieMap.get(mID).genre;
		
		genreMovie[genre].remove(mv);
		allMovie.remove(mv);
		
		mv.removed=true;
		
		for(int u=1;u<=N;u++) {
			userWatchId[u].remove(mID);
		}
        return 1;
    }

    int watch(int uID, int mID, int mRating){
    	if(userWatchId[uID].contains(mID)) return 0;
      	if(!movieMap.containsKey(mID)||movieMap.get(mID).removed) return 0;
      	
    	userWatchId[uID].add(mID);//시청 영화 목록에 영화 id 추가
    	userWatch[uID].add(new watchInfo(mID,mRating));//시청 기록 추가 
    	
    	movieInfo mv=movieMap.get(mID);
    	int genre=mv.genre;
    	
    	allMovie.remove(mv);
    	genreMovie[genre].remove(mv);
    	
    	mv.total+=mRating;
    	
    	allMovie.add(mv);
    	genreMovie[genre].add(mv);
    
        return 1;
    }
    

    Solution.RESULT suggest(int uID){
        Solution.RESULT res = new Solution.RESULT();
        
        res.cnt=0;
        
        //일단 시청 기록 보자
        //추천할 때 userWatchId에 containsKey로 있는 영화는 패스해야함
        int pickG=0;
        int max=-1;
        
        if(userWatch[uID].isEmpty()) {
        	int idx=0;
        	for(movieInfo mv:allMovie) {
        		if(idx==5) break;
        		res.IDs[idx++]=mv.id;
        		res.cnt++;
        	}
        }else {
        	int cnt=0;
        	int size=userWatch[uID].size();
        	
            for(int i=size-1;i>=0;i--) {
            	watchInfo w=userWatch[uID].get(i);
            	movieInfo mv=movieMap.get(w.id);
            	
            	if(mv==null||mv.removed) continue;
            	if(cnt==5) break;
            	
            	if(max<w.userScore) {
            		max=w.userScore;
            		pickG=movieMap.get(w.id).genre;
            	}
            	cnt++;
            }//자 가장 평점 높은 영화 장르 뽑았어
            
            int idx=0;
            
            if(pickG==0) {
	            for(movieInfo mv:allMovie) {
	            	if(userWatchId[uID].contains(mv.id)) continue;//시청기록에 있는 영화면 패스
	            	if(idx==5) break;
	            	res.IDs[idx++]=mv.id;
	            	res.cnt++;
	            }
            }else {
	            for(movieInfo mv:genreMovie[pickG]) {
	            	if(userWatchId[uID].contains(mv.id)) continue;//시청기록에 있는 영화면 패스
	            	if(idx==5) break;
	            	res.IDs[idx++]=mv.id;
	            	res.cnt++;
	            }
            }
        }
        return res;
    }
}
