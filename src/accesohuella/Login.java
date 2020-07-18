
package accesohuella;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author manuel.vargas
 */
public class Login extends javax.swing.JFrame {
private long mhDevice = 0;//Si esta en 0 significa que el dispositivo no se encuentra abierto
private int cbRegTemp = 0;
public boolean bRegister = false;
//Identificar
private boolean bIdentify = true;
 //identificación del dedo
private int iFid = 1;
 //el índice de la función de prerregistro
public int enroll_idx;//Te dice el número de registro en el que estas
private long mhDB = 0;//Catch handle
//el ancho de la imagen de la huella digital
int anchoHuella = 0;
//la altura de la imagen de la huella digital
int altoHuella = 0;
public static Connection connection;
private boolean mbStop = true;
private byte[] imgbuf = null;
private byte[] template = new byte[2048];
private int[] templateLen = new int[1];
private WorkThread workThread = null;
private final int nFakeFunOn = 1;
 //para verificar la prueba
private final byte[] lastRegTemp = new byte[2048];
Busqueda buc = new Busqueda();

    public Login() {
        initComponents(); 
        lblFoto.setText(null);
        btnImg.setText(null);
        lblFoto.setIcon(new ImageIcon("img/user.png"));
        btnImg.setVisible(false);
        Bienvenidolbl.setVisible(false);
        Nombrelbl.setVisible(false);
        this.setTitle("Login");
        
    }

      public static Connection connect() throws ClassNotFoundException{
    try{
  
         connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pruebahuellas","root","1234"); //Conexión que contiene nuestra
         //URL, USUARIO Y CONTRASEÑA la almacenamos en connection.
       System.out.println("Conectando...");
    
        
    }catch(SQLException e){
        JOptionPane.showMessageDialog(null, "No es posible conectar con el servidor\n"
        +"Por favor intentelo mas tarde:\n  " + e,"Error de operación",JOptionPane.ERROR_MESSAGE);
        
    }
    return connection; //Retornamos la conexión para usarla en otras clases.
    }
    
     private class WorkThread extends Thread {
        EscribirMapaBit mapa = new EscribirMapaBit();        
        @Override
	        public void run() {
	            super.run();
	            int ret = 0;
	            while (!mbStop) {//Si mbStop es true esta cerrado si es false esta abierto.
	            	templateLen[0] = 2048;
                        
                        /*Una vez abierto el dispositivo ret será menor a 0, al momento de colocar
                        una huella y sea detectada por AcquireFingerPrint este valor cambiara a 0.*/
	            	if (0 == (ret = FingerprintSensorEx/*Es la clase que se usa para 
                                conectar con los dispositivos de huella*/.AcquireFingerprint/*Se usa para extraer la huella digital*/
        (mhDevice/*Nuestro dispositivo*/, imgbuf/*Datos de la imagen(Ancho y alto)*/,
                template/*Datos de la plantilla*/, templateLen/*Longitud de la plantilla que va a devolver*/)))
	            	{
	            		if (nFakeFunOn == 1)
                    	{
                    		byte[] paramValue = new byte[4];
            				int[] size = new int[1];
            				size[0] = 4;
            				int nFakeStatus = 0;
            				//GetFakeStatus
            				ret = FingerprintSensorEx.GetParameters(mhDevice, 2004, paramValue, size);
            				nFakeStatus = mapa.byteArrayToInt(paramValue);
            				System.out.println("ret : "+ ret +",nFakeStatus: " + nFakeStatus);
            				if (0 == ret && (byte)(nFakeStatus & 31) != 31)
            				{
            					JOptionPane.showMessageDialog(null,"¿¬¬?");
            					return;
            				}
                    	}
                    	MostrarHuella(imgbuf);//Muestra la imagen de la huella en el botón
                                try {
                                    OnExtractOK(template, templateLen[0]);//Función que llama a otras funciones dependiendo del botón que se use.
                                } catch (FileNotFoundException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (SQLException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (ClassNotFoundException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                }
	            	}
	                try {
	                    Thread.sleep(500);/*Se detiene la ejecución del hilo en 0.5 segundos
                            Lo cual es equivalente a 500 milisegundos*/
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }

	            }
	        }
    }
    
     public void abrir(){
         // TODO Auto-generated method stub
	EscribirMapaBit mapa = new EscribirMapaBit();
      try{			
      if (0 != mhDevice)//Si mhDevice es diferente a 0 significa que el dispositivo ya se encuentra abierto
				{
					//already inited
					System.out.println("El dispositivo ya se encuentra abierto");
					return;
				}
				int ret = FingerprintSensorErrorCode.ZKFP_ERR_OK;
				//Initialize
				cbRegTemp = 0;
				bRegister = false;
				bIdentify = false;
				iFid = 1;
				enroll_idx = 0;
                                
                                /*FingerprintSensor.class
                                es una clase para controlar lectores de huellas 
                                digitales, que se puede usar para iniciar y apagar 
                                un lector de huellas digitales, verificar e identificar.*/
                                
				if (FingerprintSensorErrorCode.ZKFP_ERR_OK != FingerprintSensorEx.Init())//Init, sirve para iniciar
				{
					JOptionPane.showMessageDialog(rootPane, "Ah ocurrido un error, verifique que su\n"+
                                                "dispositivo de huella este conectado.","ERROR EN LA OPERACIÓN",JOptionPane.ERROR_MESSAGE);
					return;
				}
				ret = FingerprintSensorEx.GetDeviceCount();
				if (ret < 0)
				{
					JOptionPane.showMessageDialog(rootPane, "No hay dispositivos conectados!");
					Cerrar();
					return;
				}
				if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0)))//OpenDevice nos conecta con el dispositivo
				{//Cuando el dispositivo se conecta, el valor de mhDevice cambia, deja de ser 0 para ser otro número.
					JOptionPane.showMessageDialog(rootPane, "Dispositivo abierto falla, ret = " + ret + "!");
					Cerrar();
					return;
				}
				if (0 == (mhDB = FingerprintSensorEx.DBInit()))/*DBInit Esta función
                                    se utiliza para inicializar la biblioteca de algoritmos.*/
				{
					JOptionPane.showMessageDialog(rootPane, "Init DB fail, ret = " + ret + "!");
					Cerrar();
					return;
				}
				
