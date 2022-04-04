package gr.gunet.uLookup.generator;
import gr.gunet.uLookup.AcademicPerson;

import java.util.Collection;
import java.util.Vector;
import java.util.Collections;

import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import gr.gunet.uLookup.ldap.LdapManager;
import org.ldaptive.LdapEntry;

public class UserNameGen {

    private AcademicPerson academicPerson;
    private LdapEntry dsPerson;
    private int FNchars=3;
    private String FNtakeCharsFrom="start";

    private double FNpercentageOfName=0.5;
    private String FNtakePercentFrom="";

    private String FNplacement="";
    private String FN="";
    private String LN="";
    
    private int LNchars=4;
    private String LNtakeCharsFrom="start";

    private double LNpercentageOfName=0.5;
    private String LNtakePercentFrom="";

    private String LNplacement="";

    private int upperAmountOfNamesSuggested=0;
    private String orderBy="";
    private String[] separators;
    private String[] prioritizeBy;
    private int lowerLimit=0;
    private int upperLimit=0;
    GeneratingMethods gen;


    public UserNameGen(AcademicPerson academicPerson){
        this.academicPerson=academicPerson;
        this.dsPerson=null;
        separators= new String[]{"."};

        gen= new GeneratingMethods(academicPerson, separators, "", "");

        prioritizeBy=new String[]{"fullNames", "prefixedLastName", "partOfNames"};
        lowerLimit=6;
        upperLimit=20;
        upperAmountOfNamesSuggested=6;
        orderBy="alphabetically";
    }

    public UserNameGen(LdapEntry dsPerson){
        this.academicPerson=null;
        this.dsPerson=dsPerson;
        separators= new String[]{"."};

        gen= new GeneratingMethods(dsPerson, separators, "", "");

        prioritizeBy=new String[]{"fullNames", "prefixedLastName", "partOfNames"};
        lowerLimit=6;
        upperLimit=20;
        upperAmountOfNamesSuggested=6;
        orderBy="alphabetically";
    }
    
    public UserNameGen(String FN, String LN){
        this.academicPerson=null;
        this.dsPerson=null;
        separators= new String[]{"."};
        this.FN=FN;
        this.LN=LN;

        gen= new GeneratingMethods(FN, LN, separators, "", "");

        prioritizeBy=new String[]{"fullNames", "prefixedLastName", "partOfNames"};
        lowerLimit=6;
        upperLimit=20;
        upperAmountOfNamesSuggested=6;
        orderBy="alphabetically";
    }

    public Vector<String> proposeNames(){
        Vector<String> proposedNames = new Vector<String>();

        Vector<String> fullNames=gen.FullNames();
        Vector<String> partOfNames = new Vector<String>();
        Vector<String> percentOfNames = new Vector<String>();
        Vector<String> prefixedLastNames = new Vector<String>();

        if(gen.getFirstName()==null || gen.getLastName()==null) return null;

        partOfNames=gen.partOfNames(FNchars, FNtakeCharsFrom, LNchars, LNtakeCharsFrom);
        prefixedLastNames=gen.prefixedLastNames(FNchars);
        percentOfNames=gen.percentOfNames(FNpercentageOfName, FNtakePercentFrom, LNpercentageOfName, LNtakePercentFrom);

        for (String priority: prioritizeBy){
            switch (priority){
                case "fullNames":
                    addToArray(proposedNames, fullNames);
                    break;
                case "prefixedLastName":
                   addToArray(proposedNames, prefixedLastNames);
                   break;
                case "partOfNames":
                    addToArray(proposedNames, partOfNames);
                    break;
                case "percentOfNames":
                    addToArray(proposedNames, percentOfNames);
                    break;
                default :
                    System.out.println("prioritization technique not found: " + priority);
            }
        }

        proposedNames= charAndSizeLimit(proposedNames);
        int names=proposedNames.size();
        Vector<String> randomNames;
        if (upperAmountOfNamesSuggested!=0){
            randomNames=gen.randomNames(lowerLimit, upperLimit, upperAmountOfNamesSuggested-names, proposedNames);
        }
        else{
            randomNames=gen.randomNames(lowerLimit, upperLimit, 5, proposedNames);
        }
        addToArray(proposedNames, randomNames);

        return proposedNames;
    }

    private void addToArray(Vector<String> proposedNames, Vector<String> Names){
        if (orderBy!=null){
            if (orderBy.equals("alphabetically")){
                Collections.sort(Names);
            }
            else if (orderBy.equals("size")){
                sortBySize(Names);
            }
        }
        Vector<String> newNames= new Vector<String>();
        for (String Name: Names) newNames.add(Name.toLowerCase());
        proposedNames.addAll(proposedNames.size(), newNames);
    }

    private void sortBySize(Vector<String> vec){
        for (int i=1 ;i<vec.size(); i++)
        {
            String temp = vec.get(i);

            int j = i - 1;
            while (j >= 0 && temp.length() < vec.get(j).length())
            {
                vec.set(j+1,vec.get(j));
                j--;
            }
            vec.set(j+1,temp);
        }
    }
    public Vector<String> charAndSizeLimit(Vector<String> proposedNames){
        Vector<String> ProposedNames= new Vector<String>();
        for (String Name: proposedNames){
            if (lowerLimit!=0 && Name.length()<lowerLimit) {
                continue;
            }
            if (upperLimit!=0 && Name.length()>upperLimit) {
                continue;
            }
            ProposedNames.add(Name);
        }
        if (upperAmountOfNamesSuggested==0) return ProposedNames;

        int names=ProposedNames.size();
        for (int i=names-1; i>=upperAmountOfNamesSuggested; i--){
            ProposedNames.remove(i);
        }
        return ProposedNames;
    }

    public boolean checkIfUserNameExists(String loginName, DBConnectionPool Views, LdapConnectionPool ldapDS, String disabledGracePeriod){
        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;

        try{
            sis=Views.getSISConn();
            hrms=Views.getHRMSConn();
            hrms2=Views.getHRMS2Conn();
            ldap=ldapDS.getConn();

            Collection<AcademicPerson> existingOwners= sis.fetchAll("loginName",loginName,disabledGracePeriod);
            if (hrms!=null) existingOwners.addAll(hrms.fetchAll("loginName",loginName,disabledGracePeriod));
            if (hrms2!=null) existingOwners.addAll(hrms2.fetchAll("loginName",loginName,disabledGracePeriod));
            Collection<LdapEntry> existingDSOwners= ldap.search(ldap.createSearchFilter("(schGrAcPersonID=*)","uid="+loginName));
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }
}
