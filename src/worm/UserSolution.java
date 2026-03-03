package worm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.TreeMap;

class UserSolution{
	int N;
	int curTime;
	
	int[][] game;
	
	TreeMap<Integer,WormInfo> worms;
//	HashSet<Integer> removed;
	
	static class WormInfo{
		int id;
		int growth;
		boolean turn;
		
		Deque<Node> body;

		WormInfo(int id){
			this.id=id;
			this.growth=0;
			this.turn=true;//꺾어야하나?(지금 일직선임)
			
			this.body=new ArrayDeque<>();
		}
	}
	
	static class Node{
		int x,y,dir;
		
		Node(int x,int y,int dir){
			this.x=x;
			this.y=y;
			this.dir=dir;
		}
	}
	
	static final int[] dx = {0, 1, 0, -1};
	static final int[] dy = {-1, 0, 1, 0};
	
	private boolean Range(int x, int y) {
	    if(x>=0 && x<N && y>=0 && y<N) {
	    	return true;
	    }
	    return false;
	}
	
    public void init(int N){
    	this.N=N;
    	this.curTime=0;
    	
    	game=new int[N][N];
    	worms=new TreeMap<>();
    }
    
    private void advanceTo(int targetTime) {
        while (curTime<targetTime) {
            moveAll();
            curTime++;
        }
    }

    public void join(int mTime, int mID, int mX, int mY, int mLength){
        // 현재 시각(curTime)을 mTime까지 1초씩 시뮬레이션 진행(moveAll 호출)
        advanceTo(mTime);

        // 새 지렁이 생성
        WormInfo w = new WormInfo(mID);

        // (mX, mY)에서 아래 방향으로 길이 mLength만큼 몸통 생성
        // dir=0으로 초기화(문제에서 정한 초기 방향)
        for(int i=0;i<mLength;i++) {
            int x=mX;
            int y=mY+i;

            // 꼬리 쪽부터 addLast로 쌓으면, 결과적으로
            // body.peekFirst()가 머리, body.peekLast()가 꼬리가 되는 형태로 관리 가능
            w.body.addLast(new Node(x,y,0));

            // 게임판에 "이 칸은 mID 지렁이가 점유" 표시
            game[x][y]=mID;
        }

        // ID로 지렁이 등록 (TreeMap이라 ID 오름차순 순회가 가능)
        worms.put(mID, w);
    }

    public Solution.RESULT top5(int mTime){
        // mTime까지 시뮬레이션을 먼저 진행해서 상태를 최신으로 맞춤
        advanceTo(mTime);

        Solution.RESULT res=new Solution.RESULT();

        // 현재 살아있는 지렁이들을 리스트로 뽑아 정렬해서 상위 5개 추출
        ArrayList<WormInfo> list=new ArrayList<>(worms.values());

        // 길이 내림차순, 길이 같으면 id 내림차순
        list.sort((a,b)->{
            if(a.body.size()==b.body.size())
                return Integer.compare(b.id,a.id);
            return Integer.compare(b.body.size(), a.body.size());
        });

        res.cnt=Math.min(5, list.size());

        // 상위 5개 ID 담기
        for(int i=0;i<res.cnt;i++) {
            res.IDs[i]=list.get(i).id;
        }
        return res;
    }

