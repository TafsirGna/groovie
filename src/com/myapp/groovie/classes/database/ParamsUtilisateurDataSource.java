package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ParamsUtilisateurDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_PARAMS,MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.PERIODE,MySQLiteHelper.VISIBILITEPHOTO,
			MySQLiteHelper.VISIBILITECOORDONNEES, MySQLiteHelper.VISIBILITESTATISTIQUES};

	public ParamsUtilisateurDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createParamsUtilisateur(ParamsUtilisateur entree) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_PARAMS, entree.get_idParams());
		values.put(MySQLiteHelper.ID_UTILISATEUR, entree.get_idUtilisateur());
		values.put(MySQLiteHelper.PERIODE, entree.get_periode());
		values.put(MySQLiteHelper.VISIBILITEPHOTO, entree.get_visibilitePhoto());
		values.put(MySQLiteHelper.VISIBILITECOORDONNEES, entree.get_visibiliteCoordonnees());
		values.put(MySQLiteHelper.VISIBILITESTATISTIQUES, entree.get_visibiliteStatistiques());
		long insertParamsUtilisateur= database.insert(MySQLiteHelper.TABLE_PARAMS, null,
				values);
		return insertParamsUtilisateur;
	}

	public void deleteParamsUtilisateur(ParamsUtilisateur entree) {
		int idParams=entree.get_idParams();
		database.delete(MySQLiteHelper.TABLE_PARAMS, MySQLiteHelper.ID_PARAMS
				+ " = " + idParams, null);
	}

	public int getNombreEntrees(){
		final List<ParamsUtilisateur> values= this.getAllEntrees();
		return values.size();
	}

	public void updateParamsUtilisateur(ParamsUtilisateur entree){
		String req="update "+MySQLiteHelper.TABLE_PARAMS+" set "+MySQLiteHelper.PERIODE+" = ? , "+MySQLiteHelper.VISIBILITEPHOTO+" = ? , "+MySQLiteHelper.VISIBILITECOORDONNEES+" = ? , "+MySQLiteHelper.VISIBILITESTATISTIQUES+" = ?"+
				"where "+MySQLiteHelper.ID_PARAMS+" = ? ";
		database.execSQL(req, new String []{String.valueOf(entree.get_periode()), String.valueOf(entree.get_visibilitePhoto()), String.valueOf(entree.get_visibiliteCoordonnees()), String.valueOf(entree.get_visibiliteStatistiques()), String.valueOf(entree.get_idParams())});
	}

	public List<ParamsUtilisateur> getAllEntrees() {
		List<ParamsUtilisateur> Entrees = new ArrayList<ParamsUtilisateur>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_PARAMS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ParamsUtilisateur entree= cursorToEntree(cursor);
			Entrees.add(entree);
			//Log.e("allparam",entree.get_idParams()+"");
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return Entrees;
	}

	private ParamsUtilisateur cursorToEntree(Cursor cursor) {
		ParamsUtilisateur entree = new ParamsUtilisateur();
		entree.set_idParams(cursor.getInt(0));
		entree.set_idUtilisateur(cursor.getInt(1));
		entree.set_periode(cursor.getInt(2));
		entree.set_visibilitePhoto(cursor.getInt(3));
		entree.set_visibiliteCoordonnees(cursor.getInt(4));
		entree.set_visibiliteStatistiques(cursor.getInt(5));
		return entree;
	}

	public boolean hasAlreadySaved(ParamsUtilisateur entree, List<ParamsUtilisateur> liste)
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
		List<ParamsUtilisateur> listeEntrees= getAllEntrees();
		for (int i=0;i<listeEntrees.size();i++)
		{
			ParamsUtilisateur entree= listeEntrees.get(i);
			deleteParamsUtilisateur(entree);
		}
		//Log.e("sup param", "yes");
	}
	
	public ParamsUtilisateur get_paramsUtilisateur(int idParams,List<ParamsUtilisateur> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).get_idParams()==idParams)
			{
				return liste.get(i);
			}
		}
		return null;
	}
}
