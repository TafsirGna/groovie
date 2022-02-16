package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class InfosdulieuDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.ID_LIEU,MySQLiteHelper.DATE_MODIFICATION,
			MySQLiteHelper.PRIXMODIFIE, MySQLiteHelper.MODIFIED_BY,MySQLiteHelper.VU};

	public InfosdulieuDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createInfosdulieu( Infosdulieu entree) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_UTILISATEUR, entree.get_idUtilisateur());
		values.put(MySQLiteHelper.ID_LIEU, entree.get_idLieu());
		values.put(MySQLiteHelper.DATE_MODIFICATION, entree.get_dateModification());
		values.put(MySQLiteHelper.PRIXMODIFIE, entree.get_prixmodifie());
		values.put(MySQLiteHelper.MODIFIED_BY, entree.get_modified_by());
		values.put(MySQLiteHelper.VU, entree.get_status());
		long insertInfosduLieu = database.insert(MySQLiteHelper.TABLE_INFOSDULIEU, null,
				values);
		return insertInfosduLieu;
	}

	public void deleteInfosduLieu(Infosdulieu entree)
	{
		String req="delete " +
				" from "+MySQLiteHelper.TABLE_INFOSDULIEU+
				" where "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_LIEU+" = ?";
		database.execSQL(req, new String []{String.valueOf(entree.get_idUtilisateur()), String.valueOf(entree.get_idLieu())});

	}

	public int getNombreEntrees(){
		final List<Infosdulieu> values= getAllEntrees();
		return values.size();
	}
	public void updateInfosduLieu(Infosdulieu entree){

		String req="update "+MySQLiteHelper.TABLE_INFOSDULIEU+" set "+MySQLiteHelper.DATE_MODIFICATION+" = ? ," + MySQLiteHelper.PRIXMODIFIE+" = ? ,"+MySQLiteHelper.MODIFIED_BY+" = ?, "+MySQLiteHelper.VU+" = ?"+
				" where "+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.ID_LIEU+" = ?";
		database.execSQL(req, new String []{entree.get_dateModification(), String.valueOf(entree.get_prixmodifie()),String.valueOf(entree.get_modified_by()),String.valueOf(entree.get_status()),String.valueOf(entree.get_idUtilisateur()), String.valueOf(entree.get_idLieu())});
	}

	public List<Infosdulieu> getAllEntrees() {
		List<Infosdulieu> Entrees = new ArrayList<Infosdulieu>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_INFOSDULIEU,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Infosdulieu entree= cursorToEntree(cursor);
			Entrees.add(entree);
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return Entrees;
	}

	private Infosdulieu cursorToEntree(Cursor cursor) {
		Infosdulieu entree = new Infosdulieu();
		entree.set_idUtilisateur(cursor.getInt(0));
		entree.set_idLieu(cursor.getInt(1));
		entree.set_dateModification(cursor.getString(2));
		entree.set_prixmodifie(cursor.getInt(3));
		entree.set_modified_by(cursor.getInt(4));
		entree.set_status(cursor.getInt(5));
		return entree;
	}

	public Infosdulieu get_entree_InfosduLieu(int id_utilisateur,int id_lieu,List<Infosdulieu> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).get_idLieu()==id_lieu && liste.get(i).get_idUtilisateur()==id_utilisateur)
			{
				return liste.get(i);
			}
		}
		return null;
	}

	public boolean hasAlreadySaved(Infosdulieu entree, List<Infosdulieu> liste)
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
		List<Infosdulieu> listeEntrees= getAllEntrees();
		for (int i=0;i<listeEntrees.size();i++)
		{
			Infosdulieu entree= listeEntrees.get(i);
			deleteInfosduLieu(entree);
		}
		Log.e("sup info", "yes");
	}

	public Infosdulieu get_entree_reference(int id_lieu)
	{
		Infosdulieu resultat=null;
		String req="select "+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.DATE_MODIFICATION+","+MySQLiteHelper.PRIXMODIFIE+","+MySQLiteHelper.VU+","+MySQLiteHelper.MODIFIED_BY+
				" from "+MySQLiteHelper.TABLE_INFOSDULIEU+
				" where "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_LIEU+" = ? and "+MySQLiteHelper.DATE_MODIFICATION+">=(select max("+MySQLiteHelper.DATE_MODIFICATION+") from "+MySQLiteHelper.TABLE_INFOSDULIEU+" where "+MySQLiteHelper.ID_LIEU+" = ?)";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_lieu),String.valueOf(id_lieu)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Infosdulieu entree= new Infosdulieu(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getInt(5), c.getInt(4));
			resultat=entree;
			c.moveToNext();
		}
		return resultat;
	}

	public Cursor get_liste_updaters(int id_lieu)
	{
		String req="select "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PSEUDO+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.EMAIL+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.TELEPHONE+","+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.DATE_MODIFICATION+","+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.PRIXMODIFIE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_PARAMS+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PHOTO+
				" from "+MySQLiteHelper.TABLE_INFOSDULIEU+","+MySQLiteHelper.TABLE_UTILISATEUR+
				" where "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_UTILISATEUR+" = "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR
				+" and "+MySQLiteHelper.TABLE_INFOSDULIEU+"."+MySQLiteHelper.ID_LIEU+" = ? order by "+MySQLiteHelper.DATE_MODIFICATION+" desc";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_lieu)});
		return c;
	}

	public Infosdulieu get_entree_infosdulieu_groupe(int id_utilisateur)
	{
		Infosdulieu resultat=null;
		String req="select "+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.ID_LIEU+","+MySQLiteHelper.DATE_MODIFICATION+","+MySQLiteHelper.PRIXMODIFIE+","+MySQLiteHelper.VU+","+MySQLiteHelper.MODIFIED_BY+
				" from "+MySQLiteHelper.TABLE_INFOSDULIEU+
				" where "+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.PRIXMODIFIE+" <= (select min("+MySQLiteHelper.PRIXMODIFIE+") from "+MySQLiteHelper.TABLE_INFOSDULIEU+" where "+MySQLiteHelper.ID_UTILISATEUR+" = ?)";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur),String.valueOf(id_utilisateur)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Infosdulieu entree= new Infosdulieu(c.getInt(0), c.getInt(1), c.getString(2), c.getInt(3), c.getInt(5), c.getInt(4));
			resultat=entree;
			c.moveToNext();
		}
		return resultat;
	}


}
