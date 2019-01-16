function start(){
    gapi.load('auth2', function(){
        auth2 = gapi.auth2.init({
            client_id : "244275742778-1uretb3dr970f6r1l7fgph725hss06d8.apps.googleusercontent.com",
            scope: "http://www.googleapis.com/auth/calendar.events"
        });
    });
}

$('#signinButton').click(function(){
    auth2.grantOfflineAccess().then(signInCallback);
})

function signInCallback(authResult){
    console.log("Logging in");
    if(authResult['code']){
        $('#signinButton').attr('style', 'display:none');

        $.ajax({
            type: 'POST',
            url:'http://localhost:3001/login',
            headers:{
                'X-Requested-With' : 'XMLHttpRequest'
            },
            contentType : 'application/octet-stream; charset = utf-8',
            success : function(result){
                console.log(result);
            },
            processData : false,
            data: authResult['code']
        });
    }else{
        console.log("Error");
    }
}