package star;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

class Solution {

    private static UserSolution usersolution = new UserSolution();

    private final static int CMD_INIT     = 0;
    private final static int CMD_CNT      = 1;
	private final static int CMD_POSITION = 2;
	
	private final static int MAX_SIZE = 1000;

	private static int Map[][] = new int[MAX_SIZE][MAX_SIZE];
	private static int Piece[][] = new int[5][5];
	private static int Data[] = new int[40000];
	
	private static void init_map(int N)
	{
		int idx = 0;

		int x = 0;
		for (int i = 0; i < (N / 25); i++) {
			for (int y = 0; y < N; y++) {
				int data = Data[idx++];
				int bit = 1;
				for (int m = 0; m < 25; m++) {
					if ((data & bit) != 0) Map[y][x + m] = 1;
					else Map[y][x + m] = 0;
					bit <<= 1;
				}
			}
			x += 25;
		}

		int dcnt = N % 25;
		if (dcnt != 0) {
			for (int y = 0; y < N; y++) {
				int data = Data[idx++];
				int bit = 1;
				for (int m = 0; m < dcnt; m++) {
					if ((data & bit) != 0) Map[y][x + m] = 1;
					else Map[y][x + m] = 0;
					bit <<= 1;
				}
			}
		}
	}

	private static void make_piece(int data)
	{
		int bit = 1;
		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < 5; k++) {
				if ((data & bit) != 0) Piece[i][k] = 1;
				else Piece[i][k] = 0;
				bit <<= 1;
			}
		}
	}
	
    private static boolean run(BufferedReader br) throws Exception
    {
    	int cmd, ans, ret;
    	int N, row, col, cnt;
 
        boolean ok = false;

		StringTokenizer st = new StringTokenizer(br.readLine(), " ");
        int Q = Integer.parseInt(st.nextToken());

        for (int i = 0; i < Q; i++) {
            st = new StringTokenizer(br.readLine(), " ");
            cmd = Integer.parseInt(st.nextToken());

            if (cmd == CMD_INIT) {
				N = Integer.parseInt(st.nextToken());
				cnt = Integer.parseInt(st.nextToken());
				for (int k = 0; k < cnt; k++) {
					st = new StringTokenizer(br.readLine(), " ");
					Data[k] = Integer.parseInt(st.nextToken());
				}

				init_map(N);
                usersolution.init(N, Map);
                ok = true;
            } else if (cmd == CMD_CNT) {
            	Data[0] = Integer.parseInt(st.nextToken());
            	ans = Integer.parseInt(st.nextToken());
            	make_piece(Data[0]);

				ret = usersolution.getCount(Piece);
				if (ret != ans) {
                	ok = false;
                }				
			} else if (cmd == CMD_POSITION) {
            	row = Integer.parseInt(st.nextToken());
            	col = Integer.parseInt(st.nextToken());
            	ans = Integer.parseInt(st.nextToken());
            	
            	ret = usersolution.getPosition(row, col);
            	if (ret != ans) {
                	ok = false;
                }
			}
			else ok = false;
        }
        return ok;
    }

    public static void main(String[] args) throws Exception {

        System.setIn(new java.io.FileInputStream("res/sample_input_star.txt"));
        
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer line = new StringTokenizer(br.readLine(), " ");

        int T = Integer.parseInt(line.nextToken());
        int MARK = Integer.parseInt(line.nextToken());

        for (int tc = 1; tc <= T; tc++) {
            int score = run(br) ? MARK : 0;
            System.out.println("#" + tc + " " + score);
        }

        br.close();
    }
}