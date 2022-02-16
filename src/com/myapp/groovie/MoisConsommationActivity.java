package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Consommation;
import com.myapp.groovie.classes.database.ConsommationDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.LieuAdapter;

public class MoisConsommationActivity extends Activity {

	private ListView listView_details_consommation;
	private String[] liste_mois= {"JANVIER","FEVRIER","MARS","AVRIL","MAI","JUIN","JUILLET","AOUT","SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE"};
	private ConsommationDataSource ConsommationDS;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private LieuDataSource LieuDS;
	private List<Consommation> liste_donnees;
	private String reinitialiser_donnees_file=Groovieparams.DBurl+"reinitialiser_donnees_consommation.php";
	private int indice_mois;
	private Utilisateur current_user;
	private UtilisateurDataSource UtilisateurDS;
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private String donnees_conso_file=Groovieparams.DBurl+"liste_donnees_conso.php";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mois_consommation);

		//j'initialise les variables nécessaires de l'activité
		listView_details_consommation=(ListView) findViewById(id.mois_consommation_layout_listView_details_consommation);
		TextView textView_titre= (TextView) findViewById(id.mois_consommation_layout_textView_titre);

		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		ConsommationDS=new ConsommationDataSource(this);
		ConsommationDS.open();
		LieuDS=new LieuDataSource(this);
		LieuDS.open();

		//je récupère l'intent à l'origine de l'appel
		Intent intent= getIntent();
		indice_mois=intent.getIntExtra("indice_mois", 15);
		textView_titre.setText(textView_titre.getText().toString()+liste_mois[indice_mois-1]);
		current_user=UtilisateurDS.get_utilisateur(intent.getIntExtra("idUtilisateur", 0));

		phone_user=get_user();

		//je remplis la listeView des lieux de consommation du mois
		if (current_user.matches(phone_user))
		{
			liste_donnees=ConsommationDS.get_details_conso_du_mois(indice_mois);
			afficher_conso_mois(liste_donnees);
		}
		else
			get_currentUser_conso();
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

	private void afficher_conso_mois(List<Consommation> liste_resultats)
	{
		int listSize= liste_resultats.size();
		liste= new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> element;
		for (int i=0;i<listSize;i++)
		{
			if (liste_resultats.get(i).getCodeReinitialisation()==0)
			{
				Lieu lieu_conso=LieuDS.get_Lieu(liste_resultats.get(i).get_idLieu());
				element=new HashMap<String, Object>();
				element.put("Titre", lieu_conso.get_titre());
				String value="<i>Quantité consommée de <font color=#0000FF>"+liste_resultats.get(i).get_quantite()+" Litres</font> pour un coût total de <font color=#0000FF> "+liste_resultats.get(i).get_cout()+Groovieparams.monnaie+ " "+"</font> le <font color=#0000FF>"+FonctionsLibrary.formatDateTime(liste_resultats.get(i).get_dateConsommation())+"</font></i>";
				element.put("SousTitre", Html.fromHtml(value));
				element.put("status", 0);
				liste.add(element);	
			}
		}
		LieuAdapter adapter= new LieuAdapter(this, liste);
		listView_details_consommation.setAdapter(adapter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mois_consommation, menu);
		if (!current_user.matches(phone_user))
		{
			menu.getItem(0).setVisible(false);
		}
		return true;
	}

	private DialogInterface.OnClickListener dialog_confirmation_yes_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			reinitialisation_locale(liste_donnees);
		}
	};

	private void get_currentUser_conso()
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(current_user.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("indice_mois", String.valueOf(indice_mois)));
					
					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(donnees_conso_file);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requte en string
				try{
					BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						//Log.e("log",line);
						sb.append(line + "\n");
					}
					is.close(); 
					result=sb.toString();
				}catch(Exception e){
					Log.e("log_tag", "Error converting result " + e.toString());
				}

				// Parse  les données JSON
				Log.e("log_error", result);
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Consommation> l=new ArrayList<Consommation>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						JSONObject jObje=jArray.getJSONObject(i);
						l.add(new Consommation(jObje.getInt("idConso"), jObje.getInt("idLieu"), jObje.getInt("idUtilisateur"), jObje.getString("dateConso"), jObje.getInt("quantiteConso"), jObje.getInt("cout"), 0));
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							afficher_conso_mois(l);
						}
					});
				}catch(JSONException e){
					Log.e("log_tag1", "Error parsing data " + e.toString());
				}


			}
		}).start();
	}
	private void reinitialisation_locale(List<Consommation> liste)
	{
		List<Consommation> liste_entrees_conso= ConsommationDS.getAllEntrees();
		for (int i=0;i<liste.size();i++)
		{
			Consommation entree=ConsommationDS.get_entree_consommation(liste.get(i).get_idConsommation(), liste_entrees_conso);
			entree.setCodeReinitialisation(1);
			ConsommationDS.updateConsommation(entree);
		}
		//afficher_conso_mois(indice_mois);
		Toast.makeText(MoisConsommationActivity.this, "Réinitialisation effectuée!", Toast.LENGTH_LONG).show();
		MoisConsommationActivity.this.finish();
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.mois_consommation_menu_action_reinitialiser:
			AlertDialog.Builder confirmation= new AlertDialog.Builder(MoisConsommationActivity.this);
			confirmation.setCancelable(true);
			confirmation.setMessage("Voulez-vous vraiment réinitialiser les données de consommation de "+liste_mois[indice_mois-1]+"?");
			confirmation.setPositiveButton("OUI", dialog_confirmation_yes_listener);
			confirmation.setNegativeButton("NON", null);
			confirmation.show();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}
}
