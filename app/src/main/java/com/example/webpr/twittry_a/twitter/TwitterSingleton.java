package com.example.webpr.twittry_a.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by webpr on 04.01.2017.
 */

public class TwitterSingleton {

    private static final String TWITTER_CONSUMER_KEY = "2vjveEZeELBE0sHb9qy3JZP4q";
    private static final String TWITTER_CONSUMER_SECRET = "JasH4nfsbDm8mxgmmHb4jyGArBGOOJlmP7k9FdcCXh0yKcrTkx";

    private Twitter mTwitter;
    private static TwitterSingleton instance;


    private TwitterSingleton(){
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        this.mTwitter = new TwitterFactory(configuration).getInstance();
    }

    /**
     * Returns instance of {@link TwitterSingleton}
     * @return {@link TwitterSingleton}
     */
    public static TwitterSingleton getInstance(){
        if(instance == null){
            instance = new TwitterSingleton();
        }
        return instance;
    }

    /**
     * Returns insance of {@link Twitter}
     * @return {@link Twitter}
     *
     */
    public Twitter getTwitter(){
        return mTwitter;
    }

    /**
     * Returns insance of {@link Twitter} built upon received {@link AccessToken}
     * @return {@link Twitter}
     *
     */
    public Twitter getAuthorizedTwitter(AccessToken accessToken){
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        this.mTwitter = new TwitterFactory(configuration).getInstance(accessToken);
        return mTwitter;
    }
}
