

    $('#searchBtn').on('click', function(e){
        $('#result').empty();
        let title = $('#searchMovie').val();
        WebHandler.displaySearchResult(title);

    });

    $('#result').on('click', 'button', function(e){
        let id = $(this).attr('id');
        $('#result').empty();
        WebHandler.displayMovie(id);
    });

    $('#bookEvent').on('click', function(){

    })









