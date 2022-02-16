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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Participer;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

public class MesAutresGroupesActivity extends Activity {

	private ListView mes_autres_groupes_listView;
	private UtilisateurDataSource UtilisateurDS;
	private Vibrator myVibrator;
	private ProgressBar layout_progressbar_actualiser;
	private Dialog box;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private String sortir_du_groupe_file=Groovieparams.DBurl+"sortir_du_groupe.php";
	private String modifier_note_file=Groovieparams.DBurl+"modifier_note.php";
	private TextView textView_zero_groupe;
	private GroupeDataSource GroupeDS;
	private Utilisateur MonUser;
	private TelephonyManager phoneManager;
	private ParticiperDataSource ParticiperDS;
	private int DUREE_RAFRAICHISSEMENT=30000;
	private int position_item_selected;
	private List<Utilisateur> liste_proprietaires_groupe;
	private InfosdulieuDataSource InfosduLieuDS;
	private LieuDataSource LieuDS;
	private List<Groupe> liste_groupes;
	private String NomUtilisateur="";
	private RatingBar ratingbar=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mes_autres_groupes);

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur fait un long click sur un item de ma listView
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		layout_progressbar_actualiser= (ProgressBar) findViewById(id.mes_autres_groupes_layout_progressbar_actualiser);
		UtilisateurDS= new UtilisateurDataSource(MesAutresGroupesActivity.this);
		UtilisateurDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		phoneManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		MonUser=get_user();
		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();
		LieuDS= new LieuDataSource(this);
		LieuDS.open();

		//je récupère et initialise les variables
		mes_autres_groupes_listView=(ListView) findViewById(R.id.mes_autres_groupes_layout_listView);
		textView_zero_groupe= (TextView) findViewById(R.id.mes_autres_groupes_layout_zero_autre_groupe);
		String stringAucunLieu="<font color=#0000FF><i> Aucun autre groupe enregistré!</i></font>";
		textView_zero_groupe.setText(Html.fromHtml(stringAucunLieu));
		GroupeDS= new GroupeDataSource(MesAutresGroupesActivity.this);
		GroupeDS.open();

		if (GroupeDS.getNombreGroupes()!=0)
		{
			textView_zero_groupe.setVisibility(View.GONE);
			mes_autres_groupes_listView.setVisibility(View.VISIBLE);
			afficher_mes_autres_groupes();
		}

		//je construis le dialog box qui apparait lorsque j'appuie long sur item de ma listeView
		box= new Dialog(MesAutresGroupesActivity.this);
		box.setTitle("Actions");
		final ListView dialogbox_listView=new ListView(this);

		// lorsque l'utilisateur effectue un appui long sur la list view alors:
		mes_autres_groupes_listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				// je fais vibrer le téléphone 
				myVibrator.vibrate(100);

				position_item_selected=position;

				ArrayAdapter<String> adapter1= new ArrayAdapter<String>(MesAutresGroupesActivity.this, android.R.layout.simple_list_item_1);
				adapter1.add("Sortir du groupe");
				NomUtilisateur=liste_proprietaires_groupe.get(position).getPseudo();
				adapter1.add("Noter "+ NomUtilisateur);
				dialogbox_listView.setAdapter(adapter1);
				box.setContentView(dialogbox_listView);
				box.show();
				return true;
			}
		});

		mes_autres_groupes_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent_groupe_details= new Intent(MesAutresGroupesActivity.this, GroupeDetailsActivity.class);
				intent_groupe_details.putExtra("idGroupe", liste_proprietaires_groupe.get(position).getIdGroupe());
				startActivity(intent_groupe_details);

			}
		});

		dialogbox_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if (position==0)
				{
					box.dismiss();
					layout_progressbar_actualiser.setVisibility(View.VISIBLE);
					sortir_du_groupe(liste_proprietaires_groupe.get(position_item_selected).getIdGroupe());
				}
				if (position==1)
				{
					//Construction de la boite de dialogue de note de l'utilisateur
					RelativeLayout ratingbarLayout=(RelativeLayout) RelativeLayout.inflate(MesAutresGroupesActivity.this,R.layout.ratingbar_layout,null);
					ratingbar=(RatingBar)ratingbarLayout.findViewById(id.ratingbar);

					AlertDialog.Builder note_dialogBox= new AlertDialog.Builder(MesAutresGroupesActivity.this);
					note_dialogBox.setCancelable(true);
					note_dialogBox.setView(ratingbarLayout);
					note_dialogBox.setTitle("Note");
					note_dialogBox.setPositiveButton("VALIDER", dialog_confirmation_valider_listener);
					note_dialogBox.show();
				}
				box.dismiss();
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
						afficher_mes_autres_groupes();
					}
				});
			}
		};
		minuteur.schedule(tache, DUREE_RAFRAICHISSEMENT, DUREE_RAFRAICHISSEMENT);
	}

	private DialogInterface.OnClickListener dialog_confirmation_valider_listener= new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			//Toast.makeText(MesAutresGroupesActivity.this, String.valueOf(ratingbar.getRating()), Toast.LENGTH_LONG).show();
			modifier_note(liste_proprietaires_groupe.get(position_item_selected).getIdGroupe(),ratingbar.getRating());
		}
	};

	private void modifier_note(final int idGroupe, final float note)
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
					nameValuePair.add(new BasicNameValuePair("idUtilisateur",String.valueOf(MonUser.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("idGroupe", String.valueOf(idGroupe)));
					nameValuePair.add(new BasicNameValuePair("note",String.valueOf(note)));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(modifier_note_file);
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
					Log.e("fo", result);
					JSONObject jObject = new JSONObject(result);
					final String res=jObject.getString("res");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (res.equals("true"))
							{ 
								modifier_note_localement(idGroupe, note);
								Toast.makeText(MesAutresGroupesActivity.this, "Note enregistrée!", Toast.LENGTH_LONG).show();
							}
							else
							{
								Toast.makeText(MesAutresGroupesActivity.this, "Un problème est survenue enregistrement de la note. Veuillez réessayer!", Toast.LENGTH_LONG).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void modifier_note_localement(int idGroupe, float note)
	{
		Participer entree= ParticiperDS.get_entree_participer(idGroupe, MonUser.getIdUtilisateur(), ParticiperDS.getAllEntrees());
		entree.set_note(note);
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
	private void afficher_mes_autres_groupes()
	{
		//je récupère les autres groupes de l'utilisateur
		liste= new ArrayList<HashMap<String,Object>>();
		liste_proprietaires_groupe= new ArrayList<Utilisateur>();
		liste_groupes= new ArrayList<Groupe>();

		Cursor c=GroupeDS.get_others_groups_of(MonUser.getIdUtilisateur());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Utilisateur user_groupe= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "NULL", "NULL", "NULL", 1);
			user_groupe.setPhoto(c.getBlob(6));
			Groupe groupe= new Groupe(c.getInt(2), c.getInt(0));
			liste_proprietaires_groupe.add(user_groupe);
			liste_groupes.add(groupe);
			c.moveToNext();
		}
		if (liste_proprietaires_groupe.size()==0)
		{
			mes_autres_groupes_listView.setVisibility(View.GONE);
			textView_zero_groupe.setVisibility(View.VISIBLE);
		}
		else
		{
			mes_autres_groupes_listView.setVisibility(View.VISIBLE);
			textView_zero_groupe.setVisibility(View.GONE);
			//j'affiche les résultats de ma requete
			HashMap<String, Object> element;
			for (int i=0;i<liste_proprietaires_groupe.size();i++)
			{

				//je désigne le soustitre à afficher
				String Sous_titre="";
				if (InfosduLieuDS.get_entree_infosdulieu_groupe(liste_proprietaires_groupe.get(i).getIdUtilisateur())==null)
					Sous_titre="<i><font color=#FF0000>Aucune information disponible</font></i>";
				else
				{
					Infosdulieu entree=InfosduLieuDS.get_entree_infosdulieu_groupe(liste_proprietaires_groupe.get(i).getIdUtilisateur());
					Sous_titre+="<i>"+LieuDS.get_Lieu(entree.get_idLieu()).get_titre();
					Sous_titre+=" à <font color=#FF0000>";
					Sous_titre+= entree.get_prixmodifie()+" "+Groovieparams.monnaie+"</font>";
					Sous_titre+=" modifié le <font color=#0000FF>"+ FonctionsLibrary.formatDateTime(entree.get_dateModification())+"</font></i>";
				}

				element=new HashMap<String, Object>();
				element.put("Pseudo", "Groupe de "+liste_proprietaires_groupe.get(i).getPseudo());
				String value=Sous_titre;
				element.put("Email", Html.fromHtml(value));
				element.put("photo", ((liste_proprietaires_groupe.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(getResources(), R.drawable.icone_groupe) : getImageBitmap(liste_proprietaires_groupe.get(i).getPhoto())));
				element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_proprietaires_groupe.get(i).getIdUtilisateur())+" personne(s) qui suivent");
				element.put("note", ParticiperDS.get_note_user(liste_proprietaires_groupe.get(i).getIdGroupe()));
				liste.add(element);	
			}
			UtilisateurAdapter adapter= new UtilisateurAdapter(MesAutresGroupesActivity.this, liste,R.drawable.icone_groupe);
			mes_autres_groupes_listView.setAdapter(adapter);
		}
	}

	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	private void sortir_du_groupe(final int idGroupe)
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
					nameValuePair.add(new BasicNameValuePair("iduser",String.valueOf(MonUser.getIdUtilisateur())));
					nameValuePair.add(new BasicNameValuePair("UserGroupe", String.valueOf(idGroupe)));

					HttpClient httpClient=new DefaultHttpClient();
					HttpPost httpPost=new HttpPost(sortir_du_groupe_file);
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
							if (res.equals("true"))
							{ 
								layout_progressbar_actualiser.setVisibility(View.GONE);
								supprimer_autre_groupe_localement();
								Toast.makeText(MesAutresGroupesActivity.this, "Vous venez de sortir du groupe!", Toast.LENGTH_LONG).show();
							}
							else
							{
								layout_progressbar_actualiser.setVisibility(View.GONE);
								Toast.makeText(MesAutresGroupesActivity.this, "Un problème est survenue durant la sortie du groupe. Veuillez réessayer!", Toast.LENGTH_LONG).show();
							}
						}
					});
				}catch(JSONException e){
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			}
		}).start();
	}

	private void supprimer_autre_groupe_localement()
	{
		ParticiperDS.delete_one_other_group(liste_proprietaires_groupe.get(position_item_selected).getIdGroupe(), MonUser.getIdUtilisateur());
		afficher_mes_autres_groupes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mes_autres_groupes, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.mes_autres_groupes_menu_action_settings:
			startActivity(new Intent(MesAutresGroupesActivity.this, ParametresActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
