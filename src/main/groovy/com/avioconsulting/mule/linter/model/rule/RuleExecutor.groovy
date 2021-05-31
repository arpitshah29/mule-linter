package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application
import com.google.gson.*
import org.json.*

import javax.xml.transform.*
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.OutputKeys
class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    private Integer ruleCount = 0

    RuleExecutor(Application application, List<RuleSet> rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules() {
        rules.each { ruleSet ->
            ruleSet.getRules().each { // assigns current rule to 'it'
                results.addAll(it.execute(application))
                ruleCount++
            }
        }
    }

    void displayResults(outputFormat, OutputStream outputStream) {
        outputStream.write("$ruleCount rules executed.\n".bytes)
        outputStream.write('Rule Results\n'.bytes)
        if(outputFormat == 'json'){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json1 = gson.toJson(results)
            outputStream.write(json1)
        }
        else if(outputFormat == 'xml')
        {
            final StringBuilder builder = new StringBuilder();
            results.each { violation ->
                String json = new Gson().toJson(violation);
                JSONObject jsonObject = new JSONObject(json);
                String xml =  "<"+'rules'+">" + XML.toString(jsonObject) + "</"+'rules'+">"
                builder.append(xml + "")
            }
            String concatenatedString = builder.toString();
            String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<"+'root'+">" + concatenatedString + "</"+'root'+">";
            String xmlOutput = convertToXML(xmlString);
            outputStream.write(xmlOutput)
        }
        else
        results.each { violation ->
            outputStream.write("    [$violation.rule.severity] $violation.rule.ruleId - $violation.fileName ".bytes)
            outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
            outputStream.write("$violation.message \n".bytes)
        }
        SonarQubeReport sq = new SonarQubeReport();
        results.each { violation ->
            sq.getIssues().add( new SonarQubeReport.SonarQubeReportIssues(violation) )
        }
        outputStream.write("\nFound a total of $results.size violations.\n".bytes)
        outputStream.flush()
        outputStream.close()
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(sq)
        File file = new File('/Users/anusha.konakalla/Desktop/sonarqubeData.json')
        file.write(prettyJsonString)
    }

    String convertToXML(String xml){
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 2);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);

        Source xmlInput = new StreamSource(new StringReader(xml));
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    static class SonarQubeReport{
         static class SonarQubeReportIssues {
            static class SonarQubeReportLocation {
                static class TextRange {
                    Integer startLine
                    Integer endLine
                    Integer startColumn
                    Integer endColumn
                }
                String message
                String filePath
                TextRange textRange;
            }
            String engineId
            String ruleId
            String severity
            String type
            SonarQubeReportLocation primaryLocation;
            SonarQubeReportIssues(violation){
                ruleId = violation.rule.ruleId
                engineId = violation.rule.ruleName
                severity = violation.rule.severity
                type = violation.rule.ruleType
                this.primaryLocation = new SonarQubeReportLocation();
                this.primaryLocation.filePath = violation.fileName
                this.primaryLocation.message = violation.message
                this.primaryLocation.textRange = new SonarQubeReportLocation.TextRange();
                this.primaryLocation.textRange.startLine=(violation.lineNumber > 0  ?  violation.lineNumber  : Integer.parseInt('1'))
            }
        }

        List<SonarQubeReportIssues> issues
        SonarQubeReport(){
            this.issues = new ArrayList<>();
        }

    }

}
