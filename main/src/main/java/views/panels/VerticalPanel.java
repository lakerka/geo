package views.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class VerticalPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -1948887405459558442L;

    private class FlowPanel extends JPanel {

        /**
         * 
         */
        private static final long serialVersionUID = 8892617885299743927L;

        FlowPanel(Component[] comps) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
            for (Component comp : comps) {
                add(comp);
            }
            setAlignmentX(0);
        }

        @Override
        public Dimension getMaximumSize() {
            return super.getPreferredSize();
        }

        public int wid() {
            return getComponent(0).getPreferredSize().width;
        }

        public void wid(int i) {
            Dimension d = getComponent(0).getPreferredSize();
            d.width = i;
            getComponent(0).setPreferredSize(d);
        }
    }

    public VerticalPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public Component add(Component comp) {
        add(new Component[] { comp });
        return comp;
    }

    @Override
    public Component add(String str, Component comp) {
        add(new Component[] { new JLabel(str), comp });
        return null;
    }
    
    public Component add(JLabel jLabel, Component comp) {
        add(new Component[] { jLabel, comp });
        return null;
    }

    public void add(String str, Component... cs) {
        Component[] ar = new Component[cs.length + 1];
        ar[0] = new JLabel(str);
        System.arraycopy(cs, 0, ar, 1, cs.length);
        add(ar);
    }

    public void add(Component... comps) {
        add(-1, comps);
    }

    @Override
    public Component add(Component comp, int index) {
        add(index, comp);
        return null;
    }

    public void add(int index, Component... comps) {
        super.add(new FlowPanel(comps), index);

        int max = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            FlowPanel comp = (FlowPanel) getComponent(i);
            max = Math.max(comp.wid(), max);
        }

        for (int i = 0; i < getComponentCount(); i++) {
            FlowPanel comp = (FlowPanel) getComponent(i);
            comp.wid(max);
        }
    }

    public void add(String str, Component comp, int index) {
        add(index, new JLabel(str), comp);
    }

    @Override
    public void add(Component comp, Object constraints) {
        throw new RuntimeException(); // not valid
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        throw new RuntimeException(); // not valid
    }

    public static class HorzPanel extends JPanel {
        /**
         * 
         */
        private static final long serialVersionUID = -903412427995603531L;

        public HorzPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }

        public void add(JComponent comp) {
            comp.setAlignmentY(0);
            super.add(comp);
        }
    }

    public static class BorderPanel extends JPanel {
        /**
         * 
         */
        private static final long serialVersionUID = 2218056218487324365L;

        public BorderPanel() {
            setLayout(new BorderLayout());
        }
    }

    //test panel
//    public static void main(String[] args) {
//
//        JFrame jFrame = new JFrame();
//        jFrame.setTitle("TEST");
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        jFrame.setPreferredSize(new Dimension(1000, 500));
//
//        VerticalPanel component = new VerticalPanel();
//
//        component.add("First panel is good", new JButton("FIRST"));
//        component.add("Second panel", new JButton("SECOND"));
//        component.add("Third", new JButton("THIRD"));
//
//        jFrame.add(component);
//
//        jFrame.pack();
//        jFrame.setVisible(true);
//    }

}
