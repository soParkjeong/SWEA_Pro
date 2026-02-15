package Voca;

import java.util.Arrays;
import java.util.HashMap;

class UserSolution
{
	static int word=26;
	static int max_word=70001;
	static int max_node=70000*5+1;
	
	//Trie 
	static int[][] child=new int[max_node][word];
	static int[] sub=new int[max_node];//그 노드(알파벳)을 갖고 있는 단어 개수 저장용(subtree 개수 저장이라고 생각하면 됨)
	static int[] endId=new int[max_node];//끝인지 여부 쳌하고 그 알파벳으로 끝나는 단어 id 저장용 
	static int[] bestId=new int[max_node];//그 알파벳을 포함하는 단어 중 우선순위 조건에 따라 젤 우선으로 오는 단어 id 저장용
	
	static int nodeCnt=0;//노드 번호 부여용 
	
	//단어
	static String[] wordStr=new String[max_word];//id별 문자열 저장용 
	static int[] importance=new int[max_word];//id별 문자열 중요도 저장용 
	static int[] enroll=new int[max_word];//등록순서 저장용-클수록 최신
	static int wordCnt, enrollTime;
	
	//완전 일치 검색용
	static HashMap<String,Integer> wordToId;
	
	//현재
	static int curRank;//현재 페이지
	static int curId;//현재 단어
	
	//bestId 갱신용
	static int better(int a,int b) {
		if(a==0) return b;
		if(b==0) return a;
		if(importance[a]!=importance[b]) {
			return (importance[a]>importance[b])? a:b;
		}
		return (enroll[a]<enroll[b])? a:b;//enroll 값이 작을수록 먼저 등록된 것(우선)  
	}
	
	//기존에 없던 알파벳 경로 등장 
	static void resetNode(int u) {
		Arrays.fill(child[u], 0);//u의 모든 자식 링크 초기화
		sub[u]=0;
		endId[u]=0;
		bestId[u]=0;
	}
	
	//새 노드 만들기
	static int newNode() {
		int u=nodeCnt++;//노드번호 부여 
		resetNode(u);
		return u;
	}
	
	//Trie에 단어 추가
	//단어 id와 문자열 w를 Trie에 삽입하면서:
	//sub(prefix 단어 수) 갱신
	//bestId(prefix 최고 후보) 갱신
	//endId 세팅
	static void insertWord(int id,String w) {//id번 단어 Trie에 추가 
		int cur=0;//root에서 시작
		
		//root도 모든 단어의 prefix이므로 포함
		//sub[root]=전체 단어 수 역할
		sub[cur]++;
		bestId[cur]=better(bestId[cur],id);
		
		//한 글자씩 내려가며 노드 생성/이동
		for(int i=0;i<w.length();i++) {
			int c=w.charAt(i)-'a';//현재 문자 인덱스(0~25)
			int nxt=child[cur][c];//현재 노드에서 c로 가는 자식
			
			if(nxt==0) {//그 알파벳 노드가 없으면
				nxt=newNode();//새로 만들어
				child[cur][c]=nxt;//자식으로 추가해
			}
			cur=nxt;//밑으로 내려가 
			
			sub[cur]++;//그 prefix 가진 단어 1개 추가됨
			bestId[cur]=better(bestId[cur],id);//bestId 갱신해 
		}
	
		endId[cur]=id;//끝자리가 그 노드인 단어로 단어 id 추가
	}
	
	//prefix 문자열 s의 Trie 노드 번호를 반환
	//중간에 끊기면 -1(prefix 자체가 존재하지 않음)
	static int findNode(String s) {
		int cur=0;
		for(int i=0;i<s.length();i++) {
			int c=s.charAt(i)-'a';
			int nxt=child[cur][c];
			if(nxt==0) return -1;//경로가 없으면 prefix 없음
			cur=nxt;
		}
		return cur;
	}
	
