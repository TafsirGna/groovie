package com.myapp.groovie.classes.database;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ParseException;
import android.util.Log;

public class ConsommationDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_CONSOMMATION,MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.ID_LIEU,
			MySQLiteHelper.DATE_CONSOMMATION,MySQLiteHelper.QUANTITE_CONSOMMATION,MySQLiteHelper.COUT_CONSOMMATION,MySQLiteHelper.CODE_REINITIALISATIION};

	public ConsommationDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createConsommation(Consommation entree) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_CONSOMMATION, entree.get_idConsommation());
		values.put(MySQLiteHelper.ID_UTILISATEUR, entree.get_idUtilisateur());
		values.put(MySQLiteHelper.ID_LIEU, entree.get_idLieu());
		values.put(MySQLiteHelper.DATE_CONSOMMATION, entree.get_dateConsommation());
		values.put(MySQLiteHelper.QUANTITE_CONSOMMATION, entree.get_quantite());
		values.put(MySQLiteHelper.COUT_CONSOMMATION, entree.get_cout());
		values.put(MySQLiteHelper.CODE_REINITIALISATIION, entree.getCodeReinitialisation());
		long insertConsommation = database.insert(MySQLiteHelper.TABLE_CONSOMMATION, null,
				values);
		//Log.e("i", String.valueOf(insertUtilisateur));
		return insertConsommation;
	}

	public void deleteConsommation(Consommation entree)
	{
		String req="delete " +
				" from "+MySQLiteHelper.TABLE_CONSOMMATION+
				" where "+MySQLiteHelper.TABLE_CONSOMMATION+"."+MySQLiteHelper.ID_CONSOMMATION+" = ? ";
		database.execSQL(req, new String []{String.valueOf(entree.get_idConsommation())});

	}

	public int getNombreEntrees(){
		final List<Consommation> values= getAllEntrees();
		return values.size();
	}
	public void updateConsommation(Consommation entree){

		String req="update "+MySQLiteHelper.TABLE_CONSOMMATION+" set "+MySQLiteHelper.DATE_CONSOMMATION+" = ? ," +MySQLiteHelper.QUANTITE_CONSOMMATION+" = ? ,"+MySQLiteHelper.COUT_CONSOMMATION+" = ? ,"+MySQLiteHelper.CODE_REINITIALISATIION+" = ? "+
				"where "+MySQLiteHelper.ID_CONSOMMATION+" = ? ";
		database.execSQL(req, new String []{entree.get_dateConsommation(), String.valueOf(entree.get_quantite()), String.valueOf(entree.get_cout()),String.valueOf(entree.getCodeReinitialisation()),String.valueOf(entree.get_idConsommation())});
	}

	public List<Consommation> getAllEntrees() {
		List<Consommation> Entrees = new ArrayList<Consommation>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_CONSOMMATION,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Consommation entree= cursorToEntree(cursor);
			Entrees.add(entree);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return Entrees;
	}

	private Consommation cursorToEntree(Cursor cursor) {
		Consommation entree = new Consommation();
		entree.set_idConsommation(cursor.getInt(0));
		entree.set_idUtilisateur(cursor.getInt(1));
		entree.set_idLieu(cursor.getInt(2));
		entree.set_dateConsommation(cursor.getString(3));
		entree.set_quantite(cursor.getInt(4));
		entree.set_cout(cursor.getFloat(5));
		entree.setCodeReinitialisation(cursor.getInt(6));
		return entree;
	}

	public boolean hasAlreadySaved(Consommation entree, List<Consommation> liste)
	{
		int size= liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).matches(entree))
			{
				return true;
			}
		}
		return false;
	}

	public void deleteAllEntrees()
	{
		List<Consommation> listeEntrees= getAllEntrees();
		for (int i=0;i<listeEntrees.size();i++)
		{
			Consommation entree= listeEntrees.get(i);
			deleteConsommation(entree);
		}
	}

	public Consommation get_entree_consommation(int idConso, List<Consommation> liste)
	{
		int size= liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).get_idConsommation()==idConso)
				return liste.get(i);
		}
		return null;
	}

	public Consommation get_derniere_consommation(int idUtilisateur)
	{
		Consommation resultat=null;
		String req="select "+MySQLiteHelper.ID_CONSOMMATION+","+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.DATE_CONSOMMATION+","+MySQLiteHelper.QUANTITE_CONSOMMATION+","+MySQLiteHelper.COUT_CONSOMMATION+","+MySQLiteHelper.CODE_REINITIALISATIION+
				" from "+MySQLiteHelper.TABLE_CONSOMMATION+
				" where "+MySQLiteHelper.TABLE_CONSOMMATION+"."+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.DATE_CONSOMMATION+">=(select max("+MySQLiteHelper.DATE_CONSOMMATION+") from "+MySQLiteHelper.TABLE_CONSOMMATION+" where "+MySQLiteHelper.ID_UTILISATEUR+" = ?)";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(idUtilisateur),String.valueOf(idUtilisateur)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Consommation entree= new Consommation(c.getInt(0), c.getInt(2), c.getInt(1), c.getString(3), c.getInt(4), c.getInt(5),c.getInt(6));
			resultat=entree;
			c.moveToNext();
		}
		return resultat;
	}

	public List<Consommation> get_details_conso_du_mois(int indice_mois)
	{
		//j'initialise l'expression régulière à vérifier sur la valeur de l'email
		String date= new SimpleDateFormat("yyyy",Locale.UK).format(new java.util.Date());
		Pattern model;
		if (indice_mois<10)
			model= Pattern.compile("^"+date+"(-){1}0"+String.valueOf(indice_mois)+"(-){1}[0-9]{2}.*$");
		else
			model= Pattern.compile("^"+date+"(-){1}"+String.valueOf(indice_mois)+"(-){1}[0-9]{2}.*$");

		List<Consommation> liste_resultats=new ArrayList<Consommation>();

		int resultat=0;
		String req=null; Cursor c=null;

		req="select "+MySQLiteHelper.ID_CONSOMMATION+","+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.DATE_CONSOMMATION+","+MySQLiteHelper.QUANTITE_CONSOMMATION+","+MySQLiteHelper.COUT_CONSOMMATION+","+MySQLiteHelper.CODE_REINITIALISATIION+
				" from "+MySQLiteHelper.TABLE_CONSOMMATION;
		//" where "+MySQLiteHelper.TABLE_CONSOMMATION+"."+MySQLiteHelper.ID_LIEU+" = ? ";

		c= database.rawQuery(req,null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Consommation entree= new Consommation(c.getInt(0), c.getInt(2), c.getInt(1), c.getString(3), c.getInt(4), c.getInt(5),c.getInt(6));

			Matcher match= model.matcher(entree.get_dateConsommation());
			if (match.find())
			{
				liste_resultats.add(entree);
			}
			c.moveToNext();
		}
		return liste_resultats;
	}

	public int get_consommation_du_mois(int idUtilisateur, int indice_mois, int parametre)
	{
		//j'initialise l'expression régulière à vérifier sur la valeur de l'email
		String date= new SimpleDateFormat("yyyy",Locale.UK).format(new java.util.Date());
		Pattern model;
		if (indice_mois<10)
			model= Pattern.compile("^"+date+"(-){1}0"+String.valueOf(indice_mois)+"(-){1}[0-9]{2}.*$");
		else
			model= Pattern.compile("^"+date+"(-){1}"+String.valueOf(indice_mois)+"(-){1}[0-9]{2}.*$");

		int resultat_quantite=0;
		int resultat_cout=0;
		String req=null; Cursor c=null;

		req="select "+MySQLiteHelper.ID_CONSOMMATION+","+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.DATE_CONSOMMATION+","+MySQLiteHelper.QUANTITE_CONSOMMATION+","+MySQLiteHelper.COUT_CONSOMMATION+","+MySQLiteHelper.CODE_REINITIALISATIION+
				" from "+MySQLiteHelper.TABLE_CONSOMMATION+
				" where "+MySQLiteHelper.TABLE_CONSOMMATION+"."+MySQLiteHelper.ID_UTILISATEUR+" = ? ";

		c= database.rawQuery(req, new String []{String.valueOf(idUtilisateur)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Consommation entree= new Consommation(c.getInt(0), c.getInt(2), c.getInt(1), c.getString(3), c.getInt(4), c.getInt(5),c.getInt(6));

			Matcher match= model.matcher(entree.get_dateConsommation());
			if (match.find() && entree.getCodeReinitialisation()==0)
			{
				resultat_quantite+=entree.get_quantite();
				resultat_cout+=entree.get_cout();
			}
			c.moveToNext();
		}
		if (parametre==0)
			return resultat_quantite;
		else
			return resultat_cout;
	}

}
