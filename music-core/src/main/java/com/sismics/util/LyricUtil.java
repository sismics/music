package com.sismics.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;

/**
 * Lyrics utilities.
 * 
 * @author bgamard
 */
public class LyricUtil {
    /**
     * Lyrics Wiki URL.
     */
    private static final String LYRICS_WIKIA_URL = "http://lyrics.wikia.com/%s:%s?action=edit";
    
    /**
     * Regexp to extract lyrics.
     */
    private static final Pattern LYRICS_PATTERN = Pattern.compile("(?:&lt;|<)lyrics>(.*?)(?:&lt;|<)/lyrics>", Pattern.DOTALL);
    
    /**
     * Download lyrics and output them.
     * 
     * @param artist Artist
     * @param title Title
     * @return Lyrics
     * @throws IOException
     */
    public static String getLyrics(String artist, String title) throws IOException {
        if (artist == null || title == null) {
            throw new IllegalArgumentException("artist or title cannot be null");
        }
        
        String url = String.format(LYRICS_WIKIA_URL, wikiStyle(artist), wikiStyle(title));
        String data = HttpUtil.readUrlIntoString(new URL(url));
        
        Matcher matcher = LYRICS_PATTERN.matcher(data);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            output.append(matcher.group(1).trim()).append("\n");
        }
        
        if (output.length() == 0) {
            throw new IOException("No lyrics found");
        }
        
        String lyrics = output.deleteCharAt(output.length() - 1).toString();
        
        if (lyrics.contains("PUT LYRICS HERE")) {
            throw new IOException("No lyrics found");
        }
        
        return lyrics;
    }
    
    /**
     * Encode a string in wiki style.
     * See http://lyrics.wikia.com/LyricWiki:Page_Names.
     * 
     * @param s Input
     * @return Encoded string
     * @throws IOException
     */
    private static String wikiStyle(String s) throws IOException {
        s = WordUtils.capitalize(s);
        s = s.replace(" ", "_");
        s = s.replace("<", "Less_Than");
        s = s.replace(">", "Greater_Than");
        s = s.replace("#", "Number_");
        s = s.replace("[", "(");
        s = s.replace("]", ")");
        s = s.replace("{", "(");
        s = s.replace("}", ")");
        return URLEncoder.encode(s, "UTF-8");
    }
}
