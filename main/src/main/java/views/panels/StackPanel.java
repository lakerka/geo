package views.panels;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

//panel used to add components in vertical stack
public class StackPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 3678327555053800634L;

    public StackPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void add(JComponent comp) {
        comp.setAlignmentX(0);
        super.add(comp);
    }

    @Override
    public Dimension getMaximumSize() {
        return super.getPreferredSize();
    }
    
    //test panel 
//    public static void main(String[] args) {
//        
//        JFrame jFrame = new JFrame();
//        jFrame.setTitle("TEST");
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        jFrame.setPreferredSize(new Dimension(1000, 500));
//
//        StackPanel stackPanel = new StackPanel();
//        
//        stackPanel.add(new JButton("FIRST"));
//        stackPanel.add(new JButton("SECOND"));
//        stackPanel.add(new JButton("THIRD"));
//        
//        stackPanel.add("as", new JButton("4th of its kind"));
//        
//        jFrame.add(stackPanel);
//        
//        jFrame.pack();
//        jFrame.setVisible(true);
//    }
}
