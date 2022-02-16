package com.myapp.groovie.classes.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ParticiperDataSource extends DataSource{

	private String[] allColumns = {MySQLiteHelper.ID_GROUPE,MySQLiteHelper.ID_UTILISATEUR,MySQLiteHelper.DATEENTRE,MySQLiteHelper.DATEINVITATION,
			MySQLiteHelper.DATEDEMANDE, MySQLiteHelper.NOTE,MySQLiteHelper.VU};

	public ParticiperDataSource(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public long createParticiper(Participer entree) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ID_UTILISATEUR, entree.get_idUtilisateur());
		values.put(MySQLiteHelper.ID_GROUPE, entree.get_idGroupe());
		values.put(MySQLiteHelper.DATEENTRE, entree.get_dateEntre());
		values.put(MySQLiteHelper.DATEINVITATION, entree.get_dateInvitation());
		values.put(MySQLiteHelper.DATEDEMANDE, entree.get_dateDemande());
		values.put(MySQLiteHelper.NOTE, entree.get_note());
		values.put(MySQLiteHelper.VU, entree.get_codeVu());
		long insertParticiper= database.insert(MySQLiteHelper.TABLE_PARTICIPER, null,
				values);
		return insertParticiper;
	}

	public void deleteParticiper(Participer entree) {
		String req="delete from "+MySQLiteHelper.TABLE_PARTICIPER+" where "+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		database.execSQL(req, new String []{String.valueOf(entree.get_idGroupe()), String.valueOf(entree.get_idUtilisateur())});
	}

	public int getNombreEntrees(){
		final List<Participer> values= this.getAllEntrees();
		return values.size();
	}

	public void updateParticiper(Participer entree){
		Log.e("code avant", entree.get_codeVu()+"");
		String req="update "+MySQLiteHelper.TABLE_PARTICIPER+" set "+MySQLiteHelper.DATEENTRE+" = ? ,"+MySQLiteHelper.DATEDEMANDE+" = ?,"+MySQLiteHelper.DATEINVITATION+" = ?, "+MySQLiteHelper.NOTE+" = ?, "+MySQLiteHelper.VU+" = ?"+
				" where "+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		database.execSQL(req, new String []{entree.get_dateEntre(), entree.get_dateDemande(),entree.get_dateInvitation(),String.valueOf(entree.get_note()),String.valueOf(entree.get_codeVu()), String.valueOf(entree.get_idGroupe()), String.valueOf(entree.get_idUtilisateur())});
	}

	public List<Participer> getAllEntrees() {
		List<Participer> Entrees = new ArrayList<Participer>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_PARTICIPER,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Participer entree= cursorToEntree(cursor);
			Entrees.add(entree);
			//Log.e("participer entries", entree.get_idGroupe()+" ; "+entree.get_idUtilisateur()+" ; "+entree.get_dateEntre()+" ; "+entree.get_dateInvitation()+" ; "+entree.get_dateDemande());
			cursor.moveToNext();
		}
		// assurez-vous de la fermeture du curseur
		cursor.close();
		return Entrees;
	}

	private Participer cursorToEntree(Cursor cursor) {
		Participer entree = new Participer();
		entree.set_idGroupe(cursor.getInt(0));
		entree.set_idUtilisateur(cursor.getInt(1));
		entree.set_dateEntre(cursor.getString(2));
		entree.set_dateInvitation(cursor.getString(3));
		entree.set_dateDemande(cursor.getString(4));
		entree.set_note(cursor.getFloat(5));
		entree.set_codeVu(cursor.getInt(6));
		return entree;
	}

	public boolean hasAlreadySaved(Participer entree, List<Participer> liste)
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
		List<Participer> listeEntrees= getAllEntrees();
		for (int i=0;i<listeEntrees.size();i++)
		{
			Participer entree= listeEntrees.get(i);
			deleteParticiper(entree);
		}
		Log.e("sup particip", "yes");
	}

	public boolean isMyMember(int idGroupe, int idUtilisateur)
	{
		List<Participer> liste_entrees= getAllEntrees();
		int listeSize= liste_entrees.size();
		for(int i=0;i<listeSize;i++)
		{
			if (liste_entrees.get(i).get_idGroupe()==idGroupe && liste_entrees.get(i).get_idUtilisateur()==idUtilisateur)
			{
				if (!liste_entrees.get(i).get_dateDemande().equals(""))
					return true;
				else
					return false;
			}

		}
		return false;
	}

	public void delete_my_groupe_member(int idGroupe, int id_utilisateur)
	{
		String req="delete " +
				" from "+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		database.execSQL(req, new String []{String.valueOf(idGroupe), String.valueOf(id_utilisateur)});

	}
	public void delete_one_other_group(int idGroupe,int id_utilisateur)
	{
		String req="delete " +
				" from "+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+" = ?";
		database.execSQL(req, new String []{String.valueOf(idGroupe), String.valueOf(id_utilisateur)});
	}

	public Cursor get_liste_invitations_of(int id_utilisateur)
	{
		String req="select "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PSEUDO+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.EMAIL+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.TELEPHONE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PHOTO+","+ MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEINVITATION+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.NOTE+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.VU+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEDEMANDE+
				" from "+MySQLiteHelper.TABLE_UTILISATEUR+","+MySQLiteHelper.TABLE_PARTICIPER+","+MySQLiteHelper.TABLE_GROUPE+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+"="+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+" and "+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+"="+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+" and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.VU+" = 0";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_utilisateur),"1900-01-01 00:00:00"});
		return c;
	}
	public Cursor get_liste_demandes_of(int id_groupe)
	{
		String req="select "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_DEPARTEMENT+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PSEUDO+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.EMAIL+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.TELEPHONE+","+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.PHOTO+","+ MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEDEMANDE+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.NOTE+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.VU+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+","+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEINVITATION+
				" from "+MySQLiteHelper.TABLE_UTILISATEUR+","+MySQLiteHelper.TABLE_PARTICIPER+","+MySQLiteHelper.TABLE_GROUPE+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_UTILISATEUR+"="+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_UTILISATEUR+" and "+MySQLiteHelper.TABLE_UTILISATEUR+"."+MySQLiteHelper.ID_GROUPE+"="+MySQLiteHelper.TABLE_GROUPE+"."+MySQLiteHelper.ID_GROUPE+" and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.DATEENTRE+" = ? and "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.VU+" = 0";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_groupe),"1900-01-01 00:00:00"});
		return c;
	}
	public Participer get_entree_participer(int id_groupe, int id_utilisateur,List<Participer> liste)
	{
		int size=liste.size();
		for (int i=0;i<size;i++)
		{
			if (liste.get(i).get_idGroupe()==id_groupe && liste.get(i).get_idUtilisateur()==id_utilisateur)
			{
				return liste.get(i);
			}
		}
		return null;
	}

	public float get_note_user(int id_groupe)
	{
		float resultat=0;
		String req="select avg("+MySQLiteHelper.NOTE+")"+
				" from "+MySQLiteHelper.TABLE_PARTICIPER+
				" where "+MySQLiteHelper.TABLE_PARTICIPER+"."+MySQLiteHelper.ID_GROUPE+" = ? and "+MySQLiteHelper.DATEENTRE+" <> '1900-01-01 00:00:00'";
		Cursor c= database.rawQuery(req, new String []{String.valueOf(id_groupe)});
		c.moveToFirst();
		while (!c.isAfterLast()) {
			resultat=c.getFloat(0);
			c.moveToNext();
		}
		return resultat;
	}
}
