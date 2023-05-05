import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;

public class  Orchestrator extends JFrame {
	
	public int slotNumber;

	class BSEntity {
		
		String BSid;
		String SlotNo;
		String ArivedJobsCont;
	}
	
	List<BSEntity> list = new LinkedList<BSEntity>();
	List<BSEntity> list1 = new LinkedList<BSEntity>();

	JLabel lblConectedTo;
	JTextArea txtArivedJobs;
	JTextArea txtshJobs;
	JTextArea txtJoborSheJobs;
	JButton btnClose;
	JButton btnClear;
	
	class OSLisioner implements Runnable {
		int port;
		ServerSocket server;
		Socket connection;
		
		public OSLisioner(int port) {
			this.port = port;
//			strVal = "Waiting For PEER Connection";
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
					if (temp[0].equalsIgnoreCase(config.REGISTER_WITH_BS)) {
						
						//get random number between 0 and list.size() set it as BS for MOBILE
						//
						SendRegMsgToBS(list.get(0).BSid,config.REGISTER_WITH_BS +" "+ temp[1]);
						
					}
					if (temp[0].equalsIgnoreCase(config.REGISTER_WITH_OS)) {
						BSEntity bse = new BSEntity();
						bse.BSid = temp[1];
						list.add(bse);						
						System.out.println("Base Station:"+temp[1]+" Registered");
					}
					if (temp[0].equalsIgnoreCase(config.SHD_ACK_OS)) {
						
						System.out.println(strVal);
						if (0 < Integer.parseInt(temp[2]))	txtArivedJobs.append("From BS:"+temp[1] +" Received("+temp[2]+")Jobs Slot:"+temp[3]+"\n");
						
						ListIterator <BSEntity> iterator = list.listIterator();		
						while (iterator.hasNext()) {
							BSEntity bse = iterator.next();
							if (bse.BSid.equalsIgnoreCase(temp[1])) {
								bse.ArivedJobsCont = temp[2];
								bse.SlotNo = temp[3];
// not only job count but we must receive jobs list , it must return 3 or 2 Units to BS
							}
						}						
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

	
	void SendRegMsgToBS(String basestationid,String sendmsg){
		
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

	void SendClearToClour() {
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		String strMsg = config.CLEAR_ALL + " ";
		
		try{

			//1. Creating a Socket to Connect to the Server		
			int portid = 4004;
			requestSocket =  new Socket("localhost",portid );
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(strMsg);
			out.flush();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("Cannot Connect to an Cloud");	
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
	
	void SendClearToAllConnectedBS() {
		
		ListIterator <BSEntity> iterator = list.listIterator();		
		while (iterator.hasNext()) {
			BSEntity bs = iterator.next();			
			Socket requestSocket= null;	
			ObjectOutputStream out=null;
			
			try{
				//1. Creating a Socket to Connect to the Server		
				int portid = Integer.parseInt(bs.BSid);
				requestSocket =  new Socket("localhost",portid );
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();			
				out.writeObject(config.CLEAR_ALL+" ");
				out.flush();
			}
			catch(UnknownHostException unknownHost){
				System.err.println("Cannot Connect to an BS for clear");	
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
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
    void sendSlotToConnectedBS() {
		ListIterator <BSEntity> iterator = list.listIterator();		
		while (iterator.hasNext()) {
			BSEntity bs = iterator.next();			
			Socket requestSocket= null;	
			ObjectOutputStream out=null;
			
			try{

				//1. Creating a Socket to Connect to the Server		
				int portid = Integer.parseInt(bs.BSid);
				requestSocket =  new Socket("localhost",portid );
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();			
				out.writeObject(config.MESSAGE_TYPE_Shedule+" " + slotNumber);
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

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int totalJobCont = 0;
		int totalAvil = 0;
		
		iterator = list.listIterator();		
		while (iterator.hasNext()) {
			BSEntity bse = iterator.next();
			int arjobs = Integer.parseInt(bse.ArivedJobsCont);
			if ( Integer.parseInt(bse.SlotNo)==  slotNumber) {
				if (arjobs >0) {totalJobCont = totalJobCont +arjobs;
				list1.add(bse);
				}
				if (arjobs <0) totalAvil = totalAvil - arjobs;
			}
		}
		
		if ( totalJobCont-totalAvil > 0) SendJobsToCloud(totalJobCont-totalAvil);
		
		iterator = list.listIterator();		
		while (iterator.hasNext()) {
			BSEntity bse = iterator.next();
			int arjobs = Integer.parseInt(bse.ArivedJobsCont);
			if ( Integer.parseInt(bse.SlotNo)==  slotNumber) {
				if (arjobs <0) {
					totalJobCont = totalJobCont + arjobs;
					if (totalJobCont >= 0)
					{	txtshJobs.append("Jobs("+ (-arjobs) + ") Sent To BS:" +bse.BSid + "\n");
					  	SendAddisionJobsTOBS( bse.BSid, String.valueOf(-arjobs));
					  	//jobs sent for processing at BS
						ListIterator <BSEntity> iterator1 = list1.listIterator();	
						while (iterator1.hasNext()) {
							BSEntity bse1 = iterator1.next();
							int pendingjobs = Integer.parseInt(bse1.ArivedJobsCont);
							int deff =  pendingjobs + arjobs;
							Send_processingTime(bse1.BSid,bse1.SlotNo,arjobs); //no of jobs processed at BS
							arjobs = deff; 
							if (deff > 0) { 
								bse1.ArivedJobsCont = String.valueOf(deff);						
							}
							else {
								bse1.ArivedJobsCont = String.valueOf(0);
								break;
							}
						}
					  	//
					}
					else if (totalJobCont < 0)
					{	totalJobCont = totalJobCont - arjobs;
						txtshJobs.append("Jobs("+ totalJobCont + ") Sent To BS:" +bse.BSid + "\n");
						SendAddisionJobsTOBS( bse.BSid, String.valueOf(totalJobCont) );
						break;
					}
				}
			}
		}
	    lblConectedTo.setText("Slot No:"+slotNumber);
    }
	
    private void Send_processingTime(String BSid,String slot,int  diff) {
		// TODO Auto-generated method stub
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		String strMsg = config.OS_UPDATE_TIME + " " + diff ;
		
		System.out.println(BSid+" "+strMsg);
		
//		try{
//
//			//1. Creating a Socket to Connect to the Server	
//			int portid = Integer.parseInt(BSid);
//			requestSocket =  new Socket("localhost", portid );
//			out = new ObjectOutputStream(requestSocket.getOutputStream());
//			out.flush();			
//			out.writeObject(strMsg);
//			out.flush();
//		}
//		catch(UnknownHostException unknownHost){
//			System.err.println("Cannot Connect to an Cloud");	
//		}
//		catch(IOException ioException){
//			ioException.printStackTrace();
//		}
//		finally{
//			//4: Closing connection
//			try{
//				out.close();
//				requestSocket.close();
//			}
//			catch(IOException ioException){
//				//To Handle Input Output Exception
//				ioException.printStackTrace();
//			}
//		}			
	}

	private void SendJobsToCloud(int totalJobCont) {
		// TODO Auto-generated method stub	
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		String strMsg = config.JOBS_FROM_OS + " " + totalJobCont +" Sent to Cloud";
		txtJoborSheJobs.setText( totalJobCont +" Jobs Sent To Cloud");	
		
		try{

			//1. Creating a Socket to Connect to the Server		
			int portid = 4004;
			requestSocket =  new Socket("localhost",portid );
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(strMsg);
			out.flush();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("Cannot Connect to an Cloud");	
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

	private void SendAddisionJobsTOBS(String BSid, String arivedJobsCont) {
		// TODO Auto-generated method stub
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		String strMsg = config.JOBS_FROM_OS + " " + arivedJobsCont +" -Jobs Received From Orchester and Processed";
		
		try{

			//1. Creating a Socket to Connect to the Server		
			int portid = Integer.parseInt(BSid);
			requestSocket =  new Socket("localhost",portid );
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(strMsg);
			out.flush();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("Cannot Connect to an BS Host!");	
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

	public static void main(String args[]){    	
    	Orchestrator frame = new Orchestrator ("Orchestrator - Sheduling");
	    frame.setVisible(true);    
		Thread osthread = new Thread (frame.new OSLisioner(4001));
		osthread.setName("OS");
		osthread.start();    //Start Thread
    }
    
    class ButtonsActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		if (e.getActionCommand().equalsIgnoreCase("Clear")){
    			txtArivedJobs.selectAll();
    			txtArivedJobs.cut();
    			txtshJobs.selectAll();
    			txtshJobs.cut();
    			txtJoborSheJobs.selectAll();
    			txtJoborSheJobs.cut();   			

    			SendClearToClour();
    			SendClearToAllConnectedBS();
    			    			
    		} else if (e.getActionCommand().equalsIgnoreCase("Shedule")){
    			sendSlotToConnectedBS();
    			slotNumber++;
    		}
    	}
    }	
    
    Orchestrator(String s){
    	super(s);
    	slotNumber = 1;
    	ButtonsActionListener bl = new ButtonsActionListener();

    	this.setBounds(10, 10, 500, 530);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setSize(500,530);
	    
	    JPanel contentPane = new JPanel();
	    contentPane.setBackground(Color.PINK);
	    contentPane.setBounds(10, 10, 500, 530);
	    contentPane.setLayout(null);
	    this.setContentPane(contentPane);
	    
	    lblConectedTo = new JLabel("Slot No:"+slotNumber);
	    lblConectedTo.setHorizontalAlignment(SwingConstants.LEFT);
	    lblConectedTo.setBounds(20, 10, 400, 20);
	    contentPane.add(lblConectedTo);	

	    JLabel lblJobId = new JLabel("Arived Jobs:");
	    lblJobId.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJobId.setBounds(20, 40, 200, 20);
	    contentPane.add(lblJobId);	

	    txtArivedJobs = new JTextArea();
	    txtArivedJobs.setBounds(20, 70, 200, 370);
	    contentPane.add(txtArivedJobs);

	    JLabel lblshJobs = new JLabel("Job Assigned to BSs");
	    lblshJobs.setHorizontalAlignment(SwingConstants.LEFT);
	    lblshJobs.setBounds(240, 40, 200, 20);
	    contentPane.add(lblshJobs);	

	    txtshJobs = new JTextArea();
	    txtshJobs.setBounds(240, 70, 200, 170);
	    contentPane.add(txtshJobs);

	    
	    JLabel lblJoborSheJobs = new JLabel("Sent To Cloud for Processing");
	    lblJoborSheJobs.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJoborSheJobs.setBounds(240, 250, 200, 20);
	    contentPane.add(lblJoborSheJobs);

	    txtJoborSheJobs = new JTextArea();
	    txtJoborSheJobs.setBounds(240, 280, 200, 150);
	    contentPane.add(txtJoborSheJobs);
	    
	    btnClose = new JButton("Shedule");
	    btnClose.setBounds(20,460,100,20);	    
	    btnClose.addActionListener(bl);
	    contentPane.add(btnClose);
	    
	    btnClear =  new JButton("Clear");
	    btnClear.setBounds(150,460,100,20);	    
	    btnClear.addActionListener(bl);
	    contentPane.add(btnClear);
    }
}
