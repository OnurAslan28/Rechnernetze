package Praktikum2;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.File;

public class MimeTypes {

    public static String getMimeType(String type) {
        String mime = "";
        switch (type) {
            case "jpg":
                mime = "image/jpeg";
                break;
            case "html":
                mime = "text/html";
                break;
            case "pdf":
                mime = "application/pdf";
                break;
            case "gif":
                mime = "image/gif";
                break;
        }
        return mime;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] bytes = "testuser:super".getBytes("UTF-8");
        String encoded = Base64.getEncoder().encodeToString(bytes);
        byte[] decoded = Base64.getDecoder().decode(encoded);
        System.out.println(encoded);
        System.out.println(decoded);

    }


}

