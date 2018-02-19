package com.minimon.diocian.player;


import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


//create	"#공통
//        IOS / Android / Web"	"#1 회원가입
//        같은 이메일이라도 소셜 / 일반따로 가입할수 있음"	"{
//        ""resultCode"": ""0000"",
//        ""message"": ""회원가입에 성공하였습니다."",
//        ""data"": {
//        ""apiToken"": ""eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ3d3cubWluaW1vbi5jb20iLCJpYXQiOjE1MTU1NTg3NjUsImV4cCI6MTUxNTY0NTE2NSwiZGF0YSI6eyJpZCI6Imdqd29ndXJUZXN0MyIsImtleSI6MjI5NzA0OTIwNTMyNTM5MDE0NX19.yyVZRqYUoK3CgDB2okutmUSQv54yDgHI1i9ejYuGEQZ4PX9_LlgqyMGn_lCcxNjnMkHev12889_0bYtjBMK-cA"",
//        ""userInfo"": {
//        ""idx"": ""779"",
//        ""id"": ""gjwogurTest3"",
//        ""name"": null,
//        ""email"": ""gjwogurTEST3@gmail.com"",
//        ""state"": ""0"",
//        ""is_adult"": ""0"",
//        ""is_certificate"": ""0"",
//        ""is_social"": ""0"",
//        ""loc"": null,
//        ""point"": ""0"",
//        ""fixed"": ""0""
//        }
//        }
//        }"	Req	type			String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			String	O	아이디
//        value			String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드
//        type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        email			String	O	이메일
//        nickname			String	O	닉네임
//        nickname_social			String		소셜에서 사용중인 닉네임
//        device_id			String		디바이스 고유값
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        apiToken				세션토큰
//        userInfo				회원정보
//        idx			유저 인덱스
//        id			유저 아이디
//        name			유저 이름
//        nickname			유저 닉네임
//        email			유저 이메일
//        state			유저 상태값	0:정상, 1:휴면, 2:탈퇴
//        is_adult			유저 성인여부	0:미성년, 1:성인
//        is_certificate			유저 인증여부	0:비인증, 1:인증
//        is_social			유저 소셜회원 여부	0:일반회원, 1:소셜회원
//        loc			소셜 타입	FB:facebook, GG:google, NV:naver, KK:kakao
//        point			유저 포인트
//        fixed			유저 정액제	"정액회원 : 정액만료일
//        비정액회원 : 0
//        "
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0101				사용중인 아이디
//        0102				사용중인 닉네임
//        0103				사용중인 이메일
//login	"#공통
//        IOS / Android / Web"	#2 로그인	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""로그인에 성공하였습니다."",
//        ""data"": {
//        ""apiToken"": ""eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ3d3cubWluaW1vbi5jb20iLCJpYXQiOjE1MTU1Njc5MjAsImV4cCI6MTUxNTY1NDMyMCwiZGF0YSI6eyJpZCI6Imdqd29ndXJUZXN0MyIsImtleSI6MjI5NzA3Njk1NjA4MDUyNDE2MH19.zuwsWulaZutRLu9JNZRPZ5vU8P-OwYEhtUXWdlp5eVw9OGo7_3FxT6TmyrkPNpDYmKcF9Bh_v32ff5mMFqx_Ng"",
//        ""userInfo"": {
//        ""idx"": ""779"",
//        ""id"": ""gjwogurTest3"",
//        ""name"": null,
//        ""email"": ""gjwogurTEST3@gmail.com"",
//        ""state"": ""0"",
//        ""is_adult"": ""0"",
//        ""is_certificate"": ""0"",
//        ""is_social"": ""0"",
//        ""loc"": null,
//        ""point"": ""0"",
//        ""fixed"": ""0""
//        }
//        }
//        }"	Req	type			String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			String	O	아이디
//        value			String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드
//        type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        device_id			String		디바이스 고유값
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        apiToken				세션토큰
//        userInfo				회원정보
//        idx			유저 인덱스
//        id			유저 아이디
//        name			유저 이름
//        nickname			유저 닉네임
//        email			유저 이메일
//        state			유저 상태값	0:정상, 1:휴면, 2:탈퇴
//        is_adult			유저 성인여부	0:미성년, 1:성인
//        is_certificate			유저 인증여부	0:비인증, 1:인증
//        is_social			유저 소셜회원 여부	0:일반회원, 1:소셜회원
//        loc			소셜 타입	FB:facebook, GG:google, NV:naver, KK:kakao
//        point			유저 포인트
//        fixed			유저 정액제	"정액회원 : 정액만료일
//        비정액회원 : 0
//        "
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0104				아이디 또는 비밀번호 불일치
//find	"#공통
//        IOS / Android / Web"	"#3 아이디 / 비밀번호 찾기
//        일반회원은 뒤4자리 마스킹처리
//        소셜회원은 어느 소셜로 가입했는지 까지만 표기"	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""아이디찾기에 성공하였습니다."",
//        ""data"": {
//        ""list"": [
//        {
//        ""is_social"": ""0"",
//        ""loc"": null,
//        ""id"": ""gjwogur****""
//        },
//        {
//        ""is_social"": ""1"",
//        ""loc"": ""GG"",
//        ""id"": ""[소셜회원]구글""
//        }
//        ],
//        ""email"": ""gjwogur0308@gmail.com""
//        }
//        }"	Req	email_id			String	O	이메일주소	ex) minimon@gamil.com 일때 minimon
//        email_domain			String	O	이메일도메인	ex) minimon@gamil.com 일때 gmail.com
//        id			String		유저 아이디	값이 있으면 비밀번호찾기 없으면 아이디찾기
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        list				유저 아이디 목록
//        is_social			유저 소셜회원 여부	0:일반회원, 1:소셜회원
//        loc			소셜 타입	FB:facebook, GG:google, NV:naver, KK:kakao
//        id			유저 아이디	뒤 4글자 마스킹처리 ****
//        email				유저 이메일
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0105				입력정보와 일치하는 회원정보없음
//info	"#공통
//        IOS / Android / Web"	#4 회원정보조회	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""회원정보 조회에 성공하였습니다."",
//        ""data"": {
//        ""userInfo"": {
//        ""idx"": ""379"",
//        ""id"": ""gjwogur0308"",
//        ""name"": null,
//        ""nickname"": ""허재혁"",
//        ""email"": ""gjwogur0308@gmail.com"",
//        ""state"": ""0"",
//        ""is_adult"": ""0"",
//        ""is_certificate"": ""0"",
//        ""is_social"": ""0"",
//        ""loc"": null,
//        ""point"": ""0"",
//        ""fixed"": ""0""
//        }
//        }
//        }"	Req	id			String	O	아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//        요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//        key: Authorization  /value:  apiToken값
//        토큰 값 없이 디버그용으로 조회시에는
//        key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        apiToken				세션토큰
//        userInfo				회원정보
//        idx			유저 인덱스
//        id			유저 아이디
//        name			유저 이름
//        nickname			유저 닉네임
//        email			유저 이메일
//        state			유저 상태값	0:정상, 1:휴면, 2:탈퇴
//        is_adult			유저 성인여부	0:미성년, 1:성인
//        is_certificate			유저 인증여부	0:비인증, 1:인증
//        is_social			유저 소셜회원 여부	0:일반회원, 1:소셜회원
//        loc			소셜 타입	FB:facebook, GG:google, NV:naver, KK:kakao
//        point			유저 포인트
//        fixed			유저 정액제	"정액회원 : 정액만료일
//        비정액회원 : 0
//        "
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        checked	"#공통
//        IOS / Android / Web"	"#5 중복체크
//        이메일 체크는 일반회원일때만 사용"	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""사용가능한 id 입니다.""
//        }"	Req	field			String	O	중복체크 필드	"id: 아이디, nickname: 닉네임, email: 이메일
//        대소문자 구분없음"
//        value			String	O	중복체크 값
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0101				사용중인 아이디
//        0102				사용중인 닉네임
//        0103				사용중인 이메일
//        update	"#공통
//        IOS / Android / Web"	#6 회원정보수정	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""회원정보 수정에 성공하였습니다."",
//        ""data"": {
//        ""userInfo"": {
//        ""idx"": ""379"",
//        ""id"": ""gjwogur0308"",
//        ""name"": null,
//        ""nickname"": ""바꿔빠꿩1"",
//        ""email"": ""gjwogur0308@naver.com"",
//        ""state"": ""0"",
//        ""is_adult"": ""0"",
//        ""is_certificate"": ""0"",
//        ""is_social"": ""0"",
//        ""loc"": null,
//        ""point"": ""0"",
//        ""fixed"": ""0""
//        }
//        }
//        }"	Req	id			String	O	아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//        요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//        key: Authorization  /value:  apiToken값
//        토큰 값 없이 디버그용으로 조회시에는
//        key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        email			String	O	이메일
//        nickname			String	O	닉네임
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        userInfo				회원정보
//        idx			유저 인덱스
//        id			유저 아이디
//        name			유저 이름
//        nickname			유저 닉네임
//        email			유저 이메일
//        state			유저 상태값	0:정상, 1:휴면, 2:탈퇴
//        is_adult			유저 성인여부	0:미성년, 1:성인
//        is_certificate			유저 인증여부	0:비인증, 1:인증
//        is_social			유저 소셜회원 여부	0:일반회원, 1:소셜회원
//        loc			소셜 타입	FB:facebook, GG:google, NV:naver, KK:kakao
//        point			유저 포인트
//        fixed			유저 정액제	"정액회원 : 정액만료일
//        비정액회원 : 0
//        "
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        password	"#공통
//        IOS / Android / Web"	#7 회원 패스워드변경	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""패스워드가 변경되었습니다.""
//        }"	Req	id			String	O	아이디
//        pwd			String	O	변경할 패스워드
//        pwd_c			String	O	현재 패스워드
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0106				패스워드 불일치
//        leave	"#공통
//        IOS / Android / Web"	#8 회원탈퇴	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""회원탈퇴 처리되었습니다.""
//        }"	Req	id			String	O	유저 아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//        요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//        key: Authorization  /value:  apiToken값
//        토큰 값 없이 디버그용으로 조회시에는
//        key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        email_id			String	O	이메일주소	ex) minimon@gamil.com 일때 minimon
//        email_domain			String	O	이메일도메인	ex) minimon@gamil.com 일때 gmail.com
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        list_	"#공통
//        IOS / Android / Web"	"#9 회원 활동 목록
//        구매 / 찜 / 구독"	"{
//        ""resCode"": ""0000"",
//        ""msg"": ""목록조회성공"",
//        ""data"": {
//        ""totalCnt"": ""1"",
//        ""list"": [
//        {
//        ""idx"": ""512"",
//        ""c_idx"": ""72"",
//        ""title"": ""2화 탑모델의 사생활"",
//        ""c_title"": ""탑모델의 사생활"",
//        ""grade"": ""19"",
//        ""is_adult"": ""1"",
//        ""point"": ""4"",
//        ""ep"": ""2"",
//        ""play_time"": ""587"",
//        ""like_cnt"": ""1"",
//        ""play_cnt"": ""4"",
//        ""image_url"": ""https://static.minimon.com/content/2017/12/28/f117c3f20382011285bf460bb7b35d50.jpg"",
//        ""like_me"":""1"",
//        ""p_idx"": ""958"",
//        ""remaining_time"": ""252273""
//        }
//        ]
//        }
//        }"	Req	type			String	O	리스트 타입	"purchase : 구매 목록 , like: 찜 목록, keep: 구독 목록 만입력가능
//        대소문자 구별없음"
//        id			String	O	아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//        요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//        key: Authorization  /value:  apiToken값
//        토큰 값 없이 디버그용으로 조회시에는
//        key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        start			String	O	요청 목록 시작 인덱스
//        limit			String	O	요청 갯수	0 보내면 페이징 처리없이 통째로 넘김
//        Res	resCode			String		처리결과코드
//        msg			String		처리결과메세지
//        data			String		처리결과데이터	boolean 타입을 제외한 나머지 타입은 String
//        totalCnt		String		요청 목록 총 갯수
//        list				요청 목록
//        idx			에피소드 인덱스	구매 / 찜 목록시에만 전달
//        c_idx			채널 인덱스	구매 / 찜 목록시에만 전달
//        title			에피소드 타이틀	구매 / 찜 목록시에만 전달
//        c_title			채널 타이틀	구매 / 찜 목록시에만 전달
//        grade			채널 등급	"0: 전체관람, 12: 12세관람 ,15: 15세관람 ,19: 19세관람
//        구매 / 찜 목록시에만 전달"
//        is_adult			채널 성인인등급 여부	"0: 미성년등급, 1:성인등급
//        구매 / 찜 목록시에만 전달"
//        point			에피소드 포인트(가격)	구매 / 찜 목록시에만 전달
//        ep			에피소드 회차	구매 / 찜 목록시에만 전달
//        play_time			에피소드 재생시간	구매 / 찜 목록시에만 전달
//        like_cnt			에피소드 찜 횟수	구매 / 찜 목록시에만 전달
//        play_cnt			에피소드 재생 수	구매 / 찜 목록시에만 전달
//        image_url			에피소드 썸네일URL	구매 / 찜 목록시에만 전달
//        like_me			회원 에피소드 찜 여부	구매 / 찜 목록시에만 전달
//        p_idx			구매이력 인덱스	구매목록시에만 전달
//        remaining			남은 재생시간	구매목록시에만 전달
//        idx			채널 인덱스	구독 목록시에만 전달
//        id			채널 아이디	구독 목록시에만 전달
//        title			채널 타이틀	구독 목록시에만 전달
//        title_sub			채널 서브 타이틀	구독 목록시에만 전달
//        grade			채널 등급	"0: 전체관람, 12: 12세관람 ,15: 15세관람 ,19: 19세관람
//        구독 목록시에만 전달"
//        is_adult			채널 성인인등급 여부	"0: 미성년등급, 1:성인등급
//        구독 목록시에만 전달"
//        keep_cnt			채널 구독 수	구독 목록시에만 전달
//        like_cnt			채널 찜 횟수	구독 목록시에만 전달
//        play_cnt			채널 재생 수	구독 목록시에만 전달
//        image_url			채널 가로형 이미지 URL	구독 목록시에만 전달
//        image_list_url			채널 세로형(포스터) 이미지 URL	구독 목록시에만 전달
//        keep_me			회원 채널 구독 여부	"0: 구독X , 1:구독중
//        구독 목록시에만 전달"
//        errCode				에러상세코드	프로세스 오류시에만 전달
//        errMsg				에러상세메세지	프로세스 오류시에만 전달
//        Code	0000					정상처리
//        0400					요청 파라메터 유효성 부적합
//        0401					유효하지 않은 토큰
//        0402					비회원
//        0500					데이터베이스 오류
//        0900					프로세스 오류
//        0201				목록 요청시 등록된 데이터 없음
public class MinimonUser {
    private final String TAG = "MinimonUser";
    private final String API_URL = "http://dev.api.minimon.com/User/";
    private String currentRequest;
    private String typeSocial;

