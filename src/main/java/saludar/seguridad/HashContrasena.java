package saludar.seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contrasenias con sal (RF-02, RNF-01). Usa PBKDF2 con HMAC-SHA256,
 * incluido en el JDK, por lo que no requiere librerias externas.
 *
 * Formato almacenado en usuario.hash_contrasena:
 *   pbkdf2_sha256$iteraciones$salBase64$hashBase64
 *
 * Uso por linea de comandos para generar un hash nuevo:
 *   java -cp target/classes saludar.seguridad.HashContrasena miClave
 */
public final class HashContrasena {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private HashContrasena() { }

    /** Genera el hash de una contrasenia con una sal aleatoria nueva. */
    public static String generar(char[] clave) {
        byte[] sal = new byte[BYTES_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** Verifica una contrasenia contra un hash almacenado (comparacion en tiempo constante). */
    public static boolean verificar(char[] clave, String almacenado) {
        if (almacenado == null) return false;
        String[] partes = almacenado.split("\\$");      // ARREGLO con las 4 partes del formato
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;    // hash mal formado
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave, sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no disponible en esta JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java saludar.seguridad.HashContrasena <clave>");
            return;
        }
        System.out.println(generar(args[0].toCharArray()));
    }
}
