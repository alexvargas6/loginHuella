
package accesohuella;

import com.zkteco.biometric.FingerprintSensorEx;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author manuel.vargas
 */
class Identificación {

    public boolean identificarUsuario(long mhDB, byte[] template, JLabel lblFoto, JLabel btnImg,
            JLabel Bienvenidolbl,JLabel Nombrelbl) throws InterruptedException, IOException, SQLException, ClassNotFoundException {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    
    Busqueda buc = new Busqueda();
    Login prueba = new Login();
    
					int[] fid = new int[1];
					int[] score = new int [1];
					int ret = FingerprintSensorEx.DBIdentify(mhDB,
                                                template/*Plantilla de huella digital*/,
                                                fid/*El ID del usuario*/,
                                                score/*El puntaje de comparación*/);/*DBIdentify Esta función se utiliza para realizar una comparación 1: N.
                                        Compara la huella con las otras huellas ya registradas antes*/
                    if (ret == 0)
                    {
                      
                        buc.busqueda1(fid, lblFoto, score, Bienvenidolbl, Nombrelbl);/*Manda los parametros a la función búsqueda
                        para mostrar la información del usuario en pantalla*/
          
                        int i = Integer.parseInt(buc.id);//Parseamos el id del usuario y lo movemos a la variable i
                         prueba.Borrar(i,mhDB);/*Ese valor lo mandamos a la función borrar para proceder a eliminar al usuario
                         de la memoria*/
                          cambioVentana();
      return true;
                    } 
                    else
                        {
			JOptionPane.showMessageDialog(null,"Verificación fallida, Por favor,\n" + "Intentelo de nuevo.","VERIFICACIÓN FALLIDA, ERROR: " + ret,JOptionPane.INFORMATION_MESSAGE);
                                               
                        //prueba.Borrar(buc.idEnMemoria, mhDB);
                       
                        }
    return false;
    }
    
public void cambioVentana(){
        
          menu dg = null;
     try {
dg = new menu();
} catch (Exception ex) {
Logger.getLogger(Login.class.getName()
).log(Level.SEVERE, null, ex);
}
dg.setLocationRelativeTo(null);
dg.setVisible(true);//Hace visible a dg que en este caso es menu
//log.setVisible(false);//Hace invisible a no que en este caso es Login
//log.repaint();
     }
    
    }
    