    public interface MinimonUserListener {
        // These methods are the different events and need to pass relevant arguments with the event
        public void onResponse(JSONObject info);
    }
    private MinimonUserListener listener;

    public MinimonUser() {
        this.listener = null; // set null listener
        typeSocial = "basic";

        init();
    }

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setListener(MinimonUserListener listener) {
        this.listener = listener;
    }

    // API 인스턴스를 초기화
    private void init() {
    }

    private void requestFunction(String current, ContentValues info) {
        currentRequest = current;
        if (current.equals("find") && info.get("id") != null)
            currentRequest = "resetPassword";

        typeSocial = info.getAsString("value");
        NetworkTask networkTask = new NetworkTask(API_URL+current, info);
        networkTask.execute();
    }

    // #1 회원가입
    // 같은 이메일이라도 소셜 / 일반따로 가입할수 있음
    public void create(ContentValues info) {
//        type			    String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			    String	O	아이디
//        value			    String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드 type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        email			    String	O	이메일
//        nickname			String	O	닉네임
//        nickname_social	String		소셜에서 사용중인 닉네임
//        device_id			String		디바이스 고유값
        Log.d(TAG, "createInfo: " + info);
        requestFunction("create", info);
    }

    // #2 로그인
    public void login(ContentValues info) {
//        type			String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			String	O	아이디
//        value			String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드 type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        device_id		String		디바이스 고유값
        Log.d(TAG, "loginInfo: " + info);
        requestFunction("login", info);
    }

