package src;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;
import javax.swing.JTextArea;

public class ClientWindow {

	private JFrame frame;
	private JTextField filePath;
	private JTextArea outputLog;
	private JButton browseButton;
	private JButton encryptButton;
	private JButton decryptButton;
	private JButton uploadButton;
	private JButton idkButton;
	private JScrollPane scrollPane;
	private JFileChooser fileChooser;
	
	private File selectedFile;

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
		frame.getContentPane().setLayout(new MigLayout("", "[pref!,grow][grow][pref!,fill]", "[fill][][][][][grow]"));
		
		JLabel lblNewLabel = new JLabel("Path:");
		frame.getContentPane().add(lblNewLabel, "cell 0 0,alignx trailing");
		
		filePath = new JTextField();
		frame.getContentPane().add(filePath, "cell 1 0,growx");
		filePath.setColumns(10);
		
		browseButton = new JButton("Browse...");
		frame.getContentPane().add(browseButton, "cell 2 0");
		
		scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, "cell 0 1 2 5,grow");
		
		outputLog = new JTextArea();
		outputLog.setEditable(false);
		scrollPane.setViewportView(outputLog);
		
		encryptButton = new JButton("Encrypt");
		frame.getContentPane().add(encryptButton, "cell 2 1,aligny top");
		
		decryptButton = new JButton("Decrypt");
		frame.getContentPane().add(decryptButton, "cell 2 2");
		
		uploadButton = new JButton("Upload");
		frame.getContentPane().add(uploadButton, "cell 2 3");
		
		idkButton = new JButton("idk");
		idkButton.setEnabled(false);
		frame.getContentPane().add(idkButton, "cell 2 4");
	}

}
