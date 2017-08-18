package com.turbo.tetration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.net.ssl.HttpsURLConnection;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.MessageDigest;

public class TetrationClient {
    protected String host = "";
    protected String apiKey = "";
    protected String apiSecret = "";
    protected int timeOut = 2000; // 2 seconds, in milliseconds;
    protected int readTimeOut = 60000; // 60 seconds;

    private static final SimpleDateFormat CISCO_SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final String SHA256 = "HmacSHA256";
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String USER_AGENT_VALUE = "Cisco Tetration Java Client";
    private static final String C_TYPE_KEY = "Content-Type";
    private static final String C_TYPE_JSON = "application/json";
    private static final String CKSUM = "X-Tetration-Cksum";
    private static final String ID_KEY = "Id";
    private static final String AUTH_KEY = "Authorization";
    private static final String TIMESTAMP_KEY = "Timestamp";
    private static final String UTC = "UTC";
    private static final String TIME_POSTFIX = "+0000";
    private static final String UTF8 = "UTF8";
    private static final String NEW_LINE = "\n";

    private static final String MethodGet = "GET";
    private static final String MethodPost = "POST";

    public TetrationClient(String host, String apiKey, String apiSecret) {
        if (host.startsWith("http")) {
            this.host = host;
        } else {
            this.host = "https://" + host;
        }

        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }


    protected boolean setupConnection(HttpsURLConnection conn, String method, String uri, String payload) {
        String payloadSha256 = "";

        try {
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(this.timeOut);
            conn.setReadTimeout(this.readTimeOut);

            conn.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);
            conn.setRequestProperty(C_TYPE_KEY, C_TYPE_JSON);
            conn.setRequestProperty(ID_KEY, this.apiKey);

            if (method.equalsIgnoreCase("POST")) {
                payloadSha256 = calcSha256(payload);
                conn.setRequestProperty(CKSUM, payloadSha256);
            }

            String ts = getCurrentDate();
            String auth = encryptContent(apiSecret, method, uri, payloadSha256, ts);
            conn.setRequestProperty(TIMESTAMP_KEY, ts);
            conn.setRequestProperty(AUTH_KEY, auth);
            //System.out.println(AUTH_KEY + ": " + auth + "\n");

        } catch (Exception e) {
            System.err.println("failed to setup Tetration Connection.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected HttpsURLConnection getHttpsConnection(String url) {
        return TrustedHttpsConnection.genConnection(url);
    }

    protected String calcSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getCurrentDate() {
        CISCO_SDF.setTimeZone(TimeZone.getTimeZone(UTC));
        String date = CISCO_SDF.format(new Date());
        date = date.concat(TIME_POSTFIX);
        return date;
    }

    //TODO: delete it
    private byte[] hmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(SHA256);
        mac.init(new SecretKeySpec(key, SHA256));
        return mac.doFinal(data.getBytes(UTF8));
    }

    private String authStringBuilder(String method, String uri, String checksum, String ctype,
                                     String timeStamp) {
        StringBuilder authStringBuilder = new StringBuilder();
        authStringBuilder.append(method + NEW_LINE);
        authStringBuilder.append(uri + NEW_LINE);
        authStringBuilder.append(checksum + NEW_LINE);
        authStringBuilder.append(ctype + NEW_LINE);
        authStringBuilder.append(timeStamp + NEW_LINE);
        return authStringBuilder.toString();
    }

    private String encryptContent(String secret, String method, String uri, String cksum,
                                  String currentTimeString) throws Exception {
        String data = authStringBuilder(method, uri, cksum, C_TYPE_JSON, currentTimeString);
        //byte[] hmacSHA256 = hmacSHA256(data, secret.getBytes());

        Mac mac = Mac.getInstance(SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(), SHA256));
        byte[] hmacSHA256 = mac.doFinal(data.getBytes(UTF8));

