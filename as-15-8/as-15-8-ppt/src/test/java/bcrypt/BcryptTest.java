package bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {

	public static void main(String[] args) {
		BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
		System.out.println("{CRYPT}" + crypt.encode("123456"));
	}

}
