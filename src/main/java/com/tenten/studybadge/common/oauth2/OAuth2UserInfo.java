package com.tenten.studybadge.common.oauth2;

import com.tenten.studybadge.common.exception.oauth2.UnsupportedProviderException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.type.member.MemberRole;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import lombok.Builder;

import java.util.Map;

import static com.tenten.studybadge.common.constant.Oauth2Contant.*;

@Builder
public record OAuth2UserInfo(
        String name,
        String nickname,
        String email,
        String profile,
        Platform platform
) {



    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case NAVER -> ofNaver(attributes);
            case KAKAO -> ofKakao(attributes);
            default -> throw new UnsupportedProviderException();
        };
    }

    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(NAVER_ATTRIBUTE_KEY);

        return OAuth2UserInfo.builder()
                .name((String) response.get(NAME))
                .email((String) response.get(EMAIL))
                .nickname((String) response.get(NICKNAME))
                .profile((String) response.get(NAVER_PROFILE_IMG))
                .platform(Platform.NAVER)
                .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get(KAKAO_ACCOUNT);
        Map<String, Object> profile = (Map<String, Object>) account.get(KAKAO_ATTRIBUTE_KEY);

        return OAuth2UserInfo.builder()
                .name((String) profile.get(NICKNAME))
                .email((String) account.get(EMAIL))
                .nickname((String) profile.get(NICKNAME))
                .profile((String) profile.get(KAKAO_PROFILE_IMG))
                .platform(Platform.KAKAO)
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .imgUrl(profile)
                .password(OAUTH2_PASSWORD)
                .role(MemberRole.USER)
                .badgeLevel(BadgeLevel.NONE)
                .nickname(nickname)
                .point(0)
                .status(MemberStatus.WAIT_FOR_APPROVAL)
                .platform(platform)
                .isAuth(true)
                .isPasswordAuth(false)
                .build();
    }
}