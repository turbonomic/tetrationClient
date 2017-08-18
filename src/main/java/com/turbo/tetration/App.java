package com.turbo.tetration;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.Gson;

public class App
{
    protected static final String dimentionURI = "/openapi/v1/flowsearch/dimensions";
    protected static final String trafficURI = "/openapi/v1/flowsearch";

    protected static void test(TetrationClient client) {
        String uri = dimentionURI;
        System.out.println(client.get(uri));
    }

    protected static void testFlowTraffic(TetrationClient client) {
        String uri = trafficURI;
        String offset = "";
        FlowEntries flow = null;
        int counter = 0;
        int interval = 600;

        System.out.println("Start time is " + new Date());
        Set<String> sourceIpSet = new HashSet<String>();
        while (true) {
            counter++;
            String payload = client.createPayload(offset, interval);
            String raw = client.post(uri, payload);
            flow = new Gson().fromJson(raw, FlowEntries.class);
            if (flow == null)
                break;

            offset = flow.getOffset();
            System.out.println("The current counter is " + counter);
            for (FlowEntry fEntry : flow.getResults()) {
                System.out.println("===========================");
                System.out.println("The source is " + fEntry.getSrc_address());
                System.out.println("The dest is " + fEntry.getDst_address());
                System.out.println("The Rec_Bytes is " + fEntry.getRev_bytes());
                System.out.println("The Fwd_Bytes is " + fEntry.getFwd_bytes());
                System.out.println("The Rec_Pkts is " + fEntry.getRev_pkts());
                System.out.println("The Fwd_Pkts is " + fEntry.getFwd_pkts());

            }
            if (offset == null || "".equals(offset))
                break;
        }
        System.out.println("End time is " + new Date());
        System.out.println("counter = " + counter);
    }

    public static void printConnection(TetrationClient client) {
        client.printGetHeader(dimentionURI);

        String offset = "";
        int interval = 600;
        String payload = client.createPayload(offset, interval);

        client.printPostHeader(trafficURI, payload);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: App <host> <apiKey> <apiSecret> <printHeader> [connTimeout] [readTimeout]");
            return;
        }

        int connTimeOut = 2000; //2 seconds
        int readTimeOut = 60000; // 1 minute
        String host = args[0];
        String apiKey = args[1];
        String apiSecret = args[2];
        boolean printHeader = Boolean.parseBoolean(args[3]);

        if (args.length > 4) {
            connTimeOut = Integer.parseInt(args[4]);
        }
        if (args.length > 5) {
            readTimeOut = Integer.parseInt(args[5]);
        }

        TetrationClient client = new TetrationClient(host, apiKey, apiSecret);
        client.setPrintHeader(printHeader);
        client.setConnTimeOut(connTimeOut);
        client.setReadTimeOut(readTimeOut);

        //printConnection(client);
        test(client);

        testFlowTraffic(client);
    }
}
