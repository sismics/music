package com.sismics.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang.WordUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lyrics utilities.
 * 
 * @author bgamard
 */
public class LyricUtil {
    /**
     * Lyrics Wiki URL.
     */
    private static final String LYRICS_WIKIA_URL = "https://lyrics.wikia.com/%s:%s?action=edit";
    
    /**
     * Regexp to extract lyrics.
     */
    private static final Pattern LYRICS_PATTERN = Pattern.compile("(?:&lt;|<)lyrics>(.*?)(?:&lt;|<)/lyrics>", Pattern.DOTALL);
    
    /**
     * Regexp to extract redirection.
     */
    private static final Pattern REDIRECT_PATTERN = Pattern.compile("#REDIRECT \\[\\[(.*?):(.*?)\\]\\]", Pattern.DOTALL);
    
    /**
     * Download lyrics and output them.
     * 
     * @param artist Artist
     * @param title Title
     * @return Lyrics
     */
    public static List<String> getLyrics(String artist, String title) throws IOException {
        if (artist == null || title == null) {
            throw new IllegalArgumentException("artist or title cannot be null");
        }
        
        String url = String.format(LYRICS_WIKIA_URL, wikiStyle(artist), wikiStyle(title));
        String data = HttpUtil.readUrlIntoString(new URL(url));
        
        // Handle redirections
        Matcher matcher = REDIRECT_PATTERN.matcher(data);
        if (matcher.find()) {
            url = String.format(LYRICS_WIKIA_URL, wikiStyle(matcher.group(1)), wikiStyle(matcher.group(2)));
            data = HttpUtil.readUrlIntoString(new URL(url));
        }
        
        // Extract lyrics
        matcher = LYRICS_PATTERN.matcher(data);
        List<String> lyrics = Lists.newArrayList();
        while (matcher.find()) {
            lyrics.add(matcher.group(1).trim());
        }
        
        if (lyrics.size() == 0) {
            throw new IOException("No lyrics found");
        }
        
        if (lyrics.get(0).contains("PUT LYRICS HERE")) {
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
