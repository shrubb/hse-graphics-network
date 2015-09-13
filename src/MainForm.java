import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainForm {

    private boolean console;

    public MainForm(boolean console) {
        this.console = console;
    }

    private static class SocketProcessor implements Runnable {

        private Socket s;
        private InputStream is;
        private OutputStream os;
        private DrawPanel panel;
        private Point putPoint;
        private Color putColor;

        private SocketProcessor(Socket s, DrawPanel panel) throws Throwable {
            this.s = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
            this.panel = panel;
        }

        // server socket processor
        public void run() {

            try {
                while (s.isConnected()) {
                    boolean get = readInputHeaders();
                    //System.out.println("get == " + get);
                    if (get) {
                        // GET
                        writeResponse(panel.data.toString());
                    } else {
                        // PUT
                        panel.data.map[putPoint.x][putPoint.y] = putColor;
                        panel.repaint(666, 6 * putPoint.y, 6 * putPoint.x, 6, 6);
                    }
                }
            } catch (Throwable e) {
                //JOptionPane.showMessageDialog(null, "48 " + e);
            }

            System.err.println("Client processing finished");
        }

        private void writeResponse(String s) throws Throwable {
            //System.out.println("writing response " + s);
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + s.length() + "\r\n\r\n";
            String result = response + s + "\r\n\r\n";
            os.write(result.getBytes());
            os.flush();
        }

        private boolean readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String s = br.readLine();
            boolean get = s.startsWith("GET");
            if (!get) {
                // format: PUT x=4 y=5 R=255 G=255 B=100
                String[] tokens = s.split("[ =]");
                putPoint = new Point(new Integer(tokens[2]), new Integer(tokens[4]));
                putColor = new Color(new Integer(tokens[6]), new Integer(tokens[8]), new Integer(tokens[10]));
            }

            while(true) {
                //System.out.print("reading line...");
                s = br.readLine();
                //System.out.println("ok");
                //System.out.println("server read: " + s);
                if(s == null || s.trim().length() == 0) {
                    break;
                }
            }

            return get;
        }
    }

    private JPanel panel1;
    private JTextField textField1;

    public DrawPanel drawPanel;
    public Color ourColor;

    public Socket requestSocket;
    public OutputStream requestOut;
    public InputStream requestIn;
    public boolean update;

    // client method
    public void updateField() {
        if (!update) {
            return;
        }

        String request = "GET / HTTP/1.1\r\n\r\n";
        try {
            requestOut.write(request.getBytes());
            requestOut.flush();
            //System.out.println("request written");

            BufferedReader br = new BufferedReader(new InputStreamReader(requestIn));
            String s;
            s = br.readLine();
            if (s == null) {
                JOptionPane.showMessageDialog(null, "Server down");
                update = false;
                return;
            }

            // read response header(s)
            while (true) {
                s = br.readLine();
                if (s == null || s.trim().length() == 0) {
                    break;
                }
            }

            // read the board
            s = br.readLine();

            for (int i = 0; i < drawPanel.data.height; ++i) {
                for (int j = 0; j < drawPanel.data.width; ++j) {
                    int startChar = 3 * (i * drawPanel.data.height + j);
                    drawPanel.data.map[i][j] = new Color(
                            (int)s.charAt(startChar),
                            (int)s.charAt(startChar + 1),
                            (int)s.charAt(startChar + 2)
                    );
                }
            }

            drawPanel.repaint();
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null, "143 " + e);
        }
    }

    // client method
    public void putChange(int x, int y) {
        String request = "PUT" +
                " x=" + x +
                " y=" + y +
                " R=" + ourColor.getRed() +
                " G=" + ourColor.getGreen() +
                " B=" + ourColor.getBlue() + "\r\n\r\n";
        //System.out.println(request);
        try {
            requestOut.write(request.getBytes());
            requestOut.flush();
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private class updateTimer implements Runnable {

        private MainForm form;

        public updateTimer(MainForm form) {
            this.form = form;
        }

        public void run() {
            Timer timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //System.out.println("update");
                    form.updateField();
                }
            }, 400, 400);
        }
    }

    public void updateSocket() {
        String s = textField1.getText();
        if (s.isEmpty()) {
            s = "127.0.0.1";
        }
        try {
            requestSocket = new Socket(s, 8080);
            requestSocket.setKeepAlive(true);
            requestIn = requestSocket.getInputStream();
            requestOut = requestSocket.getOutputStream();
        } catch (Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "Couldn't connect to " + s + " :(");
            requestSocket = null;
            update = false;
            return;
        }
        update = true;
    }

    public void initAndShow(final boolean isServer) {

        drawPanel = new DrawPanel();

        if (!console) {
            if (isServer) {
                textField1.setText("This is a server instance");
                textField1.setEnabled(false);
            }

            textField1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateSocket();
                }
            });

            Random rand = new Random();
            ourColor = new Color(50 * rand.nextInt(5), 50 * rand.nextInt(5), 50 * rand.nextInt(5));

            //drawPanel.setBackground(new Color(230, 230, 230));
            drawPanel.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mousePressed(MouseEvent e) {
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isServer || update) {
                        int mapX = e.getX() / 6;
                        int mapY = e.getY() / 6;
                        drawPanel.data.map[mapY][mapX] = ourColor;
                        //drawPanel.repaint(666, e.getX() - 6, e.getY() - 6, 12, 12);
                        if (!isServer) {
                            putChange(mapY, mapX);
                        } else {
                            drawPanel.repaint(666, e.getX() - 6, e.getY() - 6, 12, 12);
                        }
                    }
                }
            });

            JFrame jf = new JFrame();
            jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            jf.setSize(475, 510);
            jf.setResizable(false);

            panel1.add(
                    drawPanel,
                    new GridBagConstraints(1, 3, 3, 1, 10., 10., GridBagConstraints.CENTER, 1, new Insets(0,0,0,0), 0, 0),
                    1);

            //System.out.println(drawPanel.getWidth());
            //System.out.println(drawPanel.getHeight());

            jf.add(panel1);
            jf.setVisible(true);
        }

        if (isServer) {
            try {
                ServerSocket ss = new ServerSocket(8080);
                while (true) {
                    Socket s = ss.accept();
                    System.err.println("Client " + s.getInetAddress() + " accepted");
                    new Thread(new SocketProcessor(s, drawPanel)).start();
                }
            } catch (Throwable e) {
                System.err.println(e);
            }
        } else {
            new Thread(new updateTimer(this)).start();
        }
    }

}

class RunServer {

    public static void main(String[] args) {

        MainForm form = new MainForm(false);
        form.initAndShow(true);
    }
}

class RunClient {

    public static void main(String[] args) {

        MainForm form = new MainForm(false);
        form.initAndShow(false);
    }
}

class RunConsoleServer {

    public static void main(String[] args) {

        MainForm form = new MainForm(true);
        form.initAndShow(true);
    }
}