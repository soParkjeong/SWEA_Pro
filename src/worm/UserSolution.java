package worm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class UserSolution
{
    // ===== 디버깅용 맵 출력 =====
    public void printMap()
    {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (game[x][y] == 0) System.out.print(". ");
                else System.out.print(game[x][y] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    static boolean debug = true;// 디버깅 on/off
    static int start=0;
    static int end=4;        

    private void advanceTo(int targetTime) {
        while (curTime<targetTime) {
            moveAll();
            // crushAll();   // 지금은 join/move만 보고 싶다 했으니 주석

            curTime++;
            
            if (debug && start<=curTime && curTime<=end) {
            	System.out.println("=== time=" + curTime + " after move ===");
                printMap();
            }
        }
    }
    
	int N;
	
	int curTime;
	
	int[][] game;
	HashMap<Integer,WormInfo> worms;
	HashSet<Integer> removed;
	
	static class WormInfo{
		int id;
		int Hdir;
		int growth;
		
		Deque<Point> body;

		
		WormInfo(int id,int Hdir,int growth){
			this.id=id;
			this.Hdir=Hdir;
			this.growth=growth;
			this.body=new ArrayDeque<>();
		}
	}
	
	static class Point{
		int x,y;
		Point(int x,int y){
			this.x=x;
			this.y=y;
		}
	}
	
    public void init(int N)
    {
    	this.N=N;
    	this.curTime=0;
    	
    	game=new int[N][N];
    	worms=new HashMap<>();
    	removed=new HashSet<>();
    	
        return;
    }

    public void join(int mTime, int mID, int mX, int mY, int mLength){
    	advanceTo(mTime);
        WormInfo w = new WormInfo(mID, 0, 0);

        for(int i=0;i<mLength;i++) {
        	int x=mX;
        	int y=mY+i;
        	
        	w.body.addLast(new Point(x,y));
        	game[x][y]=mID;
        }
        
        worms.put(mID, w);
        removed.remove(mID); 
        
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
        ArrayList<Integer> ids= new ArrayList<>(worms.keySet());
        for(int id: ids) {
            if(removed.contains(id)) continue;
            move(id); 
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
	
    //일직선인지 쳌 
    private boolean isStraight(WormInfo w) {
    	Iterator<Point> it = w.body.iterator();
    	//계속 두개씩 뽑아서 일직선인지 확인 
        Point a= it.next();
        Point b= it.next();

        int ddx= b.x-a.x;
        int ddy= b.y-a.y;

        Point before = b;
        while (it.hasNext()) {
            Point cur= it.next();
            int dx2= cur.x-before.x;
            int dy2= cur.y-before.y;
            if (dx2!=ddx|| dy2!=ddy) return false;//다르면 방향이 바뀐거니까 일직선 아님 
            before = cur;
        }
        return true;
    }
    
    public void move(int id) {
    	if(removed.contains(id)) return;
    	
    	WormInfo w=worms.get(id);
    	if(w==null) return;
    	
    	if(isStraight(w)) {//일직선이면 
    		w.Hdir=(w.Hdir+1)%4;//시계방향 
    	}
    	
    	//일직선이면 머리가 시계방향으로 돌아간 곳에 추가 
    	Point head=w.body.peekFirst();
        int nx = head.x + dx[w.Hdir];
        int ny = head.y + dy[w.Hdir];
        
        //범위 넘나?
        if(!Range(nx,ny)) {
        	removed.add(id);//소멸 처리 
        	return;
        }
        
       w.body.addFirst(new Point(nx,ny));
       
       game[nx][ny]=id;
       
       if (w.growth > 0) {//성장 잠재력 있던 애는 
           w.growth--; //머리만 앞으로 붙여주고 꼬리 그대로
       } else {//없던 애는 
           Point tail = w.body.pollLast(); //머리 앞으로 붙이고 꼬리 떼고 
           game[tail.x][tail.y] = 0;
       }
        
    }
    
    //충돌/흡수 처리 
    public void crush() {
    	//(hx1,hy1)좌표와 (hx2,hy2)좌표가 같아지는 경우는 그 두 지렁이 id는 removed에 add
    	//(hx1,hy1)좌표(m지렁이라고 치자)가 (game[x][y]==k(k!=0)인 좌표와 같음&& k지렁이의 hx,hy와는 다름)경우 m지렁이에 removed에 add/ k지렁이 growth+=m지렁이 mLength
    	//(hx1,hy1)좌표(m지렁이라고 치자)가 (game[x][y]==k(k!=0)인 좌표와 같음&& k지렁이의 hx,hy(hx2,hy2라고 치자)와는 다름)이고, (hx2,hy2)좌표(k 지렁이라고 치자)가 (game[x][y]==l(l!=0)인 좌표와 같음&& l지렁이의 hx,hy(hx3,hy3라고 치자)와는 다름)이면=> m,k지렁이 removed에 add/ l지렁이 growth+=k 지렁이 length 
        ArrayList<Integer> ids = new ArrayList<>(worms.keySet());

        HashSet<Integer> dead = new HashSet<>(removed); 
        HashMap<Integer, Integer> hit = new HashMap<>(); 

        //머리-머리 충돌: 동시 removed처리 
        for (int i = 0; i < ids.size(); i++) {
            int aId = ids.get(i);
            if (dead.contains(aId)) continue;
            WormInfo a = worms.get(aId);
            if (a == null) continue;
            Point ah = a.body.peekFirst();

            for (int j= i+1; j<ids.size(); j++) {
                int bId = ids.get(j);
                if (dead.contains(bId)) continue;
                WormInfo b = worms.get(bId);
                if (b==null) continue;
                Point bh = b.body.peekFirst();

                if (ah.x == bh.x && ah.y == bh.y) {
                    dead.add(aId);
                    dead.add(bId);
                }
            }
        }
    }
    
    
    public Solution.RESULT top5(int mTime){
    	advanceTo(mTime);

        Solution.RESULT res = new Solution.RESULT();
        res.cnt = 0;
        return res;
    }
}
