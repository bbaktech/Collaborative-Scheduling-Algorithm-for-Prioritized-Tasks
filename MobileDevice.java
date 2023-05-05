//mobileDevice.java
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;

public class  MobileDevice extends JFrame {
	
	JButton btnSend;
	JButton btnClose;
	
	JTextField txtJobID;
	JTextArea txtUpdates;
	JTextField txtJobSize;
	JComboBox comJobPriority;
	
	JTextField txtJobDetails;
	
	JLabel lblConectedTo;	
	String basestationid;	
	String DeviceId;
	
	class MDLisioner implements Runnable {
		int port;
		ServerSocket server;
		Socket connection;
		
		public MDLisioner(int port) {
			this.port = port;
//			strVal = "Waiting For PEER Connection";
		}

		/* Beginning of Run Method */
		public void run() {
			try {
				RegesterToBS();
				server = new ServerSocket(port);

				while (true) {
					connection = server.accept();	//Waiting for request from PEER	
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					String strVal = (String)in.readObject(); //Reading object and storing it in strVal	

					String[] temp;           
					temp = strVal.split(" ");  //Splitting with Space
					String retval = "";
					if (temp[0].equalsIgnoreCase(config.CONN_BS_ID)) {
						basestationid = temp[1];
						lblConectedTo.setText("Connected to Base Station:" + basestationid );
						lblConectedTo.setVisible(true);
					}

					if (temp[0].equalsIgnoreCase(config.MESSAGE_TYPE_ACK)) {
						System.out.println(strVal);	
						txtUpdates.selectAll();
						txtUpdates.cut();
						
						txtUpdates.append(strVal);
						btnSend.setEnabled(true);
					}
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

    class ButtonsActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnSend) {
				System.out.println("Message Send");
				SendJob();
				
			}
		}	
    }
    
    public static void main(String args[]){ 
    	
//    	System.out.println(args[0]+"   " + args[1]);	
    	MobileDevice frame = new MobileDevice (args[0]);
	    frame.setVisible(true); 
	    frame.DeviceId = args[0];
//	    frame.basestationid =  ;  
//	    System.out.println("BS:"+ args[1] + " "+ frame.basestationid );
	    int portid = Integer.parseInt(args[0]);
		Thread mdthread = new Thread ( frame.new MDLisioner(portid));
		mdthread.setName("MDLisioner");
		mdthread.start();    //Start Thread
    }

	public void RegesterToBS()
	{
		SendMsgToRegBS(config.REGISTER_WITH_BS +" "+ DeviceId);
	}
	void SendMsgToRegBS(String sendmsg){
		
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		try{
			System.out.println(sendmsg);
			
			//1. Creating a Socket to Connect to the Server		
//			int portid = Integer.parseInt(basestationid);
			requestSocket =  new Socket("localhost",4001 );
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(sendmsg);
			out.flush();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("Cannot Connect to an RegBS Host!");	
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				//To Handle Input Output Exception
				ioException.printStackTrace();
			}
		}		
	}

	public void SendJob()
	{
		String msg = config.MESSAGE_TYPE_JOB +" "+DeviceId +" "+ txtJobID.getText() + " " + comJobPriority.getSelectedIndex() +" " +txtJobDetails.getText();
		SendMsgToBS(msg);
		txtUpdates.selectAll();
		txtUpdates.cut();

//		btnSend.setEnabled(false);
	}
	
	void SendMsgToBS(String sendmsg){
		
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		try{
			System.out.println(sendmsg);
			
			//1. Creating a Socket to Connect to the Server		
			int portid = Integer.parseInt(basestationid);
			requestSocket =  new Socket("localhost",portid );
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(sendmsg);
			out.flush();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("Cannot Connect to an Unknown Host!");	
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				//To Handle Input Output Exception
				ioException.printStackTrace();
			}
		}		
	}

    MobileDevice(String s){
    	super(s);
    	
    	DeviceId = s;
    	ButtonsActionListener bl = new ButtonsActionListener();

    	this.setBounds(10, 10, 500, 350);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setSize(500,400);
	    
	    JPanel contentPane = new JPanel();
	    contentPane.setBackground(Color.PINK);
	    contentPane.setBounds(10, 10, 500, 350);
	    contentPane.setLayout(null);
	    this.setContentPane(contentPane);
	    
	    lblConectedTo = new JLabel("Base Station");
	    lblConectedTo.setHorizontalAlignment(SwingConstants.LEFT);
	    lblConectedTo.setBounds(20, 10, 400, 20);
	    contentPane.add(lblConectedTo);	

	    JLabel lblJobId = new JLabel("Job ID");
	    lblJobId.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJobId.setBounds(20, 40, 200, 20);
	    contentPane.add(lblJobId);	
	    
	    txtJobID = new JTextField();
	    txtJobID.setBounds(250, 40, 200, 20);
	    contentPane.add(txtJobID);
	    txtJobID.setColumns(10);

	    JLabel lblJobDetails = new JLabel("Job Details");
	    lblJobDetails.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJobDetails.setBounds(20, 70, 200, 20);
	    contentPane.add(lblJobDetails);

	    txtJobDetails = new JTextField();
	    txtJobDetails.setBounds(250, 70, 200, 20);
	    contentPane.add(txtJobDetails);
	    
	    JLabel lblJobSize = new JLabel("Job Priority");
	    lblJobSize.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJobSize.setBounds(20, 100, 200, 20);
	    contentPane.add(lblJobSize);

	    String s1[] = {"NORMAL" ,"MEDIUM", "HIGH"};
	    
        // create checkbox
	    comJobPriority = new JComboBox(s1);
	    comJobPriority.setBounds(250, 100, 200, 20);
	    contentPane.add(comJobPriority);
	    
//	    comJobPriority.add
//	    txtJobSize = new JTextField();
//	    txtJobSize.setBounds(250, 100, 200, 20);
//	    contentPane.add(txtJobSize);
	    
	    JLabel lblUpdates = new JLabel("Results:");
	    lblUpdates.setHorizontalAlignment(SwingConstants.LEFT);
	    lblUpdates.setBounds(20, 125, 200, 20);
	    contentPane.add(lblUpdates);
	    
	    contentPane.add(lblJobSize);
	    txtUpdates = new JTextArea();
	    txtUpdates.setBounds(20, 145, 425, 150);
	    contentPane.add(txtUpdates);

	    btnSend = new JButton("Send");
	    btnClose = new JButton("Close");
	    btnSend.setBounds(20, 300, 100, 20);
	    btnClose.setBounds(150,300,100,20);	    
	    btnSend.addActionListener(bl);
	    btnClose.addActionListener(bl);
	    contentPane.add(btnSend);
	    contentPane.add(btnClose);
    }
}

