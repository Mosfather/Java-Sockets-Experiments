package ClientChat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import ServerChat.Server;

//Fait par : Mohamed BAMOUH
//GL1

public class Client {
	
	static int port=4242;
	static InetAddress adr;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		adr=InetAddress.getLocalHost();
		Socket client = new Socket(adr,port);
		Thread t = new Thread(new Authentification(client));
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		t.join();//Attendre que l'authentification se termine
		Thread t1 = new Thread(new Envoi(client));
		t1.start();
		Thread t2 = new Thread(new Reception(client));
		t2.start();
	}
}

class Compte implements Serializable {//Classe gérant les comptes

	private String username;
	private String password;
	private String Salon;

	public Compte(String u, String p) {
		this.setUsername(u);
		this.setPassword(p);
		this.setSalon("defaultSalon");
	}
	
	public Compte(String u, String p,String s) {
		this.setUsername(u);
		this.setPassword(p);
		this.setSalon(s);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalon() {
		return Salon;
	}

	public void setSalon(String salon) {
		Salon = salon;
	}
	
}

class Authentification implements Runnable {

	protected Compte user;
	protected Socket client;
	static String projectLocation = System.getProperty("user.dir");
	File comptes = new File(projectLocation+"//comptes.txt");

	public Authentification(Socket c) {
		this.client = c;
	}

	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		String u;
		System.out.println("Username: ");
		u = sc.nextLine();
		String p;
		System.out.println("Password: ");
		p = sc.nextLine();
		
		user = new Compte(u,p);
		
		FileOutputStream f=null;
		ObjectOutputStream fo=null;
		try {
			f = new FileOutputStream(comptes);
			fo= new ObjectOutputStream(f);
			
			fo.writeObject(new Compte("user1","user1","Salon1"));
			fo.writeObject(new Compte("user2","user2","Salon1"));
			fo.writeObject(new Compte("user3","user3","Salon1"));
			fo.writeObject(new Compte("user4","user4","Salon2"));
			fo.writeObject(new Compte("user5","user5","Salon2"));
			fo.writeObject(new Compte("user6","user6","Salon2"));
			
			//Comptes préexistants
			
			fo.close();
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		FileInputStream fi;
		ObjectInputStream i;
		//Flux de données entrant pour lire le fichier comptes.txt
		try {
			fi=new FileInputStream(projectLocation+"//comptes.txt");
			i=new ObjectInputStream(fi);
			
			boolean Auth=false;
			while(fi.available()>0){
				Compte us = (Compte) i.readObject();
				if((us.getUsername().equals(this.user.getUsername()))&&(us.getPassword().equals(this.user.getPassword()))){
					System.out.println("Authentification réussie.");
					this.user.setSalon(us.getSalon());
					System.out.println("Vous etes connecté au "+this.user.getSalon());
					Auth=true;
				}
			}
			if(Auth==false){
				System.out.println("Authentification échouée.");
				System.out.println("Vous etes connecté au "+this.user.getSalon());
				//Une authentification echouée mène le client vers le salon invité (par defaut)
			}
			
			fi.close();
			i.close();
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(projectLocation+"//comptes.txt");
			writer.print("");//Vider le fichier comptes.txt pour des raisons de sécurité
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintWriter out=null;
		try {
			out = new PrintWriter(this.client.getOutputStream(),true);
			if(this.user.getSalon().equals("Salon1")){
				out.println("Salon1"+"&"+user.getUsername());//Envoi d'une chaine de caractères au serveur pour que ce dernier reconnaisse l'utilisateur et son salon
			}
			else if(this.user.getSalon().equals("Salon2")){
				out.println("Salon2"+"&"+user.getUsername());
			}
			else if(this.user.getSalon().equals("defaultSalon")){
				out.println("defaultSalon"+"&"+user.getUsername());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class Envoi implements Runnable{//Concacré a l'envoi de messages au serveur
	
	private Socket socket;
	
	public Envoi(Socket s){
		this.socket=s;
	}
	@Override
	public void run() {
		PrintWriter out;
		try {
			out = new PrintWriter(this.socket.getOutputStream(),true);
			Scanner sc = new Scanner(System.in);
			String readString;
			do{
				System.out.println("Envoyez un message :");
				readString=sc.nextLine();
				out.println(readString);
			}while(!(readString.isEmpty()));
			
			System.out.println("Deconnection");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
class Reception implements Runnable{//Reception des messages du serveur
	
	private Socket socket;
	
	public Reception(Socket s){
		this.socket=s;
	}
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			String readString;
			while(true){
				readString=in.readLine();
				if(!(readString.isEmpty())){
					System.out.println("Reception du message suivant : "+readString+" du serveur.");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}