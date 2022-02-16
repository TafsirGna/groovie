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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.myapp.groovie.classes.database.AjouterLieu;
import com.myapp.groovie.classes.database.AjouterLieuDataSource;
import com.myapp.groovie.classes.database.GroupeDataSource;
import com.myapp.groovie.classes.database.Infosdulieu;
import com.myapp.groovie.classes.database.InfosdulieuDataSource;
import com.myapp.groovie.classes.database.Participer;
import com.myapp.groovie.classes.database.ParticiperDataSource;
import com.myapp.groovie.classes.database.Utilisateur;
import com.myapp.groovie.classes.database.UtilisateurDataSource;
import com.myapp.groovie.classes.objects.Groovieparams;

public class GroovieService extends Service{

	private String [] liste_actions={"ajouterlieu","participer","infosdulieu"};
	private String les_lieux_ajoutes_file=Groovieparams.DBurl+"lister_lieux_ajoutes.php";
	private String liste_entrees_participer_file= Groovieparams.DBurl+"lister_entrees_participer.php";
	private String InfosduLieu_file=Groovieparams.DBurl+"liste_entrees_infosdulieu.php";
	private TelephonyManager phoneManager;
	private Utilisateur phone_user;
	private UtilisateurDataSource UtilisateurDS;
	private AjouterLieuDataSource AjouterLieuDS;
	private ParticiperDataSource ParticiperDS;
	private InfosdulieuDataSource InfosduLieuDS;
	private GroupeDataSource GroupeDS;
	private List<Object> liste_resultats=null;
	private Vibrator myVibrator;
	private long[] pattern={200,200};
	private PowerManager mypowerManager;
	private WakeLock mywakeLock;
	private int ID_NOTIFICATION;
	int DUREE_VERIFICATION=30000;
	private List<Utilisateur> liste_utilisateurs_suivis;
	private SharedPreferences groovie_preferences;


