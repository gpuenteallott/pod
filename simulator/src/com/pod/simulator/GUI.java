package com.pod.simulator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.List;

public class GUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 700;
	public static final int HEIGHT = 500;
	private static final Color BG = new Color(209, 166, 48);
	private static final int STEP = 30;
	private static final int BAR_HEIGHT = 20;
	
	private static final Color [] colors = new Color [] {
		
		Color.WHITE,
		Color.RED,
		Color.ORANGE,
		Color.DARK_GRAY,
		Color.MAGENTA
		
	};
	
	private static JPanel canvas;
	private PODCloud pod;
	private JLabel j_header;
	private JLabel j_info;
	private JLabel j_queue;
	private JButton b_start;
	private JButton b_iterate;
	
	private int [][] activityRecord;
	
	public GUI(){
		
		activityRecord = new int [10][10];
		
		JPanel jp = new JPanel();
		this.setContentPane(jp);
		this.setTitle("POD Cloud Simulator");
		
		b_start = new JButton ("Start");
		b_iterate = new JButton ("Iterate");
		
		j_info = new JLabel();
		j_info.setText("Press Start");
		j_queue = new JLabel();
		j_queue.setText("");
		j_header = new JLabel();
		j_header.setText("POD Cloud Simulator");
		
		//Paneles principales
		jp.setLayout(new BorderLayout());
		Container c_main = new JPanel();
		Container c_der = new JPanel();
		
		jp.add(c_main,BorderLayout.CENTER);
		jp.add(j_header,BorderLayout.NORTH );
		jp.add(c_der,BorderLayout.EAST);
		
		//Genera el juego y resetea
		pod = new PODCloud();
		
		//Panel de juego, se modifica paintComponent()
		JPanel c_juego = new JPanel();
		canvas = new JPanel(){
			
			private static final int BAR_WIDTH = GUI.WIDTH-20;
			
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g){
				
				g.setColor(Color.WHITE);
				g.fillRect(0,0,GUI.WIDTH, GUI.HEIGHT);
				
				List<Worker> workers = pod.getWorkers();
				
				for ( int i = 0; i < workers.size(); i++ ) {

					Integer[] ids = workers.get(i).getHistory();
					for ( int j = 0; j < Worker.HISTORY_SIZE; j++ ) {
						
						g.setColor(colors[ ids[j] % colors.length ]);
						g.fillRect(  BAR_WIDTH -Worker.HISTORY_SIZE*STEP+ 10+j*STEP , 10 + i*30 , STEP, BAR_HEIGHT);
					}
				}
				g.setColor(Color.BLACK);
				for ( int i = 0; i < workers.size(); i++ ) {
					g.drawRect(10,10 + i*30, BAR_WIDTH , 20);
					
					for ( int j = 0; j < Worker.HISTORY_SIZE; j++ ) {
						g.drawLine( BAR_WIDTH-j*STEP+10, 10 + i*30, BAR_WIDTH-j*STEP+10, 10 + i*30 + 2);
						g.drawLine( BAR_WIDTH-j*STEP+10, 10 + i*30 + BAR_HEIGHT, BAR_WIDTH-j*STEP+10, 8 + i*30 + BAR_HEIGHT);
					}
				}
				
				j_queue.setText( "Tamaño cola: "+pod.getQueueSize() );
			}
		};
		
		
		c_juego.setLayout(new BorderLayout());
		
		c_main.setBackground(BG);
		c_juego.setBackground(BG);
		c_der.setBackground(BG);
		j_header.setBackground(BG);
		
		//Setup del contenedor central
		GridBagLayout gbli = new GridBagLayout();
		GridBagConstraints gbci = new GridBagConstraints();
		gbci.insets = new Insets (20,20,20,20);
		c_main.setLayout(gbli);
		c_main.add(c_juego,gbci);
		
		c_juego.setPreferredSize( new Dimension(WIDTH, HEIGHT));
		c_juego.add(canvas,BorderLayout.CENTER);
		canvas.setOpaque(false);
	
		//Setup del derecho
		GridBagLayout gbld = new GridBagLayout();
		GridBagConstraints gbcd = new GridBagConstraints();
		gbcd.insets = new Insets (10,0,10,10);
		gbcd.fill = GridBagConstraints.BOTH;
		c_der.setLayout(gbld);
		
		gbcd.gridx = 0; gbcd.gridy = 0; gbcd.gridwidth = 1;
		c_der.add(b_start,gbcd);
		gbcd.gridx = 0; gbcd.gridy = 1;
		c_der.add(b_iterate,gbcd);
		gbcd.gridx = 0; gbcd.gridy = 2;
		c_der.add(j_info,gbcd);
		gbcd.gridx = 0; gbcd.gridy = 3;
		c_der.add(j_queue,gbcd);
		
		c_der.setPreferredSize(new Dimension(150, HEIGHT));
		
		//escuchador START
		b_start.addActionListener(new ActionListener(){
					
			public void actionPerformed(ActionEvent e){
				
				new Thread() {
					public void run(){
						
						try{
							j_info.setText("Performing...");
							
							pod.setup();
							pod.iterate();

						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}.start();
			}
			
		});
		
		
		b_iterate.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				new Thread() {
					public void run(){
						
						try{
							pod.iterate();

						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}.start();
			}
		});
	
        //escuchador CERRAR
        this.addWindowListener(new WindowAdapter(){
        	
        	  public void windowClosing(WindowEvent we){
        		  System.exit(0);
        	  }
         });
		
		this.pack();
		this.setVisible(true);
		
	}
	
	//Se llama desde Tablero.java cada vez que hace un cambio
	public static void repaintPOD (){
		if ( canvas != null )
			canvas.repaint();
	}

	//Main genera la UI
	public static void main (String[]args){
		
		Runnable run = new Runnable() {
			public void run(){
				new GUI();
			}
		};
		SwingUtilities.invokeLater(run);
	}

}