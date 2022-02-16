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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.simple_item_adapter;

public class ConfidentialiteActivity extends Activity {

	private ListView listView_visibilite_infosPerso;
	private Dialog dialog_box;
	private ListView dialog_listView;
	private ArrayAdapter<String> dialog_adapter;
	private String [][] liste_items;
	private int position_item_selected;
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;
	private UtilisateurDataSource UtilisateurDS;
	private TelephonyManager phoneManager;
	private Utilisateur phone_user;
	private ParamsUtilisateur Params_User;
	private String[] string_valuesItems={"Personne","Mes contats","Tout le monde"};
	private String modifier_paramsUtilisateur_file= Groovieparams.DBurl+"modifier_paramsutilisateur.php";
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confidentialite);

		//j'initialise les variables sus-mentionnées
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		phone_user= get_user();
		Params_User= ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());

		listView_visibilite_infosPerso=(ListView) findViewById(id.confidentialite_layout_listView_visibilite_infosPerso);
		
		afficher_items_visibilite(string_valuesItems[Params_User.get_visibilitePhoto()],string_valuesItems[Params_User.get_visibiliteCoordonnees()],string_valuesItems[Params_User.get_visibiliteStatistiques()]);

		//le construis le dialog box de groupe de visibilité
		dialog_box= new Dialog(this);
		dialog_box.setTitle("Visible par");
		dialog_listView=new ListView(this);
		dialog_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
		dialog_adapter.add(string_valuesItems[2]);
		dialog_adapter.add(string_valuesItems[1]);
		dialog_adapter.add(string_valuesItems[0]);
		dialog_listView.setAdapter(dialog_adapter);
		dialog_listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		dialog_box.setContentView(dialog_listView);

		listView_visibilite_infosPerso.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				position_item_selected=arg2;
				dialog_box.show();
			}
		});
		dialog_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				networkInfo= connectivityManager.getActiveNetworkInfo();
				switch (position_item_selected) {
				case 0:
					if (position==0)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibilitePhoto(2);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(string_valuesItems[2], liste_items[1][1], liste_items[2][1]);
						}
					}
					if (position==1)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibilitePhoto(1);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(string_valuesItems[1], liste_items[1][1], liste_items[2][1]);
						}
					}
					if (position==2)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibilitePhoto(0);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(string_valuesItems[0], liste_items[1][1], liste_items[2][1]);
						}
					}
					break;

				case 1:
					if (position==0)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteCoordonnees(2);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1], string_valuesItems[2], liste_items[2][1]);
						}
					}
					if (position==1)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteCoordonnees(1);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1], string_valuesItems[1], liste_items[2][1]);
						}
					}
					if (position==2)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteCoordonnees(0);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1], string_valuesItems[0], liste_items[2][1]);
						}
					}
					break;

				case 2:
					if (position==0)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteStatistiques(2);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1],liste_items[1][1], string_valuesItems[2]);
						}
					}
					if (position==1)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteStatistiques(1);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1], liste_items[1][1], string_valuesItems[1]);
						}
					}
					if (position==2)
					{
						if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
						{
							Toast.makeText(ConfidentialiteActivity.this, "Aucune réseau disponible!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Params_User=ParamsUtilisateurDS.get_paramsUtilisateur(phone_user.get_idParam(), ParamsUtilisateurDS.getAllEntrees());
							Params_User.set_visibiliteStatistiques(0);
							modifier_params(Params_User.get_periode(), Params_User.get_visibilitePhoto(), Params_User.get_visibiliteCoordonnees(), Params_User.get_visibiliteStatistiques());
							ParamsUtilisateurDS.updateParamsUtilisateur(Params_User);
							afficher_items_visibilite(liste_items[0][1],liste_items[1][1],string_valuesItems[0]);
						}
					}
					break;

				default:
					break;
				}
				dialog_box.dismiss();
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

	private void afficher_items_visibilite(String photo, String phone, String statistiques)
	{
		//je remplis la liste des caractéristiques de l'utilisateur 
		liste_items= new String[][]{{"Photo de profil",photo},
				{"Mes coordonnées",phone},
				{"Mes données de consommation",statistiques}};
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
		listView_visibilite_infosPerso.setAdapter(adapter);
	}

	private void modifier_params( int periode, int visibilitePhoto, int visibiliteCoordonnees, int visibiliteStatistiques )
	{
		final ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("periode", String.valueOf(periode)));
		nameValuePair.add(new BasicNameValuePair("visibilitePhoto", String.valueOf(visibilitePhoto)));
		nameValuePair.add(new BasicNameValuePair("visibiliteCoordonnees", String.valueOf(visibiliteCoordonnees)));
		nameValuePair.add(new BasicNameValuePair("visibiliteStatistiques", String.valueOf(visibiliteStatistiques)));
		nameValuePair.add(new BasicNameValuePair("idParam", String.valueOf(phone_user.get_idParam())));
		nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
		//Log.e("monlog", departement);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result="";
				InputStream is=null;

				//Envoi de la commande http
				try{
					HttpClient httpClient=new DefaultHttpClient();
					Log.d("log_tag", modifier_paramsUtilisateur_file);
					HttpPost httpPost=new HttpPost(modifier_paramsUtilisateur_file);
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
				Log.e("resultat conf", result);
				try{
					final boolean resultat;
					JSONObject jObject= new JSONObject(result);
					resultat=((jObject.getString("res").equals("true")) ? true : false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!resultat)
								Toast.makeText(ConfidentialiteActivity.this, "Erreur lors de la modification!" ,Toast.LENGTH_LONG).show();
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
		getMenuInflater().inflate(R.menu.confidentialite, menu);
		return true;
	}

}
