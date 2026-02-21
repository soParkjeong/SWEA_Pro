package Expectation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

class UserSolution
{
	/**
	 * 현재 학생이 더 크거나 동일한데 id가 더 앞서면 걔를 빼내서
	 *  다음 TreeSet으로 보내고 연쇄적으로 뒤의 TreeSet도 한칸씩 밀려나기, 
	 *  그러면서 학생의 현재 소속된 대학(exuniv)도 갱신해주기, 
	 *  연쇄적으로 소속이 바뀌게 된 학생이 있다면 그들도 다 갱신해주기
	 *  
	 * 전역변수
	 * 대학정원 N, 대학 수 M
	 * 대학교 ID=univId
	 * 대학별 과목별 가중치=mWeights[univId][5]
	 * 
	 * 학생별 과목 점수 저장
	 * HashMap<Integer,int[5]> scoresMap
	 * 
	 * 삭제 여부
	 * boolean isErase[mID]
	 * 
	 * 현재 배정된 대학Id
	 * int assigned[mID]
	 * 
	 * 학생의 대학별 총점
	 * total[mID][univId]
	 * 
	 * <대학>
	 * 대학별 후보군 TreeSet<mID>[]pool
	 * Comparator 총점 내림차순
	 * 			  총점 동일하면 id 오름차순
	 * 
	 * 대학별 선발군 TreeSet<mID>[]selected 
	 * 밀어내기니까 젤 꼴찌-총점 오름차순
	 *                 총점 동일하면 id 내림차순 
	 *                 
	 * <공통함수>
	 * 과목별 가중치 기반 총점 계산 함수- 학생 add 할 때 호출
	 * 대학 선발 함수- 학생 add할 때나, 학생 erase될 때
	 * 후보군에서 선발 함수- 선발 TreeSet에서 학생이 erase되었을 때
	 * 
	 */
	int N;
	int M;
	int[][]mWeights;
	
	HashMap<Integer,int[]>scoresMap;
	HashMap<Integer,int[]>totalMap;
	HashSet<Integer> erased;
	HashMap<Integer,Integer>assignedMap;
	
	TreeSet<Integer>[] pool;// 선발에서 밀린 학생들 
	TreeSet<Integer>[] selected;//대학별 선발군
	
	//학생 id로 대학 총점 가져오기 함수 
	int getTotal(int id,int univ) {
		int[]arr=totalMap.get(id);
		if(arr==null) return 0;
		return arr[univ];
	}
	
	public void init(int N, int M, int[][] mWeights)
	{
		this.N=N;
		this.M=M;
		this.mWeights=mWeights;
		
		scoresMap=new HashMap<>();
		totalMap=new HashMap<>();
		erased=new HashSet<>();
		assignedMap=new HashMap<>();
		
		//대학별로 TreeSet 다 초기화해주기
		pool=new TreeSet[M+1];
		selected=new TreeSet[M+1];
		
		for(int u=1;u<=M;u++) {
			//Comparator도 pool은 좋은 것부터 first()
			final int univ=u;
			pool[univ]=new TreeSet<>((a,b)->{
				int ta=getTotal(a,univ);
				int tb=getTotal(b,univ);
				if(ta!=tb) return Integer.compare(tb,ta);//내림차순
				return Integer.compare(a,b);//오름차순
			});
			//selected는 밀어내기용으로 꼴찌부터 last()
			selected[univ]=new TreeSet<>((a,b)->{
				int ta=getTotal(a,univ);
				int tb=getTotal(b,univ);
				if(ta!=tb) return Integer.compare(tb, ta);
				return Integer.compare(a, b);
			});
		}
	}

	//총점 계산 함수
	int calcTotal(int[]mScores,int[]mWeights) {
		int total=0;
		for(int i=0;i<5;i++) {
			total+=mScores[i]*mWeights[i];
		}
		return total;
	}
	
	//학생의 현재 배정 대학 조회
	int getAssigned(int mID) {
		return assignedMap.getOrDefault(mID, 0);
	}
	
	//꼴찌랑 지금 학생이랑 비교 함수
	boolean isBetter(int univ,int a, int b) {
		int ta=getTotal(a,univ);
		int tb=getTotal(b,univ);
		if(ta!=tb) return ta>tb;
		return a<b;
	}
	
