package gr.gunet.loginNameValidityChecker.routes;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.generator.UserNameGen;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import com.google.gson.Gson;
import java.util.Collection;
import java.util.Vector;
import org.ldaptive.LdapEntry;
import spark.Request;
import spark.Response;
import spark.Route;


public class LoginNameGeneratorRoute implements Route{
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String SSN;
    String SSNCountry;
    String institution;

    public LoginNameGeneratorRoute(){
    }
    @Override
    public Object handle(Request req, Response res) throws Exception{
        res.type("application/json");
        String reqBody= req.body();
        CustomJsonReader reader;
        try {
            reader= new CustomJsonReader(reqBody);
        }catch (Exception e){
            return e.getMessage();
        }

        SSN = reader.readPropertyAsString("ssn");
        SSNCountry = reader.readPropertyAsString("ssnCountry");
        institution = reader.readPropertyAsString("institution");

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);

        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        String proposeJson= "{";
        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll(SSN,SSNCountry));
            if (hrms!=null) existingOwners.addAll(hrms.fetchAll(SSN,SSNCountry));
            if (hrms2!=null) existingOwners.addAll(hrms2.fetchAll(SSN,SSNCountry));
            if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN="+SSN)));

            Vector<String> proposedNames =new Vector<String>();
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                proposeJson += "\n  \"suggestions\":  [";
                UserNameGen loginGen = null;
                if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                else loginGen= new UserNameGen(existingDSOwners.iterator().next());

                boolean firstElem = true;

                proposedNames= loginGen.proposeNames();
                if (proposedNames!=null && !proposedNames.isEmpty()){
                    for(String login : proposedNames){
                        if (loginGen.checkIfUserNameExists(login, Views, ldapDS)) continue;
                        if(firstElem){
                            firstElem = false;
                        }else{
                            proposeJson += ",";
                        }
                        proposeJson+="\n    ";
                        proposeJson += "\""+login+"\"";
                    }
                }
                proposeJson += "\n  ]\n}";
            }
            else{
                proposeJson += "\n\n}";
            }
        }catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }
        res.status(200);
        res.body(new Gson().toJson(proposeJson));
        return proposeJson;
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

}
