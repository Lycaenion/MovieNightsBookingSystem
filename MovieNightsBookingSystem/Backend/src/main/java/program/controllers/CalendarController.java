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
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import program.entities.CalendarEvent;
import program.entities.User;
import program.handlers.QueryHandler;
import program.repositories.UserRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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




    public Calendar getCalendarService(String email) throws GeneralSecurityException, IOException, SQLException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);
        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                user.getRefreshToken(),
                "798561573318-qp57ibgmiekqvh5rko17tvuk9gdlhjcs.apps.googleusercontent.com",
                "qDUyrUXATI2p3M4NjjSpB5P4"
        ).execute();

        GoogleCredential credential = new GoogleCredential().setAccessToken(response.getAccessToken());
        return new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("MovieNights")
                .build();
    }

    private GoogleCredential updateCredentials(String refreshToken) throws IOException {

        GoogleClientSecrets clientSecrets = AuthController.getClientDetails();


        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(), refreshToken,
                "798561573318-qp57ibgmiekqvh5rko17tvuk9gdlhjcs.apps.googleusercontent.com",
                "qDUyrUXATI2p3M4NjjSpB5P4"
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

    @RequestMapping("/availableDays")
    public List<LocalDate> getAvailableDays(@RequestParam(value = "userA") String emailA,
                                                @RequestParam(value = "userB") String emailB,
                                                @RequestParam(value = "startDate") String startDate,
                                                @RequestParam(value = "endDate") String endDate) throws GeneralSecurityException, SQLException, IOException {
        List<LocalDate> availableDays = new ArrayList<>();
        List<CalendarEvent> userAEvents = getUserEvents(emailA);
        List<CalendarEvent> userBEvents = getUserEvents(emailB);

        LocalDate inputStart = LocalDate.parse(startDate);
        LocalDate inputEnd = LocalDate.parse(endDate);

        for(LocalDate dateI = inputStart; !dateI.isAfter(inputEnd); dateI = dateI.plusDays(1)){
            System.out.println(dateI);
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
        System.out.println("userisAvailable: " + isAvailable);
        return isAvailable;
    }

    @GetMapping("/events")
    public List<CalendarEvent> getUserEvents(@Param(value = "user") String email) throws IOException, SQLException, GeneralSecurityException {

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

                    allEvents.add(new CalendarEvent(event.getSummary(), start, end));

                }
            }

        return allEvents;
    }

    @PostMapping(value = "/bookEvent")
    public ResponseEntity bookEvent(@RequestBody String eventInfo) throws IOException {

        JsonObject jsonObject = new JsonParser().parse(eventInfo).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("event");

        String date =  " ";
        String movieTitle = " ";

        for(int i = 0; i < jsonArray.size(); i++){
            date = jsonArray.get(i).getAsJsonObject().get("date").getAsString();
            movieTitle = jsonArray.get(i).getAsJsonObject().get("movieTitle").getAsString();

        }


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

        EventAttendee[] attendees = new EventAttendee[];


        userEvent.setAttendees(Arrays.asList(attendees));
        try{
            userEvent = calendar.events().insert("primary", userEvent).execute();
        } catch (IOException e){
            return new ResponseEntity("Event could not be booked", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Event booked");


    }
}