/*
 * NeurioSimulatorView.java
 * 
 * Created by Zi Zhang
 * UBC ECE Capstone project 2014
 * Client: Energy Aware Inc.
 */

package neuriosimulator;

import java.awt.Color;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * The application's main frame.
 */
public class NeurioSimulatorView extends FrameView {
    // Hard coded power consumption of a few appliances
    public static final int LAUNDRY = 500;
    public static final int DRYER = 5000;
    public static final int COFFEE = 1200;
    public static final int TOASTER = 800;
    public static final int MICROWAVE = 4500;
    public static final int TV = 400;
    
    int month, day, year, hour, minute, second;
    
    String data = ""; // data to send to server
    
    Calendar cal; // System date and time
    
    int totalPower; // total power consumption

    public NeurioSimulatorView(SingleFrameApplication app) {
        super(app);
        
        totalPower = 0;

        initComponents();
        initDateTime();
        
        // Setup listeners GUI componenets of simulator app
        
        // Displays system clock date and time
        ActionListener updateDateTimeTask = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDateTime();
            }
        };
        
        // Update total power consumption
        ActionListener updatePowerTask = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updatePower();
            }
        };
        
        // Send total power consumption and epoch time to the server hosting
        // update.php and meterreading.xml
        ActionListener sendDataTask = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sendData();
            }
        };
        
        // Timers
        new Timer(1000, updateDateTimeTask).start();
        new Timer(1000, updatePowerTask).start();
        // Amount of time before sending data to server
        // Tested values >= 1000ms
        new Timer(1000, sendDataTask).start();
        
        //showDateTime();

        // The code below was not modified. It is the template provided
        // by Java Netbeans when you start writing a java desktop application
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        
        
        // Change listeners for buttons of on/off state of each appliance
        laundryButton.addItemListener(new ItemListener() {
            @Override
        public void itemStateChanged(ItemEvent event) {
            if (laundryButton.isSelected()){
                laundryButton.setText("ON");
                totalPower += LAUNDRY;
            } else {
                laundryButton.setText("OFF");
                totalPower -= LAUNDRY;
            }
        }
        });
        
        dryerButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (dryerButton.isSelected()){
                dryerButton.setText("ON");
                totalPower += DRYER;
            } else {
                dryerButton.setText("OFF");
                totalPower -= DRYER;
            }
        }
        });
        
        coffeeButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (coffeeButton.isSelected()){
                coffeeButton.setText("ON");
                totalPower += COFFEE;
            } else {
                coffeeButton.setText("OFF");
                totalPower -= COFFEE;
            }
        }
        });
        
        toasterButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (toasterButton.isSelected()){
                toasterButton.setText("ON");
                totalPower += TOASTER;
            } else {
                toasterButton.setText("OFF");
                totalPower -= TOASTER;
            }
        }
        });
        
        microwaveButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (microwaveButton.isSelected()){
                microwaveButton.setText("ON");
                totalPower += MICROWAVE;
            } else {
                microwaveButton.setText("OFF");
                totalPower -= MICROWAVE;
            }
        }
        });
        
        tvButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (tvButton.isSelected()){
                tvButton.setText("ON");
                totalPower += TV;
            } else {
                tvButton.setText("OFF");
                totalPower -= TV;
            }
        }
        });
        
        otherButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
            String otherAppliance = otherTextField.getText();
            if (otherButton.isSelected()){
                otherButton.setText("ON");
                totalPower += Integer.parseInt(otherAppliance);
            } else {
                otherButton.setText("OFF");
                totalPower -= Integer.parseInt(otherAppliance);
            }
        }
        });
    }
    
    // Get system clock date and time
    public void initDateTime() {
        cal = new GregorianCalendar();
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        second = cal.get(Calendar.SECOND);
    }
    
    // Display system clock and time on gui app
    public void showDateTime() {
        cal = Calendar.getInstance();
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        second = cal.get(Calendar.SECOND);
        
        // How the time and date should be displayed
        String smonth = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        
        switch(month + 1) {
            case 1:
                smonth = "JAN";
                break;
            case 2:
                smonth = "FEB";
                break;
            case 3:
                smonth = "MAR";
                break;
            case 4:
                smonth = "APR";
                break;
            case 5:
                smonth = "MAY";
                break;
            case 6:
                smonth = "JUN";
                break;
            case 7:
                smonth = "JUL";
                break;
            case 8:
                smonth = "AUG";
                break;
            case 9:
                smonth = "SEP";
                break;
            case 10:
                smonth = "OCT";
                break;
            case 11:
                smonth = "NOV";
                break;
            case 12:
                smonth = "DEC";
                break;
            default: smonth = "Invalid month";
                break;
        }
        
        dateLabel.setText(smonth + " " + day + " " + year);
        timeLabel.setText(sdf.format(cal.getTime()));
    }
    
    public void sendData() {
        // Create post string
        try {
            data = "totalPower=" + totalPower + "&time=" + dateToEpoch();
            //data = URLEncoder.encode("totalPower", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(totalPower), "UTF-8");
            //data += URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(dateToEpoch()), "UTF-8");
        } catch(Exception ex) {
            System.out.println("Error sendData: creating post string");
        }
        
        // Send Data to Page
        try {
            // URL of php page that will write the new total power consumption
            // and epoch time values to the meterreading.xml
            URL url = new URL("http://www.ugrad.cs.ubc.ca/~b7d6/update.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data);
            writer.flush();
            
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            writer.close();
            reader.close(); 
        } catch (Exception ex) {
            System.out.println("Error sendData: sending data to page");
        }
        System.out.println(data);
        System.out.println("Sending: " + totalPower + ":" + String.valueOf(dateToEpoch()));
    }
    
    // Display total power consumption on simulator app
    public void updatePower() {
        totalPowerLabel.setText("" + totalPower);
        
        //System.out.println(totalPower);
    }
    
    // The simulator uses 24-hour time format but time must be in epoch time when
    // writing to the meterreading.xml file
    public long dateToEpoch() {
        Calendar currentDate = new GregorianCalendar(year, month, day, hour, minute, second);
        return currentDate.getTimeInMillis() / 1000;
    }

    // Everything below was not modified and provided by Netbeans for all
    // Java Desktop Applications
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = NeurioSimulatorApp.getApplication().getMainFrame();
            aboutBox = new NeurioSimulatorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        NeurioSimulatorApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        laundryLabel = new javax.swing.JLabel();
        dryerLabel = new javax.swing.JLabel();
        coffeeLabel = new javax.swing.JLabel();
        toasterLabel = new javax.swing.JLabel();
        microwaveLabel = new javax.swing.JLabel();
        waterheaterLabel = new javax.swing.JLabel();
        otherLabel = new javax.swing.JLabel();
        laundryButton = new javax.swing.JToggleButton();
        dryerButton = new javax.swing.JToggleButton();
        toasterButton = new javax.swing.JToggleButton();
        microwaveButton = new javax.swing.JToggleButton();
        tvButton = new javax.swing.JToggleButton();
        otherTextField = new javax.swing.JTextField();
        coffeeButton = new javax.swing.JToggleButton();
        otherButton = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        datePanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        totalPowerPanel = new javax.swing.JPanel();
        totalPowerLabel = new javax.swing.JLabel();
        watts = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(neuriosimulator.NeurioSimulatorApp.class).getContext().getResourceMap(NeurioSimulatorView.class);
        buttonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("buttonPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("buttonPanel.border.titleFont"))); // NOI18N
        buttonPanel.setName("buttonPanel"); // NOI18N

        laundryLabel.setText(resourceMap.getString("laundryLabel.text")); // NOI18N
        laundryLabel.setName("laundryLabel"); // NOI18N

        dryerLabel.setText(resourceMap.getString("dryerLabel.text")); // NOI18N
        dryerLabel.setName("dryerLabel"); // NOI18N

        coffeeLabel.setText(resourceMap.getString("coffeeLabel.text")); // NOI18N
        coffeeLabel.setName("coffeeLabel"); // NOI18N

        toasterLabel.setText(resourceMap.getString("toasterLabel.text")); // NOI18N
        toasterLabel.setName("toasterLabel"); // NOI18N

        microwaveLabel.setText(resourceMap.getString("microwaveLabel.text")); // NOI18N
        microwaveLabel.setName("microwaveLabel"); // NOI18N

        waterheaterLabel.setText(resourceMap.getString("waterheaterLabel.text")); // NOI18N
        waterheaterLabel.setName("waterheaterLabel"); // NOI18N

        otherLabel.setText(resourceMap.getString("otherLabel.text")); // NOI18N
        otherLabel.setName("otherLabel"); // NOI18N

        laundryButton.setBackground(resourceMap.getColor("laundryButton.background")); // NOI18N
        laundryButton.setText(resourceMap.getString("laundryButton.text")); // NOI18N
        laundryButton.setFocusPainted(false);
        laundryButton.setFocusable(false);
        laundryButton.setName("laundryButton"); // NOI18N

        dryerButton.setText(resourceMap.getString("dryerButton.text")); // NOI18N
        dryerButton.setFocusPainted(false);
        dryerButton.setFocusable(false);
        dryerButton.setName("dryerButton"); // NOI18N

        toasterButton.setText(resourceMap.getString("toasterButton.text")); // NOI18N
        toasterButton.setFocusPainted(false);
        toasterButton.setFocusable(false);
        toasterButton.setName("toasterButton"); // NOI18N

        microwaveButton.setText(resourceMap.getString("microwaveButton.text")); // NOI18N
        microwaveButton.setFocusPainted(false);
        microwaveButton.setFocusable(false);
        microwaveButton.setName("microwaveButton"); // NOI18N

        tvButton.setText(resourceMap.getString("tvButton.text")); // NOI18N
        tvButton.setFocusPainted(false);
        tvButton.setFocusable(false);
        tvButton.setName("tvButton"); // NOI18N

        otherTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        otherTextField.setText(resourceMap.getString("otherTextField.text")); // NOI18N
        otherTextField.setCaretColor(resourceMap.getColor("otherTextField.caretColor")); // NOI18N
        otherTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        otherTextField.setName("otherTextField"); // NOI18N

        coffeeButton.setText(resourceMap.getString("coffeeButton.text")); // NOI18N
        coffeeButton.setFocusPainted(false);
        coffeeButton.setFocusable(false);
        coffeeButton.setName("coffeeButton"); // NOI18N

        otherButton.setText(resourceMap.getString("otherButton.text")); // NOI18N
        otherButton.setFocusPainted(false);
        otherButton.setFocusable(false);
        otherButton.setName("otherButton"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(laundryLabel)
                    .addComponent(dryerLabel)
                    .addComponent(toasterLabel)
                    .addComponent(coffeeLabel)
                    .addComponent(waterheaterLabel)
                    .addGroup(buttonPanelLayout.createSequentialGroup()
                        .addComponent(otherLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(otherTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addComponent(microwaveLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(toasterButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(coffeeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dryerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(laundryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(microwaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tvButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(otherButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(laundryLabel)
                    .addComponent(laundryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dryerLabel)
                    .addComponent(dryerButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coffeeLabel)
                    .addComponent(coffeeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toasterLabel)
                    .addComponent(toasterButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(microwaveLabel)
                    .addComponent(microwaveButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(waterheaterLabel)
                    .addComponent(tvButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(otherLabel)
                    .addComponent(otherButton)
                    .addComponent(otherTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(389, Short.MAX_VALUE))
        );

        datePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("datePanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("datePanel.border.titleFont"))); // NOI18N
        datePanel.setName("datePanel"); // NOI18N

        dateLabel.setFont(resourceMap.getFont("dateLabel.font")); // NOI18N
        dateLabel.setText(resourceMap.getString("dateLabel.text")); // NOI18N
        dateLabel.setName("dateLabel"); // NOI18N

        timeLabel.setFont(resourceMap.getFont("timeLabel.font")); // NOI18N
        timeLabel.setText(resourceMap.getString("timeLabel.text")); // NOI18N
        timeLabel.setName("timeLabel"); // NOI18N

        javax.swing.GroupLayout datePanelLayout = new javax.swing.GroupLayout(datePanel);
        datePanel.setLayout(datePanelLayout);
        datePanelLayout.setHorizontalGroup(
            datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateLabel)
                    .addComponent(timeLabel))
                .addContainerGap(131, Short.MAX_VALUE))
        );
        datePanelLayout.setVerticalGroup(
            datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datePanelLayout.createSequentialGroup()
                .addComponent(dateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeLabel))
        );

        dateLabel.getAccessibleContext().setAccessibleName(resourceMap.getString("dateLabel.AccessibleContext.accessibleName")); // NOI18N

        totalPowerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("totalPowerPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("totalPowerPanel.border.titleFont"))); // NOI18N
        totalPowerPanel.setName("totalPowerPanel"); // NOI18N

        totalPowerLabel.setFont(resourceMap.getFont("totalPowerLabel.font")); // NOI18N
        totalPowerLabel.setText(resourceMap.getString("totalPowerLabel.text")); // NOI18N
        totalPowerLabel.setName("totalPowerLabel"); // NOI18N

        watts.setFont(resourceMap.getFont("watts.font")); // NOI18N
        watts.setText(resourceMap.getString("watts.text")); // NOI18N
        watts.setName("watts"); // NOI18N

        javax.swing.GroupLayout totalPowerPanelLayout = new javax.swing.GroupLayout(totalPowerPanel);
        totalPowerPanel.setLayout(totalPowerPanelLayout);
        totalPowerPanelLayout.setHorizontalGroup(
            totalPowerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalPowerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(totalPowerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(watts)
                .addContainerGap(103, Short.MAX_VALUE))
        );
        totalPowerPanelLayout.setVerticalGroup(
            totalPowerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalPowerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(totalPowerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPowerLabel)
                    .addComponent(watts))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(54, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(totalPowerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(datePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(datePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalPowerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(neuriosimulator.NeurioSimulatorApp.class).getContext().getActionMap(NeurioSimulatorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 428, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JToggleButton coffeeButton;
    private javax.swing.JLabel coffeeLabel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JPanel datePanel;
    private javax.swing.JToggleButton dryerButton;
    private javax.swing.JLabel dryerLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton laundryButton;
    private javax.swing.JLabel laundryLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JToggleButton microwaveButton;
    private javax.swing.JLabel microwaveLabel;
    private javax.swing.JToggleButton otherButton;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JTextField otherTextField;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JToggleButton toasterButton;
    private javax.swing.JLabel toasterLabel;
    private javax.swing.JLabel totalPowerLabel;
    private javax.swing.JPanel totalPowerPanel;
    private javax.swing.JToggleButton tvButton;
    private javax.swing.JLabel waterheaterLabel;
    private javax.swing.JLabel watts;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    
    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        int state = itemEvent.getStateChange();
        if (state == ItemEvent.SELECTED) {
          laundryButton.setText("Off");
          System.out.println("Selected");
        } else {
          System.out.println("Deselected");
          laundryButton.setText("On");
          //laundryButton.setBackground(Color.GREEN);
        }
      }
    };
}
