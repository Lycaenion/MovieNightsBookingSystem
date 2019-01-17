package program.handlers;

import program.entities.Movie;
import program.entities.User;

import java.sql.*;

public class QueryHandler {
    final static String DBURL = "jdbc:mysql://127.0.0.1:3306/Movie_Nights_Booking_System?serverTimezone=UTC";

    public static Connection connectDB() throws SQLException {

            Connection conn = DriverManager.getConnection(DBURL, "movieNights", "ThePassword");
            System.out.println("Connection Established");
            return conn;

    }

    public static boolean movieInDB(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM movie WHERE imdbid = '" + id +"'" ;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            return rs.isBeforeFirst();
    }

    public static Movie fetchMovie(Connection conn, String id) throws SQLException{
        Movie movie = new Movie();

        String query = "SELECT * FROM movie WHERE imdbid = '" + id +"'" ;

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);

        while(rs.next()){
            movie.setIMDBId(rs.getString("imdbid"));
            movie.setGenre(rs.getString("genre"));
            movie.setLanguage(rs.getString("language"));
            movie.setPlot(rs.getString("plot"));
            movie.setPoster(rs.getString("poster"));
            movie.setRated(rs.getString("rated"));
            movie.setRuntime(rs.getString("runtime"));
            movie.setTitle(rs.getString("title"));
            movie.setYear(rs.getString("year"));
        }
        return movie;
    }

    public static boolean checkExistingUser(Connection conn, String email) throws SQLException {

        String query = "SELECT * FROM user WHERE email = '" + email + "'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);

        //rs.isBeforeFirst() returns true = there is the user in db

        return rs.isBeforeFirst();

    }

    public static void updateRefreshToken(Connection conn, String accessToken,  String email) throws SQLException {
        String query = "UPDATE user SET access_token = '" + accessToken + "' WHERE email = '" + email + "'";
        Statement st = conn.createStatement();
        int rowsAffected = st.executeUpdate(query);
        System.out.println(rowsAffected);
    }

    public static User fetchUser(Connection conn, String email) throws SQLException {
        User user = new User();
        String query = "SELECT * FROM user WHERE email = '" + email + "'";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);

        while(rs.next()){
            user.setEmail(rs.getString("email"));
            user.setAccessToken(rs.getString("access_token"));
            user.setRefreshToken(rs.getString("refresh_token"));
            user.setExpiresAt(rs.getLong("expires_at"));

        }

        return user;

    }
}
