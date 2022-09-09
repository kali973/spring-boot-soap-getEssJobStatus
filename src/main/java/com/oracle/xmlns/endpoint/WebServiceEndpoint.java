package com.oracle.xmlns.endpoint;

import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.GetESSJobStatus;
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.ObjectFactory;
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.SubmitESSJobRequestResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;

import javax.net.ssl.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Endpoint
public class WebServiceEndpoint {


    /**
     * *
     * Process SOAP request to ERP cloud.
     */
    public static SOAPMessage processSoapRequest(String serviceURL, SOAPMessage soapRequest) throws Exception {

        if (serviceURL == null || soapRequest == null) {
            throw new Exception("Invalid input to processSoapRequest.");
        }

        if (serviceURL.isEmpty()) {
            throw new Exception("Invalid service URL input to processSoapRequest.");
        }

        HttpsURLConnection objHTTPSConn = null;
        SOAPConnection soapConnection = null;
        boolean blHTTPS = serviceURL.toLowerCase().startsWith("https");

        try {
            if (blHTTPS) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] objTrust
                        = new TrustManager[]{new TrustManager()};
                sslContext.init(null, objTrust, new java.security.SecureRandom());
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                URL url = new URL(serviceURL);
                objHTTPSConn = (HttpsURLConnection) url.openConnection();
                objHTTPSConn.setHostnameVerifier(new CHostnameVerifier());
                objHTTPSConn.connect();
            }
            soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //Submit SOAP request
            return soapConnection.call(soapRequest, serviceURL);
        } catch (Exception ex) {
            System.out.println("Error processing SOAP request:" + ex.getMessage());
            throw new Exception("Error processing SOAP request.");
        } finally {
            if (soapConnection != null) {
                soapConnection.close();
            }
            if (blHTTPS && objHTTPSConn != null) objHTTPSConn.disconnect();
        }
    }

    public static String httpPost(String destUrl, String postData,
                                  String authStr) throws Exception {
        URL url = new URL(destUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn == null) {
            return null;
        }
        conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setFollowRedirects(true);
        conn.setAllowUserInteraction(false);
        conn.setRequestMethod("POST");

        byte[] authBytes = authStr.getBytes("UTF-8");
        String auth = Base64.getEncoder().encodeToString(authBytes);
        conn.setRequestProperty("Authorization", "Basic " + auth);

        System.out.println("post data size:" + postData.length());

        OutputStream out = conn.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(postData);
        writer.close();
        out.close();

        System.out.println("connection status: " + conn.getResponseCode() +
                "; connection response: " +
                conn.getResponseMessage());

        InputStream in = conn.getInputStream();
        InputStreamReader iReader = new InputStreamReader(in);
        BufferedReader bReader = new BufferedReader(iReader);

        String line;
        String response = "";
        System.out.println("==================Service response: ================ ");
        while ((line = bReader.readLine()) != null) {
            System.out.println(line);
            response += line;
        }
        iReader.close();
        bReader.close();
        in.close();
        conn.disconnect();


        return response;
    }

    @PayloadRoot(namespace = "http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/", localPart = "getESSJobStatus")
    @ResponsePayload
    public SubmitESSJobRequestResponse getESSJobStatus(@RequestPayload GetESSJobStatus request) throws Exception {

        SubmitESSJobRequestResponse response;
        try {
            long outputString = request.getRequestId();
            GetESSJobStatus objGetESSJobStatus = new GetESSJobStatus();
            objGetESSJobStatus.setRequestId(outputString);

            com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.ObjectFactory objObjectFactory
                    = new com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.ObjectFactory();

            JAXBContext jaxbContext = JAXBContext.newInstance(GetESSJobStatus.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            jaxbMarshaller.marshal(objGetESSJobStatus, document);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPBody().addDocument(document);

            //Set authorization header
            String strAuth = "WS_USER" + ":" + "Welcome1*";
            soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode(strAuth.getBytes())));

            SOAPMessage soapRespMessage = processSoapRequest("https://eews-test.fa.em3.oraclecloud.com:443/fscmService/ErpIntegrationService", soapMessage);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            soapMessage.writeTo(stream);
            String message = new String(stream.toByteArray(), "utf-8");
            System.getProperties().put("http.proxyHost", "10.34.40.3");
            System.getProperties().put("http.proxyPort", "443");

            httpPost("https://" + "eews-test.fa.em3.oraclecloud.com:443" + "/fscmService/ErpIntegrationService", message, "WS_USER:Welcome1*");

            System.out.println("Request:" + message);
            System.out.println("Status:" + soapRespMessage.getSOAPBody().getElementsByTagNameNS("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/", "result").item(0).getTextContent());
            System.out.println("GetESSJobStatus completed successfully.");

            ObjectFactory factory = new ObjectFactory();
            response = factory.createSubmitESSJobRequestResponse();
            response.setResult(outputString);

        } catch (Exception ex) {
            System.out.println("GetESSJobStatus failed. Error:" + ex.getMessage());
            throw new Exception("GetESSJobStatus failed. Check logs for more info.");
        }
        return response;
    }

    /**
     * *
     * Modify this method to alter hostname verification behavior in SSL/TLS
     * later.
     */
    private static class CHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {

            try {
                if (hostname.isEmpty() || session == null) {
                    throw new Exception("Invalid input to verify.");
                }
                if (hostname.equalsIgnoreCase(session.getPeerHost()))
                    return true;
            } catch (Exception ex) {
                System.out.println("Error verifying hostname." + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * *
     * Defines trust manager for TLS connection
     */
    private static class TrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            /*
             *Default behavior. Modify this method to alter.
             */
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            /*
             *Default behavior. Modify this method to alter.
             */
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

