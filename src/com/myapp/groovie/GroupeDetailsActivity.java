package com.myapp.groovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.myapp.groovie.R.drawable;
import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.Groupe;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Lieu;
import com.myapp.groovie.classes.database.LieuDataSource;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.Item_details_layout_adapter;
import com.myapp.groovie.classes.objects.LieuAdapter;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

@SuppressLint("NewApi")
public class GroupeDetailsActivity extends Activity {

	private GroupeDataSource GroupeDS;
	private UtilisateurDataSource UtilisateurDS;
	private String[] listeStringInfosGroupe= new String[] {"Date d'adhésion","Follower(s)"};
	private List<HashMap<String, Object>> details_liste;
	private String[] listeStringValuesInfosGroupe;
	private Groupe CurrentGroupe;
	private ListView listView_infos_groupe;
	//private ListView listView_membres_groupe;
	private ParticiperDataSource ParticiperDS;
	private InfosdulieuDataSource InfosduLieuDS;
	private ImageView imageView_plus_membres;
	private TelephonyManager phoneManager;
	private Utilisateur MonUser;
	private Dialog dialog_box;
	private ImageView imageGroupe;
	private ImageView imageView_plus_notifs;

	private LieuDataSource LieuDS;
	//private ListView listView_notifications;
	private int[] listeIconeInfosGroupe= new int[]{R.drawable.icone_calendrier,
			R.drawable.icone_like};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groupe_details);

		//j'initialise les variables sus_citées
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();
		LieuDS= new LieuDataSource(this);
		LieuDS.open();
		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();
		phoneManager= (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		MonUser= get_user();
		listView_infos_groupe= (ListView) findViewById(id.groupe_details_layout_listView_infosGroupe);
		imageView_plus_membres=(ImageView) findViewById(id.groupe_details_layout_imageView_plus_membres);
		imageGroupe= (ImageView) findViewById(id.groupe_details_layout_imageGroupe);
		imageView_plus_notifs=(ImageView) findViewById(R.id.groupe_details_layout_imageView_plus_notifications);

		//je récupère l'intent qui a lancé cette activité
		Intent intent_appelant= getIntent();
		CurrentGroupe=GroupeDS.get_groupe(intent_appelant.getIntExtra("idGroupe", 0));

		//j'affiche la photo correspondante au groupe choisi 
		Utilisateur CurrentUtilisateur=UtilisateurDS.get_utilisateur(CurrentGroupe.get_idUtilisateur());
		if (CurrentUtilisateur.getPhoto()!=null && CurrentUtilisateur.getPhoto().length!=3)
			imageGroupe.setImageBitmap(getImageBitmap(CurrentUtilisateur.getPhoto()));
		else
			imageGroupe.setImageResource(drawable.icon_user);

		//je récupère l'action bar afin de modifier le titre
		ActionBar action_bar= getActionBar();
		action_bar.setTitle("Groupe de "+UtilisateurDS.get_utilisateur(CurrentGroupe.get_idUtilisateur()).getPseudo());

		//je remplis les informations sur le groupe dans la listview
		afficher_infos_groupe();

		//je remplis les informations sur les membres du groupe
		imageView_plus_membres.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				afficher_membres_groupe();
				dialog_box.show();
			}
		});

		//je remplis les informations sur les notifications du groupe
		imageView_plus_notifs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				afficher_notifications_groupe();
				dialog_box.show();
			}
		});
	}

	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	private void afficher_notifications_groupe()
	{

		dialog_box= new Dialog(this);
		ListView dialog_listView=new ListView(this);
		dialog_box.setTitle("Notifications");

		details_liste= new ArrayList<HashMap<String,Object>>();
		List<Lieu> liste_des_lieux= new ArrayList<Lieu>();
		List<Infosdulieu> liste_entrees_infosLieu= new ArrayList<Infosdulieu>();
		Cursor c= GroupeDS.get_notifs_groupe(CurrentGroupe.get_idUtilisateur());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Lieu lieu= new Lieu(); lieu.set_titre(c.getString(0));
			liste_des_lieux.add(lieu);
			Infosdulieu info= new Infosdulieu(); info.set_prixmodifie(c.getInt(1)); info.set_dateModification(c.getString(2));
			liste_entrees_infosLieu.add(info);
			c.moveToNext();
		}
		//int[] liste_icones_notifs= new int[] {};
		int listeSize= liste_des_lieux.size();
		HashMap<String, Object> element;
		for (int i=0;i<listeSize;i++)
		{
			element=new HashMap<String, Object>();
			element.put("Titre", liste_des_lieux.get(i).get_titre());
			String value="<i>Au prix actuel de <font color=#FF0000>"+liste_entrees_infosLieu.get(i).get_prixmodifie()+" "+Groovieparams.monnaie+
					"</font> le "+FonctionsLibrary.formatDateTime(liste_entrees_infosLieu.get(i).get_dateModification())+"</i>";
			element.put("SousTitre", Html.fromHtml(value));
			element.put("status",0);
			details_liste.add(element);			
		}
		LieuAdapter notifs_groupe_Adapter= new LieuAdapter(this, details_liste);
		dialog_listView.setAdapter(notifs_groupe_Adapter);

		//j'affiche enfin la dialog box
		dialog_box.setContentView(dialog_listView);

	}

	private void afficher_infos_groupe()
	{
		details_liste= new ArrayList<HashMap<String,Object>>();
		listeStringValuesInfosGroupe=new String[] {FonctionsLibrary.formatDateTime(ParticiperDS.get_entree_participer(CurrentGroupe.get_idGroupe(), MonUser.getIdUtilisateur(), ParticiperDS.getAllEntrees()).get_dateEntre()), GroupeDS.get_nombre_followers(CurrentGroupe.get_idUtilisateur())+" utilisateurs qui suivent ce groupe"};
		HashMap<String, Object> element;
		for (int i=0;i<listeStringInfosGroupe.length;i++)
		{
			element=new HashMap<String, Object>();
			element.put("libelleDetails", listeStringInfosGroupe[i]);
			element.put("valueDetails", Html.fromHtml(listeStringValuesInfosGroupe[i]));
			details_liste.add(element);
		}
		Item_details_layout_adapter infos_groupe_Adapter= new Item_details_layout_adapter(this, details_liste, listeIconeInfosGroupe);
		listView_infos_groupe.setAdapter(infos_groupe_Adapter);
	}
	private void afficher_membres_groupe()
	{
		dialog_box= new Dialog(this);
		ListView dialog_listView=new ListView(this);
		dialog_box.setTitle("Membres");

		details_liste= new ArrayList<HashMap<String,Object>>();
		List<Utilisateur> liste_membres_groupe= new ArrayList<Utilisateur>();
		List<Groupe> liste_groupes= new ArrayList<Groupe>();
		Cursor c=UtilisateurDS.get_group_members_of(CurrentGroupe.get_idGroupe());
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Utilisateur user_groupe= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "NULL", "NULL", "NULL", 1);
			Groupe groupe= new Groupe(c.getInt(2), c.getInt(0));
			liste_membres_groupe.add(user_groupe);
			liste_groupes.add(groupe);
			c.moveToNext();
		}
		HashMap<String, Object> element;
		for (int i=0;i<liste_membres_groupe.size();i++)
		{
			element=new HashMap<String, Object>();
			element.put("Pseudo", liste_membres_groupe.get(i).getPseudo());
			String value="<font color=#0000FF>"+liste_membres_groupe.get(i).getEmail()+"</font>";
			element.put("Email", Html.fromHtml(value));
			element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_membres_groupe.get(i).getIdUtilisateur())+" personne(s) qui suivent");
			element.put("note", ParticiperDS.get_note_user(liste_membres_groupe.get(i).getIdGroupe()));
			details_liste.add(element);
		}
		UtilisateurAdapter membres_groupe_Adapter= new UtilisateurAdapter(this, details_liste,R.drawable.icon_user);
		dialog_listView.setAdapter(membres_groupe_Adapter);

		//j'affiche enfin la dialog box
		dialog_box.setContentView(dialog_listView);
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
		getMenuInflater().inflate(R.menu.groupe_details, menu);
		return true;
	}
}
