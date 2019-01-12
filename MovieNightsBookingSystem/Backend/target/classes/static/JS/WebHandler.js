class WebHandler{

    static displaySearchResult(movieTitle){
        let searchResult = RequestHandler.searchByTitle(movieTitle);
        console.log(searchResult);

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
}