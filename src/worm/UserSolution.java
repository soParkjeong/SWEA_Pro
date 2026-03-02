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
    	advanceTo(mTime);
    	
        WormInfo w = new WormInfo(mID);

        for(int i=0;i<mLength;i++) {
        	int x=mX;
        	int y=mY+i;
        	
        	w.body.addLast(new Node(x,y,0));
        	game[x][y]=mID;
        }       
        worms.put(mID, w); 
    }
    
    public Solution.RESULT top5(int mTime){
    	advanceTo(mTime);
    	
    	Solution.RESULT res=new Solution.RESULT();
    	
    	ArrayList<WormInfo> list=new ArrayList<>(worms.values());
    	
    	list.sort((a,b)->{
    		if(a.body.size()==b.body.size())
    			return Integer.compare(b.id,a.id);
    		return Integer.compare(b.body.size(), a.body.size());
    	});
    	res.cnt=Math.min(5, list.size());
    	
    	for(int i=0;i<res.cnt;i++) {
    		res.IDs[i]=list.get(i).id;
    	}
    	return res;
    }

    //시뮬
    //move 
	//지금 일직선인 경우,
	//성장 잠재력이 1이상이면,
	//tx,ty는 그대로 Hdir=0이면 (hx+1,hy)/ 1이면 (hx,hy-1)/ 2이면 (hx-1,hy)/ 3이면 (hx,hy+1) && 성장잠재력--
	//성장 잠재력이 0이면 머리 이동, 꼬리 잘라내버리기 
	//Hdir=0이면 (hx+1,hy)/ 1이면 (hx,hy-1)/ 2이면 (hx-1,hy)/ 3이면 (hx,hy+1)
	
	//지금 일직선이 아닌 경우,
	//성장 잠재력이 1이상이면,   
	//tx,ty는 그대로 Hdir=0이면 (hx,hy-1)/ 1이면 (hx-1,hy)/ 2이면 (hx,hy+1)/ 3이면 (hx+1,hy) && 성장잠재력--
	//성장 잠재력이 0이면 머리 이동, 꼬리는 잘라내버리기 
	//Hdir=0이면 (hx+1,hy)/ 1이면 (hx,hy-1)/ 2이면 (hx-1,hy)/ 3이면 (hx,hy+1)
    public void moveAll() {
        ArrayList<int[]> willRemove = new ArrayList<>();

        for (int id : worms.keySet()) {
            WormInfo w = worms.get(id);
            if (w == null) continue;

            Node head = w.body.peekFirst();
            int dir = head.dir;

            // 꺾기
            if (w.turn) {
                dir = (dir + 1) & 3;
                w.turn = false;
            }
            head.dir = dir;

            // 머리 이동 
            int nx = head.x + dx[dir];
            int ny = head.y + dy[dir];
            w.body.addFirst(new Node(nx, ny, dir));

            //범위 나가면 소멸 
            if (!Range(nx, ny)) {
                willRemove.add(new int[]{id, -1, -1}); 

                if (w.growth > 0) {
                    w.growth--;
                } else {
                    Node tail = w.body.pollLast();
                    if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {
                        game[tail.x][tail.y] = 0;
                    }
                }
                continue;
            }

            //충돌 여부 쳌 
            int occ = game[nx][ny];
            if (occ != 0) {
                WormInfo other = worms.get(occ);

                //뒤에 움직일 애의 꼬리면서 성장잠재 없으면 없어질 자리임 
                boolean tailPass = false;
                if (other != null && occ > id && other.growth == 0) {
                    Node otail = other.body.peekLast();
                    if (otail != null && otail.x == nx && otail.y == ny) tailPass = true;
                }

                if (!tailPass) {
                    //머리-머리 충돌 
                    if (other != null && occ < id) {
                        Node oHead = other.body.peekFirst();
                        if (oHead != null && oHead.x == nx && oHead.y == ny) {
                            willRemove.add(new int[]{id, -1, -1});
                            willRemove.add(new int[]{occ, -1, -1});
 
                            if (w.growth > 0) w.growth--;
                            else {
                                Node tail = w.body.pollLast();
                                if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {
                                    game[tail.x][tail.y] = 0;
                                }
                            }
                            continue;
                        }
                    }

                    //머리-몸통 충돌 
                    if (w.growth > 0) w.growth--;
                    else {
                        Node tail = w.body.pollLast();
                        if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {
                            game[tail.x][tail.y] = 0;
                        }
                    }

                    int gainLen = w.body.size(); 
                    willRemove.add(new int[]{id, gainLen, occ});
                    continue;
                }
            }

            //이동한 자리로 갱신 
            game[nx][ny] = id;

            //성장/꼬리 처리
            if (w.growth > 0) {
                w.growth--;
            } else {
                Node tail = w.body.pollLast();
                if (tail != null && Range(tail.x, tail.y) && game[tail.x][tail.y] == id) {
                    game[tail.x][tail.y] = 0;
                }
            }

            //일직선이면 다음에 꺾기 
            Node newHead = w.body.peekFirst();
            Node newTail = w.body.peekLast();
            if (newHead != null && newTail != null && newHead.dir == newTail.dir) {
                w.turn = true;
            }
        }

        boolean[] removedFlag = new boolean[20001]; 

        for (int[] r : willRemove) {
            int deadId = r[0];
            if (deadId >= 0 && deadId < removedFlag.length && removedFlag[deadId]) continue;
            if (deadId >= 0 && deadId < removedFlag.length) removedFlag[deadId] = true;

            WormInfo dead = worms.remove(deadId);
            if (dead == null) continue;

            for (Node p : dead.body) {
                if (Range(p.x, p.y) && game[p.x][p.y] == deadId) game[p.x][p.y] = 0;
            }

            int gainLen = r[1];
            int victim = r[2];
            if (gainLen != -1) {
                WormInfo vw = worms.get(victim);
                if (vw != null) vw.growth += gainLen;
            }
        }
    }
}