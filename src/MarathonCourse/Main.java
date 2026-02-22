package MarathonCourse;

import java.util.Scanner;

class Solution {

    private static UserSolution usersolution = new UserSolution();

    private final static int CMD_INIT   = 100;
    private final static int CMD_ADD	= 200;
	private final static int CMD_REMOVE	= 300;
	private final static int CMD_GETLEN	= 400;
	
    private static boolean run(Scanner sc)
    {
    	int id[] = new int[10];
    	int sa[] = new int[10];
    	int sb[] = new int[10];
    	int len[] = new int[10];
		String strTmp;
 
        boolean ok = false;

        int Q = sc.nextInt();

        for (int q = 0; q < Q; q++) {
            int cmd = sc.nextInt();

            if (cmd == CMD_INIT) {
				int N = sc.nextInt();
				usersolution.init(N);
                ok = true;
            } else if (cmd == CMD_ADD) {
				strTmp = sc.next();
				int K = sc.nextInt();
				for (int i = 0; i < K; i++) {
					strTmp = sc.next();
					id[i] = sc.nextInt();
					strTmp = sc.next();
					sa[i] = sc.nextInt();
					strTmp = sc.next();
					sb[i] = sc.nextInt();
					strTmp = sc.next();
					len[i] = sc.nextInt();
				}
				usersolution.addRoad(K, id, sa, sb, len);
			} else if (cmd == CMD_REMOVE) {
				strTmp = sc.next();
				id[0] = sc.nextInt();
            	usersolution.removeRoad(id[0]);
			} 
            else if (cmd == CMD_GETLEN) {
				strTmp = sc.next();
            	id[0] = sc.nextInt();
        		int ret = usersolution.getLength(id[0]);
				strTmp = sc.next();
        		int ans = sc.nextInt();
            	if (ret != ans) {
                	ok = false;
                }
			}
			else ok = false;
        }
        return ok;
    }

    public static void main(String[] args) throws Exception {

        System.setIn(new java.io.FileInputStream("res/sample_input_MarathonCourse.txt"));
        
		Scanner sc = new Scanner(System.in);

        int T = sc.nextInt();
        int MARK = sc.nextInt();

        for (int tc = 1; tc <= T; tc++) {
            int score = run(sc) ? MARK : 0;
            System.out.println("#" + tc + " " + score);
        }

        sc.close();
    }
}
