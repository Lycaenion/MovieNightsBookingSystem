class WebHandler{

    static displaySearchResult(movieTitle){
        let searchResult = RequestHandler.searchByTitle(movieTitle);
        //console.log(searchResult);

        for(let index in searchResult){
            let div = $('<div></div>');
            let title = $(`<p></p>`);
            let year = $('<p></p>');
            let button = $(`<button id=${searchResult[index].imdbid} type=button>Mer info</button>`);

            title.text(searchResult[index].title);
            year.text(searchResult[index].year);

            div.append(title);
            div.append(year);
            div.append(button);

            $('#result').append(div);
        }
    }

    static displayMovie(id){
        let movie = RequestHandler.searchById(id);
        let div = $('<div></div>');
        let title = $('<p></p>');
        let year = $('<p></p>');
        let genre = $('<p></p>');
        let runtime = $('<p></p>');
        let plot = $('<p></p>');
        let language = $('<p></p>');
        let img = $(`<img src=${movie.poster}>`);

        let button = $(`<button id=${movie.title} type = button> Boka film</button>`);

        title.text(movie.title);
        year.text(movie.year);
        genre.text(movie.genre);
        runtime.text(movie.runtime);
        plot.text(movie.plot);
        language.text(movie.language);

        div.append(title);
        div.append(year);
        div.append(img);
        div.append(runtime);
        div.append(genre);
        div.append(language);
        div.append(plot);

        $('#result').append(div);
    }

    static pickMovie(movieTitle){

        let paragraph = $('<p></p>');

        paragraph.text(movieTitle);

        $

    }

    static bookEvent(){

    }
}