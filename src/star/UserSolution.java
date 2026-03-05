package star;

import java.util.Arrays;
import java.util.HashMap;

class UserSolution {
	int N;
	int r;
	int c;

	int[][] plane;
	
	//종류 ID 저장용
	int[][] type;
	
	HashMap<String,Integer> typeIdByKey;
	int typeCnt;
	
	//종류별 개수
	int[] catCnt;
	//그 종류의 가장 위-왼 타일 중앙값
	int[] bestCenter;
	
	StringBuilder sb=new StringBuilder();

	//문자열로 변환 함수
	String makeString(int r,int c) {
		StringBuilder sb=new StringBuilder(25);
		
		for(int i=0;i<5;i++) {
			for(int j=0;j<5;j++) {
				sb.append(plane[r+i][c+j]);
			}
		}
		return sb.toString();
	}
	
	boolean isStar(int r, int c) {
	    boolean top = false, bottom = false, left = false, right = false;

	    for (int j = 0; j < 5; j++) {
	        if (plane[r][c + j] == 1) { top = true; break; }//1개라도 있으면 패스 
	    }

	    for (int j = 0; j < 5; j++) {
	        if (plane[r + 4][c + j] == 1) { bottom = true; break; }
	    }

	    for (int i = 0; i < 5; i++) {
	        if (plane[r + i][c] == 1) { left = true; break; }
	    }

	    for (int i = 0; i < 5; i++) {
	        if (plane[r + i][c + 4] == 1) { right = true; break; }
	    }

	    return top && bottom && left && right;
	}
	
    int starCnt(int r, int c) {
        int cnt = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                cnt += plane[r + i][c + j];
            }
        }
        return cnt;
    }
	
    String rotate90(String s) {
        char[] res = new char[25];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int from = i * 5 + j;
                int ni = j;
                int nj = 4 - i;
                int to = ni * 5 + nj;
                res[to] = s.charAt(from);
            }
        }
        return new String(res);
    }
    
    // 문자열이 들어오면 4개 버전 중 사전순 젤 앞에꺼로 바꾸고 거기에 해당하는 key로 반환해주기-> 그러면 어떤 종류의 별자리 타일인지 회전해서 들어와도 찾아낼 수 있음 
    String king(String s) {
        String a = s;
        String b = rotate90(a);
        String c = rotate90(b);
        String d = rotate90(c);

        String key = a;
        if (b.compareTo(key) < 0) key = b;
        if (c.compareTo(key) < 0) key = c;
        if (d.compareTo(key) < 0) key = d;

        return key;
    }
    
    int getOrCreateTypeId(String key) {
        Integer id = typeIdByKey.get(key);
        if (id != null) return id;

        id = ++typeCnt;
        typeIdByKey.put(key, id);
        return id;
    }

    // 5x5 영역 모든 칸에 typeId 기록
    void fillType(int id, int r, int c) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                type[r + i][c + j] = id;
            }
        }
    }
    
	public void init(int N, int mPlane[][])
	{
		this.N=N;
		this.plane=mPlane;
		
		type=new int[N][N];
		for(int i=0;i<N;i++) Arrays.fill(type[i], -1);
		
		typeIdByKey=new HashMap<>();
		typeCnt=0;
		
		int cap=N*N+1;
		catCnt=new int[cap];
		bestCenter=new int[cap];
		
		for(int r=0;r<=N-5;r++) {
			for(int c=0;c<=N-5;c++) {
				//성립 조건 쳌
				if(starCnt(r,c)!=7) continue;
				if(!isStar(r,c)) continue;
				
				String s=makeString(r,c);
				
				String key=king(s);//4가지 버전 중 사전순으로 가장 앞에 해당하는 타일을 키로! 
				
				int id=getOrCreateTypeId(key);
				
				catCnt[id]++;
				
				int center=(r+2)*10000+(c+2);
				if(bestCenter[id]==0 || center<bestCenter[id]) {
					bestCenter[id]=center;
				}
				
				fillType(id,r,c);
			}
		}
	}

	public int getCount(int mPiece[][]){   
		
	   StringBuilder sb = new StringBuilder(25);
	    for (int i = 0; i < 5; i++) {
	        for (int j = 0; j < 5; j++) {
	            sb.append(mPiece[i][j]); // 0 또는 1
	        }
	    }
	    
	    String key=king(sb.toString());//이 별자리 타일 종류의 대표(4가지 회전 버전 중 사전순 젤 앞) 키
	    
	    Integer id=typeIdByKey.get(key);//그 대표키가 속한 종류의 id
	    if(id==null) return 0;
	    return catCnt[id];//그 종류id에 해당하는 별자리 타일 개수 반환 
	}

	public int getPosition(int mRow, int mCol)
	{
	    int id = type[mRow][mCol];
	    return bestCenter[id];
	}
}