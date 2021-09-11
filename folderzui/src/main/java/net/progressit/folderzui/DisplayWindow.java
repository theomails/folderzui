package net.progressit.folderzui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.DrawPanel.DPRenderException;
import net.progressit.folderzui.model.Scanner;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFileVisitEvent;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFolderEndEvent;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFolderStartEvent;
import net.progressit.folderzui.model.SizingFileVisitor.SFVProblemEvent;

public class DisplayWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	@Data
	@Builder
	private static class FolderDetailsNodeData{
		private Path path;
		private FolderDetails folderDetails;
		@Override
		public String toString() {
			long fullSize = ( folderDetails==null?0:folderDetails.getFullSize() );
			long ownSize = ( folderDetails==null?0:folderDetails.getSize() );
			String ownSizeString = " (files " + asSizeString(ownSize) + ")";
			String sizeString = " [" + asSizeString(fullSize) + (ownSize>0?ownSizeString:"") + "]";
			long fullCount = ( folderDetails==null?0:folderDetails.getFullCount() );
			long ownCount = ( folderDetails==null?0:folderDetails.getCount() );
			String ownCountString = " (files " + asCountString(ownCount) + ")";
			String countString = " [" + asCountString(fullCount) + (ownCount>0?ownCountString:"") + "]";
			return path.getName(path.getNameCount()-1) + (fullSize>0?sizeString:"") + (fullCount>0?countString:"");
		}
		private String asSizeString(double size) {
			if(size>1_000_000_000) {
				return String.format("%.1f GB", size/1e9);
			}else if(size>1_000_000) {
				return String.format("%.1f MB", size/1e6);
			}else if(size>1_000) {
				return String.format("%.1f KB", size/1e3);
			}else {
				return String.format("%.0f B", size);
			}
		}
		private String asCountString(double size) {
			if(size>1_000_000_000) {
				return String.format("%.1f G", size/1e9);
			}else if(size>1_000_000) {
				return String.format("%.1f M", size/1e6);
			}else if(size>1_000) {
				return String.format("%.1f K", size/1e3);
			}else {
				return String.format("%.0f", size);
			}
		}
	}
	
	public enum DVProblemType {FINE, WARN, ERROR}
	
	@Data
	public static class DVProblemEvent{
		private final DVProblemType type;
		private final String message;
		private final Throwable exception;
		public static DVProblemEvent fine(String message) {
			return new DVProblemEvent(DVProblemType.FINE, message, null);
		}
		public static DVProblemEvent warn(String message) {
			return new DVProblemEvent(DVProblemType.WARN, message, null);
		}
		public static DVProblemEvent error(String message, Throwable e) {
			return new DVProblemEvent(DVProblemType.ERROR, message, e);
		}
	}

	@Getter
	private final EventBus eventBus = new EventBus();
	private Scanner scanner;
	private Path rootFolder = null;
	private final Map<Path, MutableTreeNode> pathNodesMap = new HashMap<>();
	public DisplayWindow(Scanner scanner) {
		this.scanner = scanner;
		this.eventBus.register(this);
	}

	//Top bar
	private JPanel pnlBrowse = new JPanel(new MigLayout("insets 0", "[][grow, fill][][]", "[]"));
	private JLabel lblPath = new JLabel("Folder to scan");
	private JTextField tfPath = new JTextField();
	private JButton btnBrowse = new JButton("Browse...");
	private JButton btnScan = new JButton("Scan");
	
	//Display area
	private JPanel pnlResults = new JPanel(new MigLayout("insets 0", "[300::, grow, fill][400::, grow, fill]", "[grow, fill]"));
	private DefaultMutableTreeNode nodeDummyRoot = new DefaultMutableTreeNode("Select folder to scan...");
	private JTree treeFolders = new JTree();
	private JScrollPane spTreeFolders = new JScrollPane(treeFolders);
	private DrawPanel drawPanel = new DrawPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);
	
	//Footer
	private JPanel pnlStatus = new JPanel(new MigLayout("insets 0", "[grow, fill]", "[]"));
	private JLabel lblStatus = new JLabel("Ready.");
	public void init() {
		add(pnlBrowse, BorderLayout.NORTH);
		//Display area
		add(pnlResults, BorderLayout.CENTER);
		add(pnlStatus, BorderLayout.SOUTH);

		//Top bar
		pnlBrowse.add(lblPath);
		pnlBrowse.add(tfPath);
		pnlBrowse.add(btnBrowse);
		pnlBrowse.add(btnScan);
		
		//Results
		pnlResults.add(spTreeFolders);
		pnlResults.add(spDrawPanel);
		
		//Footer
		pnlStatus.add(lblStatus);

		DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
		treeModel.setAsksAllowsChildren(true);
		treeModel.setRoot(nodeDummyRoot);
		
		initData();

		addHandlers();

		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Visualize Folder");
	}
	
	private void initData() {
		tfPath.setText(System.getProperty("user.home"));
	}

	private void addHandlers() {
		MouseWheelListener[] listeners = spDrawPanel.getMouseWheelListeners();
		for (MouseWheelListener lis : listeners) {
			spDrawPanel.removeMouseWheelListener(lis);
		}

		spDrawPanel.addMouseWheelListener(new MyMouseWheelListener(drawPanel, spDrawPanel));

		btnBrowse.addActionListener(new MyActionListener(this, tfPath));
		
		btnScan.addActionListener( (e)->{
			pathNodesMap.clear();
			treeFolders.removeAll();
			DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
			treeModel.setRoot(nodeDummyRoot);

			try {
				lblStatus.setText("Scanning...");
				rootFolder = Paths.get(tfPath.getText());
				new InvokeScannerThread(this, drawPanel, rootFolder, lblStatus, scanner).start();
			}catch(RuntimeException ex) {
				eventBus.post( DVProblemEvent.error("Invalid path for the scan.. " + tfPath.getText(), ex) );
			}
			
		} );
		
		treeFolders.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selPath = treeFolders.getSelectionPath();
				if(selPath!=null) {
					DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode)selPath.getLastPathComponent());
					if(nodeDummyRoot!=selectedNode) {
						FolderDetailsNodeData nodeData = (FolderDetailsNodeData) selectedNode.getUserObject();
						try {
							drawPanel.setDetails(nodeData.getFolderDetails());
						} catch (DPRenderException ex) {
							eventBus.post( DVProblemEvent.error(ex.getMessage(), ex.getCause()) );
						}
					}
				}
			}
		});
		treeFolders.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = treeFolders.getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = treeFolders.getPathForLocation(e.getX(), e.getY());
		        if(selRow != -1 && e.getClickCount() == 2) {
					if(selPath!=null) {
						DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode)selPath.getLastPathComponent());
						if(nodeDummyRoot!=selectedNode) {
							FolderDetailsNodeData nodeData = (FolderDetailsNodeData) selectedNode.getUserObject();
							try {
								Desktop.getDesktop().open(nodeData.path.toFile());
							} catch (IOException ex) {
								eventBus.post( DVProblemEvent.error("Unable to open the folder ", ex) );
							}
						}
					}
		        }
			}
		});
	}
	
	public static void doLater(Runnable r) {
		SwingUtilities.invokeLater( r );
	}
	
	@Subscribe
	private void handleAsync(SFVFolderStartEvent event) {
		Path dir = event.getFolder();
		FolderDetails folderDetails = event.getFolderDetails();
		try {
			if(dir.equals(rootFolder)) {
				DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();			
				//Make and save
				FolderDetailsNodeData nodeData = FolderDetailsNodeData.builder().path(dir).folderDetails(folderDetails).build();
				MutableTreeNode node = new DefaultMutableTreeNode( nodeData, true );
				pathNodesMap.put(dir, node);
				//Add to tree
				doLater( ()->{
					treeModel.setRoot(node);
				});
			}else {
				DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
				//Find parent
				MutableTreeNode nodeParent = pathNodesMap.get(dir.getParent());
				//Make and save
				FolderDetailsNodeData nodeData = FolderDetailsNodeData.builder().path(dir).folderDetails(folderDetails).build();
				MutableTreeNode node = new DefaultMutableTreeNode( nodeData, true );
				pathNodesMap.put(dir, node);
				//Add to tree
				if(nodeParent!=null) {
					doLater( ()->{
						treeModel.insertNodeInto(node, nodeParent, treeModel.getChildCount(nodeParent));
					});
				}
			}
		}catch(RuntimeException ex) {
			eventBus.post( DVProblemEvent.error("Error updating the folder tree ", ex) );
		}
	}
	@Subscribe
	private void handleAsync(SFVFolderEndEvent event) {
		
	}
	@Subscribe
	private void handleAsync(SFVFileVisitEvent event) {
		Path dir = event.getFile().getParent();
		MutableTreeNode nodeDir = pathNodesMap.get(dir);
		
		DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
		doLater( ()->{
			treeModel.nodeChanged(nodeDir);
			treeModel.nodeStructureChanged(nodeDir);
		});
		
	}
	@Subscribe
	private void handleAsync(SFVProblemEvent event) {
		doLater( ()->{ 
			lblStatus.setText( event.getMessage() );
		} );
	}

	@RequiredArgsConstructor
	private static class InvokeScannerThread extends Thread{
		private final DisplayWindow displayWindow;
		private final DrawPanel drawPanel;
		private final Path rootFolder;
		private final JLabel lblStatus;
		private final Scanner scanner;
		
		@Override
		public void run() {
			try {
				Map<Path, FolderDetails> allDetailsContainer = new HashMap<>();
				scanner.scan( rootFolder, allDetailsContainer, displayWindow );
				
				doLater( ()->{
					try {
						drawPanel.setDetails(allDetailsContainer.get(rootFolder));
					} catch (DPRenderException ex) {
						displayWindow.getEventBus().post( DVProblemEvent.error(ex.getMessage(), ex.getCause()) );
					}
				} );
				lblStatus.setText("Ready.");
			} catch (IOException ex) {
				displayWindow.getEventBus().post( DVProblemEvent.error("Error scanning the folder: " + rootFolder, ex.getCause()) );
			}
		}
	}

	@RequiredArgsConstructor
	private static class MyMouseWheelListener implements MouseWheelListener{
		private final DrawPanel drawPanel;
		private final JScrollPane spDrawPanel;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			//System.out.println(e);
			if (e.isControlDown()) {
				int rotation = -1 * e.getWheelRotation();
				rotation %= 3;

				double scale = (5d + dbl(rotation)) / 5d;
				int width = (int) (dbl(drawPanel.getWidth()) * scale);
				int height = (int) (dbl(drawPanel.getWidth()) * scale);
				width = (width < spDrawPanel.getWidth()) ? spDrawPanel.getWidth() : width;
				height = (height < spDrawPanel.getHeight()) ? spDrawPanel.getHeight() : height;
				drawPanel.setPreferredSize(new Dimension(width, height));
				drawPanel.setSize(new Dimension(width, height));
				drawPanel.repaint();
			} else {
				// pass the event on to the scroll pane
				// getParent().dispatchEvent(e);
			}			
		}
		private double dbl(long val) {
			return ((double) val);
		}
	}
	
	@RequiredArgsConstructor
	private static class MyActionListener implements ActionListener{
		private final DisplayWindow displayWindow;
		private final JTextField tfPath;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File( tfPath.getText() ));
			chooser.setDialogTitle("Choose Folder to Scan...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(displayWindow) == JFileChooser.APPROVE_OPTION) {
				File result = chooser.getSelectedFile();
				if(!result.isDirectory()) {
					result = chooser.getCurrentDirectory();
				}
				tfPath.setText( result.toPath().toString() );
			}
		}
	}
}
