package prediction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

class UserSolution{
	int N;//대학별 선발 가능 학생 수
	int M;//대학 개수(<=30)
	
	//학생별로 전체 대학에 대해 총점 가지고 있어야 하려나...
	HashMap<Integer,sInfo> sMap;//학생 정보 맵
	int[][] scoreW;//대학별 과목 가중치 
	int[] scoreSum;//대학별 총점 목록
	
	static class sInfo{
		int id;//학생 ID
		int[] score;//과목별 점수 
		int[] scoreSum;//대학별 총점 목록
		int select;//선발 대학(없으면 -1)
		boolean removed;//시스템 내 학생 삭제 여부
		sInfo(int id,int[] score,int[] scoreSum,int select,boolean removed){
			this.id=id;
			this.score=score;
			this.scoreSum=scoreSum;
			this.select=select;
			this.removed=removed;
		}
	}
	
	static class selectInfo{
		int id;
		int score;//선발된 학생의 총점 
		selectInfo(int id,int score){
			this.id=id;
			this.score=score;
		}
	}
	
	TreeSet<selectInfo>[] selected;//대학별 선발 학생
	TreeSet<selectInfo>[] unselected;//대학별 선발에서 밀린 학생 
	
	Comparator<selectInfo>comp=(a,b)->{
		if(a.score!=b.score) return Integer.compare(b.score,a.score);//총점 큰 학생부터
		return Integer.compare(a.id,b.id);//총점 같으면 id 작은 학생부터 
	};
	
	public void init(int N, int M, int[][] mWeights)
	{
		this.N=N;
		this.M=M;
		
		sMap=new HashMap<>();
		
		scoreW=new int[M+1][5];
		
		for(int i=1;i<=M;i++) {
			for(int s=0;s<5;s++) {
				scoreW[i][s]=mWeights[i-1][s];
			}
		}
		
		selected=new TreeSet[M+1];
		unselected=new TreeSet[M+1];
		
		for(int i=1;i<=M;i++) {
			selected[i]=new TreeSet<>(comp);
			unselected[i]=new TreeSet<>(comp);
		}
		
		return;
	}

	public void add(int mID, int[] mScores){
		int[] scoreSum=new int[M+1];//학생별 대학별 총점 목록 
		for(int i=1;i<=M;i++) {
			scoreSum[i]+=((scoreW[i][0]*mScores[0])+(scoreW[i][1]*mScores[1])+(scoreW[i][2]*mScores[2])+(scoreW[i][3]*mScores[3])+(scoreW[i][4]*mScores[4]));
		}

		//아직은 대학에 선발이 되지 않았으니까 선발된 대학은 -1 
		sInfo s=new sInfo(mID,mScores,scoreSum,-1,false);
		sMap.put(mID,s);
		
		simul(mID);
	}	
	
	//자 입시 추천 시뮬 돌리자
	void simul(int mID) {
		//일단 이 학생 총점 갖고와서 모든 대학 앞에서부터 밀어내기 해보자
		int curId=mID;

		for(int i=1;i<=M;i++) {
			//자 이 대학의 선발군,밀린군 갖고 봐보자
			//선발군의 꼴찌랑 비교, 밀린군의 일등이랑 비교
			//지금 학생 총점 얼마야
			int curScore=sMap.get(curId).scoreSum[i];
			
			//현재 대학에 자리가 남아있어
			if(selected[i].size()<N) {
				selected[i].add(new selectInfo(curId,curScore));
				sMap.get(curId).select=i;
				return;
			}
			
			selectInfo worst=selected[i].last();//선발군 꼴찌
			int worstId=worst.id;
			int worstScore=worst.score;
			
			//새 학생이 더 우수하면 들어가고, 꼴찌 밀려나
			if(curScore>worstScore || (curScore==worstScore && curId<worstId)) {
				selected[i].pollLast();//꼴찌나와
				selected[i].add(new selectInfo(curId,curScore));//새로운 애 들어가
				sMap.get(curId).select=i;
				
				//이제 밀려난 애들은 이 대학 밀린군 으로 들어가
				unselected[i].add(new selectInfo(worstId,worstScore));
				sMap.get(worstId).select=-1;//선발 대학이 없어졌으니 -1로
				
				//밀려난 학생을 현재 학생으로 갱신
				curId=worstId;//꼬리물고 전부 다 갱신해줘야하니까
			}else {//새 학생이 꼴지보다 못하면 그냥 바로 밀린군으로 넣기
				unselected[i].add(new selectInfo(curId,curScore));
			}
		}
		
		sMap.get(curId).select=-1;
	}
	

