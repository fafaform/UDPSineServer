/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udpsineserver;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author ZENBOOK
 */
public class UDPui extends javax.swing.JFrame {
    private static int total_byte = 1638400;
    private Thread backgroundProcess;
    private ArrayList<DataCount> countSeparate;
    private Map<String, Integer> counting = new HashMap<String, Integer>();
//    private Boolean running = false;
    private Runnable background;

    private DatagramSocket serverSocket;
    private Boolean available = true;
    private XYSeries series;

    /**
     * Creates new form UDPui
     */
    public UDPui() {
        // <editor-fold defaultstate="collapsed" desc="Graph">
        series = new XYSeries("ECG Reading");
        series.setMaximumItemCount(50);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("ECG Reading", "Time (seconds)", "Voltage (volt)", dataset);

        final XYPlot plot = chart.getXYPlot();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.setVisible(true);
        jPanel1.setSize(600, 500);
        jPanel1.add(new ChartPanel(chart), BorderLayout.CENTER);
        jPanel1.validate();
        add(jPanel1);
        // </editor-fold>
        initComponents();
        receiveUDP();
//        tempReceiveUDP();
    }

    //<editor-fold defaultstate="collapsed" desc="tempReceiveUDP">
    private void tempReceiveUDP() {
        
        countSeparate = new ArrayList<>();
        background = new Runnable() {
            public void run() {
                try {
                    serverSocket = new DatagramSocket(9000);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                }
//                while (true) {
//                    byte[] receiveData = new byte[1024];
//                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    //<editor-fold defaultstate="collapsed" desc="Start timer after receive a packet">
//                    try {
//                        serverSocket.receive(receivePacket);
//                        series.clear();
//                        valuePane.setText("");
                    available = true;
//                        System.out.println(available);
//                    } catch (IOException ex) {
//                        Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
//                    }

//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            available = false;
//                            System.out.println("Finish Timer");
//                        }
//                    }, 1 * 1000);
//</editor-fold>
//                    if (!new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()).equals("")) {
//                        int count = 1;
//                        while (available) {
                        while (true) {
                            try {
                                byte[] receiveData = new byte[total_byte];
                                byte[] sendData = new byte[32];
                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                                serverSocket.receive(receivePacket);
                                
                                String word = receivePacket.getAddress().getHostAddress();
                                System.out.println(word);
                                
                                String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                                boolean looprun = true;

//                                System.out.println(message);
                                while (looprun) {
                                    Integer countt = counting.get(word);
                                    if (message.contains("&")) {
                                        message = message.substring(message.indexOf("&") + 1);
//                                        count++;
//                                        Integer countt = counting.get(word);
                                        if (countt == null) {
                                            counting.put(word, 1);
                                        } else {
                                            counting.put(word, countt + 1);
                                        }
//                                        System.out.println(count + ":" + message);
                                    } else {
                                        if (countt == null) {
                                            counting.put(word, 1);
                                        } else {
                                            counting.put(word, countt + 1);
                                        }
                                        System.out.println(counting.get(word));
                                        looprun = false;
                                    }
                                }

                                if (message.contains("start")) {
                                    if(counting.get(word) != null){
                                        counting.remove(word);
                                    }
                                } else if (message.contains("end")) {
                                    message = message.substring(message.indexOf("end")+3);
//                                    valuePane.setCaretPosition(valuePane.getDocument().getLength());
                                    //send back to mobile
                                    InetAddress IPAddress = receivePacket.getAddress();
                                    int port = receivePacket.getPort();
//                                    String capitalizedSentence = count + "";

                                    String capitalizedSentence = counting.get(word) + "";
                                    sendData = capitalizedSentence.getBytes();
                                    DatagramPacket sendPacket
                                            = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                    serverSocket.send(sendPacket);
                                    
                                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                                    String content = IPAddress.getCanonicalHostName() + "," + timeStamp + "," + (counting.get(word)-1) + "," + message;
                                    saveFile(content);
                                    //end send back to mobile
//                                    System.out.println(counting.get(word));
//                                    count = 1;
                                    counting.remove(word);
//                                    break;
                                } else if (available) {

//<editor-fold defaultstate="collapsed" desc="check hasmap key">
//                                    if (hm.size() > 0 && hm.containsKey(serverSocket.getInetAddress().getHostAddress())) {
//                                        hm.put(foundKey, new Integer(((int) hm.get(foundKey)) + 1));
//                                        hm.put(serverSocket.getInetAddress().getHostAddress(), new Integer(((int) hm.get(serverSocket.getInetAddress().getHostAddress())) + 1));
//                                    } else {
//                                        hm.put(serverSocket.getInetAddress().getHostAddress(), 1);
//                                        hm.entrySet().add(new Map<String, Integer>.Entry<String, Integer>());
//                                    }
//</editor-fold>
//                                    series.add(count, Double.parseDouble(message));
//                                    valuePane.setText(valuePane.getText().toString() + count + ":" + message + "\n");
//                                    valuePane.setCaretPosition(valuePane.getDocument().getLength());
//                                    count++;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                                valuePane.setText(valuePane.getText().toString() + "IOException" + "\n");
                            }
                        }
//                        valuePane.setText(valuePane.getText().toString() + "Out of while loop" + "\n");
//                    }
//                }
            }

            private void saveFile(String content) {
                try {
                    File desktop = new File(System.getProperty("user.home"), "Desktop");
                    File file = new File(desktop.getAbsoluteFile()+"/udp.csv");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fop=new FileOutputStream(file,true);
                    fop.write((content + "\n").getBytes());
                    fop.flush();
                    fop.close();
//                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
//                    valuePane.setText(valuePane.getText().toString() + timeStamp + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                    valuePane.setText(valuePane.getText().toString() + timeStamp + "\n");
                }
            }
        };
        backgroundProcess = new Thread(background);
    
    }
//</editor-fold>
    
    private void receiveUDP() {
        countSeparate = new ArrayList<>();
        background = new Runnable() {
            public void run() {
                try {
                    serverSocket = new DatagramSocket(9000);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                }
                    available = true;

                        while (true) {
                            try {
                                byte[] receiveData = new byte[total_byte];
                                byte[] sendData = new byte[32];
                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                                serverSocket.receive(receivePacket);
                                
//                                String word = receivePacket.getAddress().getHostAddress();
//                                System.out.println(word);
                                
                                String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                                boolean looprun = true;

//                                System.out.println(message);
//                                while (looprun) {
                                    
//                                    if (message.contains("&")) {
//                                        message = message.substring(message.indexOf("&") + 1);
//                                        if (countt == null) {
//                                            counting.put(message, 1);
//                                        } else {
//                                            counting.put(message, countt + 1);
//                                        }
////                                        System.out.println(count + ":" + message);
//                                    } else {
                                        
//                                        System.out.println(counting.get(message));
//                                        looprun = false;
//                                    }
//                                }
                                
//                                System.out.println(message.substring(1));
                                if (message.charAt(0) == 's') {
                                    if(counting.get(message.substring(1)) != null){
                                        System.out.println("START OR NOT NULL");
                                        counting.remove(message.substring(1));
                                    }
                                } else if (message.charAt(0) == 'e') {
                                    message = message.substring(1);
                                    //send back to mobile
                                    InetAddress IPAddress = receivePacket.getAddress();
                                    int port = receivePacket.getPort();

                                    String capitalizedSentence = counting.get(message) + "";
                                    System.out.println(capitalizedSentence);
                                    sendData = capitalizedSentence.getBytes();
                                    DatagramPacket sendPacket
                                            = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                    serverSocket.send(sendPacket);
                                    
//                                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
//                                    String content = IPAddress.getCanonicalHostName() + "," + timeStamp + "," + (counting.get(message.substring(1))-1) + "," + message;
//                                    saveFile(content);
                                    counting.remove(message);
                                } else {
                                    Integer countt = counting.get(message);
                                    if (countt == null) {
                                        counting.put(message, 1);
                                    } else {
                                        counting.put(message, countt + 1);
//                                            System.out.println(counting.size());
                                    }
                                    System.out.println(counting.get(message));
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                                valuePane.setText(valuePane.getText().toString() + "IOException" + "\n");
                            }
                        }
            }

            private void saveFile(String content) {
                try {
                    File desktop = new File(System.getProperty("user.home"), "Desktop");
                    File file = new File(desktop.getAbsoluteFile()+"/udp.csv");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fop=new FileOutputStream(file,true);
                    fop.write((content + "\n").getBytes());
                    fop.flush();
                    fop.close();
//                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
//                    valuePane.setText(valuePane.getText().toString() + timeStamp + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
                    valuePane.setText(valuePane.getText().toString() + timeStamp + "\n");
                }
            }
        };
        backgroundProcess = new Thread(background);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stBT = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        valuePane = new javax.swing.JTextArea();
        graphPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        stBT.setText("START");
        stBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stBTActionPerformed(evt);
            }
        });

        valuePane.setColumns(20);
        valuePane.setRows(5);
        valuePane.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                valuePaneInputMethodTextChanged(evt);
            }
        });
        jScrollPane1.setViewportView(valuePane);

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(stBT)
                        .addGap(36, 36, 36))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(stBT)
                .addGap(27, 27, 27))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void stBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stBTActionPerformed
        // TODO add your handling code here:
//        if(stBT.getText().toString().equals("START")){
//            stBT.setText("STOP");
        valuePane.setText("");
        series.clear();
        available = true;
        if (!backgroundProcess.isAlive()) {
            stBT.setText("CLEAR");
            backgroundProcess.start();
        }

//        }else{
//            stBT.setText("START");
//        }
    }//GEN-LAST:event_stBTActionPerformed

    private void valuePaneInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_valuePaneInputMethodTextChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_valuePaneInputMethodTextChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UDPui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UDPui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UDPui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UDPui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UDPui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel graphPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton stBT;
    private javax.swing.JTextArea valuePane;
    // End of variables declaration//GEN-END:variables
}
