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

import java.util.*;

import static com.tenten.studybadge.common.constant.TokenConstant.ROLE_PREFIX;

@Getter
@Setter
@Slf4j
public class CustomUserDetails implements UserDetails {

    private String email;
    private MemberStatus status;
    private MemberRole role;
    private Platform platform;
    private Long id;

    public CustomUserDetails(Member member) {
        this.email = member.getEmail();
        this.status = member.getStatus();
        this.role = member.getRole();
        this.platform = member.getPlatform();
        this.id = member.getId();
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
        return this.getEmail();
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
}