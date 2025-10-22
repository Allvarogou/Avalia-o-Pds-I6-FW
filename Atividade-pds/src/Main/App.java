package Main;
import SMModel.Mercado;
import TelasView.TelaIndentificação;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;


public class App {

    public static void main(String[] args) {

        Mercado mercado = new Mercado();

        SwingUtilities.invokeLater(() -> {
            try {
                
                new TelaIndentificação(mercado).setVisible(true); 
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro crítico na inicialização: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}