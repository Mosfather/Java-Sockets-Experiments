package tictactoeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//Fait par : Mohamed BAMOUH
//GL1

public class Server {
	
	static int port=4242;
	static int nbPlayers=0;
	static String[] players={"X","O"};
	static ArrayList<Socket> users;
	
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(port);
		users=new ArrayList<Socket>();
		while(true){
			if(nbPlayers<2){//Le serveur n'accepte plus de connections après 2 joueurs connectés
				Socket socket = server.accept();
				users.add(socket);
				PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
				out.println(players[nbPlayers]);
				System.out.println("Le joueur "+players[nbPlayers]+" s'est connecté ");
				Thread th = new Thread(new TicTacToe(socket,users));
				th.start();
				nbPlayers++;
			}
		}
	}
}

class TicTacToe implements Runnable{
	
	Socket socket;
	ArrayList<Socket> users;
	
	public TicTacToe(Socket socket,ArrayList<Socket> users) {
		this.socket=socket;
		this.users=users;
	}

	@Override
	public void run() {
		PrintWriter out=null;
		BufferedReader in=null;
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			while(true){
				String message = in.readLine();
				System.out.println(message);
				for(Socket s : users){
					if(message.contains("O&")){//Joueur O
						if(!(s.equals(this.socket))){//Meme joueur
							out = new PrintWriter(s.getOutputStream(),true);
							out.println(message+"Opponent moved. Your turn.");
							//System.out.println(message+"Opponent moved. Your turn.");
						}
						else{//Joueur X
							out = new PrintWriter(s.getOutputStream(),true);
							out.println("Valid move. Please wait.");
						}
					}
					else if(message.contains("X&")){//Joueur X
						if(!(s.equals(this.socket))){//Meme joueur
							out = new PrintWriter(s.getOutputStream(),true);
							out.println(message+"Opponent moved. Your turn.");
							//System.out.println(message+"Opponent moved. Your turn.");
						}
						else{//Joueur O
							out = new PrintWriter(s.getOutputStream(),true);
							out.println("Valid move. Please wait.");
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}