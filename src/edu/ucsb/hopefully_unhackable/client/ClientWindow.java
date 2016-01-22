package edu.ucsb.hopefully_unhackable.client;

import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import net.miginfocom.swing.MigLayout;

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
	private JList<String> list;
	private DefaultListModel<String> searchResults;
	private JButton btnDownload;

	// Settings Tab
	private JPanel settingPanel;
	private JTextField keyFile;
	private JButton btnKeygen;

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

		// Load default key
		File file = new File("keys/defaultkey");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
			AESCTR.secretKey = (SecretKey) in.readObject(); // Set secretKey
			in.close();
			keyFile.setText(file.getName());
			writeLog("Successfully loaded default key.");
		} catch (IOException | ClassNotFoundException ex) {
			writeLog("No default key found, generating new one.");
			SecretKey newKey = AESCTR.generateKey();
			// Serialize (out)
			try {
				new File("keys").mkdirs();
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
				out.writeObject(newKey);
				out.close();

				AESCTR.secretKey = newKey; // Set secretKey
				keyFile.setText(file.getName());
			} catch (IOException ex2) {
				writeLog("Failed to generate a default key.");
				ex2.printStackTrace();
			}
		}

		// Add Handlers (Upload)
		btnBrowse.addActionListener(UploadHandlers.getBrowseHandler(filePath));
		btnUpload.addActionListener(UploadHandlers.getUploadHandler(filePath));

		// Add Handlers (Search)
		btnSearch.addActionListener(SearchHandlers.getSearchHandler(queryField, list, searchResults));
		btnDownload.addActionListener(SearchHandlers.getDownloadHandler(list));
		list.addMouseListener(SearchHandlers.getListClickHandler());

		// Add Handlers (Settings)
		btnKeygen.addActionListener(SettingsHandlers.getKeygenHandler(keyFile));

		searchPanel.getRootPane().setDefaultButton(btnSearch);
	}

	public static void writeLog(String str) {
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

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		frame.getContentPane().add(tabbedPane, "cell 0 0 1 4,grow");

		uploadPanel = new JPanel();
		tabbedPane.addTab("Upload", null, uploadPanel, "Upload");
		uploadPanel.setLayout(new MigLayout("", "[fill][grow,fill][fill]", "[fill][][grow,fill]"));

		JLabel lblNewLabel = new JLabel("Path:");
		uploadPanel.add(lblNewLabel, "cell 0 0");

		// added mouse listener
		filePath = new HintTextField("Enter file path here...");
		//addBanner(filePath, "Enter file path here...");

		uploadPanel.add(filePath, "cell 1 0");
		filePath.setColumns(10);

		btnBrowse = new JButton("Browse...");
		uploadPanel.add(btnBrowse, "cell 2 0");

		scrollPane = new JScrollPane();
		uploadPanel.add(scrollPane, "cell 0 1 2 2,grow");

		outputLog = new JTextArea();
		scrollPane.setViewportView(outputLog);

		btnUpload = new JButton("Upload");
		uploadPanel.add(btnUpload, "cell 2 1");

		searchPanel = new JPanel();
		tabbedPane.addTab("Search", null, searchPanel, "Search");
		searchPanel.setLayout(new MigLayout("", "[grow][fill]", "[fill][][grow]"));

		// added mouse listener
		queryField = new HintTextField("Enter keywords here...");
		//addBanner(queryField, "Enter keywords here...");

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

		settingPanel = new JPanel();
		tabbedPane.addTab("Settings", null, settingPanel, null);
		settingPanel.setLayout(new MigLayout("", "[grow][]", "[28.00,fill]"));

		keyFile = new JTextField();
		keyFile.setColumns(10);
		settingPanel.add(keyFile, "cell 0 0,growx");

		btnKeygen = new JButton("Keygen");
		settingPanel.add(btnKeygen, "cell 1 0");
	}
}