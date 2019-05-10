package com.example.dj_fit;

//Class stores API key and allows activities to retrieve it
public class YouTubeConfig
{
    public YouTubeConfig()
    {

    }

    private  static final String API_KEY = "AIzaSyAVuqwy5VZe34elJDKEM7RrZ7eEJdFY2hU";

    public static String getApiKey()
    {
        return API_KEY;
    }
}
