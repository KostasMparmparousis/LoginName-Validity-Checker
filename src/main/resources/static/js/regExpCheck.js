function fillValidatorForm() {
    var loginName = document.forms["Form"]["loginName"].value;
//    const re = new RegExp('^[a-z0-9]+([._-]?[a-z0-9]+){0,}');
    const re = /^[a-z0-9]+([._-]?[a-z0-9]+){1,}/;
    const ok = re.exec(loginName);
    if (!ok){
        var newLine = "\r\n";
        var message="LoginName given invalid";
        message+=newLine;
        alert(message);
        return false;
    }
}