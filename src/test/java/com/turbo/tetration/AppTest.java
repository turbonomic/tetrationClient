package com.turbo.tetration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.google.gson.Gson;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /**
     * Test Gson
     */
    public void testGson()
    {
        FlowEntry fe = new FlowEntry();
        fe.setDst_address("192.168.1.1");
        fe.setDst_hostname("dst.host");
        fe.setDst_port(80);

        fe.setSrc_address("192.168.1.3");
        fe.setSrc_hostname("src.host");
        fe.setSrc_port(80);

        String content = new Gson().toJson(fe);
        System.out.println(content);

        FlowEntry nfe = new Gson().fromJson(content, FlowEntry.class);
        System.out.println(nfe.getDst_hostname());
    }
}
