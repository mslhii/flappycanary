package com.kritikalerror.FlappyCanary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;
import com.kritikalerror.FlappyCanary.ActionResolver;
import com.kritikalerror.FlappyCanary.ZBGame;
import com.kritikalerror.FlappyCanary.R;
import com.google.android.gms.games.Games;

public class MainActivity extends AndroidApplication implements GameHelperListener, ActionResolver
{
	private AdView adView;
	private GameHelper gameHelper;
	private final static int REQUEST_CODE_UNUSED = 9002;
	private static final String TAG = "MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create the layout
		RelativeLayout layout = new RelativeLayout(this);

		// Do the stuff that initialize() would do for you
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		
		// Create the GameHelper.
		gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
		gameHelper.enableDebugLog(true);

		GameHelperListener gameHelperListener = new GameHelper.GameHelperListener()
		{
			@Override
			public void onSignInSucceeded()
			{
				Log.w(TAG, "Signin successful!");
			}

			@Override
			public void onSignInFailed()
			{
				Log.w(TAG, "Signin failed!");
			}
		};
	
		gameHelper.setMaxAutoSignInAttempts(0);
		gameHelper.setup(gameHelperListener);

		// Create the libgdx View
		View gameView = initializeForView(new ZBGame(this), false);

		// Create and setup the AdMob view
		adView = new AdView(this);

		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId("ca-app-pub-6309606968767978/2177105243");
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();        

		// Add the libgdx view
		layout.addView(gameView);

		// Add the AdMob view
		RelativeLayout.LayoutParams adParams = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		layout.addView(adView, adParams);

		adView.loadAd(adRequestBuilder.build());

		// Hook it all up
		setContentView(layout);

	}

	@Override
	protected void onStart()
	{
		super.onStart();
		gameHelper.onStart(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		gameHelper.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		gameHelper.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void signIn() {
		// TODO Auto-generated method stub
		try
		{
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					gameHelper.beginUserInitiatedSignIn();
				}
			});
		}
		catch (Exception e)
		{
			Log.w(TAG, "Log in failed: " + e.getMessage() + ".");
			Gdx.app.log(TAG, "Log in failed: " + e.getMessage() + ".");
		}
	}


	@Override
	public void signOut() {
		// TODO Auto-generated method stub
		try
		{
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					gameHelper.signOut();
				}
			});
		}
		catch (Exception e)
		{
			Gdx.app.log("MainActivity", "Log out failed: " + e.getMessage() + ".");
		}
	}


	@Override
	public void rateGame() {
		// TODO Auto-generated method stub
		String str ="https://play.google.com/store/apps/details?id=com.kritikalerror.flappycanary";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
	}


	@Override
	public void submitScore(long score) {
		// TODO Auto-generated method stub
		if (isSignedIn() == true)
		{
			Games.Leaderboards.submitScore(gameHelper.getApiClient(), getString(R.string.leaderboard_id), score);
		}
		else
		{
			signIn();
		}
	}


	@Override
	public void showScores() {
		// TODO Auto-generated method stub
		if (isSignedIn() == true) 
		{
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), getString(R.string.leaderboard_id)), REQUEST_CODE_UNUSED);
		}
		else
		{
			signIn();
		}
	}

	@Override
	public boolean isSignedIn() {
		// TODO Auto-generated method stub
		return gameHelper.isSignedIn();
	}


	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		Log.e(TAG, "Sign in failed!");
	}


	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		Log.d(TAG, "Sign in successful!");
	}

	@Override
	public void unlockAchievements(String achievementId) {
		// TODO Auto-generated method stub
		if (isSignedIn() == true) 
		{
			Games.Achievements.unlock(gameHelper.getApiClient(), achievementId);
		}
		else
		{
			signIn();
		}
	}

	@Override
	public void getAchievements() {
		// TODO Auto-generated method stub
		if (isSignedIn() == true) 
		{
			startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()), REQUEST_CODE_UNUSED);
		}
		else
		{
			signIn();
		}
	}
	
	@Override
	public void generateToast(String type) {
		final String insert = type;
		try
		{
			runOnUiThread(new Runnable()
			{
				//@Override
				public void run()
				{
					Toast.makeText(getApplicationContext(), "Signing into Google Play...",
							   Toast.LENGTH_SHORT).show();
					Toast.makeText(getApplicationContext(), "Tap the icon again to see the " + insert + ".",
							   Toast.LENGTH_SHORT).show();
				}
			});
		}
		catch (Exception e)
		{
			Log.w(TAG, "Making Toast failed: " + e.getMessage() + ".");
		}
	}

}