package gr.gunet.uLookup.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private Properties properties;
    private String propertyFileName;

    private String couldNotOpenPropertyFileError(){
        return "Failed to open "+propertyFileName+" file to read ldap properties.";
    }

    private String propertyNotFoundError(String propertyName){
        return "Attribute "+propertyName+" was not found in "+propertyFileName+" file.";
    }

    private String emptyPropertyError(String propertyName){
        return "Attribute "+propertyName+" does not have a value on "+propertyFileName+" file.";
    }

    public PropertyReader(String propFileName) throws Exception{
        this.propertyFileName = propFileName;
        this.properties = new Properties();

        InputStream propertiesReader = null;
        try{
            propertiesReader = new FileInputStream(propFileName);
            properties.load(propertiesReader);
        }catch(IOException e){
            throw e;
        }
    }

    public String getProperty(String propertyName) throws Exception{
        String property = properties.getProperty(propertyName);
        if(property == null){
            throw new Exception(propertyNotFoundError(propertyName));
        }else if(property.equals("")){
            throw new Exception(emptyPropertyError(propertyName));
        }
        return property;
    }

    public Short getPropertyAsShort(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        Short converted = Short.parseShort(property);
        return converted;
    }

    public Long getPropertyAsLong(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        Long converted = Long.parseLong(property);
        return converted;
    }

    public Boolean getPropertyAsBoolean(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        Boolean converted = Boolean.parseBoolean(property);
        return converted;
    }

    public Float getPropertyAsFloat(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        Float converted = Float.parseFloat(property);
        return converted;
    }

    public Properties getPropertiesObject(){
        return this.properties;
    }
}