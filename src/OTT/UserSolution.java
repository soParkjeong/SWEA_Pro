package OTT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

class UserSolution
{
	int N;
	int time;//등록 순서용
	
	HashMap<Integer,Movie> enrolledId;//등록된 영화
	
	//유저별 시청 영화
	HashSet<Integer>[] watchedId;//유저별 시청 영화 목록 저장용/ 유저id-Movie

    //유저별 시청 기록
    ArrayList<WatchRec>[] history;
    
	static class Movie{
		int id;//mID
		int total;//총점
		int genre;//장르
		int enrollDay;//등록 순서
		boolean isDeleted;//삭제 여부
		
		Movie(int id,int total,int genre,int enrollDay){
			this.id=id;
			this.total=total;
			this.genre=genre;
			this.enrollDay=enrollDay;
			this.isDeleted = false;
		}
	}
	
	//등록된 영화 장르별,전체 정렬용(시청여부 무관)
	TreeSet<Movie>[]genreSet;
	TreeSet<Movie>allSet;
	
	Comparator<Movie> movieComparator=(a,b)->{
		if(a.total!=b.total) return Integer.compare(b.total,a.total);//총점은 큰 것부터
		if(a.enrollDay!=b.enrollDay) return Integer.compare(b.enrollDay,a.enrollDay);//등록 순도 큰 것부터
		return Integer.compare(a.id,b.id);//같으면 아이디는 작은 것부터
	};
	
	
	public void init(int N) {
		this.N=N;
		this.time=0;
		
		enrolledId=new HashMap<>();
		watchedId=new HashSet[N+1];
		for(int i=1;i<=N;i++) watchedId[i]=new HashSet<>();
		
		history=new ArrayList[N+1];
		for(int i=1;i<=N;i++) history[i]=new ArrayList<>();
		
		allSet=new TreeSet<>(movieComparator);
		
		genreSet=new TreeSet[6];
		for(int i=1;i<=5;i++) {
			genreSet[i]=new TreeSet<>(movieComparator);
		}
	}
	
	public int add(int mID,int mGenre,int mTotal) {
		//등록된 거면 패스
		if(enrolledId.containsKey(mID)) {
			return 0;
		}
		
		//영화 등록
		Movie movie=new Movie(
			mID,
			mTotal,
			mGenre,
			++time
		);
		
		enrolledId.put(mID, movie);
		allSet.add(movie);
		genreSet[mGenre].add(movie);
		
		return 1;
	}
	
	
    int erase(int mID)
    {
    	Movie movie=enrolledId.get(mID);
    	
    	if(movie==null) return 0;
    	if(movie.isDeleted) return 0;
    	
    	allSet.remove(movie);
    	genreSet[movie.genre].remove(movie);
    	
    	movie.isDeleted=true;
    	
    	//유저 시청 여부 목록에서도 삭제(시청하고 삭제되어버리면 시청 목록에서 상위 5개 꺼내올 때 제외해야함)
    	for(int i=1;i<=N;i++) {
    		watchedId[i].remove(mID);
    	}
    	
        return 1;
    }

    
    static class WatchRec{
    	int id;
    	int rating;
    	
    	WatchRec(int id,int rating){
    		this.id=id;
    		this.rating=rating;
    	}
    }
    
    
    int watch(int uID, int mID, int mRating)
    {	
    	//고유 번호 UID인 사용자-mID 영화 시청->평점:mRating
    	//총점은 mRating만큼 증가
    	
    	//등록된 영화 중 하나 가져와
		Movie movie=enrolledId.get(mID);
		
		//시청 실패
		//영화가 등록되어 있지 않다면(삭제되었다면)
		if(movie==null||movie.isDeleted) return 0;
		
		//이미 시청한 경우
		if(watchedId[uID].contains(mID)) return 0;
		
		//시청 성공
		//시청 TreeSet(장르별, 전체) 모두 갱신
		//싹 한번 제거했다가
		allSet.remove(movie);
		genreSet[movie.genre].remove(movie);
		
		//갱신해서
		movie.total+=mRating;
		
		//다시 추가 
		allSet.add(movie);
		genreSet[movie.genre].add(movie);
		
		watchedId[uID].add(mID);
		history[uID].add(new WatchRec(mID,mRating));
		
		return 1;
    }
    
    Solution.RESULT suggest(int uID)
    {
        Solution.RESULT res = new Solution.RESULT();
        
        res.cnt = 0;

//    	추천 조건
//    	1. 이미 시청, 삭제된 영화 X
//    	2. 시청 목록에 있는 가장 최근 시청한 최대 5개 영화 중 가장 평점이 높은 영화와 같은 장르 영화만 추천
    	//만약에 가장 높은 평점 영화가 여러 개면-> 그 중 가장 최근 영화와 같은 장르
//    	3. 시청 목록 없으면-> 장르 불문 다 추천
//    	4. 3가지 조건 모두 만족하면 총점 높은 순(총점 동일-최신순)

    	int pickGenre=0;
    	int maxRating=-1;
    	
    	ArrayList<WatchRec>h=history[uID];
    	int haveWatch=0;//시청한 영화 개수 카운팅용
    	
    	//일단 history에서 젤 뒤 5개 가져오기
		//사용자 평점 젤 높은 영화 id 겟
		//걍 뒤에서부터 5개 채워질 때까지 갱신하면 동일한 rating이면 최신껄로 갱신됨 
		for(int idx=h.size()-1;idx>=0 && haveWatch<5;idx--) {
			WatchRec wr=h.get(idx);
			
			Movie mv=enrolledId.get(wr.id);
			
			//history가 비어있거나, 시청목록에는 있지만 삭제되었다면
			if(mv==null||mv.isDeleted) continue;
			
			haveWatch++;
			
			if(wr.rating>maxRating) {
				maxRating=wr.rating;
				pickGenre=mv.genre;
			}
		}
        
		//장르가 없으면 전체에서, 장르가 정해졌으면 그 장르TreeSet에서 추천
		TreeSet<Movie> suggestCand=(pickGenre==0)?allSet:genreSet[pickGenre];
        	
		for(Movie mv:suggestCand) {
			if(res.cnt==5) break;
			if(mv.isDeleted) continue;//삭제되었으면 패스
    		if(watchedId[uID].contains(mv.id)) continue;//시청목록에 있으면 패스  
        
    		res.IDs[res.cnt++]=mv.id;
		}
		
		return res;
    }
}