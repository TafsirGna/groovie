package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Participer;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

public class ListeDemandesActivity extends Activity {

	private ListView liste_demandes_listView;
	private UtilisateurDataSource UtilisateurDS;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private Dialog dialog_box;
	private String accepter_demande_file=Groovieparams.DBurl+"accepter_demande.php";
	private TextView textView_zero_demande;
	private Vibrator myVibrator;
	private ProgressBar layout_progressbar_actualiser;
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private int position_item_selected;
	private int DUREE_RAFRAICHISSEMENT=30000;
	private List<Utilisateur> liste_demandes;
	private ParticiperDataSource ParticiperDS;
	private GroupeDataSource GroupeDS;
	private List<Participer> liste_resultat_entrees_participer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_liste_demandes);

		//j'initialise mes variables
		layout_progressbar_actualiser=(ProgressBar) findViewById(id.liste_demandes_layout_progressbar_actualiser);
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();
		UtilisateurDS= new UtilisateurDataSource(ListeDemandesActivity.this);
		UtilisateurDS.open();
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		phone_user= get_user();
		liste_demandes_listView=(ListView) findViewById(R.id.liste_demandes_layout_listView);

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//j'initialise le textView qui sera affiché au cas ou il n'y aurait aucune demande dans la base de données
		textView_zero_demande= (TextView) findViewById(R.id.liste_demandes_layout_aucune_demande);
		//je masque la listView
		String stringAucuneDemande="<font color=#0000FF><i> Aucune demande en attente!</i></font>";
		textView_zero_demande.setText(Html.fromHtml(stringAucuneDemande));	

		afficher_demandes();

		//lorsque l'utilisateur clique sur le textView indiquant qu'aucune demande ne lui est notifiée, je le balance sur l'activité de groupes et utilisateurs
		textView_zero_demande.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i=new Intent(ListeDemandesActivity.this, GroupeEtUtilisateursActivity.class);
				startActivity(i);
			}
		});
		//remplirListeDemandes();

		//je construis le dialog box qui apparait lorsque j'appuie long sur item de ma listeView
		dialog_box= new Dialog(ListeDemandesActivity.this);
		dialog_box.setTitle("Actions");
		ListView actions_dialog_box_listview=new ListView(this);
		ArrayAdapter<String> adapter1= new ArrayAdapter<String>(ListeDemandesActivity.this, android.R.layout.simple_list_item_1);
		adapter1.add("Accepter");
		adapter1.add("Voir Profil");
		adapter1.add("Signaler comme vu");
		actions_dialog_box_listview.setAdapter(adapter1);
		dialog_box.setContentView(actions_dialog_box_listview);

		//lorsque l'utilisateur effectue un appui long sur la listeview
		liste_demandes_listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				// je fais vibrer le téléphone 
				myVibrator.vibrate(100);

				position_item_selected=position;
				dialog_box.show();

				return true;
			}
		});

		//lorsqu'on clique sur un item de la liste des actions
		actions_dialog_box_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if (position==0)
				{
					layout_progressbar_actualiser.setVisibility(View.VISIBLE);
					accepter_demande(liste_demandes.get(position_item_selected).getIdUtilisateur());
				}
				if (position==1)
				{
					Intent profil_intent=new Intent(ListeDemandesActivity.this,ProfilUtilisateur.class);
					profil_intent.putExtra("idUtilisateur", liste_demandes.get(position_item_selected).getIdUtilisateur());
					startActivity(profil_intent);
				}
				if (position==2)
				{
					Participer entree= liste_resultat_entrees_participer.get(position_item_selected);
					signaler_comme_vu(entree);
					afficher_demandes();
					Toast.makeText(ListeDemandesActivity.this, "Demande signalée comme vu", Toast.LENGTH_LONG).show();
				}
				dialog_box.dismiss();
			}
		});
		Timer minuteur= new Timer();
		TimerTask tache= new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//afficher_mes_lieux();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						afficher_demandes();
					}
				});
			}
		};
		minuteur.schedule(tache, DUREE_RAFRAICHISSEMENT, DUREE_RAFRAICHISSEMENT);
	}

	private void signaler_comme_vu(Participer entree)
	{
		entree.set_codeVu(1);
		ParticiperDS.updateParticiper(entree);
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

	private void afficher_demandes()
	{
		liste= new ArrayList<HashMap<String,Object>>();
		liste_demandes= new ArrayList<Utilisateur>();
		List<Groupe> liste_groupes= new ArrayList<Groupe>();
		liste_resultat_entrees_participer= new ArrayList<Participer>();

		Cursor c=ParticiperDS.get_liste_demandes_of(phone_user.getIdGroupe());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Utilisateur user_invited= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "NULL", "NULL", "NULL", 1);
			liste_demandes.add(user_invited);
			liste_groupes.add(new Groupe(c.getInt(2), c.getInt(0)));
			liste_resultat_entrees_participer.add(new Participer(phone_user.getIdGroupe(), c.getInt(0), c.getString(10),c.getString(11) , c.getString(7), c.getInt(8),c.getInt(9)));
			c.moveToNext();
		}
		if (liste_demandes.size()==0)
		{
			liste_demandes_listView.setVisibility(View.GONE);
			textView_zero_demande.setVisibility(View.VISIBLE);
		}
		else
		{
			liste_demandes_listView.setVisibility(View.VISIBLE);
			textView_zero_demande.setVisibility(View.GONE);
			//j'affiche les résultats de ma requete
			HashMap<String, Object> element;
			for (int i=0;i<liste_demandes.size();i++)
			{
				element=new HashMap<String, Object>();
				element.put("Pseudo", liste_demandes.get(i).getPseudo());
				String value="<font color=#0000FF>Envoyé le "+FonctionsLibrary.formatDateTime(liste_resultat_entrees_participer.get(i).get_dateDemande())+"</font>";
				element.put("Email", Html.fromHtml(value));
				element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_demandes.get(i).getIdUtilisateur())+" personne(s) qui suivent");
				element.put("note", liste_resultat_entrees_participer.get(i).get_note());
				liste.add(element);	
			}
			UtilisateurAdapter adapter= new UtilisateurAdapter(this, liste,R.drawable.icone_demande);
			liste_demandes_listView.setAdapter(adapter);
		}
	}
	private void accepter_demande(final int id_user_selected)
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					UtilisateurDS= new UtilisateurDataSource(ListeDemandesActivity.this);
					UtilisateurDS.open();

					ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(id_user_selected)));
					nameValuePair.add(new BasicNameValuePair("idGroupe", String.valueOf(phone_user.getIdGroupe())));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(accepter_demande_file);
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
					// e.printStackTrace();
				}

				// Parse  les données JSON
				try{
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					final String date=jObject.getString("date");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("true")){
								enregistrer_acceptation_localement(id_user_selected,date);
								layout_progressbar_actualiser.setVisibility(View.GONE);
								Toast.makeText(ListeDemandesActivity.this, "Demande acceptée", Toast.LENGTH_SHORT).show();
							}else
								Toast.makeText(ListeDemandesActivity.this, "Une erreur s'est produite!", Toast.LENGTH_SHORT).show();
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	private void enregistrer_acceptation_localement(int id_utilisateur,String date)
	{
		List<Participer> liste_entrees_participer=ParticiperDS.getAllEntrees();
		Participer entree= ParticiperDS.get_entree_participer(phone_user.getIdGroupe(), id_utilisateur,liste_entrees_participer);
		if (entree!=null)
		{
			entree.set_dateEntre(date);
			ParticiperDS.updateParticiper(entree);
			afficher_demandes();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.liste_demandes, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.liste_demandes_menu_action_vider_liste:
			for (int i=0;i<liste_resultat_entrees_participer.size();i++)
				signaler_comme_vu(liste_resultat_entrees_participer.get(i));

			afficher_demandes();
			Toast.makeText(ListeDemandesActivity.this, "Liste vidée", Toast.LENGTH_LONG).show();
			return true;
		case R.id.liste_demandes_menu_action_settings:
			Intent parametres_intent=new Intent(this, ParametresActivity.class);
			startActivity(parametres_intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
