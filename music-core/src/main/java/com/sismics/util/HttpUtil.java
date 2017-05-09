package com.sismics.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP request utilities.
 * 
 * @author jtremeaux
 */
public class HttpUtil {
    /**
     * Loads the content of an URL into a string.
     * 
     * @param url URL to load
     * @return Contents of the resource
     */
    public static String readUrlIntoString(URL url) throws IOException {
        URLConnection connection;
        BufferedReader in = null;
        try {
            connection = url.openConnection();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }
    
    public static String postUrl(URL url, String data) throws IOException {
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        try {
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
    
            // Get the response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = rd.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException e) {
                    // NOP
                }
            }
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    // NOP
                }
            }
        }
    }
}
