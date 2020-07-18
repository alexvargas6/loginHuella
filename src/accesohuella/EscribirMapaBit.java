
package accesohuella;

import java.io.IOException;

/**
 *
 * @author manuel.vargas
 */
class EscribirMapaBit {
    
     //Esta función crea la imagen
    public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
			String path/*path=huella.mpb*/) throws IOException {
        
        //FileOutputStream nos dice en que dato vamos a escribir (path)
		java.io.FileOutputStream fos = new java.io.FileOutputStream(path); //Fos es path y path es la imagen que esta dibujando
		//DataOutputStraem se usa para Escribir en datos de tipo primitivo
                /*Se crea un objeto de la clase DataOutputStream vinculándolo a un un 
                objeto FileOutputStream para escribir en un archivo en disco denominado huella.mpb*/
                java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);//Esta linea crea el archivo huella.mpb
//DataOutputStream escribe el dato que FileOutputStream nos da, y lo hace utilizando el metodo write.
                
		int w = (((nWidth+3)/4)*4);
		int bfType = 0x424d; // Tipo de archivo de mapa de bits (0-1 byte)
		int bfSize = 54 + 1024 + w * nHeight;// El tamaño del archivo bmp (2-5 bytes)
		int bfReserved1 = 0;// Las palabras reservadas del archivo de mapa de bits deben ser 0 (6-7 bytes)
		int bfReserved2 = 0;// Las palabras reservadas del archivo de mapa de bits deben ser 0 (8-9 bytes)
		int bfOffBits = 54 + 1024;//El desplazamiento de bytes desde el comienzo del archivo a los datos reales del mapa de bits (10-13 bytes)
                //dos es un buffer tipo byte
		dos.writeShort(bfType); // Tipo de archivo de mapa de bits de entrada 'BM'
		dos.write(changeByte(bfSize), 0, 4); //Ingrese el tamaño del archivo de mapa de bits
		dos.write(changeByte(bfReserved1), 0, 2);//Ingrese la palabra reservada en el archivo de mapa de bits
		dos.write(changeByte(bfReserved2), 0, 2);// Ingrese la palabra reservada en el archivo de mapa de bits
		dos.write(changeByte(bfOffBits), 0, 4);//Introduzca el desplazamiento del archivo de mapa de bits

		int biSize = 40;//Número de bytes necesarios para el encabezado (14-17 bytes)
		int biWidth = nWidth;// El ancho del mapa de bits (18-21 bytes)
		int biHeight = nHeight;// Mapa de bits alto (22-25 bytes)
		int biPlanes = 1; //El nivel del dispositivo de destino debe ser 1 (26-27 bytes)
		int biBitcount = 8;// El número de bits requerido para cada píxel (28-29 bytes)
                //debe ser uno de 1 bit (dos colores), 4 bits (16 colores), 8 bits (256 colores)
                //o 24 bits (color verdadero).
		int biCompression = 0;// El tipo de compresión de mapa de bits debe ser uno de 0 (sin comprimir)
                //(30-33 bytes), 1 (tipo de compresión BI_RLEB) o 2 (tipo de compresión BI_RLE4).
		int biSizeImage = w * nHeight;//El tamaño de la imagen de mapa de bits real, 
                //es decir, el tamaño de toda la imagen realmente dibujada (34-37 bytes)
		int biXPelsPerMeter = 0;// Resolución horizontal de mapa de bits,
                //píxeles por metro (38-41 bytes) Este número es el valor predeterminado del sistema
		int biYPelsPerMeter = 0;//Resolución vertical de mapa de bits, 
                //píxeles por metro (42-45 bytes) Este número es el valor predeterminado del sistema
		int biClrUsed = 0;// El número de colores en la tabla de colores realmente utilizados 
                //por el mapa de bits (46-49 bytes), si es 0, significa que todos se utilizan
		int biClrImportant = 0;// El número de colores importantes (50-53 bytes)
                //en el proceso de visualización de mapa de bits, si es 0, significa que todos 
                //son importantes
                //ChangeByte, función que retorna otra función.
                //Write es un metodo que nos permite escribir sobre un buffer
                /*Bufer Es un archivo de transición entre java y el archivo donde copia o pega.
                Se genera un espacio en la memoria interna entre el programa y su copia.*/
		dos.write(changeByte(biSize), 0, 4);// Ingrese el número total de bytes de datos de encabezado
		dos.write(changeByte(biWidth), 0, 4);// Ingrese el ancho del mapa de bits***
		dos.write(changeByte(biHeight), 0, 4);//Ingrese la altura del mapa de bits***
		dos.write(changeByte(biPlanes), 0, 2);// Ingrese el nivel de dispositivo de destino del mapa de bits
		dos.write(changeByte(biBitcount), 0, 2);//Ingrese el número de bytes ocupados por cada píxel
		dos.write(changeByte(biCompression), 0, 4);// Tipo de compresión de entrada de mapa de bits
		dos.write(changeByte(biSizeImage), 0, 4);// Ingrese el tamaño real del mapa de bits
		dos.write(changeByte(biXPelsPerMeter), 0, 4);//Ingrese la resolución horizontal del mapa de bits
		dos.write(changeByte(biYPelsPerMeter), 0, 4);// Ingrese la resolución vertical del mapa de bits
		dos.write(changeByte(biClrUsed), 0, 4);//Ingrese el número total de colores utilizados por el mapa de bits
		dos.write(changeByte(biClrImportant), 0, 4);//Ingrese el número de colores importantes en el uso de mapas de bits

		for (int i = 0; i < 256; i++) {
                        dos.writeByte(i);
                        dos.writeByte(i);
                        dos.writeByte(i);
                        dos.writeByte(0);
		}

		byte[] filter = null;
		if (w > nWidth)
		{
			filter = new byte[w-nWidth];
		}
		
		for(int i=0;i<nHeight;i++)
		{
			dos.write(imageBuf, (nHeight-1-i)*nWidth, nWidth);
			if (w > nWidth)
				dos.write(filter, 0, w-nWidth);
		}
                dos.flush();//Vacía esta secuencia de salida de datos.
		dos.close();
		fos.close();
               
	}
    
    public static byte[] changeByte(int data) {
        //Convierte los valores a bytes
		return intToByteArray(data);
	}
    
     public static byte[] intToByteArray (final int number) {
		byte[] abyte = new byte[4];  
	    /* "&" AND (AND), realiza álgebra booleana en
            los bits correspondientes en los dos operandos
            enteros, salida 1 cuando ambos bits son 1, de lo contrario 0.*/
            /*"&" y AND multiplican los bits de ambos lados 0101 & 0011
             Las multiplicaciones de bits dan como resultado que 1 x 1 
            siempre es 1 y que 1 x 0 y 0 x 0 siempre da 0.*/
	    abyte[0] = (byte) (0xff & number);  //Operador de bit: AND
	    // ">>" cambia a la derecha, si es positivo, 
            //el orden superior se llena con 0, si es negativo, 
            //el orden alto se llena con 1  
            /*">>" operadores de desplazamiento de bits, permiten mover los bits dentro de la cadena
            (valor_binario1) >> (valor_binario2)
            Los operadores de desplazamiento toman dos operandos: 
            el primero es la cantidad a ser desplazados, y el segundo
            especifica el número de posiciones bits que el primer 
            operando debe ser desplazado.*/
	    abyte[1] = (byte) ((0xff00 & number) >> 8);//en number se almacena data
	    abyte[2] = (byte) ((0xff0000 & number) >> 16);/*Desplazamos 16bits hacia la derecha
            del número que se encuentra al otro extremo*/
	    abyte[3] = (byte) ((0xff000000 & number) >> 24);
	    return abyte; 
	}
    
      /*Esta fucnión le asigna el valor al alto y ancho de la imagen,
      Luego imgbuff las múltiplica y ese es su valor.*/
    public static int byteArrayToInt(byte[] bytes) {
			int number = bytes[0] & 0xFF;  
		    // "| =" A nivel de bit o asignación.
                   /*"<<" desplaza los bits del número hacia la 
                   izquierda y llena con “0” los bits desplazados.*/ 
		    number |= ((bytes[1] << 8) & 0xFF00);  
		    number |= ((bytes[2] << 16) & 0xFF0000);  
		    number |= ((bytes[3] << 24) & 0xFF000000);  
		    return number;  
		 }
    
}
