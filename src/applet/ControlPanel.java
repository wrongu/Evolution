package applet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlPanel extends JFrame {

	private static final long serialVersionUID = -4776406492399101407L;

	private static final int BLUR_KERNEL_MIN = 8, BLUR_KERNEL_MAX = 64;
	private static final int BLUR_RESOLUTION_MIN = 1, BLUR_RESOLUTION_MAX = 100;
	private static final int BLUR_WIDTH_MIN = 1, BLUR_WIDTH_MAX = 20;

	private ControlListener theListener;
	private GenericListener generic;
	private JSlider kernelSlider, resolutionSlider, widthSlider;

	public ControlPanel(ControlListener listener){
		this.setSize(600, 300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		theListener = listener;
		generic = new GenericListener();

		addComponents();

		setVisible(true);
	}

	private void addComponents(){
		Container cp = getContentPane();

		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		JButton updater = new JButton("Update");
		updater.addActionListener(generic);

		JLabel kernelLabel = new JLabel("Blur Kernel Width", JLabel.CENTER);
		kernelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		kernelSlider = new JSlider(JSlider.HORIZONTAL, BLUR_KERNEL_MIN, BLUR_KERNEL_MAX, 10);
		kernelSlider.setMajorTickSpacing(8);
		kernelSlider.setMinorTickSpacing(2);
		kernelSlider.setPaintTicks(true);
		kernelSlider.setPaintLabels(true);

		JLabel resolutionLabel = new JLabel("Kernel Resolution", JLabel.CENTER);
		resolutionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		resolutionSlider = new JSlider(JSlider.HORIZONTAL, BLUR_RESOLUTION_MIN, BLUR_RESOLUTION_MAX, 100);
		resolutionSlider.setMajorTickSpacing(10);
		resolutionSlider.setMinorTickSpacing(2);
		resolutionSlider.setPaintTicks(true);
		resolutionSlider.setPaintLabels(true);

		JLabel widthLabel = new JLabel("Blur Width", JLabel.CENTER);
		widthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		widthSlider = new JSlider(JSlider.HORIZONTAL, BLUR_WIDTH_MIN, BLUR_WIDTH_MAX, 15);
		widthSlider.setMajorTickSpacing(5);
		widthSlider.setMinorTickSpacing(1);
		widthSlider.setPaintTicks(true);
		widthSlider.setPaintLabels(true);

		cp.add(kernelLabel);
		cp.add(kernelSlider);
		
		cp.add(resolutionLabel);
		cp.add(resolutionSlider);
		
		cp.add(widthLabel);
		cp.add(widthSlider);
		
		cp.add(updater);
	}

	private class GenericListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			theListener.valuesUpdated();
		}
	}

	public interface ControlListener{
		void valuesUpdated();
	}
	
	public int getKernel(){
		return kernelSlider.getValue();
	}
	
	public float getResolution(){
		return (float) resolutionSlider.getValue();
	}
	
	public float getBlurWidth(){
		return ((float) widthSlider.getValue()) / 2f;
	}
}
