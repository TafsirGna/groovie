package com.myapp.groovie;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

public class MonCompteActivity extends Activity {

	private String[] listeItems= new String[]{"Confidentialité","Changer Numéro","Inviter un(e) ami(e)", "Supprimer mon compte","Mes données de consommation"};
	private ListView listeItemsView=null;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mon_compte);

		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		listeItemsView=(ListView) findViewById(id.MonCompteItemsView);
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
					Intent intent_confidentialite= new Intent(MonCompteActivity.this,ConfidentialiteActivity.class);
					startActivity(intent_confidentialite);
					break;
				case 1:
					Intent intent_changer_contact= new Intent(MonCompteActivity.this,ChangerContactActivity.class);
					startActivity(intent_changer_contact);
					break;
				case 2:
					Intent intent_sms=new Intent(Intent.ACTION_VIEW);
					intent_sms.setData(Uri.parse("sms:"));
					intent_sms.putExtra("sms_body", Groovieparams.invitation_sms_body);
					startActivity(intent_sms);
					break;
				case 3:
					Intent i= new Intent(MonCompteActivity.this,SupprimerCompteActivity.class);
					startActivity(i);
					break;
				case 4:
					Intent intent_donnees= new Intent(MonCompteActivity.this,DonneesConsommationActivity.class);
					intent_donnees.putExtra("idUtilisateur",get_user().getIdUtilisateur());
					startActivity(intent_donnees);
					break;
				}
			}
		});
	}
	private Utilisateur get_user()
	{
		List<Utilisateur> liste_users=UtilisateurDS.getAllUtilisateurs();
		int liste_user_size=liste_users.size();
		String id_device=phoneManager.getDeviceId();
		for (int i=0;i<liste_user_size;i++)
		{
			if (id_device.equals(liste_users.get(i).getIdDevice()))
			{
				return liste_users.get(i);
			}
		}
		return null;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mon_compte, menu);
		return true;
	}

}
