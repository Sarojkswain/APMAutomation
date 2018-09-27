package com.ca.apm.tests.utility;

/**
 * Utility class to create testng suite file dynamically from role properties of CODA
 * @author: Suresh Votla (votsu01)
 */

import java.io.FileInputStream;
import java.util.Properties;


public class DynamicTestNgSuite{
    
    static StringBuffer testng = new StringBuffer();
    
    public static void main(String a[]){
        Properties testng = new Properties();
        try{
            String testlogicDir = a[0]+"/";
            String clientPropertyFiles = a[1];            
            for(String file: clientPropertyFiles.split(",")){
                testng.load(new FileInputStream(testlogicDir+file));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        createDynamicTestNgSuiteFile(testng, a[2]);
        System.out.println("###Successfully created testng suite file "+a[2]+" ###");
    }
    
    public static void addNewLine(String line){
        testng.append(line);
        testng.append(System.getProperty("line.separator"));
    }
    
    public static String addAttribute(String name, String value){
        return " "+name+"=\""+value+"\"";
    }
    
    public static void createDynamicTestNgSuiteFile(Properties tests, String targetFileLocation){
        
        try{
            addNewLine("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\">");
            
            String line = "<suite";
            line = line+addAttribute("name", tests.getProperty("testng.suite.name", "tests").trim());
            line = line+addAttribute("parallel", tests.getProperty("testng.suite.parallel", "false").trim());
            line = line+addAttribute("verbose", tests.getProperty("testng.suite.verbose.level", "3").trim());
            line = line+">";
            addNewLine(line);
            
            int testCounter = 1;
            while(tests.containsKey("testng.test"+testCounter+".class")){
                String prefix = "testng.test"+testCounter+".";
                line = "<test";
                line = line+addAttribute("name", tests.getProperty(prefix+"name").trim());
                line = line+addAttribute("preserve-order", tests.getProperty(prefix+"preserve.order", "true").trim());
                line = line+">";
                addNewLine(line);
                
                addNewLine("<classes>");
                
                line = "<class";
                line = line+addAttribute("name", tests.getProperty(prefix+"class").trim());
                line = line+">";
                addNewLine(line);
                
                int methodCounter = 1;
                addNewLine("<methods>");
                while(tests.containsKey(prefix+"method."+methodCounter)){
                    if(tests.containsKey(prefix+"method.parameters."+methodCounter) && 
                            !tests.getProperty(prefix+"method.parameters."+methodCounter).trim().isEmpty()){
                        String[] parameters= tests.getProperty(prefix+"method.parameters."+methodCounter).trim().split("<<->>");
                        for(String param: parameters){
                            line = "<parameter";
                            line = line+addAttribute("name", param.split("<->")[0]);
                            line = line+addAttribute("value", param.split("<->")[1]);
                            line = line+"/>";
                            addNewLine(line);
                        }
                    }
                    line = "<include";
                    line = line+addAttribute("name", tests.getProperty(prefix+"method."+methodCounter));
                    line = line+"/>";
                    addNewLine(line);
                    methodCounter++;
                }
                addNewLine("</methods>");
                addNewLine("</class>");
                addNewLine("</classes>");
                addNewLine("</test>");
                testCounter++;
                
            }
            addNewLine("</suite>");
            QaFileUtils fu = new QaFileUtils();
            fu.writeStringToFile(targetFileLocation, testng.toString());            
          }
        catch(Exception e){
            System.out.println("###Failed to create testng suite file###");
            e.printStackTrace();
          }
          
    }    
    
    /*public static void createDynamicTestNgSuiteFile(Properties tests, String targetFileLocation){
        
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("suite");
            doc.appendChild(root);
            
            root.setAttribute("name", tests.getProperty("testng.suite.name", "tests").trim());
            root.setAttribute("parallel", tests.getProperty("testng.suite.parallel", "false").trim());
            root.setAttribute("verbose", tests.getProperty("testng.suite.verbose.level", "3").trim());
            
            int testCounter = 1;
            while(tests.containsKey("testng.test"+testCounter+".class")){
                String prefix = "testng.test"+testCounter+".";
                Element test = doc.createElement("test");
                test.setAttribute("name", tests.getProperty(prefix+"name").trim());
                test.setAttribute("preserve-order", tests.getProperty(prefix+"preserve.order", "true").trim());
                root.appendChild(test);
                
                Element classes = doc.createElement("classes");
                test.appendChild(classes);
                Element classNode = doc.createElement("class");
                classNode.setAttribute("name", tests.getProperty(prefix+"class").trim());
                classes.appendChild(classNode);
                
                int methodCounter = 1;
                Element methods = doc.createElement("methods");
                classNode.appendChild(methods);
                while(tests.containsKey(prefix+"method."+methodCounter)){
                    if(tests.containsKey(prefix+"method.parameters."+methodCounter) && 
                            !tests.getProperty(prefix+"method.parameters."+methodCounter).trim().isEmpty()){
                        String[] parameters= tests.getProperty(prefix+"method.parameters."+methodCounter).trim().split("<<->>");
                        for(String param: parameters){
                            Element parameter = doc.createElement("parameter");
                            parameter.setAttribute("name", param.split("<->")[0]);
                            parameter.setAttribute("value", param.split("<->")[1]);
                            methods.appendChild(parameter);
                        }
                    }
                    Element include = doc.createElement("include");
                    include.setAttribute("name", tests.getProperty(prefix+"method."+methodCounter));
                    methods.appendChild(include);
                    methodCounter++;
                }
                testCounter++;
                
            }
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(targetFileLocation));
            transformer.transform(source, result); 
            System.out.println("###Successfully created testng suite file###");
          }
        catch(Exception e){
            System.out.println("###Failed to create testng suite file###");
            e.printStackTrace();
          }
          
    }*/
    
        

    
    
    
}

