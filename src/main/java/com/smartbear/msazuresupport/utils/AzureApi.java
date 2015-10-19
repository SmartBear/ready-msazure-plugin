package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.support.definition.support.ReadyApiXmlException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import com.smartbear.msazuresupport.Strings;
import com.smartbear.msazuresupport.entities.ApiInfo;
import com.smartbear.msazuresupport.entities.Product;
import com.smartbear.msazuresupport.entities.Subscription;
import com.smartbear.msazuresupport.entities.User;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class AzureApi {
    private AzureApi() {
    }

    public static class ConnectionSettings {
        public final URL Url;
        public final String accessToken;

        public ConnectionSettings(URL url, String accessToken) {
            this.Url = url;
            this.accessToken = accessToken;
        }

        public ConnectionSettings(String url, String accessToken) {
            this.Url = stringToUrl(url);
            this.accessToken = accessToken;
        }
    }

    public static URL stringToUrl(String s) {
        if (StringUtils.isNullOrEmpty(s)) {
            return null;
        }

        if (!s.toLowerCase().startsWith("http://") && !s.toLowerCase().startsWith("https://")) {
            s = "https://" + s;
        }

        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    private static JsonObject performRequest(ConnectionSettings connectionSettings, String location) throws IOException{
        URL url = new URL(connectionSettings.Url, String.format("/%s?api-version=2014-02-14-preview", location));

        URLConnection connection = url.openConnection();
        connection.setDoInput(true);
        connection.setRequestProperty("Authorization", connectionSettings.accessToken);
        try {
            connection.connect();
        } catch (UnknownHostException e) {
            throw new FileNotFoundException(String.format(Strings.AzureRestApi.UNAVAILABLE_HOST_ERROR, connectionSettings.Url.toString()));
        }

        Reader reader;
        try {
            reader = new InputStreamReader(connection.getInputStream());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(String.format(Strings.AzureRestApi.UNAVAILABLE_DATA_ERROR, location));
        } catch (IOException e) {
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            if (httpConnection != null && httpConnection.getResponseCode() == 401) {
                throw new InvalidAuthorizationException(e);
            } else {
                throw e;
            }
        }

        try (javax.json.JsonReader jsonReader = javax.json.Json.createReader(reader)) {
            return jsonReader.readObject();
        } catch (JsonParsingException e) {
            throw new IOException(String.format(Strings.AzureRestApi.UNEXPECTED_RESPONSE_FORMAT_ERROR, location), e);
        }
    }


    public static List<ApiInfo> getApiList(ConnectionSettings connectionSettings) throws IOException {
        JsonObject obj = AzureApi.performRequest(connectionSettings, "apis");
        return Helper.extractList(obj, new Helper.EntityFactory<ApiInfo>() {
            @Override
            public ApiInfo create(JsonObject value) {
                return new ApiInfo(value);
            }
        });
    }

    private static List<String> getProductApis(ConnectionSettings connectionSettings, String productId) {
        try {
            JsonObject obj = AzureApi.performRequest(connectionSettings, productId.substring(1) + "/apis");
            return Helper.<String>extractList(obj, new Helper.EntityFactory<String>() {
                @Override
                public String create(JsonObject value) {
                    return value.getString("id", "");
                }
            });
        } catch (IOException e) {
            SoapUI.logError(e);
            return new ArrayList<>();
        }
    }

    private static Helper.EntityFactory<Subscription> getSubscriptionFactory(final Subscription.KeyKind keyKind, final List<User> users, final List<Product> products) {
        return new Helper.EntityFactory<Subscription>() {
            @Override
            public Subscription create(JsonObject value) {
                return new Subscription(value, keyKind, users, products);
            }
        };
    }

    public static List<Subscription> getSubscriptionList(final ConnectionSettings connectionSettings) throws IOException {
        JsonObject obj = AzureApi.performRequest(connectionSettings, "products");
        final List<Product> products = Helper.extractList(obj, new Helper.EntityFactory<Product>() {
            @Override
            public Product create(JsonObject value) {
                List<String> apis = AzureApi.getProductApis(connectionSettings, value.getString("id", ""));
                return new Product(value, apis);
            }
        });

        obj = AzureApi.performRequest(connectionSettings, "users");
        final List<User> users = Helper.extractList(obj, new Helper.EntityFactory<User>() {
            @Override
            public User create(JsonObject value) {
                return new User(value);
            }
        });

        obj = AzureApi.performRequest(connectionSettings, "subscriptions");
        List<Subscription> primaries = Helper.extractList(obj, getSubscriptionFactory(Subscription.KeyKind.PRIMARY, users, products));
        List<Subscription> secondaries = Helper.extractList(obj, getSubscriptionFactory(Subscription.KeyKind.SECONDARY, users, products));
        ArrayList<Subscription> result = new ArrayList<>();
        result.addAll(primaries);
        result.addAll(secondaries);
        Collections.sort(result, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return result;
    }

    public static File saveToTmpFile(ConnectionSettings connectionSettings, String apiID) throws IOException {
        URL url = new URL(connectionSettings.Url, apiID + "?api-version=2014-02-14&export=true");
        URLConnection connection = url.openConnection();

        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/vnd.sun.wadl+xml");
        connection.setRequestProperty("Authorization", connectionSettings.accessToken);
        connection.connect();

        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(sep);
            }
        } catch (IOException e) {
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            if (httpConnection != null && httpConnection.getResponseCode() == 401) {
                throw new InvalidAuthorizationException(e);
            } else {
                throw e;
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

    public static RestService importApiToProject(ConnectionSettings connectionSettings, ApiInfo api, WsdlProject project) throws IOException, ReadyApiXmlException {
        File file = AzureApi.saveToTmpFile(connectionSettings, api.id);
        RestService rest = (RestService) project
                .addNewInterface(ModelItemNamer.createName("Service", project.getInterfaceList()), RestServiceFactory.REST_TYPE);
        WadlImporter importer = new WadlImporter(rest);
        importer.initFromWadl(file.toURI().toURL().toString());
        addSubscriptionKeyHeaderToFirstLevelResources(rest, api);
        return rest;
    }

    private static void addSubscriptionKeyHeaderToFirstLevelResources(RestService rest, ApiInfo api) {
        String customPropertyName = "subscription-key-" + api.name.replaceAll("\\s", "-");
        WsdlProject project = rest.getProject();
        if (!project.hasProperty(customPropertyName)) {
            project.addProperty(customPropertyName);
        }

        Subscription subscription = api.getSubscription();
        project.getProperty(customPropertyName).setValue(subscription != null ? subscription.key : "");

        for (RestResource resource : rest.getResourceList()) {
            RestParamProperty param = resource.addProperty("Ocp-Apim-Subscription-Key");
            String keyValue = String.format("${#Project#%s}", customPropertyName);
            param.setValue(keyValue);
            param.setDefaultValue(keyValue);
            param.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
        }
    }
}
