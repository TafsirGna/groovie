package com.myapp.groovie.classes.database;



public class Utilisateur {

	private int idUtilisateur;
	private int IdDepartement;
	private int IdGroupe;
	private String pseudo;
	private String email;
	private String telephone;
	private byte[] photo;
	private String idDevice;
	private String password;
	private String cleActivation;
	private int actif;
	private int idParam;

	public void setIdUtilisateur(int idUtilisateur)
	{
		this.idUtilisateur=idUtilisateur;
	}
	public int getIdUtilisateur()
	{
		return this.idUtilisateur;
	}
	
	public int getIdDepartement() {
		return IdDepartement;
	}

	public void setIdDepartement(int idDepartement) {
		this.IdDepartement = idDepartement;
	}

	public int getIdGroupe() {
		return IdGroupe;
	}

	public void setIdGroupe(int IdGroupe) {
		this.IdGroupe = IdGroupe;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String Pseudo) {
		this.pseudo = Pseudo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String Email) {
		this.email = Email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String Telephone) {
		this.telephone = Telephone;
	}

	public String getIdDevice() {
		return idDevice;
	}

	public void setIdDevice(String IdDevice) {
		this.idDevice = IdDevice;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String Password) {
		this.password = Password;
	}

	public void setcleActivation(String cle)
	{
		this.cleActivation=cle;
	}
	public String getcleActivation()
	{
		return this.cleActivation;
	}
	public int getactif()
	{
		return this.actif;
	}
	public void setActif(int actif)
	{
		this.actif=actif;
	}
	public void set_idParam(int idParam)
	{
		this.idParam=idParam;
	}
	public int get_idParam()
	{
		return this.idParam;
	}
	public Utilisateur(int idUtilisateur,int IdDepartement, int IdGroupe, String pseudo, String email, String telephone, String IdDevice, String password,String cleActivation, int actif )
	{
		this.idUtilisateur=idUtilisateur;
		this.IdDepartement=IdDepartement;
		this.IdGroupe=IdGroupe;
		this.pseudo=pseudo;
		this.email=email;
		this.telephone=telephone;
		this.idDevice=IdDevice;
		this.password=password;
		this.cleActivation=cleActivation;
		this.actif=actif;
	}
	public Utilisateur()
	{
		super();
	}

	public void setPhoto(byte[] photo)
	{
		this.photo=photo;
	}
	public byte[] getPhoto()
	{
		return this.photo;
	}
	public boolean matches(Utilisateur user)
	{
		if (user.getIdUtilisateur()==this.getIdUtilisateur())
		{
			return true;
		}
		return false;
	}
}
