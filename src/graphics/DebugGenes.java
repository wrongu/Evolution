package graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import physics.PointMass;

import environment.Environment;

import structure.Organism;
import structure.OrganismFactory;

public class DebugGenes extends JPanel {

	private static final long serialVersionUID = 1L;

	private OrganismDisplayPanel leftPanel, rightPanel;
	private ArrayList<OrganismDisplayPanel> childPanels;
	private Environment dummyEnv;
	private Organism leftOrg, rightOrg;

	public DebugGenes(){
		setPreferredSize(new Dimension(800,600));

		dummyEnv = new Environment(0L);
		
		leftOrg = OrganismFactory.testDummy(OrganismFactory.TRIANGLE_WITH_TAIL,dummyEnv);
		rightOrg = OrganismFactory.testDummy(OrganismFactory.TRIANGLE_WITH_TAIL,dummyEnv);
		
		leftPanel = new OrganismDisplayPanel(400, 400).setOrganism(leftOrg);
		rightPanel = new OrganismDisplayPanel(400, 400).setOrganism(rightOrg);

		childPanels = new ArrayList<OrganismDisplayPanel>();

		setLayout(new BorderLayout());
		
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		setChildrenPanel(new ArrayList<Organism>());
	}
	
	private void setChildrenPanel(ArrayList<Organism> children){
		JPanel southPanel = new JPanel();
		for(Organism o : children){
			OrganismDisplayPanel odp = new OrganismDisplayPanel(100, 100).setOrganism(o);
			childPanels.add(odp);
			southPanel.add(odp);
		}
		this.add(southPanel, BorderLayout.SOUTH);
	}

	private class OrganismDisplayPanel extends JPanel implements MouseListener{

		private static final long serialVersionUID = 1L;

		private Organism o;
		private boolean selected;

		public OrganismDisplayPanel(int w, int h){
			setPreferredSize(new Dimension(w, h));
			selected = false;
		}

		public OrganismDisplayPanel setNewSize(int w, int h){
			setPreferredSize(new Dimension(w, h));
			return this;
		}

		public OrganismDisplayPanel setOrganism(Organism org){
			o = org;
			return this;
		}

		@Override
		public void paintComponent(Graphics g){
			// background
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			// find bounds of the organism
			double minx, miny, maxx, maxy;
			minx = miny = Double.MAX_VALUE;
			maxx = maxy = Double.MIN_VALUE;
			for(PointMass pm : o.getPoints()){
				if(minx > pm.getX()) minx = Math.floor(pm.getX());
				else if(maxx < pm.getX()) maxx = Math.ceil(pm.getX());
				if(miny > pm.getY()) miny = Math.floor(pm.getY());
				else if(maxy < pm.getY()) maxy = Math.ceil(pm.getY());
			}
			// TODO - make a class that extends graphics and abstracts the viewport away
			int shiftx = (int) -(minx+maxx)/2 + getWidth()/2;
			int shifty = (int) -(miny+maxy)/2 + getHeight()/2;
			double scalex = 1.0;//this.getWidth()  / (maxx - minx + 1) / 10.0;
			double scaley = 1.0;//this.getHeight() / (maxy - miny + 1) / 10.0;
			
			o.draw((Graphics2D) g, shiftx, shifty, scalex, scaley);

			if(selected){
				g.setColor(Color.yellow);
				((Graphics2D) g).setStroke(new BasicStroke(3f));
			}
			else g.setColor(Color.gray);
			g.drawRect(0, 0, getWidth(), getHeight());
		}

		public void mouseClicked(MouseEvent arg0) {
			selectPanel(this);
		}

		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
	}

	public void selectPanel(OrganismDisplayPanel sel) {
		System.out.println("sel: "+sel);
		for(OrganismDisplayPanel odp : childPanels) odp.selected = false;
		sel.selected=true;
	}
}
