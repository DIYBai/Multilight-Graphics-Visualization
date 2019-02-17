import java.awt.*;
import javax.swing.*;

//basic draw class sets up different panels and actually creates window
public class Draw
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setTitle("Lights and stuff");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ShapesPanel sPanel = new ShapesPanel();
        ControlPanel cPanel = new ControlPanel(sPanel);
        Container cp = frame.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(sPanel, BorderLayout.CENTER);
        cp.add(cPanel,BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }
}
