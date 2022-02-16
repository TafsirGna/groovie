package com.myapp.groovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Consommation;
import com.myapp.groovie.classes.database.ConsommationDataSource;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.simple_item_adapter;

public class DonneesConsommationActivity extends Activity {

	private ListView listView_donnees;
	private String [][] liste_items;
	private ConsommationDataSource ConsommationDS;
	//private Utilisateur phone_user;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	private LieuDataSource LieuDS;
	private Utilisateur current_user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donnees_consommation);

		//j'initialise les variables sus-citées
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		LieuDS= new LieuDataSource(this);
		LieuDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		ConsommationDS= new ConsommationDataSource(this);
		ConsommationDS.open();
		//phone_user= get_user();

		//Récupération de l'identifiant de l'utilisateur 
		Intent intent_appelant= getIntent();
		current_user=UtilisateurDS.get_utilisateur(intent_appelant.getIntExtra("idUtilisateur", 0));

		listView_donnees= (ListView) findViewById(id.donnees_consommation_listView);

		afficher_donnees();

		//lorsque l'utilisateur clique sur la listView, on a :
		listView_donnees.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if (position!=0 && position!=13)
				{
					if (ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), position,0)==0)
					{
						Toast.makeText(DonneesConsommationActivity.this, "Aucune donnée disponible pour ce mois pour le moment!", Toast.LENGTH_LONG).show();
					}
					else
					{
						Intent mois_consommation=new Intent(DonneesConsommationActivity.this, MoisConsommationActivity.class);
						mois_consommation.putExtra("indice_mois", position);
						mois_consommation.putExtra("idUtilisateur", current_user.getIdUtilisateur());
						startActivity(mois_consommation);
					}
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

	private void afficher_donnees()
	{
		Consommation derniereConso= ConsommationDS.get_derniere_consommation(current_user.getIdUtilisateur());
		//je remplis la liste des caractéristiques de l'utilisateur 
		liste_items= new String[][]{{"Dernière consommation",((derniereConso==null) ? "effectuée le ? au ? avec ? Litres achetés" : "effectué le "+FonctionsLibrary.formatDateTime(derniereConso.get_dateConsommation())+" à "+LieuDS.get_Lieu(derniereConso.get_idLieu()).get_titre()+" avec "+derniereConso.get_quantite()+ " Litres achetés pour un coût de "+ derniereConso.get_cout() +" "+ Groovieparams.monnaie)},
				{"Janvier",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 1,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 1,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 1,1) +" "+Groovieparams.monnaie)},
				{"Février",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 2,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 2,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 2,1) +" "+Groovieparams.monnaie)},
				{"Mars",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 3,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 3,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 3,1) +" "+Groovieparams.monnaie)},
				{"Avril",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 4,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 4,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 4,1) +" "+Groovieparams.monnaie)},
				{"Mai",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 5,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 5,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 5,1) +" "+Groovieparams.monnaie)},
				{"Juin",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 6,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 6,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 6,1) +" "+Groovieparams.monnaie)},
				{"Juillet",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 7,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 7,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 7,1) +" "+Groovieparams.monnaie)},
				{"Aout",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 8,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 8,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 8,1) +" "+Groovieparams.monnaie)},
				{"Septembre",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 9,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 9,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 9,1) +" "+Groovieparams.monnaie)},
				{"Octobre",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 10,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 10,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 10,1) +" "+Groovieparams.monnaie)},
				{"Novembre",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 11,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 11,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 11,1) +" "+Groovieparams.monnaie)},
				{"Décembre",((ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 12,0)==0) ? "Aucune donnée disponible pour le moment" : "Vous avez consommez "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 12,0) +" Litres pour un coût total de "+ ConsommationDS.get_consommation_du_mois(current_user.getIdUtilisateur(), 12,1) +" "+Groovieparams.monnaie)},
				{"Dernière réinitialisation","Jamais"}};
		List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> element;
		for (int i=0;i<liste_items.length;i++)
		{
			element=new HashMap<String, Object>();
			element.put("text1", liste_items[i][0]);
			element.put("text2", liste_items[i][1]);
			liste.add(element);
		}
		simple_item_adapter adapter= new simple_item_adapter(this, liste);
		listView_donnees.setAdapter(adapter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.donnees_consommation, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		}
		return super.onOptionsItemSelected(item);
	}
}
