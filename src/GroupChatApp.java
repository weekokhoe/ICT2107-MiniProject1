//FIREWALL NEEDS TO BE TURN OFF FOR SOME COMPUTERS
//import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
//import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class GroupChatApp extends JFrame {

	MulticastSocket broadcastSocket = null;
	MulticastSocket multicastSocket = null;
	InetAddress multicastGroup = null;
	InetAddress broadcastGroup = null;
	private JPanel contentPane;
	private JTextField txtGroupIp;
	private JTextField textField;
	private JTextField txtUsername;
	private JTextField textCreateGroup;
	Random rand = new Random();
	boolean joinStatus = false;
	boolean joinClicked = false;
	String currentGroupName = "";
	String username;
	//the item address to be used
	String broadcastAddress = "230.1.1.1";
	//this needs to edit. Current format would generate 230.1.1.X
	//needs to generate 230.1.X.Y
	String reservedAddress1 = "230.1.1.";
	//currently no check done for IP address. A random number is given from 2-254.
	//Needs to conduct check to ensure no duplicated IP used for chat groups.
	int reservedAddress2 = rand.nextInt(252) + 2;
	//Hashmap is used to store "Groupname" as key, "IP Address" as value.
	//Currently only stored on client that creates the group
	Map<String, String> map = new HashMap<String, String>();
	JTextArea textArea;
	JList<String> listGroups = null;
	JList<String> listOnlineUsers;
	JPanel panelGroups;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GroupChatApp frame = new GroupChatApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GroupChatApp() {
		try {
			//thread to constantly check broadcast channel for new message
			broadcastSocket = new MulticastSocket(6789);
			broadcastGroup = InetAddress.getByName(broadcastAddress);
			broadcastSocket.joinGroup(broadcastGroup);
			multicastSocket = new MulticastSocket(6789);
			new Thread(new Runnable() {
				@Override
				public void run() {

					byte buf1[] = new byte[1000];
					DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);

					while (true) {
						try {
							broadcastSocket.receive(dgpReceived);
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();
							//when a string is received on broadcast channel
							String msg = new String(receivedData, 0, length);
							//check hashmap for the groupname
							//if client has created the groupname.
							//send ipaddress back to broadcast channel
							// R:230.1.1.X
							if (map.containsKey(msg)) {
								String ipReturn = "R:";
								ipReturn += map.get(msg);
								byte buf[] = ipReturn.getBytes();
								DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, broadcastGroup, 6789);
								broadcastSocket.send(dgpSend);
								//all clients will receive R:230.1.1.X
							} else if (msg.contains("R:")) {
								//Condition to ensure that the client requested to join this channel
								if (joinStatus == false && joinClicked == true && currentGroupName.equals("")){
									//Removed content that does not related to IP Address
									msg = msg.replace("R:", "");
									// Join the IP Address
									multicastGroup = InetAddress.getByName(msg);
									multicastSocket.joinGroup(multicastGroup);
									currentGroupName = "Joined";
									// this function is at btm of code.
									startThread();
									String replyMsg = username + " have joined";
									byte buf[] = replyMsg.getBytes();
									DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
									multicastSocket.send(dgpSend);
								} else {
									continue;
								}
							} else {
								continue;
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}).start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 712, 367);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblIDLabel = new JLabel("User Name");
		lblIDLabel.setBounds(10, 11, 70, 19);
		contentPane.add(lblIDLabel);

		JLabel lblCreateGroup = new JLabel("Create Group");
		lblCreateGroup.setBounds(10, 38, 80, 19);
		contentPane.add(lblCreateGroup);

		JLabel lblIPLabel = new JLabel("Join Group");
		lblIPLabel.setBounds(10, 61, 60, 19);
		contentPane.add(lblIPLabel);

		txtGroupIp = new JTextField();
		txtGroupIp.setBounds(90, 60, 86, 20);
		contentPane.add(txtGroupIp);
		txtGroupIp.setColumns(10);

		JButton btnJoin = new JButton("Join");
		btnJoin.setEnabled(false);
		btnJoin.setBounds(197, 60, 86, 20);
		contentPane.add(btnJoin);

		JButton btnLeave = new JButton("Leave");
		btnLeave.setEnabled(false);
		btnLeave.setBounds(293, 60, 70, 20);
		contentPane.add(btnLeave);

		textArea = new JTextArea();
		textArea.setBounds(384, 36, 302, 243);
		contentPane.add(textArea);

		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(10, 300, 60, 14);
		contentPane.add(lblMessage);

		textField = new JTextField();
		textField.setBounds(69, 297, 537, 20);
		contentPane.add(textField);
		textField.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.setBounds(616, 296, 70, 23);
		contentPane.add(btnSend);

		JButton btnRegister = new JButton("Register");
		btnRegister.setBounds(197, 10, 86, 20);
		contentPane.add(btnRegister);

		JButton btnCreate = new JButton("Create");
		btnCreate.setEnabled(false);
		btnCreate.setBounds(197, 37, 86, 20);
		contentPane.add(btnCreate);

		txtUsername = new JTextField();
		txtUsername.setBounds(90, 10, 86, 20);
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);

		textCreateGroup = new JTextField();
		textCreateGroup.setBounds(90, 37, 86, 20);
		contentPane.add(textCreateGroup);
		textCreateGroup.setColumns(10);
		
		JLabel lblConversation = new JLabel("Conversation");
		lblConversation.setBounds(384, 13, 103, 14);
		contentPane.add(lblConversation);
		
		DefaultListModel<String> listModel = new DefaultListModel<>();
		listModel.addElement("user1");
		listModel.addElement("User2");
		listOnlineUsers = new JList<>(listModel);
		listOnlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JLabel lblOnlineUsers = new JLabel("Online Users");
		lblOnlineUsers.setBounds(34, 107, 80, 14);
		contentPane.add(lblOnlineUsers);
		
		DefaultListModel<String> groupModel = new DefaultListModel<>();
		
		JLabel lblGroups = new JLabel("Groups");
		lblGroups.setBounds(176, 107, 80, 14);
		contentPane.add(lblGroups);
		
		JPanel panelOnlineUsers = new JPanel();
		panelOnlineUsers.setBounds(10, 120, 126, 159);
		panelOnlineUsers.add(listOnlineUsers);
		contentPane.add(panelOnlineUsers);
		
		panelGroups = new JPanel();
		panelGroups.setBounds(160, 120, 153, 159);
		contentPane.add(panelGroups);
		
		username = txtUsername.getText();

		btnJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// set flag for identifying if this client requested to join.
					joinClicked = true;
					String requestName = txtGroupIp.getText();
					byte buf[] = requestName.getBytes();
					DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, broadcastGroup, 6789);
					broadcastSocket.send(dgpSend);
					// Disable this button
					// May have to change to accommodate multiple groups criteria
					btnJoin.setEnabled(false);
					btnCreate.setEnabled(false);
					// Enable the button leave and to send message
					btnLeave.setEnabled(true);
					btnSend.setEnabled(true);

				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Send message to the group.
					String newMsg = textField.getText();
					newMsg = username + ": " + newMsg;
					byte[] buf = newMsg.getBytes();
					DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
					multicastSocket.send(dgpSend);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		btnLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String newMsg = username + " is leaving";
					byte[] buf = newMsg.getBytes();
					DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
					multicastSocket.send(dgpSend);
					multicastSocket.leaveGroup(multicastGroup);
					//remove all condition that identify joining group.
					currentGroupName = "";
					joinClicked = false;
					joinStatus = false;
					btnJoin.setEnabled(true);
					btnSend.setEnabled(false);
					btnLeave.setEnabled(false);
					btnCreate.setEnabled(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String newMsg = username + " is changing username to ";
					username = txtUsername.getText();
					newMsg += username;
					// this condition checks that the client is in  a group.
					// possible change as currently modified to only register once, unable to update username.
					if (!currentGroupName.isEmpty()){
						byte[] buf = newMsg.getBytes();
						DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
						multicastSocket.send(dgpSend);
					} else {
						//client is not in group. Does not have to inform others.
						textArea.append(newMsg + "\n");
					}
					btnCreate.setEnabled(true);
					btnJoin.setEnabled(true);
					btnRegister.setEnabled(false);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// get 230.1.1 + random number of 2-254
					//create the group
					String reservedAddress = reservedAddress1 + Integer.toString(reservedAddress2);
					InetAddress tempAddress = InetAddress.getByName(reservedAddress);
					multicastGroup = InetAddress.getByAddress(textCreateGroup.getText(), tempAddress.getAddress());
					//get another random number for next creation.
					reservedAddress2 = rand.nextInt(252)+2;
					// add to hashmap after creating
					multicastSocket = new MulticastSocket(6789);
					map.put(textCreateGroup.getText(), reservedAddress);
					currentGroupName = "Joined";
					// Join the group
					multicastSocket.joinGroup(multicastGroup);
					String message = username + " joined";
					btnJoin.setEnabled(false);
					btnCreate.setEnabled(false);
					// add in the group lists (for active/inactive purpose)
					groupModel.addElement(textCreateGroup.getText());
					if (listGroups == null){
						listGroups = new JList<>(groupModel);
						listGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						panelGroups.add(listGroups);
					}
					byte[] buf = message.getBytes();
					DatagramPacket dgpConnected = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
					multicastSocket.send(dgpConnected);

					// Create a new thread to keep listening for packets from group
					startThread();
					// Disable this button
					btnJoin.setEnabled(false);
					// Enable the button leave and to send message
					btnLeave.setEnabled(true);
					btnSend.setEnabled(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public void startThread() {
		// this function will start the thread to keep listening for new message in the group joined
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte buf1[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
				while (true) {
					try {
						multicastSocket.receive(dgpReceived);
						byte[] receivedData = dgpReceived.getData();
						int length = dgpReceived.getLength();
						// Assumed we received string
						String newMsg = new String(receivedData, 0, length);
						textArea.append(newMsg + "\n");

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}
}
