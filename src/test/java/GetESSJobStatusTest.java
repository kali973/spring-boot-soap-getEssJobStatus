import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static com.oracle.xmlns.endpoint.WebServiceEndpoint.httpPost;


@DirtiesContext
public class GetESSJobStatusTest {

    @Test
    public void http_client() throws Exception {
        System.out.println("Invoke service using direct HTTP call with Basic Auth");
        String payload =
                "<ns1:Envelope xmlns:ns1=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "    <ns1:Body>\n" +
                        "        <ns2:getESSJobStatus xmlns:ns2=\"http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/\">\n" +
                        "            <ns2:requestId>1577789</ns2:requestId>\n" +
                        "        </ns2:getESSJobStatus>\n" +
                        "    </ns1:Body>\n" +
                        "</ns1:Envelope>";

        httpPost("https://" + "eews-test.fa.em3.oraclecloud.com:443" + "/fscmService/ErpIntegrationService", payload, "WS_USER:Welcome1*");
    }
}