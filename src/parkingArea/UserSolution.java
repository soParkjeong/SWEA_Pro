package parkingArea;

import java.util.*;

import parkingArea2.Solution;

class UserSolution {
    //전역 변수
    int N, M, L;

    HashMap<String, ParkInfo> parked;//주차 중 차량

    HashMap<String, TowInfo> towed;//견인 기록

    PriorityQueue<ExpireNode> towPQ;//견인 순서 관리용

    int[] emptySlots;//구역별 슬롯 개수 저장용 배열
    PriorityQueue<Integer>[] freeSlots;//빈 슬롯 정렬용

    //search-상위 5개 뽑기용
		//주차차 끝자리 4개 저장용,견인차 끝자리 4개 저장용 TreeSet
    TreeSet<String>[] parkedByZ;
    TreeSet<String>[] towedByZ;
		
		//-------------
    //정렬 조건
		//TreeSet 내 자동차 정렬 조건
		//1. XX 작을수록
		//2. Y 알파벳 순 빠를수록
    Comparator<String> carComparator = (a, b) -> {
        int aXX = (a.charAt(0) - '0') * 10 + (a.charAt(1) - '0');
        int bXX = (b.charAt(0) - '0') * 10 + (b.charAt(1) - '0');
        if (aXX != bXX) return aXX - bXX;

        char aY = a.charAt(2);
        char bY = b.charAt(2);
        if (aY != bY) return aY - bY;

        return a.compareTo(b);
    };

    //내부 클래스
		//주차 정보-입차시각,구역,슬롯
    static class ParkInfo {
        int enterTime, area, slot;

        ParkInfo(int t, int a, int s) {
            enterTime = t;
            area = a;
            slot = s;
        }
    }

    //견인 정보-입차시각,견인시각
    static class TowInfo {
        int enterTime, towTime;

        TowInfo(int e, int t) {
            enterTime = e;
            towTime = t;
        }
    }

    //견인차 담을 클래스
    static class ExpireNode {
        int expireTime;
        String carNo;
        int enterTime;

        ExpireNode(int e, String c, int t) {
            expireTime = e;
            carNo = c;
            enterTime = t;
        }
    }

		//---------

    //끝 4자리 떼어내기 함수
    int getTail(String carNo) {
        int t = 0;
        for (int i = 3; i < 7; i++) {
            t = t * 10 + (carNo.charAt(i) - '0');
        }
        return t;
    }

    //주차위치 조합 함수
    String parkArea(int area, int slot) {
        char c = (char) ('A' + area);
        return String.format("%c%03d", c, slot);
    }
		
		//-------------------------
    //1. init
    void init(int N, int M, int L) {
        this.N = N;
        this.M = M;
        this.L = L;

        parked = new HashMap<>();
        towed = new HashMap<>();
        towPQ = new PriorityQueue<>((a, b) -> a.expireTime - b.expireTime);//견인 기록

        emptySlots = new int[N];//구역 수만큼의 배열-빈 슬롯 개수 저장용
        freeSlots = new PriorityQueue[N];//구역 별 빈 슬롯 정렬용으로 pq 생성

        for (int i = 0; i < N; i++) {
            freeSlots[i] = new PriorityQueue<>();//N개의 구역에 각각 빈 슬롯 정렬용 PQ 생성
            for (int s = 0; s < M; s++) {
                freeSlots[i].offer(s);//M개의 빈 슬롯 저장
            }
            emptySlots[i] = M;//N개의 구역 내 각각 M개의 빈 슬롯 저장
        }

        parkedByZ = new TreeSet[10000];//0000~9999
        towedByZ = new TreeSet[10000];

        for (int i = 0; i < 10000; i++) {
            parkedByZ[i] = new TreeSet<>(carComparator);
            towedByZ[i] = new TreeSet<>(carComparator);
        }
    }

    //견인 처리
    void Tow(int mTime) {
        while (!towPQ.isEmpty() && towPQ.peek().expireTime <= mTime) {//PQ가 비어있지 않고, 출차시각이 견인될 시각(입차시각+L)보다 이미 지나있으면(<=)->견인 ㄱㄱ
            ExpireNode e = towPQ.poll();//견인될 차 뽑기
            ParkInfo info = parked.get(e.carNo);//주차된 차 중 견인될 차로 뽑힌 차량 번호에 해당하는 정보

            //이미 출차된 경우 or 재입차된 경우
            if (info == null) continue;//주차차량 정보가 빈 경우==이미 출차
            if (info.enterTime != e.enterTime) continue;//주차차량 정보 내 입차 시각이 견인될 차 입차 시각과 같지 않다면==재입차된 경우

            //견인 처리
            parked.remove(e.carNo);

            freeSlots[info.area].offer(info.slot);//차량 정보 내 구역 내 PQ에 빈 슬롯으로 추가
            emptySlots[info.area]++;//해당 구역 내 빈 슬롯 개수 +1

            int tail = getTail(e.carNo);//차 번호 중 끝자리 4개
            parkedByZ[tail].remove(e.carNo);//견인될 차니까 주차된 차에서는 제거 
            towedByZ[tail].add(e.carNo);//견인 차 목록에 추가

            towed.put(e.carNo, new TowInfo(info.enterTime, e.expireTime));//견인 목록에 추가
        }
    }

