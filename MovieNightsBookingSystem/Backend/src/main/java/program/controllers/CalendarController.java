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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import program.entities.CalendarEvent;
import program.entities.User;
import program.handlers.QueryHandler;
import program.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class CalendarController {

    private static final String APPLICATION_NAME = "Movie Nights";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    @Autowired
    UserRepository userRepository;

    public CalendarController() throws GeneralSecurityException, IOException {
    }


    public Calendar getCalendarService(String email) throws GeneralSecurityException, IOException, SQLException {
        InputStream in = CalendarController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);
        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                user.getRefreshToken(),
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret()
        ).execute();

        GoogleCredential credential = new GoogleCredential().setAccessToken(response.getAccessToken());
        return new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
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
                System.out.println("Token has expired");
                GoogleCredential newCredentials = updateCredentials(users.get(i).getRefreshToken());
                QueryHandler.updateRefreshToken(QueryHandler.connectDB(), newCredentials.getAccessToken(), users.get(i).getEmail());
            }else{
                System.out.println("Token has not expired");
            }
        }
    }

    private boolean isTokenExpired(long expiresAt, long now){

        org.joda.time.DateTime expireTime = new org.joda.time.DateTime(expiresAt);
        org.joda.time.DateTime currentTime = new org.joda.time.DateTime(now);

        return currentTime.isAfter(expireTime);

    }

    @RequestMapping(value = "/availableDays", method = RequestMethod.POST)
    public List<LocalDate> getAvailableDays(@RequestBody String calendarInfo ) throws GeneralSecurityException, SQLException, IOException {

        String emailA;
        String emailB;
        String startDate;
        String endDate;

        JsonObject jsonObject = new JsonParser().parse(calendarInfo).getAsJsonObject();

        startDate = jsonObject.get("startDate").getAsString();
        endDate = jsonObject.get("endDate").getAsString();

        System.out.println("startdate: " + startDate + " enddate: " + endDate);

        JsonArray jsonArray = jsonObject.getAsJsonArray("users");

        String[] users = new String[jsonArray.size()];

        for(int i = 0; i < jsonArray.size(); i++){
            users[i] = jsonArray.get(i).getAsString();
        }

        emailA = users[0];
        emailB = users[1];

        List<LocalDate> availableDays = new ArrayList<>();
        List<CalendarEvent> userAEvents = getUserEvents(emailA);
        List<CalendarEvent> userBEvents = getUserEvents(emailB);

        LocalDate inputStart = LocalDate.parse(startDate);
        LocalDate inputEnd = LocalDate.parse(endDate);

        for(LocalDate dateI = inputStart; !dateI.isAfter(inputEnd); dateI = dateI.plusDays(1)){
            if(dayIsAvailable(dateI, userAEvents, userBEvents)){
                availableDays.add(dateI);
            }
        }
        return availableDays;
    }

    public boolean dayIsAvailable(LocalDate date, List<CalendarEvent> listA, List<CalendarEvent> listB){

        return userIsAvailable(date, listA) && userIsAvailable(date, listB);

    }

    public boolean userIsAvailable(LocalDate date, List<CalendarEvent> list){

        LocalDateTime dateTimeAt18 = LocalDateTime.of(date, LocalTime.of(18, 0));
        LocalDateTime dateTimeAt23 = LocalDateTime.of(date, LocalTime.of(23, 0));

        boolean isAvailable = true;

        for (CalendarEvent event: list) {

            LocalDateTime start = event.getStartDateTime();
            LocalDateTime end = event.getEndDateTime();
            if(!(start.isBefore(dateTimeAt18) && end.isBefore(dateTimeAt18) || start.isAfter(dateTimeAt23) && end.isAfter(dateTimeAt23))){
                isAvailable = false;
            }
        }
        return isAvailable;
    }

    public List<CalendarEvent> getUserEvents(String email) throws IOException, SQLException, GeneralSecurityException {

        refreshToken();
        List<CalendarEvent> allEvents = new ArrayList<>();
        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);
        DateTime startDate = new DateTime(System.currentTimeMillis());
        Calendar calendar = getCalendarService(email);
        Events events = calendar.events().list("primary")
                    .setTimeMin(startDate)
                    .setTimeZone("CET")
                    .setSingleEvents(true)
                    .execute();
        List<Event> items  = events.getItems();

                for(Event event : items){
                    DateTime start = event.getStart().getDateTime();
                    if(start == null){
                        start = event.getStart().getDate();
                    }
                    DateTime end = event.getEnd().getDateTime();
                    if(end == null){
                        end = event.getStart().getDate();
                    }

                    allEvents.add(new CalendarEvent(event.getSummary(), start, end));
                }

        return allEvents;
    }

    @PostMapping(value = "/bookEvent")
    public ResponseEntity<String> bookEvent(@RequestParam(value = "movieTitle") String movieTitle,
                                    @RequestParam(value = "userA") String emailA,
                                    @RequestParam(value = "userB") String emailB,
                                    @RequestParam(value = "date") String date) throws IOException {


        Event userEvent = new Event()
                .setSummary("Filmkväll")
                .setLocation("Hos mig")
                .setDescription("Kvällens film: " + movieTitle);

        DateTime startDate = new DateTime(date+"T18:00:00+01:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDate)
                .setTimeZone("CET");
        userEvent.setStart(start);

        DateTime endDate = new DateTime(date + "T23:00:00+01:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDate)
                .setTimeZone("CET");
        userEvent.setEnd(end);

        EventAttendee[] attendees = new EventAttendee[2];

        attendees[0] = new EventAttendee().setEmail(emailA);
        attendees[1] = new EventAttendee().setEmail(emailB);



        userEvent.setAttendees(Arrays.asList(attendees));
      try{
            userEvent = getCalendarService(emailA).events().insert("primary", userEvent).execute();
        } catch (IOException e){
            return new ResponseEntity("Event could not be booked", HttpStatus.BAD_REQUEST);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Event booked");


    }
}