package com.example.demo.SentinelRestService;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

//
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;
import java.net.URL;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;



@Service
public class SentinelRestService {
    private String baseUrl = "";
    private String credential = "";
    private String token = "";
    private String contentType = "application/json";


    public SentinelRestService() {
    }

    private String sentinelRestConnect(String uri, String method, String contentType, String authorization, JSONObject parameters) {
        String result = "";

        try {
            HttpMethod httpMethod = HttpMethod.resolve(method.toUpperCase(Locale.ROOT));
            if (httpMethod == null) {
                httpMethod = HttpMethod.GET;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (!authorization.isEmpty()) {
                headers.add("Authorization", authorization);
            } else if (!token.isEmpty()) {
                headers.add("Authorization", "X-SAML " + token);
            }

            // 構建請求實體
            HttpEntity<String> entity;
            if (parameters == null) {
                entity = new HttpEntity<>("", headers);
            } else {
                String json = parameters.toString();
                entity = new HttpEntity<>(json, headers);
            }
            URL url = new URL(uriCombine(baseUrl, uri));
            boolean isHttps = url.getProtocol().equalsIgnoreCase("https");
            // 構建 URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uriCombine(baseUrl, uri));

            RestTemplate restTemplate = createRestTemplate(isHttps);

            // 發送請求
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), httpMethod, entity, String.class);

            // 處理響應
            if (response.getStatusCode().is2xxSuccessful()) {
                result = response.getBody();
            } else {
                // 錯誤處理
                result = response.getStatusCode() + " " + response.getStatusCode().getReasonPhrase();
            }

            // 日誌記錄
            // ... 日誌記錄邏輯 ...

        } catch (Exception ex) {
            // 異常處理
            // ... 異常處理邏輯 ...
        }

        return result;
    }

    private RestTemplate createRestTemplate(boolean ignoreSsl) throws Exception {
        if (ignoreSsl) {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                            .setRedirectsEnabled(false)
                            .build())
                    .setSSLSocketFactory(sslSocketFactory)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            return new RestTemplate(requestFactory);
        } else {
            return new RestTemplate();
        }
    }

    // Method to validate URL
    private boolean isValidUrl(String url) {
        Pattern pattern = Pattern.compile("^http(s)?://\\S+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    // Method to combine two URI strings
    private String uriCombine(String uri1, String uri2) {
        uri1 = uri1 != null ? uri1.trim().replaceAll("/$", "") : "";
        uri2 = uri2 != null ? uri2.trim().replaceAll("^/", "") : "";
        return uri1 + "/" + uri2;
    }

    // Method to validate if a string is a valid JSON
    public static boolean isValidJson(String strInput) {
        strInput = strInput.trim();
        if ((strInput.startsWith("{") && strInput.endsWith("}")) || // For object
                (strInput.startsWith("[") && strInput.endsWith("]"))) { // For array
            try {
                new JSONObject(strInput);
                return true;
            } catch (JSONException ex) {
                return false;
            }
        } else {
            return false;
        }
    }
}
