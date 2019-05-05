package user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class UserComposition implements LdapUserDetails {

	private static final long serialVersionUID = 1L;
	
	private UserInfo info;
	private LdapUserDetails details;

	public UserComposition(UserInfo userInfo, LdapUserDetails details) {
		this.info = userInfo;
		this.details = details;
	}

	public UserInfo getUserInfo() {
		return info;
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
