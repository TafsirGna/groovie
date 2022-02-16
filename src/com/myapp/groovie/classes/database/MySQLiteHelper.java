package com.myapp.groovie.classes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MySQLiteHelper extends SQLiteOpenHelper{

	//Je déclare ici une variable statique qui me servira dans mes intents

	public static final String TABLE_UTILISATEUR = "Utilisateur";
	public static final String ID_UTILISATEUR="IdUtilisateur";
	public static final String	PSEUDO = "pseudo";
	public static final String	EMAIL = "email";
	public static final String TELEPHONE = "telephone";
	public static final String PHOTO = "photo";
	public static final String IDDEVICE = "idDevice";
	public static final String PASSWORD = "password";
	public static final String CLE = "cleActivation";
	public static final String ACTIF = "actif";

	public static final String TABLE_LIEU = "Lieu";
	public static final String ID_LIEU="IdLieu";
	public static final String	TITRE = "titre";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	public static final String DATECREATION = "dateCreation";
	public static final String PICTURE = "picture";

	public static final String TABLE_AJOUTER_LIEU = "Ajouterlieu";
	public static final String DATEAJOUTLIEU="dateAjout";

	public static final String TABLE_GROUPE = "Groupe";
	public static final String ID_GROUPE="IdGroupe";

	public static final String TABLE_PARTICIPER = "Participer";
	public static final String DATEENTRE="dateEntre";
	public static final String	DATEDEMANDE = "dateDemande";
	public static final String	DATEINVITATION = "dateInvitation";
	public static final String	NOTE = "note";
	public static final String	VU = "vu";

	public static final String TABLE_DEPARTEMENT = "Departement";
	public static final String ID_DEPARTEMENT="idDepartement";
	public static final String LIBELLEDEPARTEMENT="libelleDepartement";

	public static final String TABLE_CONSOMMATION = "Consommation";
	public static final String ID_CONSOMMATION="idConsommation";
	public static final String DATE_CONSOMMATION="dateConsommation";
	public static final String QUANTITE_CONSOMMATION = "quantite";
	public static final String COUT_CONSOMMATION = "cout";
	public static final String CODE_REINITIALISATIION = "codeReinitialisation";
	
	public static final String TABLE_INFOSDULIEU = "Infosdugroupe";
	public static final String PRIXMODIFIE = "prixGroupe";
	public static final String DATE_MODIFICATION="dateModification";
	public static final String MODIFIED_BY="modifiedby";
	
	public static final String TABLE_PARAMS = "paramsutilisateur";
	public static final String ID_PARAMS = "idParams";
	public static final String PERIODE="periode";
	public static final String VISIBILITECOORDONNEES="visibilitecoordonnees";
	public static final String VISIBILITEPHOTO="visibilitephoto";
	public static final String VISIBILITESTATISTIQUES="visibilitestatistiques";
	
	private static final String DATABASE_NAME = "groovie.db";
	private static final int DATABASE_VERSION = 1;

	// Commande sql pour la création de la base de données
	private static final String CREATE_TABLE_UTILISATEUR = "create table "
			+ TABLE_UTILISATEUR + "( " + ID_UTILISATEUR
			+ " integer primary key, "+ID_DEPARTEMENT
			+" integer, "+ID_GROUPE+" integer, " +ID_PARAMS+" integer, " + PSEUDO
			+ " text, "+EMAIL+" text, "
			+TELEPHONE+" text, "+PHOTO+" blob, "
			+IDDEVICE+" text, "+PASSWORD+" text, "+CLE+" text, "+ACTIF+" integer);";

	private static final String CREATE_TABLE_LIEU = "create table "
			+ TABLE_LIEU + "( " + ID_LIEU
			+ " integer primary key, "+ID_UTILISATEUR
			+" integer, "+ID_DEPARTEMENT+" integer, " + TITRE
			+ " text, "+LONGITUDE+" real, "
			+LATITUDE+" real, "
			+DATECREATION+" text, "+PICTURE+" blob);";


	private static final String CREATE_TABLE_DEPARTEMENT = "create table "
			+ TABLE_DEPARTEMENT + "( " + ID_DEPARTEMENT
			+ " integer primary key, " + LIBELLEDEPARTEMENT
			+ " text);";

	private static final String CREATE_TABLE_AJOUTER_LIEU = "create table "
			+ TABLE_AJOUTER_LIEU + "( " + ID_UTILISATEUR
			+" integer not null, "+ID_LIEU+" integer not null, " + DATEAJOUTLIEU
			+ " text);";
	/*, "
	+ "constraint pk_ajouter primary key ("+ID_UTILISATEUR+","+ID_LIEU+")
	 */
	private static final String CREATE_TABLE_GROUPE = "create table "
			+ TABLE_GROUPE + "( " + ID_GROUPE
			+ " integer primary key, " + ID_UTILISATEUR
			+ " integer);";

	private static final String CREATE_TABLE_PARTICIPER = "create table "
			+ TABLE_PARTICIPER + "( " + ID_UTILISATEUR
			+" integer not null, "+ID_GROUPE+" integer not null, " + DATEENTRE
			+ " text, "+DATEINVITATION
			+" text, "+DATEDEMANDE
			+" text," +NOTE+ " real,"+VU+" integer);";
	
	private static final String CREATE_TABLE_CONSOMMATION= " create table "
			+ TABLE_CONSOMMATION+ " ( "+ ID_UTILISATEUR
			+ " integer not null, "+ ID_LIEU + " integer not null, " + ID_CONSOMMATION
			+" integer not null, "+ DATE_CONSOMMATION+ " text, "+ QUANTITE_CONSOMMATION+ " integer, "
			+ COUT_CONSOMMATION + " real, "+CODE_REINITIALISATIION+" integer );";

	private static final String CREATE_TABLE_INFOSDULIEU= " create table "
			+ TABLE_INFOSDULIEU+ " ( "+ ID_UTILISATEUR
			+ " integer not null, "+ ID_LIEU + " integer not null, " + DATE_MODIFICATION
			+" text, "+ PRIXMODIFIE+ " integer, "+MODIFIED_BY+" text,"+VU+" integer);";
	
	private static final String CREATE_TABLE_PARAMS = "create table "
			+ TABLE_PARAMS + "( " + ID_PARAMS
			+" integer not null, "+ID_UTILISATEUR+" integer not null, " + PERIODE
			+ " integer, "+VISIBILITEPHOTO
			+" integer, "+VISIBILITECOORDONNEES
			+" integer," +VISIBILITESTATISTIQUES
			+" integer);";
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_UTILISATEUR);
		database.execSQL(CREATE_TABLE_LIEU);
		database.execSQL(CREATE_TABLE_AJOUTER_LIEU);
		database.execSQL(CREATE_TABLE_DEPARTEMENT);
		database.execSQL(CREATE_TABLE_GROUPE);
		database.execSQL(CREATE_TABLE_PARTICIPER);
		database.execSQL(CREATE_TABLE_CONSOMMATION);
		database.execSQL(CREATE_TABLE_INFOSDULIEU);
		database.execSQL(CREATE_TABLE_PARAMS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEUR);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIEU);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_AJOUTER_LIEU);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTEMENT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSOMMATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFOSDULIEU);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARAMS);
		onCreate(db);
	}

}
