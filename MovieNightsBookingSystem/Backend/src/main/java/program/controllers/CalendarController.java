package program.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import program.entities.User;
import program.entities.UserEvent;
import program.handlers.QueryHandler;
import program.repositories.UserRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class CalendarController {

    private static final String APPLICATION_NAME = "MovieNightsBookingSystem";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    @Autowired
    UserRepository userRepository;

    private Calendar calendarService(String email) throws SQLException, GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);

        GoogleCredential credential = new GoogleCredential().setAccessToken(user.getAccessToken());
        return new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("MovieNights")
                .build();

    }

    private GoogleCredential updateCredentials(String refreshToken) throws IOException {

        GoogleClientSecrets clientSecrets = AuthController.getClientDetails();

        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(), refreshToken,
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret()
        ).execute();

        return new GoogleCredential().setAccessToken(response.getAccessToken());
    }

    private void refreshToken() throws IOException, SQLException {
        List<User> users = (List<User>) userRepository.findAll();
        for(int i = 0; i < users.size(); i++){
            long expiresAt = users.get(i).getExpiresAt();
            long now = new DateTime(System.currentTimeMillis()).getValue();
            if(isTokenExpired(expiresAt, now)){
                GoogleCredential newCredentials = updateCredentials(users.get(i).getRefreshToken());
                QueryHandler.updateRefreshToken(QueryHandler.connectDB(), newCredentials.getAccessToken(), users.get(i).getEmail());
            }
        }
    }

    private boolean isTokenExpired(long expiresAt, long now){

        org.joda.time.DateTime expireTime = new org.joda.time.DateTime(expiresAt);
        org.joda.time.DateTime currentTime = new org.joda.time.DateTime(now);

        return currentTime.isAfter(expireTime);

    }

    @GetMapping("/events")
    public List<UserEvent> userEvents() throws IOException, SQLException, GeneralSecurityException {

        List<User> users = (List<User>) userRepository.findAll();
        List<UserEvent> allEvents = new ArrayList<>();
        refreshToken();
        for (int i = 0; i <users.size(); i++){
            DateTime startDate = new DateTime(System.currentTimeMillis());
            Events events = null;

            events = calendarService(users.get(i).getEmail()).events().list(users.get(i).getEmail())
                    .setTimeMin(startDate)
                    .setSingleEvents(true)
                    .execute();

            List<Event> items  = events.getItems();

            if(items.isEmpty()){
                System.out.println("No upcoming events");
            }else{
                System.out.println("Upcoming events:");
                for(Event event : items){
                    DateTime start = event.getStart().getDateTime();
                    if(start == null){
                        start = event.getStart().getDate();
                    }
                    DateTime end = event.getEnd().getDateTime();
                    if(end == null){
                        end = event.getStart().getDate();
                    }

                    System.out.println(event.getSummary() + " " + start + " - " + end);

                    allEvents.add(new UserEvent(event.getSummary(), start, end));

                }
            }
        }

        return allEvents;
    }
}
