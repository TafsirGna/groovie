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
public class GroupeEtUtilisateursActivity extends TabActivity  implements OnTabChangeListener{

	private static final int ANIMATION_TIME=240;
	private TabHost mtabHost;
	private View previousView;
	private View currentView;
	private int currentTab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_groupe_et_utilisateurs);
		
		mtabHost=getTabHost();
		
		mtabHost.addTab(mtabHost.newTabSpec("first").setIndicator("autres groupes").setContent(new Intent(this, MesAutresGroupesActivity.class)));
		mtabHost.addTab(mtabHost.newTabSpec("second").setIndicator("Mon groupe").setContent(new Intent(this, MongroupeActivity.class)));
		//mtabHost.addTab(mtabHost.newTabSpec("third").setIndicator("Utilisateurs").setContent(new Intent(this, ListeUtilisateursActivity.class)));
		mtabHost.setCurrentTab(1);
		previousView=mtabHost.getCurrentView();
		mtabHost.setOnTabChangedListener(this);
		
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.groupe_et_utilisateurs, menu);
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.GroupeEtUtilisateur_menu_action_tous_utilisateurs:
			startActivity(new Intent(GroupeEtUtilisateursActivity.this, ListeUtilisateursActivity.class));
			return true;

		case R.id.GroupeEtUtilisateur_menu_action_settings:
			startActivity(new Intent(GroupeEtUtilisateursActivity.this, ParametresActivity.class));
			return true;
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
