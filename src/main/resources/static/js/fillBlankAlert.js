function fillValidatorForm() {
    var a = document.forms["Form"]["ssn"].value;
    var b = document.forms["Form"]["ssnCountry"].value;
    var c = document.forms["Form"]["birthDate"].value;
    var d = document.forms["Form"]["loginName"].value;
    if (a == null || a == "", b == null || b == "", c == null || c == "", d == null || d == "") {
        var newLine = "\r\n"
        var message="Please fill these required fields:";
        message+=newLine;
        if (a==null || a== ""){
            message+="-SSN";
            message+=newLine;
        }
        if (b==null || b== ""){
            message+="-SSN Country";
            message+=newLine;
        }
        if (c==null || c== ""){
            message+="-Date of birth";
            message+=newLine;
        }
        if (d==null || d== ""){
            message+="-LoginName";
            message+=newLine;
        }
        alert(message);
        return false;
    }
}