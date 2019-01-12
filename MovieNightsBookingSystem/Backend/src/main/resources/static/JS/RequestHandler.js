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
            console.log(json[index]);
            movie.imdbid = json[index].imdbid;
            movie.year = json[index].year;
            movie.title = json[index].title;

            movies.push(movie);
        }

        return movies;

    }
}