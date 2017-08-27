package edu.cmu.hw5htiruvee;

import android.app.Application;
import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

/**
 * Created by haripriya on 6/24/2017.
 */

public class AppExtend extends Application {
    private static final String CONSUMER_KEY = "9Ebv806WgAt3YYdDoGFWfuOUB";
    private static final String CONSUMER_SECRET = "EEJ1ubD2Mh7QvezXqAE7hroDwbpDb9RxK7hdrVwfSHSxajYi53";

    @Override
    public void onCreate(){
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }
}
