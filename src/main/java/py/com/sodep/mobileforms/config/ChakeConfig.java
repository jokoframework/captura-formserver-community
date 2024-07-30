package py.com.sodep.mobileforms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChakeConfig {

    @Value("${chake.email}")
    private String email;

    @Value("${chake.password}")
    private String password;


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
