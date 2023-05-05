//BaseStation.java
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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.*;


public class  BaseStation extends JFrame {
	
	List<String> list = new LinkedList<String>();
	
	class Job {

		public 	String jobid;
		public 	String jobMDid;
		public 	String priority;
		public 	String jobinfo;
		public 	String slotno;
		public 	String submittime;
		public 	String endtime;		
	}
	
	List<Job> jobs = new LinkedList<Job>();
	List<Job> bsjobs = new LinkedList<Job>();
	List<Job> orcjobs = new LinkedList<Job>();
	
	JLabel lblConectedTo;
	JTextArea txtArivedJobs;
	JTextArea txtshJobs;
	JTextArea txtJoborSheJobs;
	
	JButton btnClose;
	String DeviceId;
	String OstristerId;
	String capacity;
	String slotno;
	
	class BSLisioner implements Runnable {
		int port;
		ServerSocket server;
		Socket connection;
		int MAX_JOBS;
		
		public BSLisioner(int port) {
			this.port = port;
//			strVal = "Waiting For PEER Connection";
		}
		void RegAckMD(String md) 
		{
			Socket requestSocket= null;	
			ObjectOutputStream out=null;
			try {	
				int portid = Integer.parseInt(md);
				requestSocket =  new Socket("localhost",portid );
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();			
				out.writeObject(config.CONN_BS_ID+" " + DeviceId);
				out.flush();
			}
			catch(UnknownHostException unknownHost){
				System.err.println("Cannot Connect to an base station!");	
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
	
		/* Beginning of Run Method */
		public void run() {
			try {
				
				RegesterToOS();
				server = new ServerSocket(port);

				while (true) {
					connection = server.accept();	//Waiting for request from PEER	
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					String strVal = (String)in.readObject(); //Reading object and storing it in strVal	
				
					String[] temp;           
					temp = strVal.split(" ");  //Splitting with Space
				
					if (temp[0].equalsIgnoreCase(config.REGISTER_WITH_BS)) {
						list.add(temp[1]);
						System.out.println("Mobile Device:"+temp[1]+" Registered");
						RegAckMD(temp[1]);					
					} else if (temp[0].equalsIgnoreCase(config.CLEAR_ALL)) {
						System.out.println("Clear Commend received");						
						jobs.clear(); 
						bsjobs.clear();
						orcjobs.clear();
						
						txtArivedJobs.selectAll();
						txtArivedJobs.cut();
						txtshJobs.selectAll();
						txtshJobs.cut();
						txtJoborSheJobs.selectAll();
						txtJoborSheJobs.cut();
						
					}else if (temp[0].equalsIgnoreCase(config.MESSAGE_TYPE_JOB)) {
						System.out.println(temp[1]);				
						Job job = new Job();
						job.jobMDid = temp[1];
						job.jobid = temp[2];
						job.priority = temp[3];
						job.jobinfo = temp[4];							
						jobs.add(job);
						txtArivedJobs.append(strVal+"\n");
					} else if (temp[0].equalsIgnoreCase(config.MESSAGE_TYPE_Shedule)) {
						System.out.println("Shedule Commend received");
						slotno = temp[1];
						SheduleandExicuteJobs();
					} else if (temp[0].equalsIgnoreCase(config.JOBS_FROM_OS)) {
						txtshJobs.append(temp[1] + " Jobs Received from Orchister \n");
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

	void SheduleandExicuteJobs(){
		lblConectedTo.setText(slotno);
		lblConectedTo.setVisible(true);

		ListIterator <Job> iterator = jobs.listIterator();
		int cont = 0;
		int maxjobs = Integer.valueOf(capacity);		
		while (iterator.hasNext()) {
			Job jb = iterator.next();
			jb.slotno = slotno;
			if (jb.priority.equalsIgnoreCase(config.HIGH) && (cont < maxjobs)){
				bsjobs.add(jb);
				cont++;
			}			
		}		
		iterator = jobs.listIterator();
		while (iterator.hasNext()) {
			Job jb = iterator.next();
			jb.slotno = slotno;
			if (jb.priority.equalsIgnoreCase(config.MEDIUM) && (cont < maxjobs)){
				bsjobs.add(jb);
				cont++;
			}			
		}		
		iterator = jobs.listIterator();
		while (iterator.hasNext()) {
			Job jb = iterator.next();
			jb.slotno = slotno;
			if (jb.priority.equalsIgnoreCase(config.NORMAL) && (cont < maxjobs)){
				bsjobs.add(jb);
				cont++;
			}			
		}

//		System.out.println("BS:"+bsjobs.size() + " OS Jobs:" + (maxjobs - cont));
		
		iterator = bsjobs.listIterator();
		while (iterator.hasNext()) {
			Job jb = iterator.next();
			String strVal = "(MId:"+ jb.jobMDid+ " JobId:"+jb.jobid + " Priority:"+jb.priority+ " SlotNo:"+jb.slotno +")";
			txtshJobs.append(strVal+"\n");
		}
		txtshJobs.setVisible(true);
		
		jobs.removeAll(bsjobs);
		
		iterator = jobs.listIterator();
		String strMsg = "";
		while (iterator.hasNext()) {
			Job jb = iterator.next();
			jb.slotno = slotno;
			String strVal = "(MId:"+ jb.jobMDid+ " JobId:"+jb.jobid + " Priority:"+jb.priority+ " SlotNo:"+jb.slotno +")";
			strMsg = strMsg + strVal + " ";
			txtJoborSheJobs.append(strVal+"\n");
		}
		txtJoborSheJobs.setVisible(true);
		// MSG BS-ID NoOfJobs jobid jobinfo priority slot-no
		
		if (jobs.size() > 0) 	
			strMsg = config.SHD_ACK_OS + " " + DeviceId + " "+ jobs.size() + " "+ slotno +" " + strMsg;
		else {
			int diff = -(maxjobs - cont);
			strMsg = config.SHD_ACK_OS + " " + DeviceId + " "+ diff + " "+ slotno +" " + strMsg;
		}
// we must add jobs list to strMsg to decide Time Unit 2 or 3		
		SendMsgToOS(strMsg);
		sendAckToConnectedMobles();
	}
	
	void RegesterToOS(){
		SendMsgToOS(config.REGISTER_WITH_OS +" "+ DeviceId);
	}
	
	void SendMsgToOS(String sendmsg){
		Socket requestSocket= null;	
		ObjectOutputStream out=null;
		
		try{
			System.out.println(sendmsg);
			
			//1. Creating a Socket to Connect to the Server		
			int portid = Integer.parseInt(OstristerId);
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
	
    public static void main(String args[]){ 
    	
    	BaseStation frame = new BaseStation ("BaseStation-ID:"+ args[0]+ " Capacity:"+args[1]);
	    frame.setVisible(true); 
	    
	    frame.DeviceId = args[0];
	    frame.capacity = args[1];
	    frame.OstristerId = "4001";  	    
	    
	    int portid = Integer.parseInt(args[0]);
		Thread bsthread = new Thread ( frame.new BSLisioner(portid));    
		bsthread.setName("BSService"+args[0]);
		bsthread.start();    //Start Thread
    }
    
    void sendAckToConnectedMobles() {
//    	list.size();
		ListIterator <String> iterator = list.listIterator();
		
//		txtArivedJobs.selectAll();
//		txtArivedJobs.replaceSelection("");
		
		while (iterator.hasNext()) {
			String bs = iterator.next();			
			Socket requestSocket= null;	
			ObjectOutputStream out=null;
			
			try{

				String strVal = "";
				ListIterator <Job> bsiterator = bsjobs.listIterator();				
				while (bsiterator.hasNext()) {
					Job jb = bsiterator.next();
					if ( jb.jobMDid.equalsIgnoreCase(bs))
						strVal = strVal + "(JobId:" + jb.jobid + " Priority:"+jb.priority+ " SlotNo:"+jb.slotno +"Time : 1Unit)\n";					
				}
//list of jobs set to OS				
				bsiterator = jobs.listIterator();				
				while (bsiterator.hasNext()) {
					Job jb = bsiterator.next();
					if ( jb.jobMDid.equalsIgnoreCase(bs))
						strVal = strVal + "(JobId:" + jb.jobid + " Priority:"+jb.priority+ " SlotNo:"+jb.slotno +"Time : 2Unit)\n";					
				}

				//1. Creating a Socket to Connect to the Server		
				int portid = Integer.parseInt(bs);
				requestSocket =  new Socket("localhost",portid );
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();			
				out.writeObject(config.MESSAGE_TYPE_ACK+" " + strVal);
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
		
    }

    class ButtonsActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		sendAckToConnectedMobles();
    	}
    }	
  
    BaseStation(String s) {
    	super(s);
    	
    	ButtonsActionListener bl = new ButtonsActionListener();

    	this.setBounds(10, 10, 500, 530);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setSize(500,530);
	    
	    JPanel contentPane = new JPanel();
	    contentPane.setBackground(Color.PINK);
	    contentPane.setBounds(10, 10, 500, 530);
	    contentPane.setLayout(null);
	    this.setContentPane(contentPane);
	    
	    lblConectedTo = new JLabel("Slot No: 0");
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

	    JLabel lblshJobs = new JLabel("List of Jobs Processed");
	    lblshJobs.setHorizontalAlignment(SwingConstants.LEFT);
	    lblshJobs.setBounds(240, 40, 200, 20);
	    contentPane.add(lblshJobs);	

	    txtshJobs = new JTextArea();
	    txtshJobs.setBounds(240, 70, 200, 170);
	    contentPane.add(txtshJobs);

	    
	    JLabel lblJoborSheJobs = new JLabel("Sent To Orchid Sheduling");
	    lblJoborSheJobs.setHorizontalAlignment(SwingConstants.LEFT);
	    lblJoborSheJobs.setBounds(240, 250, 200, 20);
	    contentPane.add(lblJoborSheJobs);

	    txtJoborSheJobs = new JTextArea();
	    txtJoborSheJobs.setBounds(240, 280, 200, 150);
	    contentPane.add(txtJoborSheJobs);
	    
	    btnClose = new JButton("Close");
	    btnClose.setBounds(20,460,100,20);	    
	    btnClose.addActionListener(bl);

	    contentPane.add(btnClose);
    }
}