				//For ISO/Ansi
				int nFmt = 0;	//Ansi
				FingerprintSensorEx.DBSetParameter(mhDB,  5010, nFmt);				
				//Para ISO/Ansi Fin
				
				//establecer fakefun off
				//FingerprintSensorEx.SetParameter(mhDevice, 2002, changeByte(nFakeFunOn), 4);
				
				byte[] paramValue = new byte[4];
				int[] size = new int[1];
				//GetFakeOn
				//size[0] = 4;
				//FingerprintSensorEx.GetParameters(mhDevice, 2002, paramValue, size);
				//nFakeFunOn = byteArrayToInt(paramValue);
				
				size[0] = 4;
				FingerprintSensorEx.GetParameters(mhDevice/*Nuestro dispositivo*/, 1/*Cuantos dispositivos*/,
                                        paramValue/*Valor del parametro*/, size/*Tamaño de datos del parametro*/);
				anchoHuella = mapa.byteArrayToInt(paramValue);//ANCHO
				size[0] = 4;
				FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
				altoHuella = mapa.byteArrayToInt(paramValue);//ALTO
				//width = fingerprintSensor.getImageWidth();
				//height = fingerprintSensor.getImageHeight();
				imgbuf = new byte[anchoHuella*altoHuella];
				btnImg.resize(anchoHuella, altoHuella);//Le damos al botón los valores de alto y ancho
				mbStop = false;
				workThread = new WorkThread();
			    workThread.start();// Inicio de hilo, la función de workThread.
	            JOptionPane.showMessageDialog(rootPane, "El dispositivo se encuentra abierto","ABIERTO",JOptionPane.INFORMATION_MESSAGE);
                   
        }catch(Exception e){JOptionPane.showConfirmDialog(rootPane, e);}
    }
     
       private void Cerrar()
	{
		mbStop = true;
		try { //esperar a que se detenga el hilo
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (0 != mhDB)
		{
			FingerprintSensorEx.DBFree(mhDB);
			mhDB = 0;
		}
		if (0 != mhDevice)
		{
			FingerprintSensorEx.CloseDevice(mhDevice);
			mhDevice = 0;
		}
		FingerprintSensorEx.Terminate();
               
	}
       
       
       public void identificación(){
      int ret = FingerprintSensorEx.DBCount(mhDB);
      
        if(ret>0){
        int[] fid = new int[1];
					/*if(bRegister)//bRegister es un booleanos que es true unicamente para registrar usuarios
				{
					enroll_idx = 0;
					bRegister = false;
				}*/
				if(!bIdentify)
				{
					bIdentify = true;//Pasamos bIdentify a true para ejecutar nuestra siguiente función.
				}                               
//Lo decimos al usuario por medio de un mensaje en pantalla que coleque su dedo en el lector:
JOptionPane.showMessageDialog(null, "Por favor, pon tu dedo en el lector.","VERIFICACIÓN",JOptionPane.INFORMATION_MESSAGE);

 } else{bIdentify = false;}
    }
       
       public void Borrar(int i,long mhDB) throws InterruptedException{
       int ret;
       
       //En el parametro i recibimos el ID del usuario

        ret = FingerprintSensorEx.DBDel(mhDB, i);//Esta linea se encarga de borrar al usuario, que en este caso
        //El usuario es i.
        
        if(ret!=0){
       JOptionPane.showMessageDialog(rootPane, "Error al intentar borrar usuarios de memoria: " + ret);
        }
        
    }
       
        private void MostrarHuella(byte[] imgBuf)
		{
                    EscribirMapaBit wm = new EscribirMapaBit();
			try {
				wm.writeBitmap(imgBuf, anchoHuella, altoHuella, "Huella.bmp");
				btnImg.setIcon(new ImageIcon(ImageIO.read(new File("Huella.bmp"))));//Botón
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
       
     private void OnExtractOK(byte[] template, int len) throws FileNotFoundException, SQLException, ClassNotFoundException, IOException, InterruptedException//<-WorkThread llama a esta función
		{
			/*if(bRegister)//Si bRegister es true, se esta usando el botón de registrar
			{
                            Registro re = new Registro();
                            re.registrarse(template, len, bRegister, mhDB,enroll_idx, regtemparray, iFid, cbRegTemp, lastRegTemp);
                            enroll_idx++;
			}
			else
			{*/
				if (bIdentify)/*bIdentify es true, entonces no se ha usado el botón de verificar.
                                    Si bIdentify es true significa que se esta usando el botón de identiciar, ya
                                    que este devuelve true como valor.*/
                                    /*Esta función identifica las huellas registradas y te dice cual fue la huella que se 
                                    registro, siempre y cuando esta coincida con alguna.*/
				{
                                    
                                    Identificación ident = new Identificación();
                        boolean verificar = ident.identificarUsuario(mhDB, template, lblFoto, btnImg,Bienvenidolbl,Nombrelbl);
                        if(verificar){
                        this.setVisible(false);
                        }	
                        }
				else/*Si DBIdentify es false, entonces el botón verificar se esta usando. 
                                    Ya que verificar devuelve false como valor*/
                                    
                                    /*La función verificación sirve para verificar si el e identificar al último
                                    Usuario registrado.*/
				{
					if(cbRegTemp <= 0)//Si registro e identificación son false
					{//Entonces aparece este mensaje:
						JOptionPane.showMessageDialog(null,"¡NO IDENTIFICADO!");
					}
					else
					{
						int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);//Bdmatch comprara.
                                                //Esta función se utiliza para comparar dos plantillas de huellas digitales.
						if(ret > 0)
						{
							JOptionPane.showMessageDialog(null,"Verificación completa, score =" + ret);
						}
						else
						{
							JOptionPane.showMessageDialog(null,"Verificación fallida, ret =" + ret);
						}
					}
				}
			//}
		}
        
        
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnIdentificarse = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        Bienvenidolbl = new javax.swing.JLabel();
        Nombrelbl = new javax.swing.JLabel();
        lblFoto = new javax.swing.JLabel();
        btnImg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnIdentificarse.setText("IDENTIFICARSE");
        btnIdentificarse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIdentificarseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnIdentificarse))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(btnIdentificarse)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        Bienvenidolbl.setText("BIENVENIDO");

        Nombrelbl.setText("jLabel2");

        lblFoto.setText("jLabel1");

        btnImg.setText("jLabel1");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(lblFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Bienvenidolbl)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(Nombrelbl)))
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnImg)
                        .addGap(33, 33, 33))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lblFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addComponent(btnImg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Bienvenidolbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Nombrelbl)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnIdentificarseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIdentificarseActionPerformed

        abrir();
    try {
        buc.buscar(mhDB, template);
        identificación();
    } catch (ClassNotFoundException ex) {
        Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SQLException ex) {
        Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
    }
    }//GEN-LAST:event_btnIdentificarseActionPerformed

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
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Bienvenidolbl;
    private javax.swing.JLabel Nombrelbl;
    private javax.swing.JButton btnIdentificarse;
    private javax.swing.JLabel btnImg;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblFoto;
    // End of variables declaration//GEN-END:variables
}
