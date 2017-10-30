package com.flickr;

import com.flickr.utils.TextAreaOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class FlickrUploaderFrontEnd extends Frame implements ActionListener{

    JFrame frame;
    JButton btnStart;
    JButton btnStop;
    JProgressBar progressBar;

    public FlickrUploaderFrontEnd() {
        frame = new JFrame("FlickrUploader");

        btnStart = new JButton("Start");
        btnStart.setBounds(10, 10, 80, 30);// setting button position
        btnStart.addActionListener(this);

        btnStop = new JButton("Stop");
        btnStop.setBounds(100, 10, 80, 30);// setting button position
        btnStop.setEnabled(false);
        btnStop.addActionListener(this);

        JTextArea ta = new JTextArea();
        TextAreaOutputStream taos = new TextAreaOutputStream( ta, 60 );
        PrintStream ps = new PrintStream( taos );
        System.setOut( ps );
        System.setErr( ps );
        ta.setBounds(10, 90, 560, 200);
        JScrollPane scrollPane = new JScrollPane(ta);
        scrollPane.setBounds(10, 60, 560, 200);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBounds(10, 280, 560, 20);
        progressBar.setIndeterminate(false);


        frame.add(btnStart);//adding button into frame
        frame.add(btnStop);//adding button into frame
        frame.add(scrollPane);
        frame.add(progressBar);

        frame.setSize(600, 360);//frame size 300 width and 300 height
        frame.setLayout(null);//no layout manager
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnStart) {
            System.out.println("Start");
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);

            FlickrUploader.isRunning = true;
            try {
                FlickrUploader flickrUploader = new FlickrUploader(progressBar);
                flickrUploader.start();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        } else if(e.getSource() == btnStop){
            System.out.println("Stop");
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            FlickrUploader.isRunning = false;
        }
    }

    public static void main(String[] args) throws Exception {
        FlickrUploaderFrontEnd flickrUploaderFrontEnd = new FlickrUploaderFrontEnd();

        flickrUploaderFrontEnd.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

    }
}
