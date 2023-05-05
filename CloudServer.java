//CloudServer.java

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.*;



public class  CloudServer extends JFrame {
	
	 JTextArea txtJobsList;
	 
	 class CSLisioner implements Runnable {
			int port;
			ServerSocket server;
			Socket connection;
			
			public CSLisioner(int port) {
				this.port = port;
//				strVal = "Waiting For PEER Connection";
			}

			/* Beginning of Run Method */
			public void run() {
				try {
					server = new ServerSocket(port);

					while (true) {
						connection = server.accept();	//Waiting for request from PEER	
						ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
						String strVal = (String)in.readObject(); //Reading object and storing it in strVal	
					
						String[] temp;           
						temp = strVal.split(" ");  //Splitting with Space

						
						if (temp[0].equalsIgnoreCase(config.JOBS_FROM_OS)) {
							System.out.println(strVal);	
							txtJobsList.append(temp[1]+ "-Jobs Received and Processed");
						}
						
						if (temp[0].equalsIgnoreCase(config.CLEAR_ALL)) {
							System.out.println(strVal);	
							txtJobsList.selectAll();
							txtJobsList.cut();
						}
						
						in.close();
						//Closing connection
						connection.close();   								
					}
				}
				catch(IOException ioException){
					//To Handle Input-Output Exceptions
					ioException.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				} 

			}
	}

	
    public static void main(String args[]){    	
    	CloudServer frame = new CloudServer ("Cloud Data Center");
	    frame.setVisible(true);
		Thread mdthread = new Thread (frame.new CSLisioner(4004));
		mdthread.setName("CloudServerService");
		mdthread.start();    //Start Thread
    }

    
    class ButtonsActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		System.out.println("Btn Pressed");
    	}
    }	

    CloudServer(String s){
    	super(s);
    	
    	ButtonsActionListener bl = new ButtonsActionListener();

    	this.setBounds(10, 10, 300, 450);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setSize(300,450);
	    
	    JPanel contentPane = new JPanel();
	    contentPane.setBackground(Color.PINK);
	    contentPane.setBounds(10, 10, 300, 450);
	    contentPane.setLayout(null);
	    this.setContentPane(contentPane);
	    
	    JLabel lblSlotNo = new JLabel("Slot No:");
	    lblSlotNo.setHorizontalAlignment(SwingConstants.LEFT);
	    lblSlotNo.setBounds(20, 10, 200, 20);
	    contentPane.add(lblSlotNo);	

	    JLabel lblJobsList = new JLabel("List of Jobs Under processing");
	    lblJobsList.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJobsList.setBounds(20, 40, 200, 20);
	    contentPane.add(lblJobsList);	
	    
	    txtJobsList = new JTextArea();
	    txtJobsList.setBounds(20, 70, 250, 300);
	    contentPane.add(txtJobsList);

	    JButton btnClose = new JButton("Close");
	    btnClose.setBounds(20,390,100,20);	    
	    btnClose.addActionListener(bl);
	    contentPane.add(btnClose);

    }

}
