package udpsineserver;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
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

public class UDPSineServer extends UDPui{

    private Thread backgroundProcess;
    private ArrayList<DataCount> countSeparate; 
//    private Boolean running = false;
    private Runnable background;

    private DatagramSocket serverSocket;
    private Boolean available = true;
    private XYSeries series;
       
    public UDPSineServer(final javax.swing.JTextArea valuePane) {
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
        countSeparate = new ArrayList<>();
        background = new Runnable() {
            public void run() {
                try {
                    serverSocket = new DatagramSocket(9876);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    boolean found = false;
//                    String url = receivePacket.getAddress().getHostAddress();
//                    for(int i = 1; i < countSeparate.size(); i++){
//                        if(url.equals(countSeparate.get(i).getKey())){
//                            found = true;
//                            break;
//                        }
//                        if(i == countSeparate.size()-1){
//                            DataCount dataCount = new DataCount();
//                            dataCount.setKey(url);
//                            dataCount.setValue(0);
//                            countSeparate.add(dataCount);
//                        }
//                    }


                    //<editor-fold defaultstate="collapsed" desc="Start timer after receive a packet">
//                    try {
//                        serverSocket.receive(receivePacket);
//                        series.clear();
//                        valuePane.setText("");
//                        available = true;
//                    } catch (IOException ex) {
//                        Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            available = false;
//                            System.out.println("Finish Timer");
//                        }
//                    }, 1 * 1000);
//</editor-fold>
                    if (!new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()).equals("")) {
                        int count = 1;
                        while (true) {
                            try {
                                receiveData = new byte[1024];
                                byte[] sendData = new byte[1024];
                                receivePacket = new DatagramPacket(receiveData, receiveData.length);

                                serverSocket.receive(receivePacket);
                                String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                                
                                System.out.println(message);
                                
                                if (message.equals("end")) {
                                    valuePane.setCaretPosition(valuePane.getDocument().getLength());
                                    //send back to mobile
                                    InetAddress IPAddress = receivePacket.getAddress();
                                    int port = receivePacket.getPort();
                                    String capitalizedSentence = count + "";
//                                    String capitalizedSentence = "";
                                    for(int i = 0; i < countSeparate.size(); i++){
                                        if(countSeparate.get(i).getKey().equals(serverSocket.getInetAddress().getHostAddress())){
                                            capitalizedSentence = countSeparate.get(i).getValue()+"";
                                            countSeparate.remove(i);
                                        }
                                    }
                                    sendData = capitalizedSentence.getBytes();
                                    DatagramPacket sendPacket
                                            = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                    serverSocket.send(sendPacket);
                                    //end send back to mobile
                                    count = 1;
                                    break;
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
                                    
                                    series.add(count, Double.parseDouble(message));

                                    valuePane.setText(valuePane.getText().toString() + count + ":" + message + "\n");
//                                    valuePane.setCaretPosition(valuePane.getDocument().getLength());
                                    count++;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(UDPui.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        };
        backgroundProcess = new Thread(background);
    }
}
