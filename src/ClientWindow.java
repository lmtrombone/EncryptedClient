import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientWindow {

	private JFrame frame;
	private JTextField filePath;
	private JButton browseButton;
	private JButton encryptButton;
	private JButton decryptButton;
	private JButton uploadButton;
	private JButton idkButton;
	private JFileChooser fileChooser;
	
	private File selectedFile;
	private JTextField keyFile;
	private JButton btnKeygen;
	private JTabbedPane tabbedPane;
	private JPanel uploadPanel;
	private JPanel searchPanel;
	private JScrollPane scrollPane;
	private JTextArea outputLog;
	private JTextField queryField;
	private JButton btnSearch;
	private JList<String> list;
	private JButton btnDownload;
	
	private DefaultListModel<String> searchResults;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientWindow window = new ClientWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientWindow() {
		fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Select");
        fileChooser.setDialogTitle("Select a file to upload...");
		initialize();
		
		// Add Handlers
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	            {
	                System.out.println(fileChooser.getSelectedFile());
	                // TODO: This will be bugged if a user types a path in instead of using the browse button
	                selectedFile = fileChooser.getSelectedFile();
	                filePath.setText(selectedFile.getAbsolutePath());
	                writeLog("Selected file: " + selectedFile.getName());
	            }
			}
		});
		
		encryptButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				writeLog("Encrypting file...\nThis doesn't even work.");
			}
		});
		
		decryptButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				writeLog("Decrypting file...\nThis doesn't even work.");
				//SSE.decryptFile(SSE.Tset, AES.secretKey);
			}
		});
		
		uploadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				writeLog("Uploading file...");
				Map<String, ArrayList<String>> map = SSE.EDBSetup(selectedFile, AES.secretKey);
                ObjectMapper mapper = new ObjectMapper();
                try {
					String json = mapper.writeValueAsString(map);
					System.out.println(json);
					HttpUtil.HttpPost(json);
				}
                
                catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnKeygen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				
				// Would you like to load or generate key?
				if (JOptionPane.showConfirmDialog(null, "Would you like to generate a new key?", "New Key?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					fileChooser.setApproveButtonText("Save");
			        fileChooser.setDialogTitle("Select a file to save key...");
			        
			        // file chooser to save key
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				        SecretKey newKey = AES.generateKey();
				        
				        // Serialize (out)
				        try {
				        	File file = fileChooser.getSelectedFile();
							ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
							out.writeObject(newKey);
							out.close();
							
							// Set key in AES
							AES.secretKey = newKey;
							keyFile.setText(file.getName());
				        } catch (IOException ex) {
							ex.printStackTrace();
						}
			        }
					
					/*try {
						//Deserialize
						ObjectInputStream in = new ObjectInputStream(new FileInputStream("MyKey.ser"));
						SecretKey readKey = (SecretKey) in.readObject();
						in.close();
						
						byte[] bytes = readKey.getEncoded();
						//String keyStr = Base64.getEncoder().encodeToString(bytes);
						//byte[] decoded = Base64.getDecoder().decode(keyStr); // Works for decoding
						keyFile.setText(Arrays.toString(bytes)); //Should store in file and display filename instead of key
					} catch (IOException | ClassNotFoundException ex) {
						ex.printStackTrace();
					}*/
				} else {
					fileChooser.setApproveButtonText("Load");
			        fileChooser.setDialogTitle("Select a file to load key...");
			        
					//file chooser to load key
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        	// Deserialize (in)
			        	try {
							File file = fileChooser.getSelectedFile();
							ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
							AES.secretKey = (SecretKey) in.readObject(); // Set secretKey
							in.close();
							
							keyFile.setText(file.getName()); //Should store in file and display filename instead of key
						} catch (IOException | ClassNotFoundException ex) {
							ex.printStackTrace();
						}
			        }
					
				}
				
				byte[] decoded = AES.secretKey.getEncoded();
				writeLog("Loaded key: " + Arrays.toString(decoded));
			}
		});
		
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] keywords = queryField.getText().split(" +");
				//post request
				//result should be list of ind
				List<String> inds = new ArrayList<>();
				for (String i : inds) {
					searchResults.addElement(i);
				}
				searchResults.addElement("Some result");
			}
		});
		
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
					/*AWSCredentials credentials;
			        try {
			            credentials = new ProfileCredentialsProvider().getCredentials();
			        } catch (Exception ex) {
			            throw new AmazonClientException(
			                    "Cannot load the credentials from the credential profiles file. " +
			                    "Please make sure that your credentials file is at the correct " +
			                    "location (~/.aws/credentials), and is in valid format.", ex);
			        }

			        // Create S3 client
			        AmazonS3 s3 = new AmazonS3Client(credentials);
			        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
			        s3.setRegion(usWest2);

			        // Upload the files
			        try {
			            byte[] bytes = file.getBytes();
			            InputStream stream = new ByteArrayInputStream(bytes);
			            ObjectMetadata metadata = new ObjectMetadata();
			            metadata.setContentLength(bytes.length);
			            s3.putObject("SOME_BUCKET", key, stream, metadata);
			        } catch (IOException ex) {
			            System.out.println("ERROR: " + ex);
			        }*/
				} else {
					// maybe produce an error message
					System.out.println("No file selected");
				}
			}
		});
	}
	
	public void writeLog(String str) {
		outputLog.append(str + "\n");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Client");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow][grow,fill][][]"));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, "cell 0 0 1 4,grow");
		
		uploadPanel = new JPanel();
		tabbedPane.addTab("Upload", null, uploadPanel, "Upload");
		uploadPanel.setLayout(new MigLayout("", "[fill][grow,fill][fill]", "[fill][][][][fill][grow,fill][fill]"));
		
		JLabel lblNewLabel = new JLabel("Path:");
		uploadPanel.add(lblNewLabel, "cell 0 0");
		
		filePath = new JTextField();
		uploadPanel.add(filePath, "cell 1 0");
		filePath.setColumns(10);
		
		browseButton = new JButton("Browse...");
		uploadPanel.add(browseButton, "cell 2 0");
		
		scrollPane = new JScrollPane();
		uploadPanel.add(scrollPane, "cell 0 1 2 5,grow");
		
		outputLog = new JTextArea();
		scrollPane.setViewportView(outputLog);
		
		encryptButton = new JButton("Encrypt");
		uploadPanel.add(encryptButton, "cell 2 1");
		
		decryptButton = new JButton("Decrypt");
		uploadPanel.add(decryptButton, "cell 2 2");
		
		uploadButton = new JButton("Upload");
		uploadPanel.add(uploadButton, "cell 2 3");
		
		idkButton = new JButton("idk");
		uploadPanel.add(idkButton, "cell 2 4");
		idkButton.setEnabled(false);
		
		keyFile = new JTextField();
		uploadPanel.add(keyFile, "cell 0 6 2 1,growx");
		keyFile.setColumns(10);
		
		btnKeygen = new JButton("Keygen");
		uploadPanel.add(btnKeygen, "cell 2 6");
		
		searchPanel = new JPanel();
		tabbedPane.addTab("Search", null, searchPanel, "Search");
		searchPanel.setLayout(new MigLayout("", "[grow][fill]", "[fill][][grow]"));
		
		queryField = new JTextField();
		searchPanel.add(queryField, "flowx,cell 0 0,growx");
		queryField.setColumns(10);
		
		btnSearch = new JButton("Search");
		searchPanel.add(btnSearch, "cell 1 0");
		
		list = new JList<>();
		searchResults = new DefaultListModel<>();
		list.setModel(searchResults);
		searchPanel.add(list, "cell 0 1 1 2,grow");
		
		btnDownload = new JButton("Download");
		searchPanel.add(btnDownload, "cell 1 1");
	}
}

