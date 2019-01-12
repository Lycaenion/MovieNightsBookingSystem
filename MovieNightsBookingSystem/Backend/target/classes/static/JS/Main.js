

    $('#searchBtn').on('click', function(e){
        console.log("Hello")
        $('#result').empty();
        let title = $('#searchMovie').val();
        WebHandler.displaySearchResult(title);

    });

    $('#result').on('click', 'button', function(e){
        let id = $(this).attr('id');
        console.log("hello from " + id );
    });









