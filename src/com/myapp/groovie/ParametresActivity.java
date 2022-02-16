package com.myapp.groovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.myapp.groovie.classes.objects.ParametreItemAdapter;

public class ParametresActivity extends Activity {

	private ListView paramView;
	private List<HashMap<String, Object>> parametreListe= new ArrayList<HashMap<String,Object>>();;
	private String[] listeStringParametre= new String[] {"Aide","Profil", "Mon compte","Préférences"};
	private int[] listeIconeParametre= new int[]{R.drawable.ic_action_help,R.drawable.ic_action_profil,R.drawable.ic_action_moncompte,R.drawable.ic_action_notifications};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parametres);

		//j'initialise les variables
		paramView=(ListView) findViewById(R.id.parametresView);

		//j'affiche les items de la listview
		HashMap<String, Object> element;
		for (int i=0;i<listeStringParametre.length;i++)
		{
			element=new HashMap<String, Object>();
			element.put("libelleParametre", listeStringParametre[i]);
			parametreListe.add(element);
		}
		ParametreItemAdapter paramsAdapter= new ParametreItemAdapter(ParametresActivity.this, parametreListe,listeIconeParametre);
		paramView.setAdapter(paramsAdapter);

		//lorsqu'on clique sur un item, on a:
		paramView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				switch(position)
				{
				case 0:
					Intent i= new Intent(ParametresActivity.this, AideActivity.class);
					startActivity(i);
					break;
				case 1:
					Intent it= new Intent(ParametresActivity.this, ProfilActivity.class);
					startActivity(it);
					break;
				case 2:
					Intent in= new Intent(ParametresActivity.this, MonCompteActivity.class);
					startActivity(in);
					break;
				case 3:
					Intent preference_notification= new Intent(ParametresActivity.this,PreferencesNotificationActivity.class);
					startActivity(preference_notification);
					break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parametres, menu);
		return true;
	}

}