    // #3 아이디 / 비밀번호 찾기
    // 일반회원은 뒤4자리 마스킹처리
    // 소셜회원은 어느 소셜로 가입했는지 까지만 표기
    public void find(ContentValues info) {
//        email_id		String	O	이메일주소   	ex) minimon@gamil.com 일때 minimon
//        email_domain	String	O	이메일도메인	ex) minimon@gamil.com 일때 gmail.com
//        id			String		유저 아이디	값이 있으면 비밀번호찾기 없으면 아이디찾기
        Log.d(TAG, "findInfo: " + info);
        requestFunction("find", info);
    }

    // #4 회원정보조회
    public void info() {
//        id			String	O	아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//                                           요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//                                           key: Authorization  /value:  apiToken값
//                                           토큰 값 없이 디버그용으로 조회시에는
//                                           key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
    }

    // #5 중복체크
    // 이메일 체크는 일반회원일때만 사용
    public void checked() {
//        field			String	O	중복체크 필드	"id: 아이디, nickname: 닉네임, email: 이메일 대소문자 구분없음"
//        value			String	O	중복체크 값
    }

    // #6 회원정보수정
    public void update() {
//        id			String	O	아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//                                           요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//                                           key: Authorization  /value:  apiToken값
//                                           토큰 값 없이 디버그용으로 조회시에는
//                                           key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        email			String	O	이메일
//        nickname		String	O	닉네임

    }

