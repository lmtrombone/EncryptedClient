package edu.ucsb.hopefully_unhackable.client;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.utils.StringPair;
import net.miginfocom.swing.MigLayout;
import javax.swing.JProgressBar;

public class ClientWindow {
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JScrollPane scrollPane;
	private static JTextArea outputLog;
	public static File selectedFile; // TODO NO STATIC

	// Upload Tab
	private JPanel uploadPanel;
	private JTextField filePath;
	private JButton btnBrowse;
	private JButton btnUpload;

	// Search Tab
	private JPanel searchPanel;
	private JTextField queryField;
	private JButton btnSearch;
	private JList<StringPair> list;
	private DefaultListModel<StringPair> searchResults;
	private JButton btnDownload;

	// Settings Tab
	private JPanel settingPanel;
	private JComboBox<KeyItem> keyFile;
	private JButton btnKeygen;
	private JButton btnRemove;
	private JSlider matchSlider;
	private JLabel lblMinimumMatches;
	private JScrollPane scrollPane1;
	private JCheckBox ckboxUseStemmer;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
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
		initialize();

		// Load all keys
		File folder = new File("keys");
		folder.mkdirs();
		File[] files = folder.listFiles();
		boolean hasDefaultKey = false;
		for (File file : files) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
				SecretKey kS = (SecretKey) in.readObject(); // Set secretKey
				in.close();
				keyFile.addItem(new KeyItem(kS, file.getName()));
				if (file.getName().equals("defaultkey")) {
					hasDefaultKey = true;
				}
			} catch (IOException | ClassNotFoundException ex) {
				// Not a key file, don't add to list
			}
		}
		
		// Load default key
		if (hasDefaultKey) {
			keyFile.setSelectedItem(new KeyItem(null, "defaultkey"));
			AESCTR.secretKey = keyFile.getItemAt(keyFile.getSelectedIndex()).getKey();
			ClientWindow.writeLog("Successfully loaded key: defaultkey");
		} else {
			writeLog("No default key found, generating new one");
			File file = new File("keys/defaultkey");
			SecretKey newKey = AESCTR.generateKey();
			// Serialize (out)
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
				out.writeObject(newKey);
				out.close();

				AESCTR.secretKey = newKey; // Set secretKey
				
				KeyItem keyItem = new KeyItem(newKey, file.getName());
				keyFile.addItem(keyItem);
				keyFile.setSelectedItem(keyItem);
			} catch (IOException ex2) {
				writeLog("Failed to generate a default key");
				ex2.printStackTrace();
			}
		}

		// Add Handlers (Upload)
		btnBrowse.addActionListener(UploadHandlers.getBrowseHandler(filePath));
		btnUpload.addActionListener(UploadHandlers.getUploadHandler(progressBar, filePath, ckboxUseStemmer));
		btnDownload.addActionListener(SearchHandlers.getDownloadHandler(list));
		list.addMouseListener(SearchHandlers.getListClickHandler());

		// Add Handlers (Settings)
		keyFile.addActionListener(SettingsHandlers.selectKeyHandler());
		btnRemove.addActionListener(SettingsHandlers.removeKeyHandler(keyFile));
		btnKeygen.addActionListener(SettingsHandlers.getKeygenHandler(keyFile));
		
		// Add Handlers (Search)
		btnSearch.addActionListener(SearchHandlers.getSearchHandler(queryField, list, searchResults, matchSlider, ckboxUseStemmer));
		matchSlider.addChangeListener(SearchHandlers.getMatchHandler(list, searchResults));
		
		searchPanel.getRootPane().setDefaultButton(btnSearch);
	}

	public static void writeLog(String str) {
		outputLog.append(str + "\n");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Grum - Secure File Client");
		frame.setBounds(100, 100, 550, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow][grow,fill][][]"));

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		frame.getContentPane().add(tabbedPane, "cell 0 0 1 4,grow");

		uploadPanel = new JPanel();
		tabbedPane.addTab("Upload", null, uploadPanel, "Upload");
		uploadPanel.setLayout(new MigLayout("", "[fill][grow,fill][fill]", "[fill][][grow,fill][]"));

		JLabel pathLbl = new JLabel("Path:");
		uploadPanel.add(pathLbl, "cell 0 0");

		filePath = new HintTextField("Enter file path here...");

		uploadPanel.add(filePath, "cell 1 0");
		filePath.setColumns(10);

		btnBrowse = new JButton("Browse...");
		uploadPanel.add(btnBrowse, "cell 2 0");

		scrollPane = new JScrollPane();
		uploadPanel.add(scrollPane, "cell 0 1 2 2,grow");

		outputLog = new JTextArea();
		outputLog.setEditable(false);
		scrollPane.setViewportView(outputLog);

		btnUpload = new JButton("Upload");
		uploadPanel.add(btnUpload, "cell 2 1");
		
		progressBar = new JProgressBar();
		uploadPanel.add(progressBar, "cell 0 3 3 1");

		searchPanel = new JPanel();
		tabbedPane.addTab("Search", null, searchPanel, "Search");
		searchPanel.setLayout(new MigLayout("", "[grow][fill]", "[fill][][][][grow]"));

		queryField = new HintTextField("Enter keywords here...");

		searchPanel.add(queryField, "flowx,cell 0 0,growx");
		queryField.setColumns(10);

		btnSearch = new JButton("Search");
		searchPanel.add(btnSearch, "cell 1 0");
		searchResults = new DefaultListModel<>();
		
		scrollPane1 = new JScrollPane();
		searchPanel.add(scrollPane1, "cell 0 1 1 4,grow");
		
		list = new JList<>();
		scrollPane1.setViewportView(list);
		list.setModel(searchResults);

		btnDownload = new JButton("Download");
		searchPanel.add(btnDownload, "cell 1 1");
		
		lblMinimumMatches = new JLabel("Min Words to Match:");
		searchPanel.add(lblMinimumMatches, "cell 1 2");
		
		matchSlider = new JSlider();
		matchSlider.setPaintLabels(true);
		matchSlider.setMinimum(1);
		matchSlider.setSnapToTicks(true);
		matchSlider.setValue(1);
		matchSlider.setMinorTickSpacing(1);
		matchSlider.setMajorTickSpacing(1);
		matchSlider.setMaximum(1);
		searchPanel.add(matchSlider, "cell 1 3");

		settingPanel = new JPanel();
		tabbedPane.addTab("Settings", null, settingPanel, "Settings");
		settingPanel.setLayout(new MigLayout("", "[grow][][]", "[28.00,fill][]"));

		keyFile = new JComboBox<>();
		settingPanel.add(keyFile, "cell 0 0,growx");
		
		btnRemove = new JButton("Remove");
		settingPanel.add(btnRemove, "cell 1 0");

		btnKeygen = new JButton("   New   ");
		settingPanel.add(btnKeygen, "cell 2 0");
		
		ckboxUseStemmer = new JCheckBox("Use Stemmer");
		ckboxUseStemmer.setSelected(true);
		settingPanel.add(ckboxUseStemmer, "cell 0 1");
	}
}