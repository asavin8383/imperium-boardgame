package model.user.ldapUtils;

import java.util.Collection;

import model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class UserComposition implements LdapUserDetails {

	private static final long serialVersionUID = 1L;
	
	private User user;
	private LdapUserDetails details;

	public UserComposition(User user, LdapUserDetails details) {
		this.user = user;
		this.details = details;
	}

	public User getUser() {
		return user;
	}

	@Override
	public boolean isEnabled() {
		return details.isEnabled();
	}

	@Override
	public String getDn() {
		return details.getDn();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return details.getAuthorities();
	}

	@Override
	public String getPassword() {
		return details.getPassword();
	}

	@Override
	public String getUsername() {
		return details.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return details.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return details.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return details.isCredentialsNonExpired();
	}

	@Override
	public void eraseCredentials() {
		details.eraseCredentials();
	}
}
