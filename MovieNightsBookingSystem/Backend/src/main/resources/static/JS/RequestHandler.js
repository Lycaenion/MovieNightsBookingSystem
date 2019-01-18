class RequestHandler{

    static searchByTitle(title){
        let url = "http://localhost:3001/search?title="+title;
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

        let url = "http://localhost:3001/movie?id="+id;
        let movie = new Movie();
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

    static fetchDates(){
        let url = "http://localhost:3001/availableDays?userA=lycaenion92@gmail.com&userB=linn.p.martensson@gmail.com&startDate=2019-01-18&endDate=2019-02-01"
        let json;
        console.log("fetchDates");
        $.ajax({
            type: "GET",
            url : url,
            async : false,
            dataType: "json",
            success : function (data) {
                json = data;
                console.log(json);
            },
            error : function () {
                console.log("Error")
            }
        })

        return json;
    }

    static bookEvent(event){
       let url = "http://localhost:3001/bookEvent"
       let eventJson = JSON.stringify(event);

       $.ajax({
           type : "POST",
           url : url,
           contentType : "application/json",
           data : eventJson,
           success : function () {
               if(response == "Event booked"){
                   alert("Something went wrong");
               }else{
                   alert("Din filmkväll är bokad");
               }

           },
           error : function () {
               alert("Error: Something went wrong");
           }

       })
    }
}