    public void moveAll() {
        // 이번 1초(턴)에서 죽을 지렁이 목록을 모아두는 리스트
        // 원소: {deadId, gainLen, victimId}
        // - 범위밖 죽음/단순죽음: {deadId, -1, -1}
        // - 몸통 충돌로 죽고 흡수 발생: {deadId, deadLen, victimId}
        ArrayList<int[]> willRemove = new ArrayList<>();

        // 모든 지렁이를 ID 오름차순으로 이동 (TreeMap keySet 순회)
        for (int id : worms.keySet()) {
            WormInfo w = worms.get(id);//지렁이 한마리 뽑아
            if (w == null) continue;

            Node head = w.body.peekFirst(); //걔 머리의 좌표 조회
            int dir = head.dir;            //걔 머리의 방향

            if (w.turn) {//걔가 지금 일직선이면 
                dir = (dir + 1) & 3;//이번엔 시계방향으로 회전
                w.turn = false;//이제 일직선 아니지?
            }
            head.dir = dir;//바꾼 방향으로 머리 방향 갱신 

            //시계 방향으로 바꿨으니까 한 칸 가야지 
            int nx = head.x + dx[dir];
            int ny = head.y + dy[dir];

            w.body.addFirst(new Node(nx, ny, dir));//회전하고 한칸 움직인 그 머리를 새로운 머리로 지렁이 몸통에 추가(젤 앞에 추가-addFirst)

            //범위 밖으로 나가면 소멸 처리 
            if (!Range(nx, ny)) {
                //그냥 소멸 
                willRemove.add(new int[]{id, -1, -1});

                if (w.growth > 0) {//잠재력 있던 애면 
                    w.growth--;//잠재력 1 감소
                } else {//잠재력 없던 애면 
                    Node tail = w.body.pollLast();//꼬리 제거해
                    if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {//꼬리가 있고, 범위 안에 있고, 이 칸이 뽑은 지렁이 맞지?
                        game[tail.x][tail.y] = 0;//맵에서도 제거
                    }
                }
                continue; //다음 지렁이로
            }

            //충돌 검사 
            int occ = game[nx][ny]; //머리가 가려하는 그 자리(nx,ny)에 이미 들어가있는 지렁이 id
            if (occ != 0) {//거기에 이미 들어가있는 지렁이가 있으면
                WormInfo other = worms.get(occ);//그 지렁이 정보 뭔데

                // --------
                // 4-A) "꼬리 예외" 체크
                //   - 나보다 나중에 움직일 지렁이(occ > id)의 "꼬리" 칸이고
                //   - 그 지렁이가 성장잠재 0이라 꼬리가 이번 턴에 빠질 자리라면
                //   => 그 칸은 사실상 비워질 예정이므로 이동 허용
                // --------
                boolean tailPass = false;//곧 비워질 자리인지 여부 플래그
                if (other != null && occ > id && other.growth == 0) {//이미 들어가있는 지렁이가 있고 && 그 지렁이 id가 지금 뽑은 지렁이 id보다 더 크고(즉, 나보다 나중에 움직일 지렁이임) && 그 지렁이 성장잠재력이 없으면
                    Node otail = other.body.peekLast();//이미 들어가있는 지렁이 꼬리 조회
                    if (otail != null && otail.x == nx && otail.y == ny) tailPass = true;//꼬리가 있고 && 꼬리 좌표가 내 지렁이 머리가 갈 곳과 동일하면 => 곧 비워질 자리인걸로 바꿔주기(성장 잠재력 없는 지렁이 꼬리였으니까 없어질 자리임)
                }

                //곧 비워질 자리가 아니면
                if (!tailPass) {

                    // --------
                    // 4-B) 머리-머리 충돌 체크
                    //   - occ < id 인 경우, 상대는 이미 이번 턴에 이동을 끝낸 지렁이
                    //   - 따라서 "그 칸이 상대의 머리인지"를 확인 가능
                    //   - 머리-머리면 둘 다 죽음
                    // --------
                    if (other != null && occ < id) {//이미 들어가있는 지렁이가 있는데 && 그 지렁이가 지금 뽑은 지렁이 id보다 작아(즉, 나보다 먼저 움직였을 지렁이임)
                        Node oHead = other.body.peekFirst();//이미 들어가있는 지렁이 머리 조회
                        if (oHead != null && oHead.x == nx && oHead.y == ny) {//이미 들어가있는 지렁이가 머리가 있고 && 그 머리 좌표가 내 지렁이 머리가 갈 곳과 동일하면 
                            // 둘 다 소멸(흡수 없음)
                            willRemove.add(new int[]{id, -1, -1});
                            willRemove.add(new int[]{occ, -1, -1});

                            //내 지렁이 성장/꼬리 규칙 처리(정답 코드처럼 죽는 순간에도 처리)
                            if (w.growth > 0) w.growth--;//성장 잠재력 있던 애면 -1
                            else {//성장 잠재력 없던 애면
                                Node tail = w.body.pollLast();//꼬리 제거해
                                if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {//꼬리가 있고 && 범위 내에 있고 && 이 칸이 뽑은 지렁이 맞지?
                                    game[tail.x][tail.y] = 0;//맵에서도 제거 
                                }
                            }
                            continue;//다음 지렁이로
                        }
                    }

                    // --------
                    // 4-C) 머리-몸통 충돌(또는 머리-꼬리(예외 아닌)) 처리
                    //   - 나는 죽고, 상대(occ)가 내 길이만큼 growth를 얻음
                    // --------

                    // 내 성장/꼬리 규칙 처리(죽더라도 한 턴 규칙 적용)
                    if (w.growth > 0) w.growth--;//성장 잠재력이 있는 경우에는 -1
                    else {//그 외에는 
                        Node tail = w.body.pollLast();//꼬리 제거 
                        if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {//꼬리가 있고 && 범위 내에 있고 && 이 칸이 뽑은 지렁이 맞지?
                            game[tail.x][tail.y] = 0;//맵에서도 제거 
                        }
                    }

                    // 지금 w.body에는 "새 머리"까지 들어간 상태라
                    // 이 길이를 그대로 흡수량으로 사용 (정답 코드처럼 '죽는 시점 길이')
                    int gainLen = w.body.size();
                    // 죽는 id가 victim(occ)에게 gainLen만큼 성장잠재를 준다
                    willRemove.add(new int[]{id, gainLen, occ});
                    continue;
                }
            }

            // =========================
            // 5) 충돌이 없거나, 꼬리 예외로 통과한 경우 => 정상 이동 확정
            // =========================
            game[nx][ny] = id; // 새 머리 칸 점유 표시

            // 성장/꼬리 규칙
            if (w.growth > 0) {
                w.growth--; // 성장 잠재가 있으면 잠재력만 -1 
            } else {//성장 잠재가 없으면
                Node tail = w.body.pollLast();//꼬리 제거 
                if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {//꼬리가 있고 && 꼬리가 범위 내에 있고 && 맵에서도 그 지렁이로 점유되어있지?
                    game[tail.x][tail.y] = 0;//그러면 맵에서도 제거해주자 
                }
            }

            // =========================
            // 6) "직선 상태" 판정 => 다음 턴에 꺾어야 하는지(turn=true)
            //   - 머리 방향 == 꼬리 방향이면 몸 전체가 1자로 펴졌다고 판단
            // =========================
            Node newHead = w.body.peekFirst();
            Node newTail = w.body.peekLast();
            if (newHead != null && newTail != null && newHead.dir == newTail.dir) {//머리 방향이랑 꼬리 방향 같아?
                w.turn = true;//그러면 일직선이야(다음 턴에 꺾어야 해) 
            }
        }

        // willRemove에 같은 id가 여러 번 들어갈 수 있어서 중복 제거용 플래그
        boolean[] removedFlag = new boolean[20001];

        // =========================
        // 7) 이번 턴에 죽는 지렁이들 일괄 제거 + 흡수 적용
        // =========================
        for (int[] r : willRemove) {
            int deadId = r[0];

            // 이미 제거 처리한 지렁이면 skip
            if (deadId >= 0 && deadId < removedFlag.length && removedFlag[deadId]) continue;
            if (deadId >= 0 && deadId < removedFlag.length) removedFlag[deadId] = true;

            // 자료구조에서 삭제
            WormInfo dead = worms.remove(deadId);
            if (dead == null) continue;

            // 맵(game)에서도 해당 지렁이 몸통 칸들 전부 지움
            for (Node p : dead.body) {
                if (Range(p.x, p.y) && game[p.x][p.y] == deadId) game[p.x][p.y] = 0;
            }

            // 흡수 처리: gainLen != -1 이면 victim에게 growth 더해줌
            int gainLen = r[1];
            int victim = r[2];
            if (gainLen != -1) {
                WormInfo vw = worms.get(victim);
                if (vw != null) vw.growth += gainLen;
            }
        }
    }
}