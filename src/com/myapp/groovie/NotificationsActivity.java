package com.myapp.groovie;

import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class NotificationsActivity extends TabActivity implements OnTabChangeListener{

	private static final int ANIMATION_TIME=240;
	private TabHost mtabHost;
	private View previousView;
	private View currentView;
	private int currentTab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_notifications);

		mtabHost=getTabHost();
		mtabHost.addTab(mtabHost.newTabSpec("first").setIndicator("Invitations").setContent(new Intent(this, ListeInvitationsActivtity.class)));
		//mtabHost.addTab(mtabHost.newTabSpec("second").setIndicator("Infos").setContent(new Intent(this, MesLieuxActivity.class)));
		mtabHost.addTab(mtabHost.newTabSpec("second").setIndicator("Demandes").setContent(new Intent(this, ListeDemandesActivity.class)));
		mtabHost.setCurrentTab(0);
		previousView=mtabHost.getCurrentView();
		mtabHost.setOnTabChangedListener(this);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notifications, menu);
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			//onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onTabChanged(String arg0) {
		// TODO Auto-generated method stub
		currentView=mtabHost.getCurrentView();
		if (mtabHost.getCurrentTab()>currentTab)
		{
			previousView.setAnimation(outToLeftAnimation());
			currentView.setAnimation(inFromRightAnimation());
		}
		else
		{
			previousView.setAnimation(outToRightAnimation());
			currentView.setAnimation(inFromLeftAnimation());
		}
		previousView=currentView;
		currentTab=mtabHost.getCurrentTab();
	}

	private Animation setProperties(Animation animation)
	{
		animation.setDuration(ANIMATION_TIME);
		animation.setInterpolator(new AccelerateInterpolator());
		return animation;
		
	}
	private Animation inFromRightAnimation()
	{
		Animation inFromRight=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f,Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f);
		return setProperties(inFromRight);
	}
	
	private Animation outToRightAnimation()
	{
		Animation outToRight=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,Animation.RELATIVE_TO_PARENT,1.0f, Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f);
		return setProperties(outToRight);
	}
	
	private Animation inFromLeftAnimation()
	{
		Animation inFromLeft=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f);
		return setProperties(inFromLeft);
	}
	
	private Animation outToLeftAnimation()
	{
		Animation outToLeft=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,Animation.RELATIVE_TO_PARENT,-1.0f, Animation.RELATIVE_TO_PARENT,0.0f, Animation.RELATIVE_TO_PARENT,0.0f);
		return setProperties(outToLeft);
	}
}
