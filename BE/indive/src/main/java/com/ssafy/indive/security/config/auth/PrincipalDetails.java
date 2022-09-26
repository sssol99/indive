package com.ssafy.indive.security.config.auth;

import com.ssafy.indive.domain.member.entity.Member;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class PrincipalDetails implements UserDetails{

	private Member member;

    public PrincipalDetails(Member member){
        this.member = member;
    }

    public Member getMember() {
		return member;
	}

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getNickname();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
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

    //TODO : 이게 뭘까..
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }
//	@Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
//        member.getRole(r -> {
//
//        	authorities.add(member.getRole);
//        return authorities;
//    }
}
