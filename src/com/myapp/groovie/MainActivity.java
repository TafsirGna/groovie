package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.myapp.groovie.classes.database.Departement;
import com.myapp.groovie.classes.database.DepartementDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UpdateDbObject;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private Button bouton_inscription;
	private ProgressDialog progressDialog;
	private List<Object> liste_resultats=null;
	private String [] liste_actions={"departement"};
	private String liste_departement_file=Groovieparams.DBurl+"lister_les_departements.php";
	private UtilisateurDataSource UtilisateurDS;
	private DepartementDataSource DepartementDS;
	private int isActif;
	private TelephonyManager phoneManager;
	private Utilisateur phone_user;
	private UpdateDbObject update_db_object;

	private Handler mhandler = new Handler(){

		//Gére la communication avec le thread de récupération des lieux
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg){
			super.handleMessage(msg);

			if (msg.obj!=null)
			{	
				if (msg.arg1==0)
				{
					liste_resultats=(List<Object>)msg.obj;
					charger_donnees_locales(liste_actions[msg.arg1],liste_resultats);
				}
			}
		}

	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		//j'initialise le progressbar
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Please wait...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();

		// je récupère l'identifiant de l'appareil et j'initialise les variables
		UtilisateurDS= new UtilisateurDataSource(MainActivity.this);
		UtilisateurDS.open();

		DepartementDS= new DepartementDataSource(this);
		DepartementDS.open();

		phoneManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		bouton_inscription=(Button) findViewById(R.id.main_activity_layout_bouton_inscription);

		phone_user=get_user();
		
		if (phone_user!=null)
		{
			isActif=phone_user.getactif();
			if (isActif==1)
			{
				progressDialog.dismiss();
				bouton_inscription.setText("Démarrer");
				
				update_db_object= new UpdateDbObject(this, phone_user);
				update_db_object.update_db();
				
				Intent i=new Intent(MainActivity.this,LesLieuxActivity.class);
				startActivity(i);
				this.finish();
			}
			else if (isActif==0)
			{
				Intent i=new Intent(MainActivity.this,ConfirmationInscription.class);
				i.putExtra("email", phone_user.getEmail());
				startActivity(i);
				this.finish();
			}
			/*
			new java.util.Timer().schedule(new java.util.TimerTask(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (liste_resultats==null)
							{
								Toast.makeText(MainActivity.this, "Connexion impossible!", Toast.LENGTH_SHORT).show();
								Intent i=new Intent(MainActivity.this,LesLieuxActivity.class);
								startActivity(i);
							}
						}
					});
				}
			}, 7000);
			 */
		}
		else
		{
			progressDialog.dismiss();
			get_departements();
		}
		bouton_inscription.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (bouton_inscription.getText().toString().equals("Démarrer"))
				{
					Intent i=new Intent(MainActivity.this,LesLieuxActivity.class);
					startActivity(i);
				}
				else
				{
					Intent i=new Intent(MainActivity.this,InscriptionActivity.class);
					startActivity(i);
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

	private void charger_donnees_locales(String action,List<Object> liste)
	{
		if (action=="departement")
		{
			int listeSize=liste.size();
			List<Departement> liste_departements= DepartementDS.getAllDepartements();
			for (int i=0;i<listeSize;i++)
			{
				JSONObject jObject=(JSONObject) liste.get(i);
				Departement departement=null;
				try {
					departement = new Departement(jObject.getInt("idDepartement"),jObject.getString("libelleDepartement"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!DepartementDS.hasAlreadySaved(departement, liste_departements))
				{
					DepartementDS.createDepartement(departement);
				}
			}
		}
	}
	public void get_departements()
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("null", "null"));
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(liste_departement_file);
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					HttpResponse response=httpClient.execute(httpPost);
					HttpEntity entity= response.getEntity();
					is=entity.getContent();
				}
				catch(Exception e){
					Log.e("log_tag", "Error in http connection " + e.toString());
					//e.printStackTrace();
				}

				// Conversion de la requete en string
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
				try{
					JSONArray jArray = new JSONArray(result);
					final List<Object> l=new ArrayList<Object>();
					int i;
					for (i=0;i<jArray.length();i++)
					{
						l.add(jArray.getJSONObject(i));

					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Message msg=mhandler.obtainMessage();
							msg.obj=l;
							msg.arg1=0;
							mhandler.sendMessage(msg);
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.main_activity_menu_action_recuperer_compte:
			Intent monI=new Intent(MainActivity.this, RecupererCompteActivity.class);
			startActivity(monI);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
}