	public void erase(int mID){
		sInfo s=sMap.get(mID);
		if(s==null || s.removed) return;//이미 삭제되었으면 return 
		
		int prevSelect=s.select;//지울 친구의 현재 대학이었던 곳(공석이 발생할 곳)
		
		if(prevSelect==-1) {//이전에 선발되었던 대학이 없었다면(다 선발되지 못했을 거니까 모든 밀린군에 다 있겠지)
			for(int i=1;i<=M;i++) unselected[i].remove(new selectInfo(mID,s.scoreSum[i]));//밀린 군에서도 삭제
		}else {//선발되었던 대학이 있었다면(선발대학 앞의 대학의 밀린군까지만 있겠지)
			for(int i=1;i<prevSelect;i++) {
				unselected[i].remove(new selectInfo(mID,s.scoreSum[i]));//밀린군에서 다 삭제
			}
			selected[prevSelect].remove(new selectInfo(mID,s.scoreSum[prevSelect]));//선발군에서도 삭제 
		}
		
		s.removed=true;//시스템 내에서만 삭제처리
		s.select=-1;//선발 대학도 초기화
		
		//자 지웠으니 그 빈자리 채우기 들어가자 
		if(prevSelect!=-1) {//이전에 선발되었던 대학이 있었다면-> 선발군에 빈자리 생김, 그래서 복구 필요함/ 이전에 선발되었던 대학이 없었다면-> 선발군에 빈자리 생기지 않고, 복구도 필요 없음
			int cur=prevSelect;//공석이 발생한 곳 
			
			while(cur<=M) {//연쇄복구가 필요할 수도 있으니
				if(selected[cur].size()>=N) return;//아마 선발군 대학에서 지워졌으니까 빈자리가 무조건 생길테지만, 호옥시나 해놓는 장치(이미 지금 대학 선발군 다 찼으면 패스)
				if(unselected[cur].isEmpty()) return;//cur 대학에 빈자리가 생겨도 밀린군에 없으면 패스 
				
				selectInfo best=unselected[cur].first();//밀린군의 1등 나와
				int bestId=best.id;
				sInfo bs=sMap.get(bestId);
				
				int prev=bs.select;//밀린군 1등의 대학
				
				int end;
				if(prev==-1) end=M;//만약에 밀린군 1등이 선발된 대학이 없었다면, 모든 대학 밀린군에 있었을거야..그러면 cur 대학에 붙게 되면 cur~M 대학의 밀린군에서 얘를 다 지워야 함
				else end=prev-1;//밀린군 1등이 선발된 대학이 있었다면, 그러면 cur 대학에 붙게 되면 cur~prev-1 대학의 밀린군에서 얘를 지워야 함 

				for(int i=cur;i<=end;i++) {
					unselected[i].remove(new selectInfo(bestId,bs.scoreSum[i]));//밀린군에서 지워
				}
				
				if(prev != -1) {
				    selected[prev].remove(new selectInfo(bestId, bs.scoreSum[prev]));
				}
				
				selected[cur].add(new selectInfo(bestId,bs.scoreSum[cur]));//선발군에 추가해 
				bs.select=cur;//이제 후보메꾼애의 대학도 cur이 됨 
				
				if(prev==-1) return;
				
				cur=prev;//이제 메꾼친구가 원래 선발되었던 대학이 공석이 생김 
			}
		}
	}

	public int suggest(int mID){
		//추천 전 시뮬 싹 돌자
		sInfo s=sMap.get(mID);
		if(s==null || s.removed) return -1;
		
		return s.select;
	}
}