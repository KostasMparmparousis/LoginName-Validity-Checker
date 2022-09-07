package gr.gunet.uLookup.tools.parsers;

public class CommandLineParser {
  private String institution;
  private String mode="public";
  
  public CommandLineParser(String[] args){
    this.institution = null;
    int i = 0;
    while(i < args.length){
        if(args[i].equals("-i")){
          i++;
          if((i >= args.length)||(args[i].startsWith("-"))){
              System.err.println("-i option given without a value.");
              System.err.println("Use option --help for more details on how to use this option correctly.");
              System.exit(1);
          }
          this.institution = args[i];
        }
        else if(args[i].equals("-m")){
          i++;
          if((i >= args.length)||(args[i].startsWith("-"))){
              System.err.println("-m option given without a value.");
              System.err.println("Use option --help for more details on how to use this option correctly.");
              System.exit(1);
          }
          this.mode = args[i];
        }
        else{
            System.err.println("Unknown option \""+args[i]+"\" received.");
            return;
        }
        i++;
    }
  }
  public String getInstitution(){
    return institution;
  }

  public String getMode(){
    return mode;
  }
}
