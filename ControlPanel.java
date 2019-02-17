import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

//responsible for housing/displaying all GUI elements
class ControlPanel extends JPanel implements ChangeListener, PropertyChangeListener, ActionListener
{
    ShapesPanel sPanel;
    JSlider xSlider, ySlider, zSlider, arbSlider, irSlider, igSlider, ibSlider, radSlider;
    JButton createButton, removeAllButton;
    JFormattedTextField arbXField, arbYField, arbZField, xPosField, yPosField, zPosField;

    //initializes all the GUI components
    public ControlPanel(ShapesPanel sp){
        sPanel = sp;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        xSlider = new JSlider(JSlider.VERTICAL, 0, 360, 0);
        JPanel x = createSliderPanel(xSlider, 60, true, true, "Rotate around x");
        ySlider = new JSlider(JSlider.VERTICAL, 0, 360, 0);
        JPanel y = createSliderPanel(ySlider, 60, true, true, "Rotate around y");
        zSlider = new JSlider(JSlider.VERTICAL, 0, 360, 0);
        JPanel z = createSliderPanel(zSlider, 60, true, true, "Rotate around z");
        arbSlider = new JSlider(JSlider.VERTICAL, 0, 360, 0);
        JPanel arb = createSliderPanel(arbSlider, 60, true, true, "Rotate around arb");

        arbXField = new JFormattedTextField();
        JPanel xAx = createFieldPanel(arbXField, 1, "X");
        arbYField = new JFormattedTextField();
        JPanel yAx = createFieldPanel(arbYField, 1, "Y");
        arbZField = new JFormattedTextField();
        JPanel zAx = createFieldPanel(arbZField, 1, "Z");

        //create a panel for the fields
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(xAx);
        fields.add(yAx);
        fields.add(zAx);

        irSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        JPanel irS = createSliderPanel(irSlider, 10, true, false, "Red inten");
        igSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        JPanel igS = createSliderPanel(igSlider, 10, true, false, "Green inten");
        ibSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        JPanel ibS = createSliderPanel(ibSlider, 10, true, false, "Blue inten");
        radSlider = new JSlider(JSlider.VERTICAL, 1, 51, 10); //offset by 1 later
        JPanel radS = createSliderPanel(radSlider, 10, true, false, "Light radius");

        xPosField = new JFormattedTextField();
        JPanel xPos = createFieldPanel(xPosField, 0, "X");
        yPosField = new JFormattedTextField();
        JPanel yPos = createFieldPanel(yPosField, 0, "Y");
        zPosField = new JFormattedTextField();
        JPanel zPos = createFieldPanel(zPosField, 0, "Z");

        JPanel posFields = new JPanel();
        posFields.setLayout(new BoxLayout(posFields, BoxLayout.Y_AXIS));
        posFields.add(xPos);
        posFields.add(yPos);
        posFields.add(zPos);

        createButton = new JButton("Create light");
        JPanel cButton = createButtonPanel(createButton, "c");
        removeAllButton = new JButton("Remove lights");
        JPanel rButton = createButtonPanel(removeAllButton, "r");

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(cButton);
        buttons.add(rButton);


        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(x);
        topPanel.add(y);
        topPanel.add(z);
        topPanel.add(arb);
        topPanel.add(fields);
        add(topPanel);

        JPanel botP = new JPanel(); //bottomPanel
        botP.setLayout(new BoxLayout(botP, BoxLayout.X_AXIS));
        botP.add(irS);
        botP.add(igS);
        botP.add(ibS);
        botP.add(radS);
        botP.add(posFields);
        botP.add(buttons);
        add(botP);
    }

    //generalizes creading a panel for a slider and related parts
    private JPanel createSliderPanel(JSlider slider, int spacing, Boolean paintTicks, Boolean paintLabels, String msg){
        slider.setMajorTickSpacing(spacing);
        slider.setPaintTicks(paintTicks);
        slider.setPaintLabels(paintLabels);
        slider.addChangeListener(this);
        JLabel label = new JLabel(msg);
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.add(label);
        s.add(slider);
        return s;
    }

    //generalizes creading a panel for a button and related parts
    private JPanel createFieldPanel(JFormattedTextField field, int init, String label){
        field.setValue(new Integer(init));
        field.setColumns(10);
        field.addPropertyChangeListener("value", this);
        JLabel fLabel = new JLabel(label);
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new BoxLayout(fPanel, BoxLayout.X_AXIS));
        fPanel.add(fLabel);
        fPanel.add(field);
        return fPanel;
    }

    //generalizes creading a panel for a button and related parts
    private JPanel createButtonPanel(JButton button, String command){
        button.setActionCommand(command);
        button.addActionListener(this);
        JPanel bPanel = new JPanel();
        bPanel.add(button);
        return bPanel;
    }

    //sends slider vals corresponding to angles to ShapesPanel
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if( xSlider == source || ySlider == source || zSlider == source || arbSlider == source){
            double xTheta   =   xSlider.getValue() * Math.PI/180;
            double yTheta   =   ySlider.getValue() * Math.PI/180;
            double zTheta   =   zSlider.getValue() * Math.PI/180;
            double arbTheta = arbSlider.getValue() * Math.PI/180;
            sPanel.setThetas(xTheta, yTheta, zTheta, arbTheta);
        }
    }

    //sends arbitrary axis-field vals to ShapesPanel
    public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if(source == arbXField || source == arbYField || source == arbZField){
            sPanel.setAxis( (int)arbXField.getValue(), (int)arbYField.getValue(), (int)arbZField.getValue() );
        }
    }

    //if create new light button is pressed, creates new light based on various
    //GUI element values. Otherwise, if the remove all lights button is pressed,
    //calls the removeAllLights() function
    public void actionPerformed(ActionEvent e) {
        String cmdStr = e.getActionCommand();
        if( cmdStr.equals("c") ){
            int x = (int)xPosField.getValue();
            int y = (int)yPosField.getValue();
            int z = (int)zPosField.getValue();
            double r = irSlider.getValue() / 100.0;
            double g = igSlider.getValue() / 100.0;
            double b = ibSlider.getValue() / 100.0;
            int radius = radSlider.getValue();
            sPanel.addLight( (double)x, (double)y, (double)z, r, g, b, radius );
        }
        else if( cmdStr.equals("r") ){
            sPanel.removeAllLights();
        }
    }
}
