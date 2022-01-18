package gr.gunet.loginNameValidityChecker.routes;
import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.Vector;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameSearcherRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String SSN;
    String SSNCountry;
    String institution;

    public LoginNameSearcherRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        res.type("application/json");
        String CONN_FILE_DIR = "/etc/v_vd/conn";
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

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
            if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
            String ssnJson="{";
            ssnJson+= "\n  \"personPairedWith\": [";
            Vector<String> existingUserNames= new Vector<String>();
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {
                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem = false;
                            else ssnJson += ",";
                            ssnJson += "\n    ";
                            ssnJson += "\"" + person.getLoginName() + "\"";
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                else if (!existingDSOwners.isEmpty()) {
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem = false;
                            else ssnJson += ",";
                            ssnJson += "\n    ";
                            ssnJson += "\"" + uid + "\"";
                            existingUserNames.add(uid);
                        }
                    }
                }
                ssnJson += "\n  ]\n}";
                res.status(200);
                res.body(new Gson().toJson(ssnJson));
                return ssnJson;
            }
        }catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }
        return "";
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

}