        return Base64.getEncoder().encodeToString(hmacSHA256);
    }

    protected String doGet(HttpsURLConnection conn) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            conn.connect();
            if (conn.getResponseCode() != 200 ) {
                System.err.println("Bad result: " + conn.getResponseCode() + ", " + conn.getResponseMessage());
                conn.disconnect();
                return "";
            }
            InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());
            br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(NEW_LINE);
            }

        } catch (Exception e) {
            System.err.println("exception when doGet.");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error during closing bufferedReader");
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return sb.toString();
    }

    public String get(String uri) {
        String url = this.host + uri;

        HttpsURLConnection conn = getHttpsConnection(url);
        if (conn == null) {
            System.out.println("get " + url + " failed");
            return "";
        }

        if (!setupConnection(conn, MethodGet, uri, "")) {
            return "";
        }

        return doGet(conn);
    }

    protected String doPost(HttpsURLConnection conn, String data) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();

            if (conn.getResponseCode() != 200 ) {
                System.err.println("Bad result: " + conn.getResponseCode() + ", " + conn.getResponseMessage());
                conn.disconnect();
                return "";
            }

            String line;
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(NEW_LINE);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception when execute post");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error during closing bufferedReader");
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return sb.toString();
    }

    public String post(String uri, String data) {
        String url = this.host + uri;

        HttpsURLConnection conn = getHttpsConnection(url);
        if (conn == null) {
            System.out.println("get " + url + " failed");
            return "";
        }

        if (!setupConnection(conn, MethodPost, uri, data)) {
            return "";
        }

        return doPost(conn, data);
    }

	/*
	 * private static String createPayload(String offset) { Gson gson = new
	 * Gson(); Map<String, String> map = new LinkedHashMap<String,String>();
	 * String lastPollTime = String.valueOf(Instant.now().getEpochSecond() -
	 * 600); String currentTime =
	 * String.valueOf(Instant.now().getEpochSecond()); //LastPollTime: 10mins
	 * ago String payload = null; map.put(PAYLOAD_START_TIME_KEY, lastPollTime
	 * ); map.put(PAYLOAD_END_TIME_KEY, currentTime); map.put(PAYLOAD_LIMIT_KEY,
	 * DEFAULT_QUERY_LIMIT); if (offset.trim().length() > 0)
	 * map.put(PAYLOAD_OFFSET_KEY, offset); payload = gson.toJson(map);
	 * System.out.println(payload); return payload; }
	 */

    // interval = x seconds, usually interval=600, which is 10 minutes;
    public String createPayload(String offset, int interval) {
        long now = System.currentTimeMillis()/1000;
        String currentTime = String.valueOf(now);

        // LastPollTime: 10mins ago
        String lastPollTime = String.valueOf(now - interval);
        String payload = null;
        if (!"".equals(offset)) {
            payload = "{\"t0\":\"" + lastPollTime + "\",\"t1\":\"" + currentTime + "\"" + ",\"filter\":{}"
                    + ",\"limit\":5000" + ",\"offset\":\"" + offset + "\"}";
        } else {
            payload = "{\"t0\":\"" + lastPollTime + "\",\"t1\":\"" + currentTime + "\"" + ",\"filter\":{}"
                    + ",\"limit\":5000" + "}";
        }
        System.out.println(payload);
        return payload;

    }

    //------------- for debug ----------------------
    /*
    * we cannot get back the "Authorization" RequestProperty field.
    * https://stackoverflow.com/questions/6564015/httpurlconnection-c-url-openconnection-c-setrequestproperty-doesnt-work
    * */
    protected String getRequestContent(HttpsURLConnection conn) {
        StringBuilder sb = new StringBuilder();

        sb.append("url :" + conn.getURL());
        sb.append("\n[request.properties]\n");
        Map<String, List<String>> properties = conn.getRequestProperties();
        for (String key : properties.keySet()) {
            sb.append(key);
            sb.append(": ");
            sb.append(conn.getRequestProperty(key));
            sb.append('\n');
        }

        //String key = AUTH_KEY;
        //sb.append(key);
        //sb.append(": ");
        //sb.append(conn.getRequestProperty(key));
        sb.append("\n\n");

        return sb.toString();
    }

    public void printGetHeader(String uri) {
        String url = this.host + uri;

        HttpsURLConnection conn = getHttpsConnection(url);
        if (conn == null) {
            System.out.println("get " + url + " failed");
            return ;
        }

        if (!setupConnection(conn, MethodGet, uri, "")) {
            return ;
        }

        System.out.println(getRequestContent(conn));
        conn.disconnect();
        return;
    }


    public void printPostHeader(String uri, String payload) {
        String url = this.host + uri;

        HttpsURLConnection conn = getHttpsConnection(url);
        if (conn == null) {
            System.out.println("get " + url + " failed");
            return ;
        }

        if (!setupConnection(conn, MethodPost, uri, payload)) {
            return ;
        }

        System.out.println(getRequestContent(conn));
        System.out.println(payload);
        conn.disconnect();
        return;
    }
}

