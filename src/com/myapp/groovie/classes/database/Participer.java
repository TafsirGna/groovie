package com.myapp.groovie.classes.database;

public class Participer {

	private int idUtilisateur;
	private int idGroupe;
	private String dateEntre;
	private String dateInvitation;
	private String dateDemande;
	private float note;
	private int code_vu;
	
	public Participer()
	{
		super();
	}
	public Participer( int idGroupe,int idUtilisateur, String dateEntre, String dateInvitation, String dateDemande, float note, int code_vu)
	{
		this.idUtilisateur=idUtilisateur;
		this.idGroupe=idGroupe;
		this.dateEntre=dateEntre;
		this.dateInvitation=dateInvitation;
		this.dateDemande=dateDemande;
		this.note=note;
		this.code_vu=code_vu;
	}
	
	public void set_idUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	public int get_idUtilisateur()
	{
		return this.idUtilisateur;
	}
	public void set_idGroupe(int idGroupe)
	{
		this.idGroupe=idGroupe;
	}
	public int get_idGroupe()
	{
		return this.idGroupe;
	}
	public void set_dateEntre(String dateEntre)
	{
		this.dateEntre=dateEntre;
	}
	public String get_dateEntre()
	{
		return this.dateEntre;
	}
	public String get_dateInvitation()
	{
		return this.dateInvitation;
	}
	public void set_dateInvitation(String dateInvitation)
	{
		this.dateInvitation=dateInvitation;
	}
	public void set_dateDemande(String dateDemande)
	{
		this.dateDemande=dateDemande;
	}
	public String get_dateDemande()
	{
		return this.dateDemande;
	}
	public void set_note(float note)
	{
		this.note=note;
	}
	public void set_codeVu(int code)
	{
		this.code_vu=code;
	}
	public int get_codeVu()
	{
		return this.code_vu;
	}
	public float get_note()
	{
		return this.note;
	}
	public boolean matches(Participer entree)
	{
		if (entree.get_idUtilisateur()==this.idUtilisateur && entree.get_idGroupe()==this.idGroupe)
		{
			return true;
		}
		return false;
	}
}
