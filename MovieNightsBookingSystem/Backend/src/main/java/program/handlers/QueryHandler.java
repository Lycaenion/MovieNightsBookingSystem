package program.handlers;

import program.entities.Movie;

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
}
