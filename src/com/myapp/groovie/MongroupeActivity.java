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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

public class MongroupeActivity extends Activity {

	//je déclare les variables qui me seront nécessaires tout au long du développement de cette activité
	private ListView mes_membres_listView;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private UtilisateurDataSource UtilisateurDS;
	private Dialog box;
	private int position_item_selected;
	private String fileRetirer= Groovieparams.DBurl+"retirer_membre_groupe.php";
	private ProgressBar progressbar;
	private Vibrator myVibrator;
	private TextView textView_zero_membres;
	private ParticiperDataSource ParticiperDS;
	private Utilisateur MonUser;
	private TelephonyManager phoneManager;
	private List<Utilisateur> liste_membres;
	private List<Groupe> liste_membres_groupe;
	private int DUREE_RAFRAICHISSEMENT=30000;
	private GroupeDataSource GroupeDS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mongroupe);

		//j'initialise les variables 
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mes_membres_listView=(ListView) findViewById(R.id.mon_groupe_layout_membres_listView);
		progressbar=(ProgressBar) findViewById(id.mongroupe_layout_progressbar_actualiser);
		ParticiperDS= new ParticiperDataSource(MongroupeActivity.this);
		ParticiperDS.open();
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		//j'initialise le textView qui sera affiché au cas ou il n'y aurait aucune demande dans la base de données
		textView_zero_membres= (TextView) findViewById(R.id.mon_groupe_layout_indication_zero_membre);
		//je masque la listView
		String stringAucunLieu="<font color=#0000FF><i> Aucun membre enregistré dans votre groupe!</i></font>";
		textView_zero_membres.setText(Html.fromHtml(stringAucunLieu));

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		UtilisateurDS= new UtilisateurDataSource(MongroupeActivity.this);
		UtilisateurDS.open();
		MonUser= get_user();

		if (ParticiperDS.getNombreEntrees()!=0)
		{
			textView_zero_membres.setVisibility(View.GONE);
			mes_membres_listView.setVisibility(View.VISIBLE);
			afficher_membres_groupe();
		}

		box= new Dialog(MongroupeActivity.this);
		box.setTitle("Actions");
		ListView vue=new ListView(this);

		ArrayAdapter<String> adapter1= new ArrayAdapter<String>(MongroupeActivity.this, android.R.layout.simple_list_item_1);
		adapter1.add("Retirer du groupe");
		vue.setAdapter(adapter1);
		box.setContentView(vue);

		mes_membres_listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				// je fais vibrer le téléphone 
				myVibrator.vibrate(100);

				position_item_selected=position;
				box.show();
				return true;
			}
		});
		mes_membres_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent profil_intent=new Intent(MongroupeActivity.this,ProfilUtilisateur.class);
				profil_intent.putExtra("idUtilisateur", liste_membres.get(position).getIdUtilisateur());
				startActivity(profil_intent);
			}
		});

		vue.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if (position==0)
				{
					progressbar.setVisibility(View.VISIBLE);
					retirer_du_groupe(String.valueOf(liste_membres.get(position_item_selected).getIdUtilisateur()));
				}
				box.dismiss();
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
						afficher_membres_groupe();
					}
				});
			}
		};
		minuteur.schedule(tache, DUREE_RAFRAICHISSEMENT, DUREE_RAFRAICHISSEMENT);
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
	private void afficher_membres_groupe()
	{
		//je récupère les autres groupes de l'utilisateur
		liste= new ArrayList<HashMap<String,Object>>();
		liste_membres= new ArrayList<Utilisateur>();
		liste_membres_groupe= new ArrayList<Groupe>();
		
		Cursor c=UtilisateurDS.get_group_members_of(MonUser.getIdGroupe());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Utilisateur user_groupe= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "NULL", "NULL", "NULL", 1);
			user_groupe.setPhoto(c.getBlob(6));
			Groupe groupe= new Groupe(c.getInt(2), c.getInt(0));
			liste_membres.add(user_groupe);
			liste_membres_groupe.add(groupe);
			c.moveToNext();
		}

		if (liste_membres.size()==0)
		{
			mes_membres_listView.setVisibility(View.GONE);
			textView_zero_membres.setVisibility(View.VISIBLE);
		}
		else
		{
			mes_membres_listView.setVisibility(View.VISIBLE);
			textView_zero_membres.setVisibility(View.GONE);
			//j'affiche les résultats de ma requete
			HashMap<String, Object> element;
			for (int i=0;i<liste_membres.size();i++)
			{
				element=new HashMap<String, Object>();
				element.put("Pseudo", liste_membres.get(i).getPseudo());
				String value="<font color=#0000FF>"+liste_membres.get(i).getEmail()+"</font>";
				element.put("Email", Html.fromHtml(value));
				element.put("photo", ((liste_membres.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(MongroupeActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_membres.get(i).getPhoto())));
				element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_membres.get(i).getIdUtilisateur())+" personne(s) qui suivent");
				element.put("note", ParticiperDS.get_note_user(liste_membres.get(i).getIdGroupe()));
				liste.add(element);	
			}
			UtilisateurAdapter adapter= new UtilisateurAdapter(this, liste,R.drawable.icon_user);
			mes_membres_listView.setAdapter(adapter);
		}

	}

	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	private void retirer_du_groupe(final String idUtilisateur)
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
					nameValuePair.add(new BasicNameValuePair("idGroupe", String.valueOf(MonUser.getIdGroupe())));
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", idUtilisateur));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(fileRetirer);
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
				try{
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("true")){
								supprimer_membre_localement();
								progressbar.setVisibility(View.GONE);
								Toast.makeText(MongroupeActivity.this, "Utilisateur retiré avec succès!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void supprimer_membre_localement()
	{
		ParticiperDS.delete_my_groupe_member(MonUser.getIdGroupe(), liste_membres.get(position_item_selected).getIdUtilisateur());
		afficher_membres_groupe();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mongroupe, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.mon_groupe_menu_action_tous_utilisateurs:
			startActivity(new Intent(MongroupeActivity.this, ListeUtilisateursActivity.class));
			return true;

		case R.id.mon_groupe_menu_action_settings:
			startActivity(new Intent(MongroupeActivity.this, ParametresActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
