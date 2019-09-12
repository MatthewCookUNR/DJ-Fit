// Program Information /////////////////////////////////////////////////////////
/*
 * @file YouTubeConfig.java
 *
 * @brief Class holds API key for YouTubePlayerAPI
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.fitness.dj_fit;

// YouTubeConfig Class ////////////////////////////////////////////////////////////////

public class YouTubeConfig
{
    public YouTubeConfig()
    {

    }

    //Insert your own YouTube API key
    private  static final String API_KEY = "Insert Key Here";

    public static String getApiKey()
    {
        return API_KEY;
    }
}
