/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pst2Gmail;

import javax.swing.UIManager;

/**
 *
 * @author toweruser
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception err) {}
        MainWindow temp = new MainWindow();
        temp.setVisible(true);
    }

}
