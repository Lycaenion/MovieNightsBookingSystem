package program.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import program.entities.User;
import program.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AuthController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public @ResponseBody
    String login(@RequestBody String code) throws IOException {
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final String CREDENTIALS_FILE_PATH = "/credentials.json";

        InputStream in = AuthController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        GoogleTokenResponse tokenResponse = null;

        tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://www.googleapis.com/oauth2/v4/token",
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                code,
                "http://127.0.0.1:3001").execute();

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        Long expiresAt = System.currentTimeMillis() + (tokenResponse.getExpiresInSeconds()*1000);



        GoogleIdToken idToken = null;

        try{
            idToken = tokenResponse.parseIdToken();
        }catch(IOException e){
            e.printStackTrace();
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();

        User user = new User();

        user.setEmail(email);
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setExpiresAt(expiresAt);

        userRepository.save(user);

        return "OK";



    }
}
