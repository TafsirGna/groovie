package com.myapp.groovie;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.objects.Groovieparams;

public class AideActivity extends Activity {

	private String[] listeItems= new String[]{"A propos", "Manuel d'utilisation","Contactez-nous","Aidez-nous"};
	private ListView listeItemsView=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aide);

		listeItemsView=(ListView) findViewById(id.aideItemsView);
		//j'affiche les items de l'activity
		List<String> arraylist = new ArrayList<String>();
		for (int i=0;i<listeItems.length;i++)
		{
			arraylist.add(listeItems[i]);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arraylist);
		listeItemsView.setAdapter(adapter);

		//lorsque l'utilisateur clique sur un des items, on a:
		listeItemsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				switch (position)
				{
				case 0:
					Intent i= new Intent(AideActivity.this,AboutActivity.class);
					startActivity(i);
					break;

				case 1:
					Uri uri_web=Uri.parse("http:"+Groovieparams.groovie_site_url);
					Intent intent_web=new Intent(Intent.ACTION_VIEW,uri_web);
					startActivity(intent_web);
					break;

				case 2:
					Intent intentEmail= new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto", Groovieparams.EmailGroovie, null));
					intentEmail.putExtra(Intent.EXTRA_SUBJECT, "");
					intentEmail.putExtra(Intent.EXTRA_TEXT, "");
					startActivity(Intent.createChooser(intentEmail, "Send Email..."));
					break;
					
				case 3:
					Intent intent_payement;
					try {
						intent_payement= getPackageManager().getLaunchIntentForPackage("com.myapp.noru");
						if (intent_payement==null)
							throw new PackageManager.NameNotFoundException();
						intent_payement.addCategory(Intent.CATEGORY_LAUNCHER);
						startActivity(intent_payement);
					} catch (PackageManager.NameNotFoundException e) {
						// TODO: handle exception
						Toast.makeText(AideActivity.this, "Appli Noru requise pour effectuer l'action", Toast.LENGTH_LONG).show();
					}
					break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aide, menu);
		return true;
	}

}
