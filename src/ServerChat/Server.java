package ServerChat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

//Fait par : Mohamed BAMOUH
//GL1

public class Server {

	static int port = 4242;
	public static final ArrayList<Socket> defaultSalon = new ArrayList<Socket>();
	public static final ArrayList<Socket> salon1 = new ArrayList<Socket>();
	public static final ArrayList<Socket> salon2 = new ArrayList<Socket>();
	public static ArrayList<Socket> salon;
	static String projectLocation = System.getProperty("user.dir");
	static File fsalon1=new File(projectLocation+"//salon1.txt");
	static File fsalon2=new File(projectLocation+"//salon2.txt");
	static File fdefaultSalon=new File(projectLocation+"//defaultSalon.txt");
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("Serveur lancé.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (true) {
				Socket client = server.accept();
				Scanner inn = new Scanner(client.getInputStream());
				String sal = inn.nextLine();//Message que le client nouvellement connecté envoie automatiquement au serveur afin que ce dernier lui affecte un salon
				String username=null;
				if(sal.contains("Salon1")){
					salon1.add(client);
					username = sal.substring(sal.indexOf("&")+1);
					salon=salon1;
				}
				else if(sal.contains("Salon2")){
					salon2.add(client);
					username = sal.substring(sal.indexOf("&")+1);
					salon=salon2;
				}
				else if(sal.contains("defaultSalon")){
					defaultSalon.add(client);
					username = sal.substring(sal.indexOf("&")+1);
					salon=defaultSalon;
				}
				//Attribue un salon au nouveau client connecté selon ce que ce dernier a envoyé au serveur (identifiant, mdp...)
				int activeClients = defaultSalon.size()+salon1.size()+salon2.size();
				System.out.println("Un nouveau client s'est connecté (Clients actifs: "+activeClients+").");
				System.out.println("Salon 1 :"+Server.salon1.size()+" clients.");
				System.out.println("Salon 2 :"+Server.salon2.size()+" clients.");
				System.out.println("Salon invité :"+Server.defaultSalon.size()+" clients.");
				Thread th = new Thread(new Chat(client,salon,username));
				th.start();
			}
		} finally {
			server.close();
		}
	}
}

class Chat implements Runnable {

	ArrayList<Socket> salon;
	Socket socket;
	private BufferedReader in;
	String username;
	static String m="";

	public Chat(Socket s, ArrayList<Socket> out,String username) {
		this.socket = s;
		this.salon = out;
		this.username=username;
	}

	@Override
	public synchronized void run() {//Meme chose que le TP3
		String msgRecu = "";
		FileWriter fo;
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			String readString;
			while (true) {
				readString = in.readLine();
				if (!(readString.isEmpty())) {
					msgRecu = readString;
					System.out.println("Reception du message suivant :" + msgRecu + " du client " + this.username);
					String msgEnvoye;
					msgEnvoye = msgRecu;
					for (Socket s : salon) {
						if (s.equals(socket)) {} 
						else {
							try {
								PrintWriter out = new PrintWriter(s.getOutputStream(), true);
								System.out.println("Envoi du message suivant: " + msgEnvoye + " au client " + s.getPort());
								out.println(msgEnvoye);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					//Ecriture de l'historique des messages dans un fichier (selon le salon)
					m+=this.username+" : "+msgEnvoye+"\r\n";
					if(salon.equals(Server.salon1)){
						fo=new FileWriter(Server.fsalon1);
						fo.write(m);
						fo.flush();
					}
					else if(salon.equals(Server.salon2)){
						fo=new FileWriter(Server.fsalon2);
						fo.write(m);
						fo.flush();
					}
					else if(salon.equals(Server.defaultSalon)){
						fo=new FileWriter(Server.fdefaultSalon);
						fo.write(m);
						fo.flush();
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}