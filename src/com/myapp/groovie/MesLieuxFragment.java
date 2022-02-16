package com.myapp.groovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

public class MesLieuxFragment extends Fragment{

	//public static final String TAG = "MesLieuxFragment";
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
	private Utilisateur MonUser;
	private TelephonyManager phoneManager;
	private InfosdulieuDataSource InfosduLieuDS;
	private List<Lieu> liste_mes_lieux;
	private int DUREE_RAFRAICHISSEMENT=30000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup
			container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_mes_lieux, container, false);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
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
			MonUser=get_user();

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
					//titreLieuSelected=liste.get(position).get("Titre").toString();
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
		 */
	}

	/*
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
					nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(MonUser.getIdUtilisateur())));
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
		List<Lieu> liste_des_lieux= LieuDS.getAllLieux();
		int idLieu;
		for (int i=0;i<liste_des_lieux.size();i++)
		{
			if (idLieuSelected==liste_mes_lieux.get(i).get_idLieu())
			{
				idLieu=liste_des_lieux.get(i).get_idLieu();
				AjouterLieu entree=new AjouterLieu();
				entree.set_idLieu(idLieu);
				AjouterLieuDS.deleteEntree(entree);

				//je décremente le nombre de like du lieu 
				Lieu lieu=LieuDS.get_Lieu(idLieu);
				LieuDS.updateLieu(lieu);
				break;
			}
		}
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

		Cursor c=LieuDS.get_places_of(MonUser.getIdUtilisateur());
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
				if (liste_mes_lieux.get(i).get_Picture()!=null && liste_mes_lieux.get(i).get_Picture().length!=3)
					element.put("picture", (getImageBitmap(liste_mes_lieux.get(i).get_Picture())));
				else 
					element.put("picture", (BitmapFactory.decodeResource(MesLieuxActivity.this.getResources(), R.drawable.map2)));

				//Log.e("size", liste_mes_lieux.get(i).get_Picture().length+"");
				liste.add(element);	
			}
			LieuAdapter adapter= new LieuAdapter(MesLieuxActivity.this, liste);
			mes_lieux_listView.setAdapter(adapter);
		}
	}
	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}		
	*/
}
