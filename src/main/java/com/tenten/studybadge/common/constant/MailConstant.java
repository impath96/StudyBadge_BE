package com.tenten.studybadge.common.constant;

public class MailConstant {

    public static final String SIGNUP_SUBJECT = "Study Badge 가입 인증 메일입니다.";
    public static final String SIGNUP_BODY =  "<h3> 안녕하세요, Study Badge 가입을 환영합니다. "
                                                + "아래 링크를 클릭해, 인증을 완료해주세요.</h3>"
                                                + "<div><a href='%s/members/auth?email=%s&code=%s'>"
                                                + " 인증하기 </a></div>";
}
