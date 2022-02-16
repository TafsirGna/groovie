package com.myapp.groovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.ParamsUtilisateur;
import com.myapp.groovie.classes.database.ParamsUtilisateurDataSource;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.FonctionsLibrary;
import com.myapp.groovie.classes.objects.Groovieparams;
import com.myapp.groovie.classes.objects.UtilisateurAdapter;

public class NotificationsLieuActivity extends Activity {

	private ListView notifications_listview;
	private InfosdulieuDataSource InfosDuLieuDS;
	List<HashMap<String, Object>> liste= new ArrayList<HashMap<String,Object>>();
	private int id_current_lieu;
	private GroupeDataSource GroupeDS;
	private TextView no_notifs_textView;
	private ParticiperDataSource ParticiperDS;
	private ParamsUtilisateurDataSource ParamsUtilisateurDS;
	private Utilisateur phone_user;
	private UtilisateurDataSource UtilisateurDS;
	private List<ParamsUtilisateur> liste_paramsUtilisateur;
	private TelephonyManager phoneManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications_lieu);

		//je récupère l'intent appelant de l'activité
		Intent intent_activite_appelant=getIntent();
		id_current_lieu=intent_activite_appelant.getIntExtra("id_lieu", 0);

		//j'initialise les variables déclarées
		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		InfosDuLieuDS= new InfosdulieuDataSource(this);
		InfosDuLieuDS.open();
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();
		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();
		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();
		ParamsUtilisateurDS= new ParamsUtilisateurDataSource(this);
		ParamsUtilisateurDS.open();
		phone_user=get_user();
		liste_paramsUtilisateur=ParamsUtilisateurDS.getAllEntrees();

		notifications_listview= (ListView) findViewById(id.notifications_lieu_layout_listView);
		no_notifs_textView= (TextView) findViewById(id.notifications_lieu_layout_noNotifications_textView);
		no_notifs_textView.setText(Html.fromHtml("<i><font color=#0000FF>"+ no_notifs_textView.getText()+ "</font></i>"));

		//je remplis la listView des éléments de notifications
		afficher_notifications();
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
	private void afficher_notifications()
	{
		List<Utilisateur> liste_updaters= new ArrayList<Utilisateur>();
		List<Infosdulieu> liste_infos= new ArrayList<Infosdulieu>();

		Cursor c=InfosDuLieuDS.get_liste_updaters(id_current_lieu);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Utilisateur user= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "", "", "", 0);
			user.set_idParam(c.getInt(8));
			user.setPhoto(c.getBlob(9));
			Infosdulieu info= new Infosdulieu();
			info.set_dateModification(c.getString(6));
			info.set_prixmodifie(c.getInt(7));

			liste_updaters.add(user);
			liste_infos.add(info);
			c.moveToNext();
		}
		if (liste_updaters.size()==0)
		{
			notifications_listview.setVisibility(View.GONE);
			no_notifs_textView.setVisibility(View.VISIBLE);
		}
		else
		{
			liste= new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object> element;
			for (int i=0;i<liste_updaters.size();i++)
			{
				element=new HashMap<String, Object>();
				element.put("Pseudo", liste_updaters.get(i).getPseudo());
				String value="<font color=#0000FF> Le prix est de "+liste_infos.get(i).get_prixmodifie()+" "+Groovieparams.monnaie+" modifié le "+FonctionsLibrary.formatDateTime(liste_infos.get(i).get_dateModification())+"</font>";
				element.put("Email", Html.fromHtml(value));
				
				// affichage de la photo uniquement lorsque c'est l'utilisateur concerné le permet
				ParamsUtilisateur paramsutilisateur= ParamsUtilisateurDS.get_paramsUtilisateur(liste_updaters.get(i).get_idParam(), liste_paramsUtilisateur);
				boolean status_utilisateur= ParticiperDS.isMyMember(liste_updaters.get(i).getIdGroupe(), phone_user.getIdUtilisateur());
				if(paramsutilisateur.get_visibilitePhoto()==2 || (paramsutilisateur.get_visibilitePhoto()==1 && status_utilisateur==true))
					element.put("photo", ((liste_updaters.get(i).getPhoto()==null) ? BitmapFactory.decodeResource(NotificationsLieuActivity.this.getResources(), R.drawable.icon_user): getImageBitmap(liste_updaters.get(i).getPhoto())));
				else 
					element.put("photo", (BitmapFactory.decodeResource(NotificationsLieuActivity.this.getResources(), R.drawable.icon_user)));
				
				element.put("nbUtilisateur",GroupeDS.get_nombre_followers(liste_updaters.get(i).getIdUtilisateur())+" personne(s) qui suivent");
				element.put("note", ParticiperDS.get_note_user(liste_updaters.get(i).getIdGroupe()));

				liste.add(element);			
			}

			UtilisateurAdapter adapter= new UtilisateurAdapter(NotificationsLieuActivity.this, liste,R.drawable.icon_user);
			notifications_listview.setAdapter(adapter);
		}
	}
	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notifications_lieu, menu);
		return true;
	}

}
