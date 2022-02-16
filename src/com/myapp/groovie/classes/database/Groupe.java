package com.myapp.groovie.classes.database;

public class Groupe {

	private int idGroupe;
	private int idUtilisateur;
	
	public Groupe()
	{ super(); }
	
	public Groupe (int idGroupe, int idUtilisateur)
	{
		this.idGroupe=idGroupe;
		this.idUtilisateur=idUtilisateur;
	}
	
	public void set_idGroupe(int idGroupe)
	{
		this.idGroupe=idGroupe;
	}
	public int get_idGroupe()
	{
		return this.idGroupe;
	}
	public void set_idUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	public int get_idUtilisateur()
	{
		return this.idUtilisateur;
	}
	public boolean matches(Groupe groupe)
	{
		if (groupe.get_idGroupe()==this.get_idGroupe())
		{
			return true;
		}
		return false;
	}
}
