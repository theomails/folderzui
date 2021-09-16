package net.progressit.folderzui.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.eventbus.Subscribe;

import lombok.Builder;
import lombok.Data;
import net.progressit.folderzui.DisplayWindow.DVProblemEvent;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFileVisitEvent;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFolderEndEvent;
import net.progressit.folderzui.model.SizingFileVisitor.SFVFolderStartEvent;
import net.progressit.pcomponent.PComponent;

public class VFResultsTreePanel extends PComponent<Path, Path>{

	@Data
	public static class VFRTPFolderClickEvent{
		private final FolderDetails folder;
		private final int clickCount;
	}
	
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
	
	private final Map<Path, MutableTreeNode> pathNodesMap = new HashMap<>();
	private DefaultMutableTreeNode nodeDummyRoot = new DefaultMutableTreeNode("Select folder to scan...");
	private JTree treeFolders = new JTree();
	private JScrollPane spTreeFolders = new JScrollPane(treeFolders);

	public VFResultsTreePanel(PPlacementHandler placementHandler) {
		super(placementHandler);
	}

	@Override
	protected PDataHandler<Path> getDataHandler() {
		return new PDataHandler<Path>( (data)->Set.of( data ), (data)->Set.of() );
	}

	@Override
	protected PRenderHandler<Path> getRenderHandler() {
		return new PRenderHandler<Path>( ()-> spTreeFolders, (data)->{  }, (data)-> new PChildrenPlan() );
	}
	

	@Subscribe
	private void handleAsync(SFVFolderStartEvent event) {
		Path dir = event.getFolder();
		FolderDetails folderDetails = event.getFolderDetails();
		try {
			Path rootFolder = getData();
			if(dir.equals( rootFolder )) {
				pathNodesMap.clear();
				treeFolders.removeAll();
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
			post( DVProblemEvent.error("Error updating the folder tree ", ex) );
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
	
	public static void doLater(Runnable r) {
		SwingUtilities.invokeLater( r );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
				treeModel.setAsksAllowsChildren(true);
				treeModel.setRoot(nodeDummyRoot);
				
				treeFolders.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						TreePath selPath = treeFolders.getSelectionPath();
						if(selPath!=null) {
							DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode)selPath.getLastPathComponent());
							if(nodeDummyRoot!=selectedNode) {
								FolderDetailsNodeData nodeData = (FolderDetailsNodeData) selectedNode.getUserObject();
								post(new VFRTPFolderClickEvent(nodeData.getFolderDetails(), 1));

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
									post(new VFRTPFolderClickEvent(nodeData.getFolderDetails(), 2));

								}
							}
				        }
					}
				});
			}
			@Override
			public void postProps() {
				setData( getProps() );
			}
		};
	}

}