	//대학 선발 함수(밀어내기 타임)
	//일단 넣고, 정원 넘으면 꼴찌 빼서 밀려난 애들 저장하는 pool에 넣기
	int tryAccept(int univ,int student) {
		
		if(erased.contains(student)) return student;
		
		//이미 배정되어있어? 그럼 일단 나와봐 
		int cur=getAssigned(student);
		if(cur!=0) {
			selected[cur].remove(student);
			assignedMap.put(student, 0);
		}
		
		//배정 안되어있으면 일단 선발
		selected[univ].add(student);
		assignedMap.put(student, univ);
		
		//정원 안넘으면 배정 끝
		if(selected[univ].size()<=N) {
			return 0;
		}
		
		//정원 넘었으면 꼴찌 나와->다음 대학으로 ㄱ
		int worst=selected[univ].last();
		selected[univ].remove(worst);
		assignedMap.put(worst,0);
		
		//꼴찌는 밀려난 애들 저장해놓는 pool로
		pool[univ].add(worst);
		
		return worst;
	}
	

///////////////////////////////////////////////////////////////////////////////////////////////	
	//1. 학생 추가 
	public void add(int mID, int[] mScores)
	{
		scoresMap.put(mID, mScores);
		erased.remove(mID);//일단 추가된거니까 혹시나 삭제 목록에서는 지우자 
		assignedMap.put(mID, 0);
		
		//대학별로 총점 저장하고 대학별 pool에도 추가해 
		int[] totals=new int[M+1];
		for(int u=1;u<=M;u++) {
			totals[u]=calcTotal(mScores,mWeights[u-1]);
		}
		totalMap.put(mID, totals);
		
		//1번 대학부터 쭉 시뮬(일단 넣어->꼴찌 밀어내) 돌려보기 
		int student=mID;
		for(int u=1;u<=M;u++) {
			student=tryAccept(u,student);
			if(student==0) break;//배정 되었으면 끝 
		}
		
		if(student!=0) {//끝까지 갔는데도 0이 안되었으면 걍 미배정으로 둠 
			assignedMap.put(student, 0);
		}
	}
	

	//2. 학생 삭제 
	public void erase(int mID)
	{
		erased.add(mID);//삭제목록에 추가
		
		//현재 배정 대학 쳌
		int curUniv=getAssigned(mID);
		if(curUniv==0) return;//미배정이면 노상관 
		
		//배정된 학생이면 
		selected[curUniv].remove(mID);//선발명단에서 삭제
		assignedMap.put(mID, 0);//미배정 상태로 되돌리기  
		
		
		int empty=curUniv;//지금 공석난 대학 
		
		//공석 메꾸기 ㄱ
		//정원 안넘칠 때까지 연쇄적으로 메꾸기 
		while(empty!=0 && selected[empty].size()<N) {
			Integer cutLine=selected[empty].isEmpty()?null:selected[empty].last();//공석 난 대학에 선발된 학생의 커트라인(꼴찌 컷)
			Integer cand=(cutLine==null)?(pool[empty].isEmpty()?null:pool[empty].first()):pool[empty].higher(cutLine);//커트라인이 없어->그럼 걍 밀려난 애들 중 1등 데려와, 커트라인이 있으면 커트라인 바로 앞 애를 데려와 
			
			//공석 메꾸기용 cand 괜찮은 애를 찾을 때까지 스킵
			while(cand!=null) {
				
				if(erased.contains(cand)) {//삭제된 애면
					cand=pool[empty].higher(cand);//다음 대기후보로 패스
					continue;
				}
				
				int a=getAssigned(cand);//걔가 배정된 대학 
				
				if(a!=0 && a<empty) {//이미 더 좋은 대학이어도 패스
					cand=pool[empty].higher(cand);//다음 대기후보로
					continue;
				}
				
				if(a==empty) {//이미 그 대학에 배정된 애면
					cand=pool[empty].higher(cand);//다음 대기후보로 
					continue;
				}
				
				break;//메꿀 아이 찾음 
			}
			
			if(cand==null) break;//후보 없으면 메꾸기 불가
			
			//메꿀 아이의 현재 배정 대학
			int candAssigned=getAssigned(cand);
			
			if(candAssigned!=0) {//공석난 대학 메꿨으면
				selected[candAssigned].remove(cand);//원래 선발되었던 대학 선발 명단에서는 지워야지
				assignedMap.put(cand,0);
				
				//공석이 원래 얘의 대학으로 옮겨 감
				int nextEmpty=candAssigned;
				
				//공석 대학에 메꾸기
				selected[empty].add(cand);
				assignedMap.put(cand, empty);
				
				empty=nextEmpty;
			}else {//현재 배정 대학 없었으면
				//걍 바로 메꿔
				selected[empty].add(cand);
				assignedMap.put(cand, empty);
				break;
			}
		}
	}

	public int suggest(int mID)
	{
		if(erased.contains(mID)) return -1;
		
		int a=getAssigned(mID);//해당 학생의 현재 배정 대학
		return(a==0)?-1:a;//미배정이면 -1,배정되어있으면 그 대학 
	}
}