		//---------------------------
    //2. enter
    Solution.RESULT_E enter(int mTime, String mCarNo) {
        Tow(mTime);
        
        String carNo = new String(mCarNo);
        int tail=getTail(carNo);

        //견인 기록에 지금 차가 기록되어있으면 재입차된거니까 견인기록에서 삭제해줘야 함
        if (towed.containsKey(carNo)) {
            towed.remove(carNo);
            towedByZ[tail].remove(carNo);
        }

        //입차한 차 주차
        //구역 먼저 선택(빈 슬롯 젤 많은 구역 먼저)
        int bestArea = -1;
        int maxEmpty = 0;
        for (int i = 0; i < N; i++) {
            if (emptySlots[i] > maxEmpty) {
                maxEmpty = emptySlots[i];
                bestArea = i;
            }
        }
        
        Solution.RESULT_E res=new Solution.RESULT_E();

        //빈 슬롯이 없는 경우(주차할 곳이 없는 경우)에는 0 반환
        if (maxEmpty == 0) {
            res.success = 0;
            res.locname = "";
            return res;
        }

        //이제 구역 내 어떤 슬롯에 주차할지 정하기
        //구역 내 PQ에서 젤 먼저 뽑힌 슬롯에 주차
        int slot = freeSlots[bestArea].poll();
        emptySlots[bestArea]--;//그 구역 내 빈 슬롯 개수 한 개 차감

        parked.put(carNo, new ParkInfo(mTime, bestArea, slot));//주차 목록에 1대 추가
        parkedByZ[getTail(carNo)].add(carNo);//끝 4자리 treeSet에 carNo 저장

        towPQ.offer(new ExpireNode(mTime + L, carNo, mTime));

        res.success = 1;
        res.locname = parkArea(bestArea, slot);
        return res;
    }

		//---------------

    //3. pullout
    int pullout(int mTime, String mCarNo) {
        Tow(mTime);//출차시각 기준으로 자동 견인
        
        String carNo = new String(mCarNo);
        int tail=getTail(carNo);

        //출차했으니까 주차 목록에서 제거
        if (parked.containsKey(carNo)) {
            ParkInfo p_info = parked.remove(carNo);
            freeSlots[p_info.area].offer(p_info.slot);//출차된 차가 있던 구역 내 슬롯 PQ에 슬롯 추가(이제 그 슬롯에 자리가 비니까)
            emptySlots[p_info.area]++;//구역 내 빈 슬롯 개수+1
            parkedByZ[getTail(carNo)].remove(carNo);
            return mTime - p_info.enterTime;
        }

        //출차했으니까 견인 목록에도 있으면 안되니까 제거
        if (towed.containsKey(carNo)) {
            TowInfo t_info = towed.remove(carNo);
            towedByZ[getTail(carNo)].remove(carNo);

            int parkedTime = t_info.towTime - t_info.enterTime;//주차되어있던 시간
            int towTime = mTime - t_info.towTime;//견인되어있던 시간
            return -(parkedTime + towTime * 5);
        }

        return -1;
    }

		//---------------

    //4. search
    Solution.RESULT_S search(int mTime, String mStr) {
        Tow(mTime);//현재 시각기준, 자동 견인 처리해버리기
        int tail = Integer.parseInt(new String(mStr));//비교할 끝 4자리 떼서 저장해두기

        Solution.RESULT_S res = new Solution.RESULT_S();
        res.cnt = 0;

        for (String car : parkedByZ[tail]) {//주차차량끝자리4개 담긴 배열 내에서 mStr과 동일한 treeSet내 원소 하나씩
            if (res.cnt == 5) break;
            res.carlist[res.cnt++] = car;
        }

        for (String car : towedByZ[tail]) {
            if (res.cnt == 5) break;
            res.carlist[res.cnt++] = car;
        }

        return res;
    }
}