package com.myapp.groovie.classes.database;

public class Infosdulieu {

	private int id_utilisateur;
	private int id_lieu;
	private String dateModification;
	private int prixmodifie;
	private int modified_by;
	private int status;
	
	public Infosdulieu()
	{super();}
	public Infosdulieu(int id_utilisateur, int id_lieu, String dateModification, int prixmodifie, int modified_by, int status)
	{
		this.id_utilisateur=id_utilisateur;
		this.id_lieu=id_lieu;
		this.dateModification=dateModification;
		this.prixmodifie=prixmodifie;
		this.modified_by=modified_by;
		this.status=status;
	}
	public void set_idUtilisateur(int id_utilisateur)
	{
		this.id_utilisateur=id_utilisateur;
	}
	public void set_idLieu(int id_lieu)
	{
		this.id_lieu=id_lieu;
	}
	public void set_dateModification(String dateModification)
	{
		this.dateModification=dateModification;
	}
	public void set_status(int status)
	{
		this.status=status;
	}
	public void set_prixmodifie(int prixmodifie)
	{
		this.prixmodifie=prixmodifie;
	}
	public void set_modified_by(int id)
	{
		this.modified_by=id;
	}
	public int get_idUtilisateur()
	{
		return this.id_utilisateur;
	}
	public int get_idLieu()
	{
		return this.id_lieu;
	}
	public String get_dateModification()
	{
		return this.dateModification;
	}
	public int get_prixmodifie()
	{
		return this.prixmodifie;
	}
	public int get_modified_by()
	{
		return this.modified_by;
	}
	public int get_status()
	{
		return this.status;
	}
	public boolean matches(Infosdulieu infos)
	{
		if (infos.get_idLieu()==this.get_idLieu() && infos.get_idUtilisateur()==this.id_utilisateur)
		{
			return true;
		}
		return false;
	}
}
