var aplikacija = "/" + document.location.pathname.split("/")[1];
var wsUri = "ws://" + document.location.host + aplikacija + "/infoPutnik";
wsocket = new WebSocket(wsUri);
wsocket.onmessage = onMessage;  

function onMessage(evt){
    var data = evt.data;
    var button = document.getElementById("forma:preuzmiLetove");
    button.click();
}

