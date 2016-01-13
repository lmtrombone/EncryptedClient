import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

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

import org.eclipse.wb.swing.FocusTraversalOnArray;

import net.miginfocom.swing.MigLayout;

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
	private JTextField textField;
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
			}
		});
		
		uploadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				writeLog("Uploading file...\nThis doesn't even work.");
			}
		});
		
		btnKeygen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyFile.setText("Key was generated (not really...)");
			}
		});
		
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchResults.addElement("Some result");
			}
		});
		
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
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
		uploadPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{lblNewLabel, filePath, browseButton, scrollPane, outputLog, encryptButton, decryptButton, uploadButton, idkButton, keyFile, btnKeygen}));
		
		searchPanel = new JPanel();
		tabbedPane.addTab("Search", null, searchPanel, "Search");
		searchPanel.setLayout(new MigLayout("", "[grow][fill]", "[fill][][grow]"));
		
		textField = new JTextField();
		searchPanel.add(textField, "flowx,cell 0 0,growx");
		textField.setColumns(10);
		
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

