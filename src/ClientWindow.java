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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

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
	private JButton uploadButton;
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
				if (AES.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
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
		
		uploadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectedFile == null) {
					JOptionPane.showMessageDialog(null, "Please select a file");
					return;
				}
				writeLog("Encrypting file...");
				//for now uses same key to encrypt keywords
				String key = UUID.randomUUID().toString();
				Map<String, String> map = SSE.EDBSetup(selectedFile, AES.secretKey, key);
                ObjectMapper mapper = new ObjectMapper();
                try {
					String json = mapper.writeValueAsString(map);
					System.out.println(json);
					writeLog("Indexing file...");
					HttpUtil.HttpPost(json);
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
					writeLog("Upload failed!");
					return;
				}
                writeLog("Uploading file...");
                FileUtils.uploadFile(selectedFile, key);
                writeLog("Upload successful!");
                JOptionPane.showMessageDialog(null, "Upload successfull!");
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
							JOptionPane.showMessageDialog(null, "Invalid key file!");
						}
			        }
					
				}
				
				byte[] decoded = AES.secretKey.getEncoded();
				writeLog("Loaded key: " + Arrays.toString(decoded));
			}
		});
		
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AES.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				//String[] keywords = queryField.getText().split(" +");
				String[] keyWord = queryField.getText().split(" ");
				if(keyWord.length != 1) {
					System.out.println("Only single word search is supported."
							+ "Searching for documents with only the first search term.");
				}
				//get request
				//result should be list of ind
				//HashMap<String, ArrayList<String>> encIndex = HttpUtil.HttpGet(keyWord[0]);
				//HashMap<String, String> encIndex = HttpUtil.HttpGet(keyWord[0]);
				
				//gets set of encrypted ids and decrypts
				//SecurityHelperCTR securityHelperCTR = new SecurityHelperCTR();
				//ArrayList <String> values, ids = new ArrayList<String>();
				//ArrayList <String> ids = new ArrayList<String>();
				/*
				for (Entry<String, ArrayList<String>> entry : encIndex.entrySet()) {
					String key = entry.getKey();
					System.out.println("Key: " + key);
					values = entry.getValue();
					for(int i = 0; i < values.size(); i++){
						System.out.println("Values: " + values.get(i));
						SecretKey kE = SHA256.createIndexingKey(AES.secretKey, key);
						ids.add(securityHelperCTR.decrypt(values.get(i), kE));
					}
				}
				*/
				SecretKey kE = SHA256.createIndexingKey(AES.secretKey, keyWord[0]);
				List<String> inds = Collections.emptyList();
				if (!keyWord[0].isEmpty()) {
					String encWord = SHA256.createIndexingString(kE, keyWord[0]).replace("+", "X"); // remove + signs TEMP FIX TODO
					inds = HttpUtil.HttpGet(encWord);
				}
				String[] ids = inds.toArray(new String[inds.size()]);
				
				searchResults.clear();
				if (ids.length == 0) {
					searchResults.addElement("No results...");
				} else {
					SecurityHelperCTR securityHelperCTR = new SecurityHelperCTR();
					String[] x = new String[ids.length];
					for(int i = 0; i < ids.length; i++) {
						x[i] = securityHelperCTR.decrypt(ids[i], kE);
						searchResults.addElement(x[i]);
					}
				}
			}
		});
		
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setApproveButtonText("Save");
			        fileChooser.setDialogTitle("Select a file...");
			        
			        // file chooser to save file
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        	//JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
			        	String path = fileChooser.getSelectedFile().getAbsolutePath();
						FileUtils.downloadFile(path, list.getSelectedValue());
						writeLog("Downloaded to " + path);
						JOptionPane.showMessageDialog(null, "Downloaded to " + path);
			        }
				} else {
					// maybe produce an error message
					System.out.println("No file selected");
					JOptionPane.showMessageDialog(null, "No file selected");
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
		
		uploadButton = new JButton("Upload");
		uploadPanel.add(uploadButton, "cell 2 1");
		
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

