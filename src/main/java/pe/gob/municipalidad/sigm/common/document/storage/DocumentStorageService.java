package pe.gob.municipalidad.sigm.common.document.storage;

import java.io.InputStream;

/**
 * Abstracción de bajo nivel para el almacenamiento de archivos documentales.
 * Permite desacoplar el sistema de archivos local de futuras implementaciones en la nube (S3, MinIO).
 */
public interface DocumentStorageService {

    /**
     * Almacena el stream de un archivo bajo un nombre interno seguro.
     *
     * @param inputStream flujo de bytes del documento
     * @param extension   extensión del archivo sin punto (ej. "pdf")
     * @return nombre interno único generado bajo el cual quedó almacenado
     */
    String store(InputStream inputStream, String extension);

    /**
     * Lee el contenido del archivo con el nombre interno indicado.
     *
     * @param nombreInterno nombre del archivo en el almacén
     * @return flujo de lectura del archivo
     */
    InputStream read(String nombreInterno);

    /**
     * Elimina el archivo del almacenamiento físico si existe.
     *
     * @param nombreInterno nombre del archivo en el almacén
     * @return true si se eliminó con éxito
     */
    boolean delete(String nombreInterno);

    /**
     * Verifica la existencia física del archivo.
     *
     * @param nombreInterno nombre del archivo
     * @return true si el archivo existe y es legible
     */
    boolean exists(String nombreInterno);

    /**
     * Retorna el tamaño en bytes del archivo almacenado.
     *
     * @param nombreInterno nombre del archivo
     * @return longitud en bytes
     */
    long getSizeBytes(String nombreInterno);
}
