/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_go;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Sergey
 */
public class Events_go {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(main_window.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Events_go.class.getName()).log(Level.SEVERE, null, ex);
        }
        // TODO code application logic here
        main_window main_window_class = new main_window();
        main_window_class.setLocation(10, 10);
        main_window_class.setVisible(true);
    }
    
}
