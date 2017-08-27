package edu.cmu.hw5htiruvee;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

public class Title1 extends ListFragment {
    private TwitterLoginButton loginButton;
    TwitterSession session;
    TwitterAuthToken authToken;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_title1, container, false);
    }

    private class GetTimeline extends AsyncTask<Void, Void, TweetTimelineListAdapter> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //fetching….
        }

        protected TweetTimelineListAdapter doInBackground(Void... params) {
            final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                    .query("@08723Mapp")
                    .build();
            final TweetTimelineListAdapter timelineAdapter = new TweetTimelineListAdapter(getActivity(),
                    searchTimeline);
            return timelineAdapter;
        }

        protected void onPostExecute(TweetTimelineListAdapter result) {
            setListAdapter(result);

        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginButton = (TwitterLoginButton) getActivity().findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                Toast.makeText(getActivity(),"Authentication Success!!!",Toast.LENGTH_SHORT).show();
                session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                authToken = session.getAuthToken();
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Log.d("Failed",exception.getLocalizedMessage());
                Toast.makeText(getActivity(),"Oops failed!!!" + exception.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        new GetTimeline().execute();
    }




}
