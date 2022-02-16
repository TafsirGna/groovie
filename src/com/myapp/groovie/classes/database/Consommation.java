package com.myapp.groovie.classes.database;

public class Consommation {

	private int id_consommation;
	private int id_lieu;
	private int id_utilisateur;
	private String dateConsommation;
	private int quantite;
	private float cout;
	private int code_reinitialisation;
	
	public Consommation()
	{
		super();
	}
	public Consommation(int idConsommation, int idLieu, int idUtilisateur, String dateConsommation, int quantite,float cout, int code_r)
	{
		this.id_consommation=idConsommation;
		this.id_lieu=idLieu;
		this.id_utilisateur=idUtilisateur;
		this.dateConsommation= dateConsommation;
		this.quantite=quantite;
		this.cout= cout;
		this.code_reinitialisation=code_r;
	}
	public void set_idConsommation(int idConsommation)
	{
		this.id_consommation=idConsommation;
	}
	public int get_idConsommation()
	{
		return this.id_consommation;
	}
	public int get_idLieu()
	{
		return this.id_lieu;
	}
	public void set_idLieu(int idLieu)
	{
		this.id_lieu=idLieu;
	}
	public void set_idUtilisateur( int idUtilisateur)
	{
		this.id_utilisateur=idUtilisateur;
	}
	public int get_idUtilisateur()
	{
		return this.id_utilisateur;
	}
	public String get_dateConsommation()
	{
		return this.dateConsommation;
	}
	public void set_dateConsommation(String dateConsommation)
	{
		this.dateConsommation=dateConsommation;
	}
	public int get_quantite()
	{
		return this.quantite;
	}
	public void set_quantite(int quantite)
	{
		this.quantite=quantite;
	}
	public void set_cout(float cout)
	{
		this.cout= cout;
	}
	public float get_cout()
	{
		return this.cout;
	}
	public void setCodeReinitialisation(int code_r)
	{
		this.code_reinitialisation=code_r;
	}
	public int getCodeReinitialisation()
	{
		return this.code_reinitialisation;
	}
	public boolean matches(Consommation entree)
	{
		if (entree.get_idConsommation()==this.id_consommation)
		{
			return true;
		}
		return false;
	}
}
