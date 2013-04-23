package fr.eolya.simplepipeline.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import fr.eolya.extraction.ScriptsWrapper;
import fr.eolya.simplepipeline.config.PipelineConfig;
import fr.eolya.simplepipeline.document.Doc;
import fr.eolya.utils.Utils;

/*
 * Configuration snippet sample :
 * 
 *	<stage position="0" classname="fr.eolya.simplepipeline.stage.MetaExtractor">
 *      <param name="onaction">add</param>
 *      <param name="scriptspath">/opt/crawler/config/scripts</param>
 *      <param name="url">item_url</param>
 *      <param name="contenttype">item_contenttype</param>
 *      <param name="contentcharset">item_charset</param>
 *      <param name="source_html">content</param>
 *      <param name="source_text">text</param>
 *	</stage>
 */

public class MetaExtractor extends Stage {
    
    private String scriptsPath = null;
    private String urlElement = null;
    private String contentTypeElement = null;
    private String contentCharsetElement = null;
    private String sourceHtmlElement = null;
    private String sourceTextElement = null;
    
    /**
     * Perform initialization.
     */
    public void initialize() {
        super.initialize();
        
        scriptsPath = props.getProperty("scriptspath");
        scriptsPath = Utils.getValidPropertyPath(scriptsPath, null, "HOME");
        urlElement = props.getProperty("url");
        contentTypeElement = props.getProperty("contenttype");
        contentCharsetElement = props.getProperty("contentcharset");
        sourceHtmlElement = props.getProperty("source_html");
        sourceTextElement = props.getProperty("source_text");
    }
    
    @Override
    public void processDoc(Doc doc) throws Exception {
        
        String accountId = "";
        String contentType = "";
        String contentCharset = "";
        String url = "";
        String rawData = "";
        
        // Check onaction
        if (!doProcess(doc)) {
            if (nextStage != null)
                nextStage.processDoc(doc);	
            return;
        }
        
        java.util.Date startTime = new java.util.Date();
        
        if (logger!=null) logger.log("    field copy");
        
        if (urlElement != null && !"".equals(urlElement)) {
            url = doc.getElementText("//" + urlElement);
        }

        if (contentTypeElement != null && !"".equals(contentTypeElement)) {
            contentType = doc.getElementText("//" + contentTypeElement);
        }

        if (contentCharsetElement != null && !"".equals(contentCharsetElement)) {
            contentCharset = doc.getElementText("//" + contentCharsetElement);
        }

        if (contentType.startsWith("text/html")) {
            if (sourceHtmlElement != null && !"".equals(sourceHtmlElement)) {
                rawData = doc.getElementText("//" + sourceHtmlElement);
                rawData = StringEscapeUtils.unescapeHtml(rawData);
            }
        } else {
            if (sourceTextElement != null && !"".equals(sourceTextElement)) {
                rawData = doc.getElementText("//" + sourceTextElement);
            }
        }
        if (rawData!=null && !"".equals(rawData)) {

        accountId = doc.getElementText("//account_id");
        
        String scriptName = "";
        if (accountId!=null && !"".equals(accountId))
            scriptName = ScriptsWrapper.getScriptName (scriptsPath + "/" + accountId, url);
        
        if (scriptName==null || "".equals(scriptName))
            scriptName = ScriptsWrapper.getScriptName (scriptsPath, url);   
            if (url!=null && !"".equals(url) && scriptName!=null && !"".equals(scriptName)) {
                if (logger!=null && verbose) {
                    logger.log("    url          = " + url);
                    logger.log("    scripts name = " + scriptName);
                }
                HashMap<String, String> m = ScriptsWrapper.extractMeta(url, rawData, contentType, contentCharset, scriptName, scriptName);
                if (m!=null && m.size()>0) {
                    for (Map.Entry<String, String> entry : m.entrySet()) {
                        doc.addElement("/job", "meta_extracted_" + entry.getKey(), entry.getValue());
                    }
                }
            }
        }
       		
        java.util.Date endTime = new java.util.Date();
        processingTime += (endTime.getTime() - startTime.getTime());
        
        if (nextStage != null) {
            nextStage.processDoc(doc);
        }		
    }
    
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }
    
}