	//rank/select
	//rank: 사전 순서 찾기 함수
	static int rank(String w) {
		int cur=0;//root에서 시작
		int ans=0;//w보다 앞에 있는 단어 개수 누적
		
		for(int i=0;i<w.length();i++) {//문자열 길이만큼 
			if(endId[cur]!=0) ans++;//현재 노드가 단어 끝이면, 그 단어는 w보다 앞 
			
			int c=w.charAt(i)-'a';
			
			for(int ch=0;ch<c;ch++) {
				int nxt=child[cur][ch];
				if(nxt!=0) ans+=sub[nxt];
			}
			
			cur=child[cur][c];
		}
		return ans;
	}
	
	//select: k번째 단어의 id 찾기 함수
	static int selectByRank(int k) {
		int cur=0;//root에서 시작
		
		while(true) {
			if(endId[cur]!=0) {
				if(k==1) return endId[cur];
				k--;
			}
			
			for(int ch=0;ch<26;ch++) {
				int nxt=child[cur][ch];
				if(nxt==0) continue;
				
				if(sub[nxt]<k) {
					k-=sub[nxt];
				}else {
					cur=nxt;
					break;
				}
			}
		}
	}
	
	static Solution.PAGE page(int num,String w){
        Solution.PAGE p = new Solution.PAGE();
        p.no = num;
        p.word = w;
        return p;
	}
	
	static Solution.PAGE failPage(){
		return page(-1,"");
	}
	
	static Solution.PAGE makePageById(int id){
		curId=id;
		curRank=rank(wordStr[id])+1;
		return page(curRank,wordStr[id]);
	}

////////////////////////////////////////////////////////////////
	//API 구현
	public void init()
	{
		//단어를 id로 해시맵
		wordToId=new HashMap<>(max_word*2);
		
		//Trie 초기화
		nodeCnt=0;
		newNode();
		
		//단어 info 초기화
		wordCnt=0;
		enrollTime=0;
		
		//초기 단어 'a'등록
		int id=++wordCnt;
		wordStr[id]="a";
		importance[id]=1;
		enroll[id]=++enrollTime;
		wordToId.put("a",id);
		
		//Trie에 삽입하여 다 갱신
		insertWord(id,"a");
		
		curId=id;
		curRank=1;

	}

	public Solution.PAGE add(String mWord, int mImportance)
	{
		//새 id 부여 + 단어 정보 저장
		int id=++wordCnt;
		wordStr[id]=mWord;
		importance[id]=mImportance;
		enroll[id]=++enrollTime;
		
		//완전 일치 검색용-HashMap에 등록
		wordToId.put(mWord, id);
		
		//Trie 삽입(여기서 sub/best/terminal/endId) 갱신용
		insertWord(id,mWord);
		
		//현재 페이지를 방금 추가한 단어로 변경+반환
		return makePageById(id);
	}

	public Solution.PAGE move(int mDir)
	{
		//mDir=1이면 다음 페이지, -1이면 이전 페이지
		curRank+=mDir;
		
		//사전순 curRank번째 단어 id 찾기
		int id=selectByRank(curRank);
		curId=id;
		
		//rank는 이미 알고 있으니 바로 반환
		Solution.PAGE res=new Solution.PAGE();
		res.no=curRank;
		res.word=wordStr[id];
		return res;
	}

	public Solution.PAGE search(String mStr)
	{
		//완전 일치 찾기
		Integer exact=wordToId.get(mStr);
		if(exact!=null) {
			return makePageById(exact);
		}
		
		//prefix가 존재하는지 Trie에서 확인
		int node=findNode(mStr);
		if(node==-1) return failPage();//prefix로 시작하는 단어 자체가 없음
		
		//prefix 하에서 best 후보 id
		int cand=bestId[node];
		if(cand==0) return failPage();
		
		//현재 페이지를 cand로 변경 + 반환 
		return makePageById(cand);
	}

	public Solution.PAGE go(int mNo)
	{
		//mNo 페이지로 이동
		curRank=mNo;
		
		int id=selectByRank(curRank);
		curId=id;
		
		Solution.PAGE res=new Solution.PAGE();
		res.no=curRank;
		res.word=wordStr[id];
		return res;
	}
}