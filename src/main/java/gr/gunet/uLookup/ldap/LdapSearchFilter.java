package gr.gunet.uLookup.ldap;

import java.util.Collection;
import java.util.HashSet;

public class LdapSearchFilter {
    private String searchDN;
    private String searchFilter;
    
    public LdapSearchFilter(String attribute,String operation,String value,String dn){
        if(attribute == null || operation==null || value==null){
            searchFilter = null;
        }else{
            if(operation.equals("!=")){
                searchFilter = "(!("+attribute+"="+value+"))";
            }else{
                searchFilter = "("+attribute+operation+value+")";
            }
        }
        searchDN = dn;
    }
    
    public LdapSearchFilter(String filter,String dn){
        if(filter == null){
            searchFilter = null;
        }else{
            if(Character.compare(filter.charAt(0),'(') == 0){
                searchFilter = filter;
            }else{
                searchFilter = "("+filter+")";
            }
        }
        searchDN = dn;
    }
    
    public LdapSearchFilter(String operation, LdapSearchFilter... filters) throws Exception{
        HashSet<LdapSearchFilter> nonNullFilters = new HashSet();
        for(LdapSearchFilter filter : filters){
            if(filter.searchFilter != null){
                nonNullFilters.add(filter);
            }
            if(searchDN == null){
                searchDN = filter.searchDN;
            }
        }
        
        if(nonNullFilters.isEmpty()){
            searchFilter = "";
            return;
        }
        
        if(nonNullFilters.size() == 1){
            LdapSearchFilter firstFilter = nonNullFilters.iterator().next();
            this.searchFilter = firstFilter.searchFilter;
            this.searchDN = firstFilter.searchDN;
            return;
        }
        
        if(operation == null){
            throw new Exception("Invalid operation provided");
        }else if(operation.equals("merge")){
            searchFilter = "(";
        }else if(operation.equals("and")){
            searchFilter = "(&";
        }else if(operation.equals("or")){
            searchFilter = "(|";
        }else{
            throw new Exception("Invalid operation provided");
        }
        
        String commonDN = null;
        for(LdapSearchFilter filter : nonNullFilters){
            if(commonDN == null){
                commonDN = filter.searchDN;
            }else{
                if(!commonDN.equals(filter.searchDN)){
                    throw new Exception("Filters provided must all have the same searchDN to merge.");
                }
            }
            String currentSearchFilter = filter.toString();;
            if(currentSearchFilter != null){
                searchFilter += filter.toString();
            }
        }
        
        searchFilter += ")";
        this.searchDN = commonDN;
    }
    
    public LdapSearchFilter(String operation,Collection<LdapSearchFilter> filters) throws Exception{
        HashSet<LdapSearchFilter> nonNullFilters = new HashSet();
        for(LdapSearchFilter filter : filters){
            if(filter.searchFilter != null){
                nonNullFilters.add(filter);
            }
            if(searchDN == null){
                searchDN = filter.searchDN;
            }
        }
        
        if(nonNullFilters.isEmpty()){
            searchFilter = "";
            return;
        }
        if(nonNullFilters.size() == 1){
            LdapSearchFilter firstFilter = nonNullFilters.iterator().next();
            this.searchFilter = firstFilter.searchFilter;
            this.searchDN = firstFilter.searchDN;
            return;
        }
        
        if(operation == null){
            throw new Exception("Invalid operation provided");
        }else if(operation.equals("merge")){
            searchFilter = "(";
        }else if(operation.equals("and")){
            searchFilter = "(&";
        }else if(operation.equals("or")){
            searchFilter = "(|";
        }else{
            throw new Exception("Invalid operation provided");
        }
        
        String commonDN = null;
        for(LdapSearchFilter filter : nonNullFilters){
            if(commonDN == null){
                commonDN = filter.searchDN;
            }else{
                if(!commonDN.equals(filter.searchDN)){
                    throw new Exception("Filters provided must all have the same searchDN to merge.");
                }
            }
            String currentSearchFilter = filter.toString();;
            if(currentSearchFilter != null){
                searchFilter += filter.toString();
            }
        }
        
        searchFilter += ")";
        searchDN = commonDN;
    }
    
    @Override
    public String toString(){
        return searchFilter;
    }
    
    public String getDN(){
        return searchDN;
    }
}
