/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.team3.view;

import com.team3.network.ClientSocket;
import javax.swing.*;

public class RoomInfoFrame extends JFrame {

    public RoomInfoFrame() {
        setTitle("객실 정보");
        setSize(300, 150);

        JTextField roomField = new JTextField();
        JButton btn = new JButton("조회");

        add(new JLabel("객실 번호 입력"), "North");
        add(roomField, "Center");
        add(btn, "South");

        btn.addActionListener(e -> {
            String resp = new ClientSocket().send("ROOM " + roomField.getText());
            JOptionPane.showMessageDialog(this, resp);
        });

        setVisible(true);
    }
}
