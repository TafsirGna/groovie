package com.myapp.groovie;

import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Intent;
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
public class LesLieuxActivity extends TabActivity implements OnTabChangeListener{

	private static final int ANIMATION_TIME=240;
	private TabHost mtabHost;
	private View previousView;
	private View currentView;
	private int currentTab;

	//private String[] drawerItemsList;
	//private ListView myDrawer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.activity_les_lieux);
		//je demarre le service groovie service afin de mettre à jour la base de données et de vérification ses chagements d'états
		Intent intent_service=new Intent(this, GroovieService.class);
		startService(intent_service);

		//mtabHost=(TabHost) findViewById(id.tabhost);
		mtabHost=getTabHost();
		//mtabHost.setup();
		mtabHost.addTab(mtabHost.newTabSpec("first").setIndicator("Mes Lieux").setContent(new Intent(this, MesLieuxActivity.class)));
		mtabHost.addTab(mtabHost.newTabSpec("second").setIndicator("Tous Les Lieux").setContent(new Intent(this, AccueilActivity.class)));
		mtabHost.setCurrentTab(0);
		previousView=mtabHost.getCurrentView();
		mtabHost.setOnTabChangedListener(this);

		/*
		drawerItemsList = getResources().getStringArray(R.array.items);
        myDrawer = (ListView) findViewById(R.id.my_drawer);
        myDrawer.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_item, drawerItemsList));
		 */
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.les_lieux, menu);
		/*
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		 */
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			//onBackPressed();

			//case R.id.action_search:

		case R.id.les_lieux_menu_action_ajouter_lieu:
			Intent o=new Intent(LesLieuxActivity.this, AjouterLieuActivity.class);
			startActivity(o);
			return true;

		case R.id.les_lieux_menu_action_notifications:
			Intent in=new Intent(LesLieuxActivity.this, NotificationsActivity.class);
			startActivity(in);
			return true;

		case R.id.les_lieux_menu_action_mongroupe:
			Intent inte=new Intent(LesLieuxActivity.this, GroupeEtUtilisateursActivity.class);
			startActivity(inte);
			return true;

		case R.id.les_lieux_menu_action_settings:
			Intent monI=new Intent(LesLieuxActivity.this, ParametresActivity.class);
			startActivity(monI);
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