    // #7 회원 패스워드변경
    public void password() {
//        id			String	O	아이디
//        pwd			String	O	변경할 패스워드
//        pwd_c			String	O	현재 패스워드
    }

    // #8 회원탈퇴
    public void leave() {
//        id			String	O	유저 아이디	"헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//                                               요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//                                               key: Authorization  /value:  apiToken값
//                                               토큰 값 없이 디버그용으로 조회시에는
//                                               key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        email_id		String	O	이메일주소	    ex) minimon@gamil.com 일때 minimon
//        email_domain	String	O	이메일도메인	ex) minimon@gamil.com 일때 gmail.com
    }

    // #9 회원 활동 목록
    // 구매 / 찜 / 구독
    public void list() {
//        type			String	O	리스트 타입	"purchase : 구매 목록 , like: 찜 목록, keep: 구독 목록 만입력가능 대소문자 구별없음"
//        id			String	O	아이디	    "헤당 토큰 과 요청 아이디값이 일치해야지만 조회가능
//                                               요청 헤더정보에 key Authorization 로 토큰을 담아서 보내야지만 조회가능
//                                               key: Authorization  /value:  apiToken값
//                                               토큰 값 없이 디버그용으로 조회시에는
//                                               key: Develop / value:  dev.debug 로보내면 토큰 없이 조회가능 "
//        start			String	O	요청 목록 시작 인덱스
//        limit			String	O	요청 갯수	    0 보내면 페이징 처리없이 통째로 넘김
    }

    // 로그아웃 처리(토큰도 함께 삭제)
    public void logout() {
        UserInfo.getInstance().setData(null);
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;
        private String token;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
            this.token = null;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values, token); // 해당 URL로 부터 결과물을 얻어온다.

            Log.e(TAG, "doInBackground: " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            responseNetworkTask(s);
        }
    }

    public void responseNetworkTask(String s) {
        if (s == null) {
            return;
        }
        try {
            JSONObject objJSON = new JSONObject(s);
            objJSON.put("current_request", currentRequest);
            objJSON.put("current_social", typeSocial);
            Log.d(TAG, "responseNetworkTask: " + objJSON);

            if (listener != null)
                listener.onResponse(objJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
