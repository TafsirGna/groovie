package com.myapp.groovie;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Base64;
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
import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.LieuAdapter;

public class MesLieuxActivity extends Activity {

	//je déclare les variables qui me seront nécessaires tout au long du développement de cette activité
	private ListView mes_lieux_listView; 
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private int idLieuSelected;
	private String supprimer_mon_lieu_file=Groovieparams.DBurl+"supprimer_mon_lieu.php";
	private TextView zero_lieu_TextView;
	private Vibrator myVibrator;
	private Dialog box;
	private UtilisateurDataSource UtilisateurDS;
	private AjouterLieuDataSource AjouterLieuDS;
	private LieuDataSource LieuDS;
	private ProgressBar layout_progressbar;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private Utilisateur phone_user;
	private TelephonyManager phoneManager;
	private InfosdulieuDataSource InfosduLieuDS;
	private List<Lieu> liste_mes_lieux;
	private int DUREE_RAFRAICHISSEMENT=30000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mes_lieux);

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView
		//je crée une instance de vibrator
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		UtilisateurDS= new UtilisateurDataSource(MesLieuxActivity.this);
		UtilisateurDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		AjouterLieuDS= new AjouterLieuDataSource(MesLieuxActivity.this);
		AjouterLieuDS.open();
		LieuDS= new LieuDataSource(MesLieuxActivity.this);
		LieuDS.open();
		mes_lieux_listView=(ListView) findViewById(R.id.mes_lieux_layout_listView);
		layout_progressbar=(ProgressBar) findViewById(id.mes_lieux_layout_progressbar);
		connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		networkInfo= connectivityManager.getActiveNetworkInfo();
		phone_user=get_user();

		//j'initialise le textView qui sera affiché au cas ou il n'y aurait aucune demande dans la base de données
		zero_lieu_TextView= (TextView) findViewById(R.id.mes_lieux_layout_indication_zero_lieux);
		//je masque la listView
		mes_lieux_listView.setVisibility(View.GONE);
		String stringAucunLieu="<font color=#0000FF><i> Aucun lieu enregistré dans vos favoris!</i></font>";
		zero_lieu_TextView.setText(Html.fromHtml(stringAucunLieu));	

		// je récupère tous les lieux de la base de données
		afficher_mes_lieux();

		// lorsque l'utilisateur effectue un appui long sur la list view alors:

		mes_lieux_listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				// je fais vibrer le téléphone 
				myVibrator.vibrate(100);

				idLieuSelected=liste_mes_lieux.get(position).get_idLieu();
				box.show();

				return true;
			}
		});
		mes_lieux_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				idLieuSelected=liste_mes_lieux.get(position).get_idLieu();
				Intent details_lieu_intent= new Intent(MesLieuxActivity.this, LieuDetailsActivity.class);
				details_lieu_intent.putExtra("idLieu", idLieuSelected);
				details_lieu_intent.putExtra("activityparent", "meslieuxactivity");
				details_lieu_intent.putExtra("status_lieu", true);
				details_lieu_intent.putExtra("image_lieu", liste_mes_lieux.get(position).get_Picture());
				details_lieu_intent.putExtra("modifier_prix", 0);
				startActivity(details_lieu_intent);
			}
		});
		box= new Dialog(MesLieuxActivity.this);
		box.setTitle("Actions");
		ListView vue=new ListView(this);
		ArrayAdapter<String> adapter1= new ArrayAdapter<String>(MesLieuxActivity.this, android.R.layout.simple_list_item_1);
		adapter1.add("Modifier Prix");
		adapter1.add("Supprimer de mes lieux");
		adapter1.add("Voir Notifications");
		vue.setAdapter(adapter1);
		box.setContentView(vue);

		vue.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0,
					View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				if (position==1)
				{	
					if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
					{
						box.dismiss();
						Toast.makeText(MesLieuxActivity.this, "Connexion impossible!", Toast.LENGTH_LONG).show();
					}
					else
					{
						box.dismiss();
						layout_progressbar.setVisibility(View.VISIBLE);
						supprimer_lieu(idLieuSelected);
					}
				}
				if (position==0)
				{
					Intent details_lieu_intent= new Intent(MesLieuxActivity.this, LieuDetailsActivity.class);
					details_lieu_intent.putExtra("idLieu", idLieuSelected);
					details_lieu_intent.putExtra("activityparent", "meslieuxactivity");
					details_lieu_intent.putExtra("status_lieu", true);
					details_lieu_intent.putExtra("modifier_prix", 1);
					startActivity(details_lieu_intent);
				}
				if (position==2)
				{
					box.dismiss();
					Intent notifications_lieu_intent= new Intent(MesLieuxActivity.this, NotificationsLieuActivity.class);
					notifications_lieu_intent.putExtra("id_lieu", idLieuSelected);
					startActivity(notifications_lieu_intent);
				}
			}
		});
		Timer minuteur= new Timer();
		TimerTask tache= new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						afficher_mes_lieux();
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

	private void supprimer_lieu(final int idLieu)
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
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("idLieu", String.valueOf(idLieu)));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(supprimer_mon_lieu_file);
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
				Log.e("suppression favoris", result+" "+idLieu);
				try{
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("false")){
								Toast.makeText(MesLieuxActivity.this, "Erreur lors de a suppression du lieu de vos préférences!", Toast.LENGTH_SHORT).show();
							}
							else{
								supprimer_monlieu_localement();
								Toast.makeText(MesLieuxActivity.this, "Le lieu a été supprimé de vos préférences avec succès!", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
	}

	private void supprimer_monlieu_localement()
	{
		AjouterLieuDS.deleteEntree(new AjouterLieu(phone_user.getIdUtilisateur(), idLieuSelected, ""));
		afficher_mes_lieux();
		layout_progressbar.setVisibility(View.GONE);
		if (AjouterLieuDS.getAllEntrees().size()==0)
		{
			mes_lieux_listView.setVisibility(View.GONE);
			zero_lieu_TextView.setVisibility(View.VISIBLE);
		}
	}

	private void afficher_mes_lieux()
	{
		liste= new ArrayList<HashMap<String,Object>>();
		liste_mes_lieux= new ArrayList<Lieu>();
		List<AjouterLieu> liste_ajouterlieu_entrees= new ArrayList<AjouterLieu>();

		Cursor c=LieuDS.get_places_of(phone_user.getIdUtilisateur());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Lieu mon_lieu= new Lieu(c.getInt(0), c.getInt(6), c.getInt(1), c.getString(2), c.getDouble(5), c.getDouble(4), c.getString(5),c.getBlob(8));
			AjouterLieu entree= new AjouterLieu(c.getInt(6), c.getInt(0), c.getString(7));
			liste_mes_lieux.add(mon_lieu);
			liste_ajouterlieu_entrees.add(entree);
			c.moveToNext();
		}
		if (liste_mes_lieux.size()==0)
		{
			mes_lieux_listView.setVisibility(View.GONE);
			zero_lieu_TextView.setVisibility(View.VISIBLE);
		}
		else
		{
			mes_lieux_listView.setVisibility(View.VISIBLE);
			zero_lieu_TextView.setVisibility(View.GONE);
			//j'affiche les résultats de ma requete
			HashMap<String, Object> element;
			for (int i=0;i<liste_mes_lieux.size();i++)
			{
				element=new HashMap<String, Object>();
				element.put("Titre", liste_mes_lieux.get(i).get_titre());
				//String value="<i>Au prix actuel de <font color=#FF0000>"+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_prixmodifie())+" "+Groovieparams.monnaie+"</font> modifié par "+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_modified_by()).getPseudo())+" le "+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "?" : InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_dateModification())+"</i>";
				String value="<i>Au prix actuel de <font color=#FF0000>"+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_prixmodifie())+" "+Groovieparams.monnaie+"</font> modifié par "+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "?" : UtilisateurDS.get_utilisateur(InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_modified_by()).getPseudo())+" le "+((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "?" : FonctionsLibrary.formatDateTime(InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_dateModification()))+"</i>";
				element.put("SousTitre", Html.fromHtml(value));
				element.put("status", ((InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu())==null) ? "0" : InfosduLieuDS.get_entree_reference(liste_mes_lieux.get(i).get_idLieu()).get_status()));

				// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
				if (liste_mes_lieux.get(i).get_Picture()!=null && liste_mes_lieux.get(i).get_Picture().length!=3 && liste_mes_lieux.get(i).get_Picture().length!=0)
					element.put("picture", (getImageBitmap(liste_mes_lieux.get(i).get_Picture())));
				else 
					element.put("picture", (BitmapFactory.decodeResource(MesLieuxActivity.this.getResources(), R.drawable.map2)));

				liste.add(element);	
			}
			LieuAdapter adapter= new LieuAdapter(MesLieuxActivity.this, liste);
			mes_lieux_listView.setAdapter(adapter);
		}
	}
	public Bitmap StringToBitMap(String encodedString) {
		try {
			byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			return bitmap;
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	public String BitMapToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b, Base64.DEFAULT);
		return temp;
	}
	private Bitmap getImageBitmap(byte[] image)
	{
		Bitmap result=null;
		try {
			result=BitmapFactory.decodeByteArray(image, 0, image.length);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mes_lieux, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.mes_lieux_menu_action_actualiser:
			layout_progressbar.setVisibility(View.VISIBLE);
			if (!(networkInfo!=null && networkInfo.isAvailable() && networkInfo.isConnected()))
			{
				Toast.makeText(MesLieuxActivity.this, "Aucun réseau disponible!", Toast.LENGTH_LONG).show();
				layout_progressbar.setVisibility(View.GONE);
			}
			else 
			{
				layout_progressbar.setVisibility(View.GONE);
				Toast.makeText(MesLieuxActivity.this, "Mise à jour effectuée avec succès!", Toast.LENGTH_LONG).show();
				afficher_mes_lieux();
			}
			return true;

		case R.id.mes_lieux_menu_action_settings:
			//j'invoque la page de paramètres
			startActivity(new Intent(this, ParametresActivity.class));
			return true;

		case R.id.mes_lieux_menu_action_mongroupe:
			//j'invoque la page de paramètres
			startActivity(new Intent(this, GroupeEtUtilisateursActivity.class));
			return true;

		case R.id.mes_lieux_menu_action_mesnotifications:
			//j'invoque la page de paramètres
			startActivity(new Intent(this, NotificationsActivity.class));
			return true;

		case R.id.mes_lieux_menu_action_map:
			//j'invoque la page de paramètres
			Intent intent_map= new Intent(MesLieuxActivity.this, MapActivity.class);
			intent_map.putExtra("identifiant", 3);
			startActivity(intent_map);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
