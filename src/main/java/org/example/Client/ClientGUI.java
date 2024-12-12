package org.example.Client;

import org.example.figure.Circle;
import org.example.figure.Line;
import org.example.figure.Rectangle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class ClientGUI extends JFrame {
    private ClientLogic logic;

    private JComboBox<String> objectTypeBox;
    private JPanel paramsPanel;
    private CardLayout cardLayout;

    private JPanel circlePanel;
    private JTextField circleXField, circleYField, circleRadiusField;

    private JPanel rectanglePanel;
    private JTextField rectXField, rectYField, rectWidthField, rectHeightField;

    private JPanel linePanel;
    private JTextField lineX1Field, lineY1Field, lineX2Field, lineY2Field;

    private JButton sendButton, getObjectsButton;
    private JTextArea responseArea;
    private DrawingPanel drawingPanel;

    public ClientGUI(){
        super("Client REST");
        setSize(800,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logic=new ClientLogic("localhost",4567);

        drawingPanel=new DrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(400,400));
        add(drawingPanel,BorderLayout.WEST);

        JPanel rightPanel=new JPanel(new BorderLayout());
        add(rightPanel,BorderLayout.CENTER);

        JPanel topPanel=new JPanel();
        objectTypeBox=new JComboBox<>(new String[]{"Circle","Rectangle","Line"});
        topPanel.add(new JLabel("Класс Объекта:"));
        topPanel.add(objectTypeBox);
        rightPanel.add(topPanel,BorderLayout.NORTH);

        cardLayout=new CardLayout();
        paramsPanel=new JPanel(cardLayout);

        circlePanel=new JPanel(new GridLayout(3,2,5,5));
        circleXField=new JTextField("100");
        circleYField=new JTextField("200");
        circleRadiusField=new JTextField("50");
        circlePanel.add(new JLabel("X:"));circlePanel.add(circleXField);
        circlePanel.add(new JLabel("Y:"));circlePanel.add(circleYField);
        circlePanel.add(new JLabel("Радиус:"));circlePanel.add(circleRadiusField);

        rectanglePanel=new JPanel(new GridLayout(4,2,5,5));
        rectXField=new JTextField("10");
        rectYField=new JTextField("10");
        rectWidthField=new JTextField("100");
        rectHeightField=new JTextField("50");
        rectanglePanel.add(new JLabel("X:"));rectanglePanel.add(rectXField);
        rectanglePanel.add(new JLabel("Y:"));rectanglePanel.add(rectYField);
        rectanglePanel.add(new JLabel("Ширина:"));rectanglePanel.add(rectWidthField);
        rectanglePanel.add(new JLabel("Высота:"));rectanglePanel.add(rectHeightField);

        linePanel=new JPanel(new GridLayout(4,2,5,5));
        lineX1Field=new JTextField("0");
        lineY1Field=new JTextField("0");
        lineX2Field=new JTextField("200");
        lineY2Field=new JTextField("200");
        linePanel.add(new JLabel("X1:"));linePanel.add(lineX1Field);
        linePanel.add(new JLabel("Y1:"));linePanel.add(lineY1Field);
        linePanel.add(new JLabel("X2:"));linePanel.add(lineX2Field);
        linePanel.add(new JLabel("Y2:"));linePanel.add(lineY2Field);

        paramsPanel.add(circlePanel,"Circle");
        paramsPanel.add(rectanglePanel,"Rectangle");
        paramsPanel.add(linePanel,"Line");

        JPanel centerParamsPanel=new JPanel();
        centerParamsPanel.add(paramsPanel);
        rightPanel.add(centerParamsPanel,BorderLayout.CENTER);

        JPanel bottomPanel=new JPanel(new BorderLayout());
        sendButton=new JButton("Отправить");
        responseArea=new JTextArea(5,20);
        responseArea.setEditable(false);
        bottomPanel.add(new JScrollPane(responseArea),BorderLayout.NORTH);
        bottomPanel.add(sendButton,BorderLayout.CENTER);
        getObjectsButton=new JButton("Получить фигуры с сервера");
        bottomPanel.add(getObjectsButton,BorderLayout.SOUTH);
        rightPanel.add(bottomPanel,BorderLayout.SOUTH);

        objectTypeBox.addActionListener(e->{
            String sel=(String)objectTypeBox.getSelectedItem();
            cardLayout.show(paramsPanel,sel);
            drawingPanel.repaint();
        });

        DocumentListener docListener=new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){onTextChanged();}
            @Override
            public void removeUpdate(DocumentEvent e){onTextChanged();}
            @Override
            public void changedUpdate(DocumentEvent e){onTextChanged();}
            private void onTextChanged(){drawingPanel.repaint();}
        };
        circleXField.getDocument().addDocumentListener(docListener);
        circleYField.getDocument().addDocumentListener(docListener);
        circleRadiusField.getDocument().addDocumentListener(docListener);
        rectXField.getDocument().addDocumentListener(docListener);
        rectYField.getDocument().addDocumentListener(docListener);
        rectWidthField.getDocument().addDocumentListener(docListener);
        rectHeightField.getDocument().addDocumentListener(docListener);
        lineX1Field.getDocument().addDocumentListener(docListener);
        lineY1Field.getDocument().addDocumentListener(docListener);
        lineX2Field.getDocument().addDocumentListener(docListener);
        lineY2Field.getDocument().addDocumentListener(docListener);

        sendButton.addActionListener(e->sendObjectToServer());
        getObjectsButton.addActionListener(e->{
            List<Object> objs=logic.getAllObjects();
            showObjectsDialog(objs);
        });
    }

    private void sendObjectToServer(){
        String selected=(String)objectTypeBox.getSelectedItem();
        String response="";
        switch(selected){
            case "Circle":
                int cx=parseIntOrZero(circleXField.getText());
                int cy=parseIntOrZero(circleYField.getText());
                int cr=parseIntOrZero(circleRadiusField.getText());
                Circle c=new Circle(cx,cy,cr);
                response=logic.sendCircle(c);
                break;
            case "Rectangle":
                int rx=parseIntOrZero(rectXField.getText());
                int ry=parseIntOrZero(rectYField.getText());
                int rw=parseIntOrZero(rectWidthField.getText());
                int rh=parseIntOrZero(rectHeightField.getText());
                Rectangle r=new Rectangle(rx,ry,rw,rh);
                response=logic.sendRectangle(r);
                break;
            case "Line":
                int lx1=parseIntOrZero(lineX1Field.getText());
                int ly1=parseIntOrZero(lineY1Field.getText());
                int lx2=parseIntOrZero(lineX2Field.getText());
                int ly2=parseIntOrZero(lineY2Field.getText());
                Line l=new Line(lx1,ly1,lx2,ly2);
                response=logic.sendLine(l);
                break;
        }
        responseArea.append("Server: "+response+"\n");
    }

    private void showObjectsDialog(List<Object> objs){
        JDialog dialog=new JDialog(this,"Список фигур на сервере",true);
        dialog.setSize(300,200);
        DefaultListModel<String> listModel=new DefaultListModel<>();
        for(Object o:objs){
            listModel.addElement(o.toString());
        }
        JList<String> list=new JList<>(listModel);
        dialog.add(new JScrollPane(list));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int parseIntOrZero(String s){
        try{
            return Integer.parseInt(s);
        }catch(NumberFormatException e){return 0;}
    }

    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            String selected = (String) objectTypeBox.getSelectedItem();
            int leftMargin = 10;
            int topMargin = 10;
            int width = getWidth();
            int height = getHeight();
            int drawWidth = width - 2*leftMargin;
            int drawHeight = height - 2*topMargin;

            Graphics2D g2 = (Graphics2D) g;

            double scaleX = 0, scaleY = 0;
            switch (selected) {
                case "Circle":
                    int cx = parseIntOrZero(circleXField.getText());
                    int cy = parseIntOrZero(circleYField.getText());
                    int cr = parseIntOrZero(circleRadiusField.getText());
                    scaleX = (double)drawWidth / (cx + cr * 2);
                    scaleY = (double)drawHeight / (cy + cr * 2);
                    break;
                case "Rectangle":
                    int rx = parseIntOrZero(rectXField.getText());
                    int ry = parseIntOrZero(rectYField.getText());
                    int rw = parseIntOrZero(rectWidthField.getText());
                    int rh = parseIntOrZero(rectHeightField.getText());
                    scaleX = (double)drawWidth / (rx + rw);
                    scaleY = (double)drawHeight / (ry + rh);
                    break;
                case "Line":
                    int lx1 = parseIntOrZero(lineX1Field.getText());
                    int ly1 = parseIntOrZero(lineY1Field.getText());
                    int lx2 = parseIntOrZero(lineX2Field.getText());
                    int ly2 = parseIntOrZero(lineY2Field.getText());
                    int ly = Math.min(lx1, lx2);
                    int lx = Math.min(ly1, ly2);
                    int lw = Math.abs(lx2 - lx1);
                    int lh = Math.abs(ly2 - ly1);
                    scaleX = (double)drawWidth / (lx + lw);
                    scaleY = (double)drawHeight / (ly + lh);
                    break;
            }

            double scale = Math.min(Math.min(scaleX, scaleY), 1);

            g2.translate(leftMargin, topMargin);

            g2.setColor(Color.WHITE);
            g2.fillRect(0,0, drawWidth, drawHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(0,0, drawWidth, drawHeight);
            g2.drawString(new DecimalFormat("x#0.00:1").format(scale), 340,10);
            float lineWidth = (float)(scale);
            g2.setStroke(new BasicStroke(lineWidth));

            g2.scale(scale, scale);
            g2.setColor(Color.RED);
            switch (selected) {
                case "Circle":
                    int cx = parseIntOrZero(circleXField.getText());
                    int cy = parseIntOrZero(circleYField.getText());
                    int cr = parseIntOrZero(circleRadiusField.getText());
                    g2.drawOval(cx, cy, cr, cr);
                    break;
                case "Rectangle":
                    int rx = parseIntOrZero(rectXField.getText());
                    int ry = parseIntOrZero(rectYField.getText());
                    int rw = parseIntOrZero(rectWidthField.getText());
                    int rh = parseIntOrZero(rectHeightField.getText());
                    g2.drawRect(rx, ry, rw, rh);
                    break;
                case "Line":
                    int lx1 = parseIntOrZero(lineX1Field.getText());
                    int ly1 = parseIntOrZero(lineY1Field.getText());
                    int lx2 = parseIntOrZero(lineX2Field.getText());
                    int ly2 = parseIntOrZero(lineY2Field.getText());
                    g2.drawLine(lx1, ly1, lx2, ly2);
                    break;
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new ClientGUI().setVisible(true));
    }
}
