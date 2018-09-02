package tictactoeServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Fait par : Mohamed BAMOUH
//GL1

public class Fenetre extends JFrame{
	Player player;
	Board board;
	Game game;
	String p;
	Socket socket;
	
	public Fenetre(Socket socket,String p){
		this.socket=socket;
		PrintWriter out=null;
		BufferedReader in=null;
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(),true);
		}catch(IOException e){
			e.printStackTrace();
		}
		this.p=p;
		this.setTitle("Tic Tac Toe");
		this.setVisible(true);
		this.setSize(500, 500);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		player = new Player(this,this.p);//Label montrant le joueur actuel
		game = new Game(this,this.p);//Historique de la partie
		board= new Board(socket,in,out,this,this.p,game);//Plateforme de jeu 
		this.setLayout(new BorderLayout());
		JLabel left=new JLabel("_______");
		
		JLabel right=new JLabel("_______");
		
		this.getContentPane().add(player, BorderLayout.NORTH);
		this.getContentPane().add(board, BorderLayout.CENTER);
		this.getContentPane().add(game, BorderLayout.SOUTH);
		this.getContentPane().add(right, BorderLayout.EAST);
		this.getContentPane().add(left, BorderLayout.WEST);
	}
}

class Player extends JPanel{
	Fenetre fen;
	JLabel info;
	String player;
	
	public Player(Fenetre fen,String player){
		this.fen=fen;
		info = new JLabel();
		this.add(info);
		this.player=player;
		this.info.setText("You are Player "+this.player);
	}
	
}
class Board extends JPanel implements ActionListener{
	Fenetre fen;
	String player;
	ArrayList<JButton> cases = new ArrayList<JButton>();
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	Game game;
	
	public Board(Socket socket, BufferedReader in,PrintWriter out, Fenetre fen,String player,Game game) {
		this.socket=socket;
		this.game=game;
		this.in=in;
		this.out=out;
		this.fen=fen;
		this.player=player;
		this.setLayout(new GridLayout(3,3));
		for(int i=0;i<9;i++){
			JButton cas = new JButton();
			cas.setBackground(Color.WHITE);
			cas.setSize(new Dimension(50,50));
			cas.addActionListener(this);
			this.add(cas);
			cases.add(cas);
		}
		Thread jeu = new Thread(new Jeu(cases,in,game));//Thread concacré au changements venant de l'autre joueur
		jeu.start();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		for(JButton button : cases){
			if(e.getSource()==button){
				if(player.equals("O")){
					button.setText("O");
					out.println("O&"+cases.indexOf(button));
					System.out.println("O&"+cases.indexOf(button));//Envoie le signe O et la coordonnée de la case pour que la meme case chez l'autre joueur soit changée
				}
				else if(player.equals("X")){//Envoie le signe X et la coordonnée de la case pour que la meme case chez l'autre joueur soit changée
					button.setText("X");
					out.println("X&"+cases.indexOf(button));
					System.out.println("O&"+cases.indexOf(button));
				}
				
			}
		}
	}
	
}

class Jeu implements Runnable{
	
	ArrayList<JButton> cases;
	BufferedReader in;
	Game game;
	
	public Jeu(ArrayList<JButton> cases,BufferedReader in,Game game){
		this.cases=cases;
		this.in=in;
		this.game=game;
	}

	@Override
	public synchronized void run() {
		String message=null;
		while(true){
			try {
				message=in.readLine();//Lit une chaine de caractères repréentant un changement venu de l'autre joueur
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(message);
			if(message.contains("O&")){
				String coor = message.substring(2,3);
				cases.get(Integer.parseInt(coor)).setText("O");//Ecrit O sur la case
				this.game.historique.add(message.substring(3));//Note le changement sur l'historique de la partie
			}
			else if(message.contains("X&")){
				String coor = message.substring(2,3);
				cases.get(Integer.parseInt(coor)).setText("X");//Ecrit X sur la case
				this.game.historique.add(message.substring(3));//Note le changement sur l'historique de la partie
			}
			else if(message.startsWith("Valid")){
				this.game.historique.add(message);
			}
		}
	}
}

class Game extends JPanel{
	Fenetre fen;
	JLabel info;
	String player;
	ArrayList<String> historique = new ArrayList<String>();
	//JScrollPane scroll;
	
	public Game(Fenetre fen,String player) {
		this.fen=fen;
		this.player=player;
		info = new JLabel();
		this.add(this.info);
		info.setText("Game start.");
		//scroll = new JScrollPane(this);
		//scroll.setMinimumSize(new Dimension(fen.getWidth(),100));
		//scroll.add(info);
		
		Thread th = new Thread(new Historique(this.historique,info));//Lance le thread pour mettre a jour l'historique de la partie
		th.start();
	}
}
class Historique implements Runnable{
	
	ArrayList<String> historique;
	JLabel info;
	
	
	public Historique(ArrayList<String> historique,JLabel info){
		this.historique=historique;
		this.info=info;
	}
	
	@Override
	public synchronized void run() {
		while(true){
			try {
				Thread.currentThread().sleep(1000);//Pour eviter que des changements soient apportés a l'ArrayList en meme temps qu'il soit parcouru, lancant un ConcurrentModificationException 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String histo="<html>";
			for(String m : historique){
				histo+=m+"<br/>";
			}
			histo+="</html>";
			this.info.setText(histo);//Affichage de l'historique
		}
	}
	
}