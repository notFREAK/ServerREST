package org.example.Server;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame implements ServerLogic.ServerListener {
    private final ServerLogic logic;
    private final RestServer restServer;
    private final JButton startButton;
    private final JButton stopButton;
    private final DefaultListModel<String> objectListModel;
    private final JList<String> objectList;
    private boolean serverStarted=false;

    public ServerGUI(){
        super("Server GUI (REST)");
        setSize(400,300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        logic=new ServerLogic();
        logic.addListener(this);

        restServer=new RestServer(logic);

        startButton=new JButton("Start Server");
        stopButton=new JButton("Stop Server");
        stopButton.setEnabled(false);

        JPanel topPanel=new JPanel();
        topPanel.add(startButton);
        topPanel.add(stopButton);

        objectListModel=new DefaultListModel<>();
        objectList=new JList<>(objectListModel);
        JScrollPane scroll=new JScrollPane(objectList);

        add(topPanel,BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);

        startButton.addActionListener(e->startServer());
        stopButton.addActionListener(e->stopServer());
    }

    private void startServer(){
        if(serverStarted)return;
        logic.loadObjectsFromFile();
        restServer.startServer(4567);
        serverStarted=true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer(){
        if(!serverStarted)return;
        restServer.stopServer();
        serverStarted=false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    public void onNewObjectReceived(Object obj) {
        SwingUtilities.invokeLater(()->objectListModel.addElement(obj.toString()));
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new ServerGUI().setVisible(true));
    }
}
