package utb.fai;

import java.net.*;
import java.io.*;



public class EmailSender {
    /*
     * Constructor opens Socket to host/port. If the Socket throws an exception
     * during opening,
     * the exception is not handled in the constructor.
     */
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private BufferedReader reader;

    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(in));

        System.out.println("Klient se připojil k serveru");

        String response = reader.readLine();
        System.out.println(response);

        if (response.startsWith("220")) {
            String message = "EHLO localhost\r\n";
            out.write(message.getBytes());
            out.flush();

            do {
                response = reader.readLine();
                System.out.println(response);
            } while (response.startsWith("250-"));

        } else {
            throw new IOException("SMTP server neodpověděl kódem 220");
        }
    }

    /*
     * Sends email from an email address to an email address with some subject and
     * text.
     * If the Socket throws an exception during sending, the exception is not
     * handled by this method.
     */
    public void send(String from, String to, String subject, String text) throws IOException {
        String message;
        String response;

        message = "MAIL FROM:<" + from + ">\r\n";
        out.write(message.getBytes());
        out.flush();

        response = reader.readLine();

        if (!response.startsWith("250")) {
            throw new IOException("Nepodařilo se zadat odesilatele");
        }

        message = "RCPT TO:<" + to + ">\r\n";
        out.write(message.getBytes());
        out.flush();

        response = reader.readLine();

        if (!response.startsWith("250")) {
            throw new IOException("Nepodařilo se zadat příjemce");
        }

        message = "DATA\r\n";
        out.write(message.getBytes());
        out.flush();

        response = reader.readLine();

        if (!response.startsWith("354")) {
            throw new IOException("Nepodařilo se zahájit odesílání zprávy");
        }

        message = "From: " + from + "\r\n"
                + "To: " + to + "\r\n"
                + "Subject: " + subject + "\r\n"
                + "\r\n"
                + text + "\r\n"
                + ".\r\n";

        out.write(message.getBytes());
        out.flush();

        response = reader.readLine();

        if (response.startsWith("250")) {
            System.out.println("Zpráva úspěšně odeslána");
        } else {
            throw new IOException("Zpráva se neodeslala");
        }
    }
    /*
     * Sends QUIT and closes the socket
     */
    public void close() {
        try {
            String message = "QUIT\r\n";
            out.write(message.getBytes());
            out.flush();

            String response = reader.readLine();

            if (response.startsWith("221")) {
                System.out.println("Spojení úspěšně ukončeno");
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Chyba při ukončování spojení: " + e.getMessage());
        }
    }
}
