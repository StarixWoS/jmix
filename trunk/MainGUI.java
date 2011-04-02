import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Map;

public class MainGUI {
	static JFrame Frame;
	static JTextField playerName = new JTextField();
	static JLabel textFieldPlayerName = new JLabel();
	static JButton makeGM = new JButton("Make GM");
	static JButton removeGM = new JButton("Remove GM");
	
	public MainGUI() {	
		// create JFrame
		FrameListenerClass FrameListener = new FrameListenerClass();
		Frame = new JFrame();
		Frame.addWindowListener(FrameListener);
		Frame.getContentPane().setLayout(null);
		Frame.setResizable(false);
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Frame.setTitle("JMIX Server");

		// add to system tray?
		final TrayIcon trayIcon;
		if (SystemTray.isSupported()) {

		    SystemTray tray = SystemTray.getSystemTray();
		    Image image = Toolkit.getDefaultToolkit().getImage("tray.gif");

		    MouseListener mouseListener = new MouseListener() {
		                
		        public void mouseClicked(MouseEvent e) {
		            //System.out.println("Tray Icon - Mouse clicked!"); 
		        	Frame.setVisible(true);
		        	Frame.setState(JFrame.NORMAL);
		        }

		        public void mouseEntered(MouseEvent e) {
		            //System.out.println("Tray Icon - Mouse entered!");                 
		        }

		        public void mouseExited(MouseEvent e) {
		            //System.out.println("Tray Icon - Mouse exited!");                 
		        }

		        public void mousePressed(MouseEvent e) {
		            //System.out.println("Tray Icon - Mouse pressed!");                 
		        }

		        public void mouseReleased(MouseEvent e) {
		            //System.out.println("Tray Icon - Mouse released!");                 
		        }
		    };

		    ActionListener exitListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.exit(0);
		        }
		    };
		            
		    /*PopupMenu popup = new PopupMenu();
		    MenuItem defaultItem = new MenuItem("Exit");
		    defaultItem.addActionListener(exitListener);
		    popup.add(defaultItem);*/

		    trayIcon = new TrayIcon(image, "JMIX Server");

		    ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            /*trayIcon.displayMessage("Action Event", 
		                "An Action Event Has Been Performed!",
		                TrayIcon.MessageType.INFO);*/
		        }
		    };
		            
		    trayIcon.setImageAutoSize(true);
		    trayIcon.addActionListener(actionListener);
		    trayIcon.addMouseListener(mouseListener);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }

		} else {
		    //  System Tray is not supported
		}

		
		// add JLabel
		textFieldPlayerName.setText("Player Name:");
		textFieldPlayerName.setSize(100, 25);
		textFieldPlayerName.setLocation(5, 10);
		Frame.add(textFieldPlayerName);
		
		// set up player name text field
		PlayerNameListenerClass PlayerNameListener = new PlayerNameListenerClass();
		playerName.addActionListener(PlayerNameListener);
		playerName.setSize(185, 25);
		playerName.setLocation(5, 40);
		Frame.add(playerName);
		
		// add/remove GM buttons
		MakeGMListenerClass MakeGMListener = new MakeGMListenerClass();
		RemoveGMListenerClass RemoveGMListener = new RemoveGMListenerClass();
		makeGM.addActionListener(MakeGMListener);
		makeGM.setSize(185, 25);
		makeGM.setLocation(5, 70);
		Frame.add(makeGM);
		
		removeGM.addActionListener(RemoveGMListener);
		removeGM.setSize(185, 25);
		removeGM.setLocation(5, 100);
		Frame.add(removeGM);
		
		// set up JFrame
		Dimension size = new Dimension (200, 175);
		Frame.pack();
		Frame.setSize(size);
		Frame.setVisible(true);
		Frame.validate();
	}
	
	class MakeGMListenerClass implements ActionListener {
		public void actionPerformed(ActionEvent e) {
//			Player player = ClientHandler.getPlayerList().get(playerName.getText());
//			if (player != null) {
//				//player.setMod(true);
//			}
		}
	}
	
	class RemoveGMListenerClass implements ActionListener {
		public void actionPerformed(ActionEvent e) {
//			Player player = ClientHandler.getPlayerList().get(playerName.getText());
//			if (player != null) {
//				//player.setMod(false);
//			}
		}
	}
	
	class PlayerNameListenerClass implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	class FrameListenerClass implements WindowListener {
		public void windowClosing(WindowEvent e) {
			ActionListener task = new ActionListener() {
				boolean alreadyDisposed = false;
				public void actionPerformed(ActionEvent e) {
					if (Frame.isDisplayable()) {
						alreadyDisposed = true;
						Frame.dispose();
						System.exit(0);
					}
				}
			};
			Timer timer = new Timer(500, task); //fire every half second
			timer.setInitialDelay(2000);        //first delay 2 seconds
			timer.setRepeats(false);
			timer.start();
		}

		
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			Frame.setVisible(false);
		}

		
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
}