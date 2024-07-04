package com.tenten.studybadge.type.study.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Category {

    IT("컴퓨터/IT/개발"),
    LANGUAGE("언어/어학"),
    EMPLOYMENT("취업/이직"),
    SELF_DEVELOPMENT("자기계발"),
    ;

    private String name;

}
