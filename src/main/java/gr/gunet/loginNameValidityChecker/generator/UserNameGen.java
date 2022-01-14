package gr.gunet.loginNameValidityChecker.generator;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.Vector;
import java.util.Collections;
import org.ldaptive.LdapEntry;

public class UserNameGen {
    private AcademicPerson academicPerson;
    private LdapEntry dsPerson;
    private int FNchars=3;
    private String FNtakeCharsFrom="start";

    private double FNpercentageOfName=0.5;
    private String FNtakePercentFrom="";

    private String FNplacement="";

    private int LNchars=5;
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
        separators= new String[]{"_"};

        gen= new GeneratingMethods(academicPerson, separators, "", "");

        prioritizeBy=new String[]{"fullNames", "partOfNames", "percentOfNames"};
        lowerLimit=6;
        upperLimit=16;
        upperAmountOfNamesSuggested=6;
        orderBy="alphabetically";
    }

    public UserNameGen(LdapEntry dsPerson){
        this.academicPerson=null;
        this.dsPerson=dsPerson;
        separators= new String[]{"_"};

        gen= new GeneratingMethods(dsPerson, separators, "", "");

        prioritizeBy=new String[]{"fullNames", "partOfNames", "percentOfNames"};
        lowerLimit=6;
        upperLimit=16;
        upperAmountOfNamesSuggested=6;
        orderBy="alphabetically";
    }

    public Vector<String> proposeNames(){
        Vector<String> proposedNames = new Vector<String>();

        Vector<String> fullNames=gen.FullNames();
        Vector<String> partOfNames = new Vector<String>();
        Vector<String> percentOfNames = new Vector<String>();

        if(gen.getFirstName()==null || gen.getLastName()==null) return null;

        partOfNames=gen.partOfNames(FNchars, FNtakeCharsFrom, LNchars, LNtakeCharsFrom);
        percentOfNames=gen.percentOfNames(FNpercentageOfName, FNtakePercentFrom, LNpercentageOfName, LNtakePercentFrom);

        for (String priority: prioritizeBy){
            switch (priority){
                case "fullNames":
                    addToArray(proposedNames, fullNames);
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
}
