package com.tenten.studybadge.common.security;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.type.MemberRole;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static com.tenten.studybadge.common.constant.TokenConstant.ROLE_PREFIX;

@Getter
@Setter
@Slf4j
public class CustomUserDetails implements UserDetails, OAuth2User {

    private String email;
    private MemberStatus status;
    private MemberRole role;
    private Platform platform;
    private Long id;
    private Map<String, Object> attributes;
    private String attributeKey;


    public CustomUserDetails(Member member) {
        this.email = member.getEmail();
        this.status = member.getStatus();
        this.role = member.getRole();
        this.platform = member.getPlatform();
        this.id = member.getId();
    }

    public CustomUserDetails(Member member, Map<String, Object> attributes, String attributeKey) {
        this.email = member.getEmail();
        this.status = member.getStatus();
        this.role = member.getRole();
        this.platform = member.getPlatform();
        this.id = member.getId();
        this.attributes = attributes;
        this.attributeKey = attributeKey;
        this.status = member.getStatus();


    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(ROLE_PREFIX + getRole().name()));
    }

    @Override
    public String getPassword() {
        return this.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return getStatus().equals(MemberStatus.ACTIVE);
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return attributes.get(attributeKey).toString();
    }
}