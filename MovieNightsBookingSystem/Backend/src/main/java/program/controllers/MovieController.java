package program.controllers;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import program.entities.Movie;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MovieController {

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

}
