import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

interface ClienteInterface3 {
    void ConectarCliente() throws IOException;
    void EnviarMensagem(String mensagem) throws IOException;
    void AtualizarCliente() throws IOException;
    void DesconectarCliente() throws IOException;
    void SelecionarUsuario();
}

class ClienteAdapter3 implements ClienteInterface3 {
    private final Cliente3 cliente3;

    public ClienteAdapter3(Cliente3 cliente) {
        this.cliente3 = cliente;
    }

    @Override
    public void ConectarCliente() throws IOException {
        cliente3.ConectarCliente();
    }

    @Override
    public void EnviarMensagem(String mensagem) throws IOException {
        cliente3.EnviarMensagem(mensagem);
    }

    @Override
    public void AtualizarCliente() throws IOException {
        cliente3.AtualizarCliente();
    }

    @Override
    public void DesconectarCliente() throws IOException {
        cliente3.DesconectarCliente();
    }

    @Override
    public void SelecionarUsuario() {
        cliente3.SelecionarUsuario();
    }
}

public class Cliente3 extends JFrame implements ActionListener {
    private final JTextField tfIP;
    private final JTextField tfPorta;
    private final JTextField tfNome;

    private final JPanel chat = new JPanel();
    private final JTextArea areaDeTexto = new JTextArea(30, 53);
    private final JTextField tfMensagem = new JTextField(50);
    private final JLabel lbTitulo = new JLabel("Histórico do Chat");
    private final JLabel lbComandos = new JLabel("Comandos úteis: /sair /limparchat. /privado <nome> <mensagem>");

    private final JButton bEnviar = new JButton("Enviar");
    private final JButton bLimpar = new JButton("Limpar");
    private final JButton bSair = new JButton("Sair");
    private final JButton bSelecionarUsuario = new JButton("Selecionar Usuário");

    private String usuarioSelecionado = null;
    private Socket socket;
    private BufferedWriter escritor;

    public Cliente3() throws IOException {
        JPanel painelIP = new JPanel();
        JPanel painelPorta = new JPanel();
        JPanel painelNome = new JPanel();

        tfIP = new JTextField("127.0.0.1");
        tfPorta = new JTextField("9000");
        tfNome = new JTextField("VISITANTE");

        painelIP.add(new JLabel("ENDEREÇO DE IP: "));
        painelIP.add(tfIP);

        painelPorta.add(new JLabel("PORTA: "));
        painelPorta.add(tfPorta);

        painelNome.add(new JLabel("QUAL SEU NOME? "));
        painelNome.add(tfNome);

        Object[] info = { painelIP, painelPorta, painelNome };
        JOptionPane.showMessageDialog(null, info);

        lbTitulo.setForeground(Color.WHITE);
        lbComandos.setForeground(Color.WHITE);
        chat.setBackground(Color.DARK_GRAY);
        areaDeTexto.setBackground(Color.LIGHT_GRAY);
        areaDeTexto.setForeground(Color.BLACK);
        areaDeTexto.setFont(new Font("Verdana", Font.ITALIC, 12));
        areaDeTexto.setMargin(new Insets(20, 20, 0, 0));
        areaDeTexto.setEditable(false);
        tfMensagem.setPreferredSize(new Dimension(100, 25));

        bEnviar.addActionListener(this);
        bLimpar.addActionListener(this);
        bSair.addActionListener(this);
        bSelecionarUsuario.addActionListener(this);

        JScrollPane painelRolagem = new JScrollPane(areaDeTexto);

        chat.add(lbTitulo);
        chat.add(painelRolagem);
        chat.add(tfMensagem);
        chat.add(bEnviar);
        chat.add(bLimpar);
        chat.add(bSair);
        chat.add(bSelecionarUsuario);
        chat.add(lbComandos);

        setSize(600, 600);
        setTitle(tfNome.getText());
        setContentPane(chat);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
    }

    public void ConectarCliente() throws IOException {
        socket = new Socket(tfIP.getText(), Integer.parseInt(tfPorta.getText()));
        escritor = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        escritor.write(tfNome.getText() + "\r\n");
        areaDeTexto.append("Você conectou ao chat. Seja bem-vindo(a) " + tfNome.getText() + ".\r\n");
        escritor.flush();
    }

    public void EnviarMensagem(String mensagem) throws IOException {
        if (mensagem.length() < 1) {
            areaDeTexto.append("Você deve escrever algo no campo de texto para enviar! \r\n");
        } else if (mensagem.equals("/sair")) {
            DesconectarCliente();
        } else if (mensagem.equals("/limparchat")) {
            areaDeTexto.selectAll();
            areaDeTexto.replaceSelection("O chat foi limpado. \r\n");
        } else if (usuarioSelecionado != null) {
            escritor.write("/privado " + usuarioSelecionado + " " + mensagem + "\r\n");
            areaDeTexto.append("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())
                    + "] " + tfNome.getText() + " (privado para " + usuarioSelecionado + "): " + mensagem + "\r\n");
        } else {
            escritor.write(mensagem + "\r\n");
            areaDeTexto.append("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())
                    + "] " + tfNome.getText() + ": " + mensagem + "\r\n");
        }
        escritor.flush();
        tfMensagem.setText("");
    }

    public void AtualizarCliente() throws IOException {
        String mensagem = "";
        BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (!"/sair".equalsIgnoreCase(mensagem)) {
            if (leitor.ready()) {
                mensagem = leitor.readLine();
                if (mensagem.equals("/sair")) {
                    areaDeTexto.append("Você desconectou do chat! \r\n");
                } else {
                    areaDeTexto.append(mensagem + "\r\n");
                }
            }
        }
    }

    public void DesconectarCliente() throws IOException {
        areaDeTexto.append("Você desconectou do chat! \r\n");
        escritor.close();
        socket.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(bEnviar.getActionCommand())) {
                EnviarMensagem(tfMensagem.getText());
            } else if (e.getActionCommand().equals(bLimpar.getActionCommand())) {
                areaDeTexto.selectAll();
                areaDeTexto.replaceSelection("O chat foi limpado. \r\n");
            } else if (e.getActionCommand().equals(bSair.getActionCommand())) {
                DesconectarCliente();
            } else if (e.getActionCommand().equals(bSelecionarUsuario.getActionCommand())) {
                SelecionarUsuario();
            }
        } catch (IOException erro) {
            System.out.println(erro.toString());
        }
    }

    public void SelecionarUsuario() {
        String usuario = JOptionPane.showInputDialog("Digite o nome do usuário para enviar uma mensagem privada:");
        if (usuario != null && !usuario.isEmpty()) {
            usuarioSelecionado = usuario;
            areaDeTexto.append("Você selecionou o usuário: " + usuario + "\r\n");
        }
    }

    public static void main(String[] args) throws IOException {
        Cliente3 cliente3 = new Cliente3();
        cliente3.ConectarCliente();
        cliente3.AtualizarCliente();
    }
}
