
package accesohuella;

import static accesohuella.Login.connect;
import com.zkteco.biometric.FingerprintSensorEx;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author manuel.vargas
 */
public class Busqueda {
    
      
  String id = null;
  int idEnMemoria;
  String nombreUS;
  String clave;
    
    public void buscar(Long mhDB,byte[] template) throws ClassNotFoundException, SQLException, IOException{
        //Primero buscar por medio de la clave ingresada por el usuario
      Connection con = connect();
      int ret;
      Image rpta=null;
    
   clave = JOptionPane.showInputDialog("Ingresa la clave de usuario: ");
if(clave==null || clave.length()==0)
{JOptionPane.showMessageDialog(null, "No has ingresado nigún valor valido");}else{    
//if(clave!=null || clave!="" ){
        try{
         int clav = Integer.parseInt(clave);
    if(clav==idEnMemoria){
    JOptionPane.showMessageDialog(null, "¡El usuario con el ID: '"+ clave +"' ya se encuentra en la memoria!","ERROR",JOptionPane.INFORMATION_MESSAGE);
    }else{
        
        try{
   PreparedStatement st = con.prepareStatement("SELECT * FROM usuariosconhuellas WHERE idusuariosConHuellas LIKE '%"+clave+"%'");
    ResultSet rs = st.executeQuery();
                        if (rs.next()) {                
   idEnMemoria = rs.getInt("idusuariosConHuellas");//udEnMemoria es una variable instanciada.
   Blob bytesImagen = rs.getBlob("Huella");
   rpta = javax.imageio.ImageIO.read(bytesImagen.getBinaryStream());
   //Creamos la imagen en disco:
 ImageIO.write((RenderedImage) rpta, "bmp", new File("C:\\Users\\manuel.vargas\\Documents\\NetBeansProjects\\AccesoHuella\\copia_Huella.bmp"));
 int[] TamañoPlantilla = new int[1];
 TamañoPlantilla[0] = 2048;
 byte[] plantillaDevuelta = new byte[2048];
 
 //Establecemos la ruta en donde creamos la imagen en disco:
 String ruta = "C:\\Users\\manuel.vargas\\Documents\\NetBeansProjects\\AccesoHuella\\copia_Huella.bmp"; 
 ret = FingerprintSensorEx.ExtractFromImage( mhDB, ruta, 500, plantillaDevuelta, TamañoPlantilla);//Registramos cada imagen 
 if(ret==0){//Si ret=0 significa que todo es corecto, entonces procedemos a registrar en memoria
     
 ret = FingerprintSensorEx.DBAdd( mhDB, idEnMemoria, plantillaDevuelta);//Guardamos la plantilla en memoria
 }
 
 //DBCount va sumando usuarios, dependiendo del número en el que se encuentre!
 ret = FingerprintSensorEx.DBCount(mhDB);//DbCount nos dice cuantos usuarios existen en memoria.
 if(ret>=0){
 JOptionPane.showMessageDialog(null, ret + " Usuarios en meoria");//Aquí nos muestra cuantos usuarios tenemos en memoria por medio de un mensaje en pantalla

 
 }else{//Si ret es menor a 0 significa que algo salio mal, y lo mostramos en el siguiente mensaje:
 JOptionPane.showMessageDialog(null, "Ocurrio un error al contar los usuarios en memoria: " + ret);
 }
            
 con.close();//Cerramos la conexión
 
                        }else{JOptionPane.showMessageDialog(null,"USUARIO NO ENCONTRADO!","ERROR DE OPERACIÓN",JOptionPane.ERROR_MESSAGE);
                        Login prueba = new Login();
                        ret = FingerprintSensorEx.DBCount(mhDB);//Cuenta cuantos usuarios existen en memoria
                        if(ret>0){/*Si existe algún usuario en memoria borra el último usuario ingresado en memoria
                                    que si existe, el cual debe estar almacenado en la variable idEnMemoria*/
                        prueba.Borrar(idEnMemoria,mhDB);
                        }
                        con.close();}

    }catch (Exception e){JOptionPane.showMessageDialog(null, e);}
    }
    }catch(Exception e){System.out.println("ERROR: "+e);}
    }
    }
    
    public void busqueda1(int[] fid
    ,JLabel lblFoto, int[] score,JLabel Bienvenidolbl,JLabel Nombrelbl) throws IOException, SQLException, ClassNotFoundException, InterruptedException{
    //Después generar la búsqueda por medio del ID del usuario.
        int mandar = fid[0];
        
Connection con = connect();//Establecemos conexión

PreparedStatement st = con.prepareStatement("SELECT * FROM usuariosconhuellas WHERE idusuariosConHuellas LIKE '%"+mandar+"%'");
 ResultSet rs = st.executeQuery();
 
                        if (rs.next()) {
 id = rs.getString("idusuariosConHuellas");
 nombreUS = rs.getString("Nombre");
 Blob foto1 = rs.getBlob("Foto");
 Image foto = javax.imageio.ImageIO.read(foto1.getBinaryStream());//Función que lee la foto que se recibe de la base de dato 
 
 //MUESTRA LOS RESULTADOS EN PANTALLA:
 lblFoto.setIcon(new ImageIcon (foto));//Muestra la foto que esta dentro de la variable foto en el botón.
menu menu = new menu();
menu.btnFoto1.setIcon(new ImageIcon(foto));
mostrarlbl(Bienvenidolbl,Nombrelbl);

Nombrelbl.setText(nombreUS);

//Regresa idEnMemoria a 0, para evitar errores.
idEnMemoria = 0;
                        }
                         
                    	JOptionPane.showMessageDialog(null,"Identificación completa, el usuario: " + nombreUS /*fid[0]
                                Fid es un array en donde se almacena el número del usuario que fue registrado*/ +"\n Con el ID: "+id+ " obtuvo un puntaje de: " + score[0] +" En identificación."/*
                        En score se almacena el puntuaje que obtuvo el lector de huellas.*/);
                        
                        con.close();
                         
    }
 
    public void mostrarlbl(JLabel Bienvenidolbl,JLabel Nombrelbl){
        Bienvenidolbl.setVisible(true);
        Nombrelbl.setVisible(true);
        
    }
    
}
