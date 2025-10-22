package TelasView;

import SMModel.Carrinho;
import SMModel.Produto;
import SMModel.Mercado;
import SMModel.Pessoa;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import DAO.CarrinhoDAO;

import java.awt.*;
import java.sql.SQLException;

public class TelaDeCompra extends JFrame {

    private static final long serialVersionUID = 1L;
    private CarrinhoDAO carrinhoDAO = new CarrinhoDAO();

    private Mercado supermercado;
    private String nomeCliente;
    private String cpfCliente;
    private Carrinho carrinho;
    private JFrame telaAnterior;

    private DefaultListModel<Produto> listModelProdutos;
    private JList<Produto> listaProdutos;
    private DefaultListModel<Produto> listModelCarrinho;
    private JList<Produto> listaCarrinho;
    private JLabel labelTotal;

    public TelaDeCompra(Mercado supermercado, String nomeCliente, String cpfCliente,
            JFrame telaAnterior) {
        this.supermercado = supermercado;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
        this.carrinho = new Carrinho();
        this.telaAnterior = telaAnterior;

        setTitle("Tela de Compra - Bem-vindo, " + nomeCliente);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel Principal
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelPrincipal.setBackground(Color.DARK_GRAY);

        // Painel de Produtos Disponíveis
        listModelProdutos = new DefaultListModel<>();
        listaProdutos = new JList<>(listModelProdutos);
        JScrollPane scrollPaneProdutos = new JScrollPane(listaProdutos);
        TitledBorder tituloProdutos = BorderFactory.createTitledBorder("Produtos Disponíveis");
        tituloProdutos.setTitleColor(Color.WHITE);
        scrollPaneProdutos.setBorder(tituloProdutos);

        // Painel do Carrinho
        listModelCarrinho = new DefaultListModel<>();
        listaCarrinho = new JList<>(listModelCarrinho);
        JScrollPane scrollPaneCarrinho = new JScrollPane(listaCarrinho);
        TitledBorder tituloCarrinho = BorderFactory.createTitledBorder("Carrinho de Compras");
        tituloCarrinho.setTitleColor(Color.WHITE);
        scrollPaneCarrinho.setBorder(tituloCarrinho);

        // Painel de Controle (Botões e Total)
        JPanel painelControle = new JPanel(new BorderLayout());
        painelControle.setBackground(Color.DARK_GRAY);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.setBackground(Color.DARK_GRAY);

        JButton btnAdicionar = new JButton(">> Adicionar");
        btnAdicionar.setBackground(new Color(60, 179, 113));
        btnAdicionar.setForeground(Color.WHITE);
        painelBotoes.add(btnAdicionar);

        JButton btnRemover = new JButton("Remover <<");
        btnRemover.setBackground(new Color(220, 20, 60));
        btnRemover.setForeground(Color.WHITE);
        painelBotoes.add(btnRemover);

        JButton btnNotaFiscal = new JButton("Emitir Nota Fiscal");
        btnNotaFiscal.setBackground(new Color(100, 149, 237));
        btnNotaFiscal.setForeground(Color.WHITE);
        painelBotoes.add(btnNotaFiscal);

        JButton btnVoltar = new JButton("Sair/Trocar Usuário");
        btnVoltar.setBackground(new Color(105, 105, 105));
        btnVoltar.setForeground(Color.WHITE);
        painelBotoes.add(btnVoltar);

        labelTotal = new JLabel("Total a Pagar: R$ 0.00", SwingConstants.CENTER);
        labelTotal.setFont(new Font("Arial", Font.BOLD, 18));
        labelTotal.setForeground(Color.WHITE);

        painelControle.add(painelBotoes, BorderLayout.CENTER);
        painelControle.add(labelTotal, BorderLayout.SOUTH);

        // Adicionando painéis ao painel principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneProdutos, scrollPaneCarrinho);
        splitPane.setResizeWeight(0.5);
        painelPrincipal.add(splitPane, BorderLayout.CENTER);
        painelPrincipal.add(painelControle, BorderLayout.SOUTH);

        add(painelPrincipal);

        // Carregar produtos no início
        carregarProdutos();

        // Ações dos botões
        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());
        btnRemover.addActionListener(e -> removerDoCarrinho());
        btnNotaFiscal.addActionListener(e -> emitirNotaFiscal());
        btnVoltar.addActionListener(e -> {
            this.dispose();
            telaAnterior.setVisible(true);
        });
    }

    // Método que lista os produtos do BD (via Mercado)
    private void carregarProdutos() {
        listModelProdutos.clear();
        try {
            for (Produto p : supermercado.listarProdutos()) {
                // Exibe o produto e a quantidade em estoque para o cliente
                listModelProdutos.addElement(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o estoque: " + e.getMessage(), "Erro no BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarAoCarrinho() {
        Produto produtoSelecionado = listaProdutos.getSelectedValue();
        if (produtoSelecionado != null) {
            carrinho.adicionarAoCarrinho(produtoSelecionado);
            atualizarCarrinho();
            JOptionPane.showMessageDialog(this, "Produto adicionado ao carrinho!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para adicionar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removerDoCarrinho() {
        Produto produtoSelecionado = listaCarrinho.getSelectedValue();
        if (produtoSelecionado != null) {
            carrinho.removerDoCarrinho(produtoSelecionado);
            atualizarCarrinho();
            JOptionPane.showMessageDialog(this, "Produto removido do carrinho!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para remover.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarCarrinho() {
        listModelCarrinho.clear();
        for (Produto p : carrinho.getProdutosCarrinho()) {
            // OBS: O método toString() da classe Produto deve ser ajustado para exibir os
            // dados corretamente na JList
            listModelCarrinho.addElement(p);
        }
        double total = carrinho.calcularTotal();
        labelTotal.setText(String.format("Total a Pagar: R$ %.2f", total));
    }

  private void emitirNotaFiscal() {
    if (carrinho.getProdutosCarrinho().isEmpty()) {
        JOptionPane.showMessageDialog(this, "O carrinho está vazio. Adicione produtos antes de emitir a nota.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

  
    Pessoa cliente = new Pessoa(nomeCliente, cpfCliente, false); 
    
   
    StringBuilder notaFiscal = new StringBuilder();
    notaFiscal.append("========================\n");
    notaFiscal.append("        NOTA FISCAL         \n");
    notaFiscal.append("========================\n");
    notaFiscal.append("Nome do Cliente: ").append(nomeCliente).append("\n");
    notaFiscal.append("CPF: ").append(cpfCliente).append("\n");
    notaFiscal.append("------------------------\n");
    notaFiscal.append("Produtos Comprados:\n");

    for (Produto p : carrinho.getProdutosCarrinho()) {
        notaFiscal.append(String.format(" - %s (R$ %.2f x %d)\n", p.getProduto(), p.getPreco(), p.getQuantidade()));
    }

    notaFiscal.append("------------------------\n");
    notaFiscal.append(String.format("Total Pago: R$ %.2f\n", carrinho.calcularTotal()));
    notaFiscal.append("========================\n");

    try {
       
        boolean sucesso = carrinhoDAO.finalizarCompra(cliente, carrinho);
        
        if (sucesso) {
            
            JOptionPane.showMessageDialog(this, new JTextArea(notaFiscal.toString()), "Nota Fiscal",
                    JOptionPane.PLAIN_MESSAGE);

           
            carrinho.limparCarrinho();
            carregarProdutos(); 
            atualizarCarrinho();
        } else {
            JOptionPane.showMessageDialog(this, "Falha ao registrar a compra no banco de dados.", "Erro de Transação", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException ex) {
       
        JOptionPane.showMessageDialog(this, "Erro de banco de dados ao finalizar compra: " + ex.getMessage(), "Erro de Transação", JOptionPane.ERROR_MESSAGE);
    }
}
}