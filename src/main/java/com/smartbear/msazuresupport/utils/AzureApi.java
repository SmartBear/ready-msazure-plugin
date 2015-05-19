package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.support.definition.support.ReadyApiXmlException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public final class AzureApi {
    private AzureApi() {
    }

    public static class ApiInfo {
        public final String name;
        public final String description;
        public final String path;
        public final String id;

        public ApiInfo(String name, String description, String path, String id) {
            this.name = name;
            this.description = description;
            this.path = path;
            this.id = id;
        }

        public ApiInfo(JsonObject obj) {
            this.name = obj.getString("name", null);
            this.description = obj.getString("description", null);
            this.path = obj.getString("path", null);
            this.id = obj.getString("id", null);
        }

        public Boolean isValid() {
            return StringUtils.hasContent(name) && StringUtils.hasContent(path) && StringUtils.hasContent(id);
        }

        @Override
        public String toString() {
            return String.format("name = %s, path = %s, id = %s", name, path, id);
        }
    }

    public static URL stringToUrl(String s) {
        if (StringUtils.isNullOrEmpty(s))
            return null;

        if (!s.toLowerCase().startsWith( "http://") && !s.toLowerCase().startsWith("https://")) {
            s = "https://" + s;
        }

        try {
            return new URL(s);
        }
        catch (MalformedURLException e){
            SoapUI.logError(e);
            return null;
        }
    }

    public static List<ApiInfo> getApiList(URL portalUrl) throws IOException {
        URL url = new URL(portalUrl, "/apis?api-version=2014-02-14-preview");
        Reader reader;
        try {
            reader = new InputStreamReader(url.openStream());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("No APIs available at this location. Please contact MS Azure support for assistance.");
        }

        JsonObject jsonObject;
        try (javax.json.JsonReader jsonReader = javax.json.Json.createReader(reader)) {
            jsonObject = jsonReader.readObject();
        } catch (JsonParsingException e) {
            throw new IOException("The list of APIs is not in the expected JSON format. Please contact MS Azure support for assistance.", e);
        }

        JsonValue apiList = jsonObject.get("value");
        if (apiList == null || !(apiList instanceof JsonArray)) {
            throw new IOException("API specification list has incorrect format: no \"value\" entry has been found.\nPlease contact MS Azure support for assistance.");
        }

        JsonArray apis = (JsonArray) apiList;
        ArrayList<ApiInfo> result = new ArrayList<>();
        for (javax.json.JsonValue it : apis) {
            if (it instanceof JsonObject) {
                ApiInfo api = new ApiInfo((JsonObject) it);
                if (api.isValid()) {
                    result.add(api);
                }
            }
        }

        return result;
    }

    public static File saveToTmpFile(String portalUrl, String apiID) throws IOException {
        URL url = new URL(new URL(portalUrl), apiID + "?api-version=2014-02-14&export=true");
        URLConnection connection = url.openConnection();

        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/vnd.sun.wadl+xml");
        connection.connect();

        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(sep);
            }
        }

        File file = File.createTempFile("msazure-", ".wadl");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
        }

        //set unique 'id' to the 'resource' elements
        try {
            XmlObject e = XmlUtils.createXmlObject(file.toURI().toURL());

            Document doc = (Document) e.getDomNode();
            Element element = doc.getDocumentElement();
            NodeList nodes = element.getElementsByTagName("resource");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node attr = doc.createAttribute("id");
                attr.setNodeValue("" + i);
                nodes.item(i).getAttributes().setNamedItem(attr);
            }

            e.save(file);
        } catch (XmlException e) {
            throw new IOException("Invalid format", e);
        }

        return file;
    }

    public static RestService importApiToProject(String portalUrl, ApiInfo api, WsdlProject project) throws IOException, ReadyApiXmlException {
        File file = AzureApi.saveToTmpFile(portalUrl, api.id);
        RestService rest = (RestService) project
                .addNewInterface(ModelItemNamer.createName("Service", project.getInterfaceList()), RestServiceFactory.REST_TYPE);
        WadlImporter importer = new WadlImporter(rest);
        importer.initFromWadl(file.toURI().toURL().toString());
        addSubscriptionKeyHeaderToAllRequests(rest, api);
        return rest;
    }

    private static void addSubscriptionKeyHeaderToAllRequests(RestService rest, ApiInfo api) {
        String customPropertyName = "subscription-key-" + api.name.replaceAll("\\s", "-");
        if (!rest.getProject().hasProperty(customPropertyName)) {
            rest.getProject().addProperty(customPropertyName).setValue("");
        }

        for (RestResource resource: rest.getAllOperations()) {
            for (int i = 0; i < resource.getRequestCount(); i++) {
                RestRequest request = resource.getRequestAt(i);
                StringToStringsMap headers = request.getRequestHeaders();
                headers.add("Ocp-Apim-Subscription-Key", String.format("${#Project#%s}", customPropertyName));
                request.setRequestHeaders(headers);
            }
        }
    }
}
