package angerona.fw.gui.view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import angerona.fw.Agent;
import angerona.fw.Angerona;
import angerona.fw.gui.AngeronaWindow;
import angerona.fw.gui.TreeController;
import angerona.fw.internal.Entity;
import angerona.fw.internal.IdGenerator;
import angerona.fw.report.ReportEntry;
import angerona.fw.report.ReportListener;

public class ReportView extends BaseView implements ReportListener {

	/** kill warning */
	private static final long serialVersionUID = 697392233654570429L;
    
    private JTree tree = new JTree();
 
    private TreeModel model = null;
    
    private DefaultMutableTreeNode rootNode;
    
    private DefaultMutableTreeNode actTickNode;
    
    private DefaultMutableTreeNode actAgentNode;
    
    public class Leaf {

    	private ReportEntry entry;
    	
    	public Leaf(ReportEntry entry) {
    		this.entry = entry;
    	}
    	
    	@Override
    	public String toString() {
    		return entry.getMessage();
    	}
    }
    
    @Override
	public void init() {
		setTitle("Report");
		
		setLayout(new BorderLayout());
		
		JLabel lbl = new JLabel("Reports");
		add(lbl, BorderLayout.NORTH);
		
		rootNode = new DefaultMutableTreeNode("Report");
		model = new DefaultTreeModel(rootNode);
		tree.setModel(model);
		JScrollPane pane = new JScrollPane(tree);
        add(pane, BorderLayout.CENTER);
        setVisible(true);
        
        MouseListener ml = new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		onMouseClick(e);
		    }
		};
		tree.addMouseListener(ml);
        Angerona.getInstance().addReportListener(this);
	}

    /**
	 * Helper method: called when user clicks on the tree 
	 * @param e structure containing data about the (click)mouse-event.
	 */
	private void onMouseClick(MouseEvent e) {
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
         if(selRow == -1)
        	 return;
         
         if(e.getClickCount() == 2) {
             selectHandler(selPath);
         }
	}
	
	private void selectHandler(TreePath selPath) {
		if(selPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
			Object uo = node.getUserObject();
			if(uo instanceof Leaf) {
				ReportEntry entry = ((Leaf)uo).entry;
				if(entry.getAttachment() == null)
					return;
				
				AngeronaWindow wnd = AngeronaWindow.getInstance();
				BaseView view = wnd.getBaseViewObservingEntity(entry.getAttachment());
				if(view != null) {
					if(view instanceof ReportListener) {
						((ReportListener)view).reportReceived(entry);
					}
					wnd.addComponentToCenter(view);
				} else {
					
				}
			}
		}
	}
    
	@Override
	public void reportReceived(ReportEntry entry) {
		Integer tick = new Integer(entry.getSimulationTick());
		Entity attach = entry.getAttachment();
		
		updateTickNode(tick);
		boolean useAgentNode = updateAgentNode(attach);
		
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Leaf(entry));
		if(useAgentNode)
			actAgentNode.add(newNode);
		else
			actTickNode.add(newNode);
		/*
		for(int i=0; i<rootNode.getChildCount(); ++i) {
			TreeNode n = rootNode.getChildAt(i);
			if(n.equals(actTickNode)) {
				tree.collapsePath(new TreePath(n));
			} else {
				for(int k=0; k<n.getChildCount(); k++) {
					TreeNode m = n.getChildAt(k);
					tree.expandPath(new TreePath(m));
				}
				tree.expandPath(new TreePath(n));
			}
		}
		tree.expandPath(new TreePath(rootNode));
		*/
		TreeController.expandAll(tree, true);
		tree.updateUI();
	}
	
	private void updateTickNode(Integer tick) {
		if(	actTickNode == null || 
			!tick.equals(actTickNode.getUserObject())) {
			actTickNode = new DefaultMutableTreeNode(tick);
			rootNode.add(actTickNode);
		}
	}

	private boolean updateAgentNode(Entity attach) {
		if(attach != null) {
			while(attach.getParent() != null) {
				attach = IdGenerator.getEntityWithId(attach.getParent());
			}
			if(attach instanceof Agent) {
				Agent ag = (Agent)attach;
				for(int i=0; i<actTickNode.getChildCount(); ++i) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)actTickNode.getChildAt(i);
					if(child.getUserObject().equals(ag)) {
						actAgentNode = child;
						return true;
					}
				}
				actAgentNode = new DefaultMutableTreeNode(ag);
				actTickNode.add(actAgentNode);
				return true;
			}
		}
		
		return false;
	}
}
