package program.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import program.entities.User;
import program.handlers.QueryHandler;
import program.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

@RestController
public class AuthController {

    @Autowired
    UserRepository userRepository;


    public static GoogleClientSecrets getClientDetails() throws IOException {
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final String CREDENTIALS_FILE_PATH = "/client.json";

        InputStream in = AuthController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    }

    @RequestMapping(value = "/storeauthcode", method = RequestMethod.POST)
    public String storeAuthCode(@RequestBody String code, @RequestHeader("X-Requested-With") String encoding) throws IOException, SQLException {
        System.out.println("Hello");
        if(encoding == null || encoding.isEmpty()){
            return "Error, wrong headers";
        }

        GoogleClientSecrets clientSecrets = getClientDetails();


        GoogleTokenResponse tokenResponse = null;

        tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://www.googleapis.com/oauth2/v4/token",
                "244275742778-1uretb3dr970f6r1l7fgph725hss06d8.apps.googleusercontent.com",
                "ezMnFqo94ZnXGMLosDQfxbYp",
                code,"http://localhost:3001")
                .execute();

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

        if(QueryHandler.checkExistingUser(QueryHandler.connectDB(), email)){
            return "OK";
        }else{

            User user = new User();

            user.setEmail(email);
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setExpiresAt(expiresAt);

            userRepository.save(user);

            return "OK";
        }
    }

}
