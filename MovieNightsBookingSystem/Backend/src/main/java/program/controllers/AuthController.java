package program.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import program.repositories.UserRepository;

public class AuthController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public @ResponseBody
    String login(@RequestBody String code){
        private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
        final String CLIENT_ID =

        GoogleTokenResponse tokenResponse = null;

        tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://www.googleapis.com/oauth2/v4/token",

        )

    }
}