	private Handler mhandler= new Handler(){
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
				if (msg.arg1==1)
				{
					liste_resultats=(List<Object>)msg.obj;
					charger_donnees_locales(liste_actions[msg.arg1],liste_resultats);
				}
				if (msg.arg1==2)
				{
					liste_resultats=(List<Object>)msg.obj;
					charger_donnees_locales(liste_actions[msg.arg1],liste_resultats);
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate()
	{
		super.onCreate();

		//Initialisation des datasources
		UtilisateurDS= new UtilisateurDataSource(GroovieService.this);
		UtilisateurDS.open();

		AjouterLieuDS= new AjouterLieuDataSource(this);
		AjouterLieuDS.open();

		GroupeDS= new GroupeDataSource(this);
		GroupeDS.open();

		ParticiperDS= new ParticiperDataSource(this);
		ParticiperDS.open();

		InfosduLieuDS= new InfosdulieuDataSource(this);
		InfosduLieuDS.open();

		phoneManager=(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		phone_user=get_user();

		//Initialisation de l'objet de préférence partagée
		groovie_preferences = PreferenceManager.getDefaultSharedPreferences(this);

		//Ceci me permet de faire vibrer le téléphone à chaque que l'utilisateur recoit une notification
		//je crée une instance de vibrator
		myVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//ceci me permet d'allumer l'écran lorsque l'utilisateur recoit une notification 
		mypowerManager= (PowerManager) getSystemService(Context.POWER_SERVICE);
		mywakeLock=mypowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE,"MyWakeLock");

		mettre_a_jour_BD();
	}

	private String listeString_to_String(List<String> liste)
	{
		String resultat=new String();
		for (int i=0;i<liste.size();i++)
		{
			if (i==1) resultat+=",";
			resultat+=liste.get(i);
		}
		return resultat;
	}
	private String get_pseudo(List<Utilisateur> liste, AjouterLieu entree)
	{
		int listeSize=liste.size();
		for (int i=0;i<listeSize;i++)
		{
			if (entree.get_idUtilisateur()==liste.get(i).getIdUtilisateur())
				return liste.get(i).getPseudo();
		}
		return null;
	}

	private String get_pseudo(List<Utilisateur> liste, Infosdulieu entree)
	{
		int listeSize=liste.size();
		for (int i=0;i<listeSize;i++)
		{
			if (entree.get_modified_by()==liste.get(i).getIdUtilisateur())
				return liste.get(i).getPseudo();
		}
		return null;
	}

	private String get_pseudo(List<Utilisateur> liste, Participer entree, String cas)
	{
		int listeSize=liste.size();
		if (cas.equals("from id_utilisateur"))
		{
			for (int i=0;i<listeSize;i++)
			{
				if (entree.get_idUtilisateur()==liste.get(i).getIdUtilisateur())
					return liste.get(i).getPseudo();
			}
		}
		if (cas.equals("from id_groupe"))
		{
			for (int i=0;i<listeSize;i++)
			{
				if (entree.get_idGroupe()==liste.get(i).getIdGroupe())
					return liste.get(i).getPseudo();
			}
		}
		return null;
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
		if (action.equals("ajouterlieu"))
		{
			//je récupère la liste des utilisateurs que je suis
			liste_utilisateurs_suivis= new ArrayList<Utilisateur>();
			List<notifiant> liste_notifiants= new ArrayList<GroovieService.notifiant>();

			Cursor c=GroupeDS.get_others_groups_of(phone_user.getIdUtilisateur());
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Utilisateur user_suivi= new Utilisateur(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), "NULL", "NULL", "NULL", 1);
				liste_utilisateurs_suivis.add(user_suivi);
				c.moveToNext();
			}

			//List<Utilisateur> liste_des_utilisateurs=UtilisateurDS.getAllUtilisateurs();
			List<AjouterLieu> liste_entrees= AjouterLieuDS.getAllEntrees();

			int mode_update=((liste_entrees.size()==0) ? 0 : 1);

			int listeSize=liste_resultats.size();

			//Log.e("tag_ajouterlieu", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					AjouterLieu entree=null;
					try {
						entree = new AjouterLieu(jObject.getInt("idUtilisateur"), jObject.getInt("idLieu"), jObject.getString("dateAjout"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					AjouterLieuDS.createEntree(entree);
					String pseudo=get_pseudo(liste_utilisateurs_suivis, entree);
					if (pseudo!=null)
					{
						liste_notifiants.add(new notifiant(pseudo, 0));
					}
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						for (int j=0;j<liste_entrees.size();j++)
						{
							AjouterLieu entree=liste_entrees.get(j);
							String value=String.valueOf(entree.get_idUtilisateur())+String.valueOf(entree.get_idLieu());
							if (value.equals(String.valueOf(numeroEnregistrement)))
							{
								AjouterLieuDS.deleteEntree(entree);
								String pseudo=get_pseudo(liste_utilisateurs_suivis, entree);
								if (pseudo!=null)
								{
									liste_notifiants.add(new notifiant(pseudo, 0));
								}
							}
						}
					}
					else if (status==2)
					{
						//récupération de l'entree Ajouterlieu mise à jour 
						AjouterLieu entree=null;
						try {
							entree = new AjouterLieu(jObject.getInt("idUtilisateur"), jObject.getInt("idLieu"), jObject.getString("dateAjout"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (!AjouterLieuDS.hasAlreadySaved(entree, liste_entrees))
						{
							AjouterLieuDS.createEntree(entree);
							String pseudo=get_pseudo(liste_utilisateurs_suivis, entree);
							if (pseudo!=null)
							{
								liste_notifiants.add(new notifiant(pseudo, 2));
							}
						}
					}
				}
			}
			afficher_notification(liste_notifiants,"ajouterlieu");
		}

		if (action.equals("participer"))
		{
			List<notifiant> liste_notifiants= new ArrayList<GroovieService.notifiant>();
			List<Participer> liste_entrees= ParticiperDS.getAllEntrees();
			List<Utilisateur> liste_des_utilisateurs=UtilisateurDS.getAllUtilisateurs();

			int mode_update=((liste_entrees.size()==0) ? 0 : 1);

			int listeSize=liste_resultats.size();

			//Log.e("tag_particip", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					Participer entree=null;
					try {
						entree = new Participer(jObject.getInt("idGroupe"),jObject.getInt("idUtilisateur"),jObject.getString("dateEntre"),jObject.getString("dateInvitation"),jObject.getString("dateDemande"),Float.parseFloat(jObject.getString("note")),0);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ParticiperDS.createParticiper(entree);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						//Log.e("status entry", "yes ; "+status);
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						for (int j=0;j<liste_entrees.size();j++)
						{
							Participer entree=liste_entrees.get(j);
							String value=String.valueOf(entree.get_idUtilisateur())+String.valueOf(entree.get_idGroupe());
							if (value.equals(String.valueOf(numeroEnregistrement)))
							{
								if (entree.get_idUtilisateur()==phone_user.getIdUtilisateur())
								{
									String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_groupe");
									if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 0));
								}
								if (entree.get_idGroupe()==phone_user.getIdGroupe())
								{
									String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_utilisateur");
									if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 0));
								}
								ParticiperDS.deleteParticiper(entree);
							}
						}
					}
					else 
					{
						//récupération de l'entree participer mise à jour 
						Participer entree=null;
						try {
							entree = new Participer(jObject.getInt("idGroupe"),jObject.getInt("idUtilisateur"),jObject.getString("dateEntre"),jObject.getString("dateInvitation"),jObject.getString("dateDemande"),Float.parseFloat(jObject.getString("note")),0);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==1)
						{
							if ((entree.get_idUtilisateur()==phone_user.getIdUtilisateur() && !entree.get_dateEntre().equals("1900-01-01 00:00:00") && ParticiperDS.get_entree_participer(entree.get_idGroupe(), entree.get_idUtilisateur(),liste_entrees).get_dateEntre().equals("1900-01-01 00:00:00")))
							{
								String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_groupe");
								if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 1));
							}
							if ((entree.get_idGroupe()==phone_user.getIdGroupe() && !entree.get_dateEntre().equals("1900-01-01 00:00:00") && ParticiperDS.get_entree_participer(entree.get_idGroupe(), entree.get_idUtilisateur(),liste_entrees).get_dateEntre().equals("1900-01-01 00:00:00")))
							{
								String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_utilisateur");
								if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 1));
							}
							ParticiperDS.updateParticiper(entree);
						}
						else if (status==2)
						{

							if (!ParticiperDS.hasAlreadySaved(entree, liste_entrees))
							{
								//Log.e("status entry", "yes1 ; "+status);

								if ((entree.get_idUtilisateur()==phone_user.getIdUtilisateur()))
								{
									String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_groupe");
									//Log.e("from id_groupe", "yes ; "+pseudo);
									if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 2));
								}

								if ((entree.get_idGroupe()==phone_user.getIdGroupe()))
								{
									String pseudo=get_pseudo(liste_des_utilisateurs, entree, "from id_utilisateur");
									//Log.e("from id_utilisateur", "yes ; "+pseudo);
									if (pseudo!=null) liste_notifiants.add(new notifiant(pseudo, 2));
								}

								//Log.e("nothing worked", "yes");
								ParticiperDS.createParticiper(entree);

							}

							//	Log.e("status entry", "yes2 ; "+status);
						}
					}
				}
			}
			afficher_notification(liste_notifiants, "participer");
		}

		if (action.equals("infosdulieu"))
		{
			List<Infosdulieu> liste_entrees= InfosduLieuDS.getAllEntrees();
			List<Utilisateur> liste_des_utilisateurs=UtilisateurDS.getAllUtilisateurs();
			List<notifiant> liste_notifiants= new ArrayList<GroovieService.notifiant>();

			int mode_update=((liste_entrees.size()==0) ? 0 : 1);

			int listeSize=liste_resultats.size();

			//Log.e("tag_infosdulieu", liste_entrees.size()+" "+listeSize);

			//permet de remplir la base de données locale des données de participation aux groupes au premier démarrage (condition mode_update==0)
			if (mode_update==0)
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					Infosdulieu entree=null;
					try {
						entree = new Infosdulieu(jObject.getInt("idUtilisateur"), jObject.getInt("idLieu"), jObject.getString("dateModification"), jObject.getInt("prixmodifie"), jObject.getInt("modifiedby"),0);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InfosduLieuDS.createInfosdulieu(entree);
				}
			}
			//permet de remplir la base de données locale des données de participation aux groupes lors de démarrages suivant le premier démarrage (condition mode_update==1)
			else
			{
				for (int i=0;i<listeSize;i++)
				{
					JSONObject jObject=(JSONObject) liste_resultats.get(i);
					int status=0;
					try {
						status=jObject.getInt("status");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//permet de supprimer les entrées non valables dans la base de données locale
					if(status==0)
					{
						int numeroEnregistrement=0;
						try {
							//récupération de l'identifiant de la ligne à supprimer
							numeroEnregistrement=jObject.getInt("numeroEnregistrement");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//suppression de la ligne concernée
						for (int j=0;j<liste_entrees.size();j++)
						{
							Infosdulieu entree=liste_entrees.get(j);
							String value=String.valueOf(entree.get_idUtilisateur())+String.valueOf(entree.get_idLieu());
							if (value.equals(String.valueOf(numeroEnregistrement)))
							{
								InfosduLieuDS.deleteInfosduLieu(entree);
							}
						}
					}
					else 
					{
						//récupération de l'entree infosdulieu mise à jour 
						Infosdulieu entree=null;
						try {
							entree = new Infosdulieu(jObject.getInt("idUtilisateur"), jObject.getInt("idLieu"), jObject.getString("dateModification"), jObject.getInt("prixmodifie"), jObject.getInt("modifiedby"),0);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (status==1)
						{
							if (entree.get_idUtilisateur()==phone_user.getIdUtilisateur())
								liste_notifiants.add(new notifiant(get_pseudo(liste_des_utilisateurs, entree), 1));

							//je ne modifie le status de la notification que si le prix change
							entree.set_status(InfosduLieuDS.get_entree_InfosduLieu(entree.get_idUtilisateur(), entree.get_idLieu(), liste_entrees).get_status());

							InfosduLieuDS.updateInfosduLieu(entree);
						}
						else if (status==2)
						{
							if (!InfosduLieuDS.hasAlreadySaved(entree, liste_entrees))
							{
								if ((entree.get_idUtilisateur()==phone_user.getIdUtilisateur()))
									liste_notifiants.add(new notifiant(get_pseudo(liste_des_utilisateurs, entree), 2));
								InfosduLieuDS.createInfosdulieu(entree);
							}
						}
					}
				}
			}
			afficher_notification(liste_notifiants, "infosdulieu");
		}
	}

	private void afficher_notification(List<notifiant> liste_notifiants, String cas)
	{
		if(liste_notifiants.size()!=0)
		{
			if (cas.equals("participer"))
			{
				List<String> liste_pseudos= new ArrayList<String>();
				String message= new String();
				// case action==0:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 0);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "n'est plus votre ami" : "ne sont plus vos amis")+"!";
					executer_notification(liste_pseudos.size(), message,"groupe");
				}

				// case action==1:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 1);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "a modifié le status d'amitié vous concernant" : "ont modifié le status d'amitié vous concernant")+"!";
					executer_notification(liste_pseudos.size(), message,"groupe");
				}

				// case action==2:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 2);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "vous veut comme ami" : "vous veulent comme amis")+"!";
					executer_notification(liste_pseudos.size(), message,"groupe");
				}
			}
			if (cas.equals("ajouterlieu"))
			{
				List<String> liste_pseudos= new ArrayList<String>();
				String message= new String();
				// case action==0:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 0);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "a supprimé lieu de ses favoris" : "ont supprimé des lieux de leurs favoris")+"!";
					executer_notification(liste_pseudos.size(), message,"lieu");
				}

				// case action==2:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 2);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "a ajouté un nouveau lieu à ses favoris" : "ont ajouté de nouveaux lieux à leurs favoris")+"!";
					executer_notification(liste_pseudos.size(), message,"lieu");
				}
			}
			if (cas.equals("infosdulieu"))
			{
				List<String> liste_pseudos= new ArrayList<String>();
				String message= new String();

				// case action==1:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 1);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "a modifié le prix d'un lieu" : "ont modifié le prix de lieux")+"!";
					executer_notification(liste_pseudos.size(), message,"lieu");
				}

				// case action==2:
				liste_pseudos= selectionner_entrees_notifiants(liste_notifiants, 2);
				if (liste_pseudos.size()!=0)
				{
					message= listeString_to_String(liste_pseudos)+" "+((liste_pseudos.size()==1) ? "a modifié le prix d'un lieu" : "ont modifié le prix de lieux")+"!";
					executer_notification(liste_pseudos.size(), message,"lieu");
				}
			}

		}
	}

	private void executer_notification(int nombre_notifications,String message, String categorie_notification)
	{
		int icon= R.drawable.groovie_logo;
		CharSequence tickerText="Groovie Notifications!";
		long when= System.currentTimeMillis();
		Notification notification_inscription= new Notification(icon, tickerText, when);
		notification_inscription.flags=Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent=null;
		if (categorie_notification.equals("lieu"))
		{
			notificationIntent= new Intent(GroovieService.this, AccueilActivity.class);
		}
		else if (categorie_notification.equals("groupe"))
		{
			notificationIntent= new Intent(GroovieService.this, GroupeEtUtilisateursActivity.class);
		}
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent= PendingIntent.getActivity(this, 0,notificationIntent, 0);

		notification_inscription.setLatestEventInfo(GroovieService.this, nombre_notifications+" Nouvelle(s) notification(s)", message, contentIntent);
		//notification_inscription.defaults|=Notification.DEFAULT_SOUND;
		//notification_inscription.defaults|=Notification.DEFAULT_VIBRATE;
		//notification_inscription.defaults|=Notification.DEFAULT_LIGHTS;

		// permet de gerer les vibrations du téléphone 
		if (categorie_notification.equals("lieu"))
		{
			if (groovie_preferences.getBoolean("vibreur_notifs_lieu_key",true)==true)
				notification_inscription.defaults|=Notification.DEFAULT_VIBRATE;
			//myVibrator.vibrate(pattern,-1);
		}
		else if (categorie_notification.equals("groupe"))
		{
			if (groovie_preferences.getBoolean("vibreur_notifs_groupe_key",true)==true)
				notification_inscription.defaults|=Notification.DEFAULT_VIBRATE;
			//myVibrator.vibrate(pattern,-1);
		}

		// permet de gerer la couleur du led de notification
		notification_inscription.ledARGB=0xff00ff00;

		if (categorie_notification.equals("lieu"))
		{
			if (groovie_preferences.getString("voyant_notifs_lieu_key","").equals("Bleu"))
				notification_inscription.ledARGB=Color.BLUE;
			if (groovie_preferences.getString("voyant_notifs_lieu_key","").equals("Rouge"))
				notification_inscription.ledARGB=Color.RED;
			if (groovie_preferences.getString("voyant_notifs_lieu_key","").equals("Vert"))
				notification_inscription.ledARGB=Color.GREEN;
		}
		else if (categorie_notification.equals("groupe"))
		{
			if (groovie_preferences.getString("voyant_notifs_groupe_key","").equals("Bleu"))
				notification_inscription.ledARGB=Color.BLUE;
			if (groovie_preferences.getString("voyant_notifs_groupe_key","").equals("Rouge"))
				notification_inscription.ledARGB=Color.RED;
			if (groovie_preferences.getString("voyant_notifs_groupe_key","").equals("Vert"))
				notification_inscription.ledARGB=Color.GREEN;
		}

		notification_inscription.ledOnMS=300;
		notification_inscription.ledOffMS=1000;
		notification_inscription.flags|=Notification.FLAG_SHOW_LIGHTS;

		NotificationManager notification_manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification_manager.notify(ID_NOTIFICATION, notification_inscription);

		//ce code permet de faire jouer la sonnerie de notification
		if (categorie_notification.equals("lieu"))
		{
			String alarm= groovie_preferences.getString("sonnerie_notifs_lieu_key","");
			if (!alarm.equals(""))
			{
				Uri uri=Uri.parse(alarm);
				Ringtone r= RingtoneManager.getRingtone(this,uri);
				r.play();
			}
		}
		else if (categorie_notification.equals("groupe"))
		{
			String alarm= groovie_preferences.getString("sonnerie_notifs_groupe_key","");
			if (!alarm.equals(""))
			{
				Uri uri=Uri.parse(alarm);
				Ringtone r= RingtoneManager.getRingtone(this,uri);
				r.play();
			}
		}

		//j'allume l'ecran lorsque le terminal recoit une notification 
		mywakeLock.acquire();
	}
	private void mettre_a_jour_BD()
	{
		get_les_lieux_ajoutes();
		get_entrees_participer();
		get_InfosduLieu();
	}

	private void get_InfosduLieu()
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true)
				{

					String result="";
					InputStream is=null;
					int mode_update=((InfosduLieuDS.getNombreEntrees()==0) ? 0 : 1);

					//Envoi de la commande http
					try{
						ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
						nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));
						nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));

						HttpClient httpClient=new DefaultHttpClient();
						HttpPost httpPost=new HttpPost(InfosduLieu_file);
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
						// e.printStackTrace();
					}

					// Parse  les données JSON
					//Log.e("tag_infosdulieu", result);
					try{
						JSONArray jArray = new JSONArray(result);
						final List<Object> l=new ArrayList<Object>();
						int i;
						for (i=0;i<jArray.length();i++)
						{
							l.add(jArray.getJSONObject(i));

						}
						Message msg=mhandler.obtainMessage();
						msg.obj=l;
						msg.arg1=2;
						mhandler.sendMessage(msg);
					}catch(JSONException e){
						Log.e("log_tag", "Error parsing data " + e.toString());
						//e.printStackTrace();
					}
					try {
						Thread.sleep(DUREE_VERIFICATION);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}).start();
	}

	private void get_les_lieux_ajoutes()
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true)
				{

					String result="";
					InputStream is=null;
					int mode_update=((AjouterLieuDS.getNombreEntrees()==0) ? 0 : 1);

					//Envoi de la commande http
					try{
						ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
						nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
						nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

						HttpClient httpClient=new DefaultHttpClient();
						HttpPost httpPost=new HttpPost(les_lieux_ajoutes_file);
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
						// e.printStackTrace();
					}

					// Parse  les données JSON
					//Log.e("tag_ajouterlieu", result);
					try{
						JSONArray jArray = new JSONArray(result);
						final List<Object> l=new ArrayList<Object>();
						int i;
						for (i=0;i<jArray.length();i++)
						{
							l.add(jArray.getJSONObject(i));

						}
						Message msg=mhandler.obtainMessage();
						msg.obj=l;
						msg.arg1=0;
						mhandler.sendMessage(msg);
					}catch(JSONException e){
						Log.e("log_tag", "Error parsing data " + e.toString());
						//e.printStackTrace();
					}
					try {
						Thread.sleep(DUREE_VERIFICATION);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}).start();
	}



	private void get_entrees_participer()
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true)
				{
					String result="";
					InputStream is=null;
					int mode_update=((ParticiperDS.getNombreEntrees()==0) ? 0 : 1);

					//Envoi de la commande http
					try{
						ArrayList<NameValuePair> nameValuePair= new ArrayList<NameValuePair>();
						nameValuePair.add(new BasicNameValuePair("mode_update", String.valueOf(mode_update)));
						nameValuePair.add(new BasicNameValuePair("idUtilisateur", String.valueOf(phone_user.getIdUtilisateur())));

						HttpClient httpClient=new DefaultHttpClient();
						HttpPost httpPost=new HttpPost(liste_entrees_participer_file);
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
					//Log.e("tag_participer", phone_user.getIdUtilisateur()+" ; "+mode_update+ " ; "+result);
					try{
						JSONArray jArray = new JSONArray(result);
						final List<Object> l=new ArrayList<Object>();
						int i;
						for (i=0;i<jArray.length();i++)
						{
							l.add(jArray.getJSONObject(i));
						}

						Message msg=mhandler.obtainMessage();
						msg.obj=l;
						msg.arg1=1;
						mhandler.sendMessage(msg);
					}catch(JSONException e){
						Log.e("log_tag", "Error parsing data " + e.toString());
					}
					try {
						Thread.sleep(DUREE_VERIFICATION);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private List<String> selectionner_entrees_notifiants(List<notifiant> liste,int action)
	{
		List<String> result= new ArrayList<String>();
		for (int i=0;i<liste.size();i++)
		{
			if (liste.get(i).get_action()==action)
				result.add(liste.get(i).get_pseudo());
		}
		return result;
	}
	public class notifiant
	{
		private String pseudo;
		private int action;

		public notifiant(String pseudo, int action)
		{
			this.pseudo=pseudo;
			this.action=action;
		}
		public String get_pseudo()
		{return this.pseudo;}

		public int get_action()
		{return this.action;}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.onStartCommand(intent, flags, startId);
	}
}
