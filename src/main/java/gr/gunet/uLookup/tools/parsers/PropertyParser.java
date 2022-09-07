package gr.gunet.uLookup.tools.parsers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertyParser {
    private final Properties properties;
    private final String propertyFileName;

    private String couldNotOpenPropertyFileError(){
        return "Failed to open "+propertyFileName+" file to read ldap properties.";
    }

    private String propertyNotFoundError(String propertyName){
        return "Attribute "+propertyName+" was not found in "+propertyFileName+" file.";
    }

    private String emptyPropertyError(String propertyName){
        return "Attribute "+propertyName+" does not have a value on "+propertyFileName+" file.";
    }

    public PropertyParser(String propFileName) throws Exception{
        this.propertyFileName = propFileName;
        this.properties = new Properties();

        InputStream propertiesReader;
        propertiesReader = new FileInputStream(propFileName);
        properties.load(propertiesReader);
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
        return Short.parseShort(property);
    }

    public Long getPropertyAsLong(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        return Long.parseLong(property);
    }

    public Boolean getPropertyAsBoolean(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        return Boolean.parseBoolean(property);
    }

    public Float getPropertyAsFloat(String propertyName) throws Exception{
        String property = getProperty(propertyName);
        return Float.parseFloat(property);
    }

    public Properties getPropertiesObject(){
        return this.properties;
    }
}