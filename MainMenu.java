import java.io.*;

public class MainMenu {
    private static int ancho, alto, tamanoPagina;

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            // Recibe los parámetros desde la consola
            System.out.print("Ingrese el tamaño de la página (en bytes): ");
            tamanoPagina = Integer.parseInt(br.readLine());

            System.out.print("Ingrese el nombre del archivo de imagen: ");
            String nombreArchivo = br.readLine();

            // Cargar la imagen utilizando la clase proporcionada
            Imagen imagen = new Imagen(nombreArchivo);
            ancho = imagen.ancho;
            alto = imagen.alto;

            // Recuperar la longitud del mensaje oculto en la imagen
            int longitudMensaje = imagen.leerLongitud();
            System.out.println("Longitud del mensaje oculto: " + longitudMensaje);

            // Recuperar el mensaje oculto
            char[] mensajeRecuperado = new char[longitudMensaje];
            imagen.recuperar(mensajeRecuperado, longitudMensaje);

            // Generar las referencias
            generarReferencias(imagen, mensajeRecuperado, longitudMensaje);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para generar las referencias de las páginas de la imagen y del mensaje oculto en un solo recorrido
    private static void generarReferencias(Imagen imagen, char[] mensajeRecuperado, int longitudMensaje) {
        try {
            // Archivo de salida con las referencias
            PrintWriter writer = new PrintWriter(new FileWriter("referencias2.txt"));

            // Cálculo de la cantidad de páginas necesarias
            int tamanoImagen = ancho * alto * 3; // 3 bytes por píxel (RGB)
            int numPaginasImagen = (int) Math.ceil((double) tamanoImagen / tamanoPagina);

            // Generar las referencias para la imagen y el mensaje oculto en el mismo recorrido
            writer.println("TP=" + tamanoPagina);
            writer.println("NF=" + alto);
            writer.println("NC=" + ancho);
            writer.println("NR=" + ((alto * ancho) * 3 + longitudMensaje * 8)); // Referencias para imagen (3 por píxel) + mensaje
            writer.println("NP=" + (numPaginasImagen + calcularPaginasMensaje(longitudMensaje)));

            int contadorBytes = 0; // Controla la posición actual en bytes de la imagen
            int mensajeBitPos = 16 * 8; // Comienza después de los 16 bytes (cada byte son 8 bits)
            int desplazamientoMensaje = 0; // Desplazamiento en la página del mensaje
            int paginaActualMensaje = numPaginasImagen; // El mensaje comienza después de la imagen

            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Generar las referencias de la imagen (RGB)
                    writer.println("Imagen[" + i + "][" + j + "].R," + obtenerPagina(contadorBytes) + "," + (contadorBytes % tamanoPagina));
                    contadorBytes++;
                    writer.println("Imagen[" + i + "][" + j + "].G," + obtenerPagina(contadorBytes) + "," + (contadorBytes % tamanoPagina));
                    contadorBytes++;
                    writer.println("Imagen[" + i + "][" + j + "].B," + obtenerPagina(contadorBytes) + "," + (contadorBytes % tamanoPagina));
                    contadorBytes++;

                    // Procesar el mensaje después de los 16 bytes (desplazamiento de mensaje en la página 1152)
                    if (contadorBytes >= 16 && mensajeBitPos < (16 * 8 + longitudMensaje * 8)) {
                        // Generar la referencia para los bits del mensaje
                        int bytePos = (mensajeBitPos - 16 * 8) / 8;
                        int bitPos = (mensajeRecuperado[bytePos] >> (mensajeBitPos % 8)) & 1;

                        // Escribir la referencia en la página correspondiente (página 1152 en adelante)
                        writer.println("Mensaje[" + bytePos + "]," + paginaActualMensaje + "," + desplazamientoMensaje + "," + (bitPos == 1 ? "W" : "R"));
                        desplazamientoMensaje++;

                        // Mover a la siguiente página si alcanzamos el límite de la página actual
                        if (desplazamientoMensaje >= tamanoPagina) {
                            paginaActualMensaje++;
                            desplazamientoMensaje = 0;
                        }

                        mensajeBitPos++;
                    }
                }
            }

            writer.close();
            System.out.println("Archivo de referencias generado: referencias.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para calcular la página virtual de la imagen y del mensaje
    private static int obtenerPagina(int byteActual) {
        return byteActual / tamanoPagina;
    }

    // Método para calcular cuántas páginas se necesitan para el mensaje oculto
    private static int calcularPaginasMensaje(int longitudMensaje) {
        int tamanoMensajeBits = longitudMensaje * 8;
        return (int) Math.ceil((double) tamanoMensajeBits / tamanoPagina);
    }
}
