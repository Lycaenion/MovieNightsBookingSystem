class RequestHandler{

    static searchByTitle(title){
        let url = "http://127.0.0.1:3001/search?title="+title;
        let movies = [];
        let json = [];

        var collectSearchResult = (function (){
            $.ajax({
                type : "GET",
                async : false,
                url: url,
                dataType : "json",
                success : function(data){
                    json = data;
                },
                error : function () {
                    console.log("fail");
                    json = [];
                }
            });
        })();

        for (let index in json){
            let movie = new Movie();
            //console.log(json[index]);
            movie.imdbid = json[index].imdbid;
            movie.year = json[index].year;
            movie.title = json[index].title;

            movies.push(movie);
        }

        return movies;

    }

    static searchById(id){

        let url = "http://127.0.0.1:3001/movie?id="+id;
        let movie = new Movie();
        let json;

        var collectSearchResult = (function (){
            $.ajax({
                type : "GET",
                async : false,
                url: url,
                dataType : "json",
                success : function(data){
                    json = data;
                },
                error : function () {
                    console.log("fail");
                    json = [];
                }
            });
        })();

        movie.imdbid = json.imdbid;
        movie.title = json.title;
        movie.year = json.year;
        movie.rated = json.rated;
        movie.runtime = json.runtime;
        movie.genre = json.genre;
        movie.plot = json.plot;
        movie.language = json.language;
        movie.poster = json.poster;

        return movie;



    }
}