package program.controllers;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import program.entities.Movie;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import program.handlers.QueryHandler;
import program.repositories.MovieRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MovieController {

    @Autowired
    MovieRepository movieRepository;

    @RequestMapping("/search")
    public static List<Movie> searchByTitle(@RequestParam(value="title") String title){
        List<Movie> searchResult = new ArrayList<>();

        RestTemplate template = new RestTemplate();

        String result = template.getForObject("http://www.omdbapi.com/?s="+title+"&apikey=fda66a87", String.class);

        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();

        JsonArray resultArray = jsonObject.getAsJsonArray("Search");

        for (int i = 0; i < resultArray.size(); i++){
            Movie movie = new Movie();
            movie.setIMDBId(resultArray.get(i).getAsJsonObject().get("imdbID").getAsString());
            movie.setTitle(resultArray.get(i).getAsJsonObject().get("Title").getAsString());
            movie.setYear(resultArray.get(i).getAsJsonObject().get("Year").getAsString());

            searchResult.add(movie);
        }

        return searchResult;
    }

    @RequestMapping("/movie")
    public Movie searchById(@RequestParam(value = "id") String id) throws SQLException {
        Movie movie = new Movie();
        RestTemplate template = new RestTemplate();
        Connection conn = QueryHandler.connectDB();

        if(QueryHandler.movieInDB(conn, id)){
            System.out.println("Hello");
        }else{
            String result = template.getForObject("http://www.omdbapi.com/?i=" + id + "&apikey=fda66a87", String.class);

            JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
            movie.setIMDBId(jsonObject.get("imdbID").getAsString());
            movie.setTitle(jsonObject.get("Title").getAsString());
            movie.setYear(jsonObject.get("Year").getAsString());
            movie.setGenre(jsonObject.get("Genre").getAsString());
            movie.setLanguage(jsonObject.get("Language").getAsString());
            movie.setRated(jsonObject.get("Rated").getAsString());
            movie.setRuntime(jsonObject.get("Runtime").getAsString());
            movie.setPlot(jsonObject.get("Plot").getAsString());
            movie.setPoster(jsonObject.get("Poster").getAsString());

            movieRepository.save(movie);
        }



        return movie;
    }
}
