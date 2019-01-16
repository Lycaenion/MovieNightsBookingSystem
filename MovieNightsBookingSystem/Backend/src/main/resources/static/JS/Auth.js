

$('#signinButton').click(function(){
    auth2.grantOfflineAccess().then(signInCallback);
})

function signInCallback(authResult){
    console.log("Logging in");
    if(authResult['code']){
        $('#signinButton').attr('style', 'display:none');
        $.ajax({
            type: 'POST',
            url:'http://localhost:3001/storeauthcode',
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
        console.log("Tried")
    }else{
        console.log("Error");
    }
}