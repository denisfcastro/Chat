import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


// Classe abstrata para representar comandos
abstract class Command {
    public abstract void execute(Servidor servidor, String mensagem) throws IOException;
}


// Comando para enviar uma mensagem pública
class PublicMessageCommand extends Command {
    @Override
    public void execute(Servidor servidor, String mensagem) throws IOException {
        servidor.enviarMensagemPublica(mensagem);
    }
}


// Comando para enviar uma mensagem privada
class PrivateMessageCommand extends Command {
    @Override
    public void execute(Servidor servidor, String mensagem) throws IOException {
        servidor.enviarMensagemPrivada(mensagem);
    }
}


public class Servidor extends Thread {
    private String nome;
    private String enderecoIP;

    final static String[] opcoes = {"Ok", "Cancelar"};
    final static String PORTA_PADRAO = "9000";

    private final Socket socket;
    private static ServerSocket servidorSocket;

    private BufferedReader leitor;
    private BufferedWriter escritor;

    private static Map<String, BufferedWriter> connectedClients = new ConcurrentHashMap<>();

    private Command command; // Campo para armazenar o comando atual

    public Servidor(Socket socket) {
        this.socket = socket;
        try {
            leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            escritor = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            nome = leitor.readLine();
            enderecoIP = socket.getInetAddress().getHostAddress();
            connectedClients.put(nome, escritor);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void run() {
        try {
            String mensagem;
            while (true) {
                mensagem = leitor.readLine();
                if (mensagem == null || "Sair".equalsIgnoreCase(mensagem)) {
                    connectedClients.remove(nome);
                    break;
                }

                if (mensagem.startsWith("/privado")) {
                    setCommand(new PrivateMessageCommand());
                } else {
                    setCommand(new PublicMessageCommand());
                }

                command.execute(this, mensagem); // Executar o comando correspondente
                System.out.println("[" + enderecoIP + "] " + nome + ": " + mensagem);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        try {
            JTextField tfPorta = new JTextField(PORTA_PADRAO);
            JPanel painel = new JPanel();
            painel.add(new JLabel("Porta do Servidor: "));
            painel.add(tfPorta);
            int resultado = JOptionPane.showOptionDialog(null, painel, "Chat via Socket", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opcoes, 0);
            if (resultado == JOptionPane.NO_OPTION) {
                System.out.println("O servidor foi encerrado.");
                System.exit(0);
            }
            JOptionPane.showMessageDialog(null, "Servidor iniciado na porta: " + tfPorta.getText());
            servidorSocket = new ServerSocket(Integer.parseInt(tfPorta.getText()));
            while (true) {
                System.out.println("O servidor está online, aguardando conexões...");
                Socket socket = servidorSocket.accept();
                System.out.println(socket.getLocalAddress() + " conectado ao servidor.");
                Thread thread = new Servidor(socket);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro: Não foi possível iniciar o servidor na porta especificada.");
        }
    }

    public void enviarMensagemPublica(String mensagem) throws IOException {
        for (BufferedWriter escritorCliente : connectedClients.values()) {
            if (escritorCliente != escritor) {
                escritorCliente.write("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())
                        + "] " + nome + ": " + mensagem + "\r\n");
                escritorCliente.flush();
            }
        }
    }

    public void enviarMensagemPrivada(String mensagem) throws IOException {
        String[] partes = mensagem.split(" ", 3);
        if (partes.length == 3) {
            String destinatario = partes[1];
            String mensagemPrivada = partes[2];
            BufferedWriter destinatarioWriter = connectedClients.get(destinatario);
            if (destinatarioWriter != null) {
                destinatarioWriter.write("Mensagem privada de " + nome + ": " + mensagemPrivada + "\r\n");
                destinatarioWriter.flush();
            }
        }
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}


