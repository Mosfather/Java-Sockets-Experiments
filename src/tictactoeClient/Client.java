package tictactoeClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import tictactoeServer.Fenetre;

//Fait par : Mohamed BAMOUH
//GL1

public class Client {
	
	static int port=4242;
	static InetAddress adr;
	static int nbPlayers=0;
	
	public static void main(String[] args) throws IOException {
		adr = InetAddress.getLocalHost();
		Socket client = new Socket(adr,port);
		Thread t = new Thread(new Authentification(client));
		t.start();
	}
}

class Authentification implements Runnable{
	
	Socket client;
	String id;
	
	public Authentification(Socket s){
		this.client=s;
	}
	@Override
	public void run() {
		PrintWriter out=null;
		BufferedReader in=null;
		try {
			out = new PrintWriter(this.client.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			try {
				id=in.readLine();//Le serveur assigne le role X ou O au client
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if((id.equals("X"))||(id.equals("O"))){
				break;
			}
		}
		Thread th = new Thread(new Traitement(client,id));//Lance un thread gérant le joueur actuel 
		th.start();
	}
}

class Traitement implements Runnable{
	Fenetre fenetre;
	String player;
	Socket socket;
	
	public Traitement(Socket client, String player){
		this.socket=client;
		this.player=player;
	}
	@Override
	public void run() {
		fenetre = new Fenetre(socket,this.player);
	}
}