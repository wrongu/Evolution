package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import environment.generators.IGenerator;

public class RandomGeneratorVisualizer {
	public static void display(IGenerator g, int size, int oct){
		JFrame window = new JFrame("Debugging RNG with "+oct+" octaves");
		window.setSize(size, size);
		JPanel p = new PixelRenderPanel(g, size, size);
		window.add(p);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static class PixelRenderPanel extends JPanel{
		private static final long serialVersionUID = 8206827629492173879L;
		
		private IGenerator gen;
		private int width, height;
		
		public PixelRenderPanel(IGenerator g, int w, int h){
			this.gen = g;
			this.width = w;
			this.height = h;
			this.setPreferredSize(new Dimension(w, h));
		}
		
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
			Color failcolor = Color.red;
			for(int x=0; x<this.width; x++){
				for(int y=0; y<this.height; y++){
					int xcoord = x-this.width/2;
					int ycoord = y-this.height/2;
					float val = (float) this.gen.terrainValue(xcoord,ycoord);
					if(xcoord*ycoord == 0) val = 1.0f;
					try{
						Color c = new Color(val, val, val);
						img.setRGB(x, this.height-1-y, c.getRGB());
					} catch (Exception e){
						System.out.println("pixel "+x+","+y+" failed with color value "+val);
						img.setRGB(x, this.height-1-y, failcolor.getRGB());
					}
				}
			}
			((Graphics2D) g).drawImage(img, null, null);
		}
	}
}
