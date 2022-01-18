package gr.gunet.loginNameValidityChecker.routes;
import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.Conflict;
import gr.gunet.loginNameValidityChecker.LoginNameConflictDetector;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.RequestPerson;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;


public class LoginNameConflictDetectorRoute implements Route{
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;

    public LoginNameConflictDetectorRoute() {
    }
    @Override
    public Object handle(Request req, Response res) throws Exception{
        res.type("application/json");
        CustomJsonReader jsonReader = new CustomJsonReader(req.body());
        boolean verbose=false;
        String disabledGracePeriod = null;

        RequestPerson reqPerson;
        try{
            reqPerson = new RequestPerson(jsonReader);
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"error\":\""+e.getMessage()+"\"\n}\n";
        }

        Views= new DBConnectionPool(reqPerson.getInstitution());
        ldapDS= new LdapConnectionPool(reqPerson.getInstitution());
        LoginNameConflictDetector loginChecker = new LoginNameConflictDetector(Views, ldapDS);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson, disabledGracePeriod);
            if (!UIDPersons.isEmpty()){
                String warningJson = "{\n  \"warning\": {\n    \"uids\": [";
                boolean firstElem=true;
                for (String person: UIDPersons){
                    if (firstElem) firstElem=false;
                    else warningJson+= ",";
                    warningJson+="\n      ";
                    warningJson+= "\"" + person + "\"";
                }
                warningJson+="\n    ],\n    \"message\": \"Uids above already appended to a uid record in the DS\"\n  }\n}";
                return warningJson;                
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }

        Collection<Conflict> conflicts;
        String responseJson = "{";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            if (!conflicts.isEmpty()){
                responseJson+=getConflicts(conflicts, reqPerson);
            }

            responseJson+="\n}\n";
            res.status(200);
            res.body(new Gson().toJson(responseJson));
            return responseJson;
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }
    }

    public String getConflicts(Collection<Conflict> conflicts, RequestPerson reqPerson){
        String conflictsJson = "\n  \"conflicts\": " ;
        verbose=reqPerson.getVerbose();
        if(verbose){
            conflictsJson = "\n  \"detailedConflicts\": " ;
            boolean firstElem = true;
            for(Conflict conflict : conflicts){
              if(firstElem){
                firstElem = false;
                conflictsJson+="[\n";
               }else{
                conflictsJson += ",\n";
               }
               conflictsJson += conflict.toJson();
            }
            conflictsJson += "\n  ]";
        }else{
            conflictsJson+=" {";
            HashMap<String,HashSet<String>> conflictsPerSource = new HashMap();
            for(Conflict conflict : conflicts){
                if(conflictsPerSource.containsKey(conflict.getConflictSource())){
                    conflictsPerSource.get(conflict.getConflictSource()).add(conflict.getConflictRecordKey());
                }else{
                    HashSet<String> conflRecKeys = new HashSet();
                    conflRecKeys.add(conflict.getConflictRecordKey());
                    conflictsPerSource.put(conflict.getConflictSource(),conflRecKeys);
                }
            }
            boolean firstElem = true;
            for(Map.Entry<String,HashSet<String>> sourceConfls : conflictsPerSource.entrySet()){
                if(firstElem){
                    firstElem = false;
                }else{
                    conflictsJson += ",";
                }
                conflictsJson+="\n    ";
                conflictsJson += "\""+sourceConfls.getKey()+"Conflicts\":"+sourceConfls.getValue().size();
            }
            conflictsJson+="\n  }";
        }
        return conflictsJson;
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

}
