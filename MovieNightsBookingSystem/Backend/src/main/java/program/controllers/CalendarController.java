package program.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
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


    @Autowired
    UserRepository userRepository;

    public CalendarController() throws GeneralSecurityException, IOException {
    }


    public Calendar getCalendarService(String email) throws SQLException {

        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);

        GoogleCredential credential = new GoogleCredential().setAccessToken(user.getAccessToken());
        return new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private GoogleCredential updateCredentials(String email) throws IOException, SQLException {

        GoogleClientSecrets clientSecrets = AuthController.getClientDetails();
        User user = QueryHandler.fetchUser(QueryHandler.connectDB(), email);

        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(), user.getRefreshToken(),
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret()
        ).execute();

        return new GoogleCredential().setAccessToken(response.getAccessToken());
    }

    private void refreshAccessToken() throws IOException, SQLException {

        List<User> users = (List<User>) userRepository.findAll();
        for(int i = 0; i < users.size(); i++){
            GoogleCredential newCredentials = updateCredentials(users.get(i).getEmail());
            QueryHandler.updateAccessToken(QueryHandler.connectDB(), newCredentials.getAccessToken(), users.get(i).getEmail());

        }
    }

    @RequestMapping(value = "/availableDays", method = RequestMethod.POST)
    public List<LocalDate> getAvailableDays(@RequestBody String calendarInfo ) throws GeneralSecurityException, SQLException, IOException {

        String startDate;
        String endDate;

        JsonObject jsonObject = new JsonParser().parse(calendarInfo).getAsJsonObject();

        startDate = jsonObject.get("startDate").getAsString();
        endDate = jsonObject.get("endDate").getAsString();


        LocalDate inputStart = LocalDate.parse(startDate);
        LocalDate inputEnd = LocalDate.parse(endDate);

        JsonArray jsonArray = jsonObject.getAsJsonArray("users");
        String[] users = new String[jsonArray.size()];

        for(int i = 0; i < jsonArray.size(); i++){
            users[i] = jsonArray.get(i).getAsString();
        }

        List<LocalDate> availableDays = new ArrayList<>();

        List<List<CalendarEvent>> allUsersEvents = new ArrayList<>();

        for(int j = 0; j < users.length; j++){

            List<CalendarEvent> userEvents = getUserEvents(users[j]);
            allUsersEvents.add(userEvents);

        }

        for(LocalDate dateI = inputStart; !dateI.isAfter(inputEnd); dateI = dateI.plusDays(1)){
            if(dayIsAvailable(dateI,allUsersEvents)){
                availableDays.add(dateI);
            }
        }

        return availableDays;
    }

    public boolean dayIsAvailable(LocalDate date, List<List<CalendarEvent>> users){

        boolean allAvailable = true;

        for (int i = 0; i < users.size(); i++){

            allAvailable = allAvailable && userIsAvailable(date, users.get(i));

        }

        return allAvailable;

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

        refreshAccessToken();
        List<CalendarEvent> allEvents = new ArrayList<>();
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
    public ResponseEntity<String> bookEvent(@RequestBody String eventInfo) throws IOException {

        String movieId;
        String date;
        String bookingUser;
        String movieTitle;
        List<String> attendeesList = new ArrayList<>();

        JsonObject jsonObject = new JsonParser().parse(eventInfo).getAsJsonObject();

        movieId = jsonObject.get("movieId").getAsString();
        date = jsonObject.get("date").getAsString();
        bookingUser = jsonObject.get("bookingUser").getAsString();
        movieTitle = jsonObject.get("movieTitle").getAsString();

        JsonArray jsonArray = jsonObject.getAsJsonArray("attendees");

        for (int i = 0; i < jsonArray.size(); i++){
            attendeesList.add(jsonArray.get(i).getAsString());
        }

        Event userEvent = new Event()
                .setSummary("Filmkväll!")
                .setLocation("Hos " + bookingUser)
                .setDescription("Kvällens film: " + movieTitle + " (https://www.imdb.com/title/" + movieId + "/)");

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

        EventAttendee[] attendees = new EventAttendee[attendeesList.size()];

        for (int j = 0; j < attendees.length; j++){
            attendees[j] = new EventAttendee().setEmail(attendeesList.get(j));
        }

        userEvent.setAttendees(Arrays.asList(attendees));
      try{
            userEvent = getCalendarService(bookingUser).events().insert("primary", userEvent).execute();
        } catch (IOException e){
            return new ResponseEntity("Event could not be booked", HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Event booked");

    }
}