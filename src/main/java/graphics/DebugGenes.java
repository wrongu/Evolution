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

import bio.organisms.OrganismFactory;
import bio.organisms.PointRodOrganism;


import environment.Environment;
import environment.TestEnvironment;
import environment.physics.PointMass;


public class DebugGenes extends JPanel {

	private static final long serialVersionUID = 1L;

	private OrganismDisplayPanel leftPanel, rightPanel;
	private ArrayList<OrganismDisplayPanel> childPanels;
	private Environment dummyEnv;
	private PointRodOrganism leftOrg, rightOrg;

	public DebugGenes(){
		setPreferredSize(new Dimension(800,600));

		dummyEnv = new TestEnvironment(0L, true);
		
		leftOrg = OrganismFactory.testDummy(OrganismFactory.TRIANGLE_WITH_TAIL,dummyEnv);
		rightOrg = OrganismFactory.testDummy(OrganismFactory.TRIANGLE_WITH_TAIL,dummyEnv);
		
		leftPanel = new OrganismDisplayPanel(400, 400).setOrganism(leftOrg);
		rightPanel = new OrganismDisplayPanel(400, 400).setOrganism(rightOrg);

		childPanels = new ArrayList<OrganismDisplayPanel>();

		setLayout(new BorderLayout());
		
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		setChildrenPanel(new ArrayList<PointRodOrganism>());
	}
	
	private void setChildrenPanel(ArrayList<PointRodOrganism> children){
		JPanel southPanel = new JPanel();
		for(PointRodOrganism o : children){
			OrganismDisplayPanel odp = new OrganismDisplayPanel(100, 100).setOrganism(o);
			childPanels.add(odp);
			southPanel.add(odp);
		}
		this.add(southPanel, BorderLayout.SOUTH);
	}

	private class OrganismDisplayPanel extends JPanel implements MouseListener{

		private static final long serialVersionUID = 1L;

		private PointRodOrganism o;
		private boolean selected;

		public OrganismDisplayPanel(int w, int h){
			setPreferredSize(new Dimension(w, h));
			selected = false;
		}

		public OrganismDisplayPanel setNewSize(int w, int h){
			setPreferredSize(new Dimension(w, h));
			return this;
		}

		public OrganismDisplayPanel setOrganism(PointRodOrganism org){
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
				if(minx > pm.getPosX()) minx = Math.floor(pm.getPosX());
				else if(maxx < pm.getPosX()) maxx = Math.ceil(pm.getPosX());
				if(miny > pm.getPosY()) miny = Math.floor(pm.getPosY());
				else if(maxy < pm.getPosY()) maxy = Math.ceil(pm.getPosY());
			}
			// TODO - make a class that extends graphics and abstracts the viewport away
			int shiftx = (int) -(minx+maxx)/2 + getWidth()/2;
			int shifty = (int) -(miny+maxy)/2 + getHeight()/2;
			float scalex = 1f;//this.getWidth()  / (maxx - minx + 1) / 10.0;
			float scaley = 1f;//this.getHeight() / (maxy - miny + 1) / 10.0;
			
			o.draw((Graphics2D) g, (float) shiftx, (float) shifty, scalex, scaley);

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
