package edu.cmu.hw5htiruvee;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import static android.content.ContentValues.TAG;

public class Twitter4jActivity extends AppCompatActivity {

    public static String TWITTER_CONSUMER_KEY = "4vPUq1nMXpU4Xj1ir80wVJbrN";
    public static String TWITTER_CONSUMER_SECRET = "gB2GLKhyrZgl7n8oSSbOhcdXimxZFTEUlqBqepiFnxzjjO6fP0";
    public static String PREFERENCE_TWITTER_LOGGED_IN="TWITTER_LOGGED_IN";

    private Button checkInButton;
    Dialog auth_dialog;
    WebView web;
    SharedPreferences pref;
    Twitter twitter;
    RequestToken requestToken ;
    AccessToken accessToken;
    private TwitterLoginButton loginButton;
    String oauth_url,oauth_verifier,profile_url;
    TwitterSession session;
    TwitterAuthToken authToken;
    TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter4j);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", TWITTER_CONSUMER_KEY);
        edit.putString("CONSUMER_SECRET", TWITTER_CONSUMER_SECRET);
        edit.commit();
        Bundle recdData = getIntent().getExtras();
        String myVal = recdData.getString("itemData");
        name = (TextView)findViewById(R.id.name);
        name.setText(getIntent().getStringExtra("itemData"));
        Toast.makeText(getBaseContext(),"Status:"+ myVal, Toast.LENGTH_SHORT).show();
        Log.d("Status",myVal);



        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""), pref.getString("CONSUMER_SECRET", ""));
        checkInButton = (Button) findViewById(R.id.checkInButton);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIn();
            }
        });



    }



    private void checkIn() {
        if (!pref.getBoolean(PREFERENCE_TWITTER_LOGGED_IN,false))
        {
            new TokenGet().execute(); //no Token obtained, first time use
        }
        else
        {
            new PostTweet().execute(); //when Tokens are obtained , ready to Post
        }
    }

    private class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            twitter4j.Status response=null;
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
            builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
            AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""),
                    pref.getString("ACCESS_TOKEN_SECRET", ""));
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            String currentDateTime = sdf.format(new Date());
            //getIntent().getStringExtra("EventName");
            Bundle recdData = getIntent().getExtras();
            String myVal = recdData.getString("itemData");
            //Toast.makeText(getBaseContext(),"Value to be tweeted: " + myVal, Toast.LENGTH_SHORT).show();
            String status = "@08723Mapp" + " " +
                    "htiruvee " + myVal;
            Log.d("Status",status);
            try {
                response = twitter.updateStatus(status);
            } catch (TwitterException e) {
                if (e.getErrorCode()==187)
                    Toast.makeText(getBaseContext(),"Duplicate message so could not tweet!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return response.toString();
        }

        protected void onPostExecute(String res) {
            if (res != null) {
                //progress.dismiss();
                Toast.makeText(getBaseContext(), "Tweet successfully Posted", Toast.LENGTH_SHORT).show();
            } else {
                //progress.dismiss();
                Toast.makeText(getBaseContext(), "Error while tweeting !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class TokenGet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            try {
                requestToken = twitter.getOAuthRequestToken();
                oauth_url = requestToken.getAuthorizationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return oauth_url;
        }
        @Override
        protected void onPostExecute(String oauth_url) {
            if(oauth_url != null){
                auth_dialog = new Dialog(Twitter4jActivity.this);
                auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                auth_dialog.setContentView(R.layout.oauth_webview);
                web = (WebView)auth_dialog.findViewById(R.id.webViewOAuth);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(oauth_url);
                web.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon){
                        super.onPageStarted(view, url, favicon);
                    }
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains("oauth_verifier") && authComplete == false){
                            authComplete = true;
                            Uri uri = Uri.parse(url);
                            oauth_verifier = uri.getQueryParameter("oauth_verifier");
                            auth_dialog.dismiss();
                            new AccessTokenGet().execute();
                        }else if(url.contains("denied")){
                            auth_dialog.dismiss();
                            Toast.makeText(getBaseContext(), "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, auth_dialog.toString());
                auth_dialog.show();
                auth_dialog.setCancelable(true);
            }else
            {
                Toast.makeText(getBaseContext(), "Sorry !, Error or Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AccessTokenGet extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... args) {
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("ACCESS_TOKEN", accessToken.getToken());
                edit.putString("ACCESS_TOKEN_SECRET", accessToken.getTokenSecret());
                edit.putBoolean(PREFERENCE_TWITTER_LOGGED_IN, true);
                User user = twitter.showUser(accessToken.getUserId());
                profile_url = user.getOriginalProfileImageURL();
                edit.putString("NAME", user.getName());
                edit.putString("IMAGE_URL", user.getOriginalProfileImageURL());
                edit.commit();
            } catch (TwitterException e) {
                if(e.getStatusCode()==401L){
                    AlertDialog alertDialog = new AlertDialog.Builder(Twitter4jActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Access not granted. Please login");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                e.printStackTrace();
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean response) {
            if(response){
                //progress.hide(); after login, tweet Post right away
                new PostTweet().execute();
            }
        }
    }

}
