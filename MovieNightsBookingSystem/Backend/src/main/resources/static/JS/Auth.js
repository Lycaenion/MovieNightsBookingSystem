function start(){
    gapi.load('auth2', function(){
        auth2 = gapi.auth2.init({
            cliet_id: '750423709392-1gkuvfcnm7tgsndcfgqfle81tvhm3m0c.apps.googleusercontent.com'

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
            url:'http://127.0.0.1:3001/login',
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