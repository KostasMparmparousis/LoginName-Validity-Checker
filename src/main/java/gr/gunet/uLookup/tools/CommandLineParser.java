/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.gunet.uLookup.tools;

/**
 *
 * @author barbarousisk
 */
public class CommandLineParser {
  private String institution;
  
  public CommandLineParser(String[] args){
    this.institution = null;
    int i = 0;
    while(i < args.length){
        if(args[i].equals("-i")){
          i++;
          if((i >= args.length)||(args[i].startsWith("-"))){
              System.err.println("-o option given without a value.");
              System.err.println("Use option --help for more details on how to use this option correctly.");
              System.exit(1);
          }
          this.institution = args[i];
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

}
