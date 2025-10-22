package TelasView;

import SMModel.Carrinho;
import SMModel.Produto;
import SMModel.Mercado;
import SMModel.Pessoa;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import DAO.CarrinhoDAO;
import DAO.ProdutoDAO; // Importação adicionada para uso potencial no futuro

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
    
    // NOVO COMPONENTE: Spinner para selecionar a quantidade
    private JSpinner spinnerQuantidade; 

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
        // Customiza a renderização para mostrar o estoque
        listaProdutos.setCellRenderer(new ProdutoListCellRenderer()); 
        
        JScrollPane scrollPaneProdutos = new JScrollPane(listaProdutos);
        TitledBorder tituloProdutos = BorderFactory.createTitledBorder("Produtos Disponíveis (Estoque)");
        tituloProdutos.setTitleColor(Color.WHITE);
        scrollPaneProdutos.setBorder(tituloProdutos);

        // Painel do Carrinho
        listModelCarrinho = new DefaultListModel<>();
        listaCarrinho = new JList<>(listModelCarrinho);
        // Customiza a renderização para mostrar o nome e a quantidade comprada
        listaCarrinho.setCellRenderer(new ProdutoListCellRenderer());
        
        JScrollPane scrollPaneCarrinho = new JScrollPane(listaCarrinho);
        TitledBorder tituloCarrinho = BorderFactory.createTitledBorder("Carrinho de Compras");
        tituloCarrinho.setTitleColor(Color.WHITE);
        scrollPaneCarrinho.setBorder(tituloCarrinho);

        // Painel de Controle (Botões, Spinner e Total)
        JPanel painelControle = new JPanel(new BorderLayout());
        painelControle.setBackground(Color.DARK_GRAY);

        // NOVO PAINEL para a Quantidade e Botão Adicionar
        JPanel painelAdicao = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelAdicao.setBackground(Color.DARK_GRAY);
        
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // Inicializa com 1, Mín 1, Máx 100
        ((JSpinner.DefaultEditor) spinnerQuantidade.getEditor()).getTextField().setColumns(3); // Ajusta o tamanho
        spinnerQuantidade.setPreferredSize(new Dimension(60, 30));
        
        JLabel labelQuantidade = new JLabel("Qtd:");
        labelQuantidade.setForeground(Color.WHITE);

        JButton btnAdicionar = new JButton(">> Adicionar");
        btnAdicionar.setBackground(new Color(60, 179, 113));
        btnAdicionar.setForeground(Color.WHITE);
        
        painelAdicao.add(labelQuantidade);
        painelAdicao.add(spinnerQuantidade);
        painelAdicao.add(btnAdicionar);

        // Painel com os outros botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.setBackground(Color.DARK_GRAY);

        JButton btnRemover = new JButton("Remover <<");
        btnRemover.setBackground(new Color(220, 20, 60));
        btnRemover.setForeground(Color.WHITE);
        painelBotoes.add(btnRemover);

        JButton btnNotaFiscal = new JButton("Finalizar Compra/Nota");
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

        // Layout do Painel de Controle: Adição em cima, Outros botões no meio, Total em baixo
        JPanel painelMeioControle = new JPanel(new BorderLayout());
        painelMeioControle.add(painelAdicao, BorderLayout.NORTH);
        painelMeioControle.add(painelBotoes, BorderLayout.CENTER);
        
        painelControle.add(painelMeioControle, BorderLayout.NORTH);
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
    public void carregarProdutos() {
        listModelProdutos.clear();
        try {
            // OBS: Uso do método listarProdutos() do Mercado que assume um DAO subjacente
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
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para adicionar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade;
        try {
            quantidade = (int) spinnerQuantidade.getValue(); // Obtém a quantidade do Spinner
            if (quantidade <= 0) {
                JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validação da quantidade em estoque
        if (quantidade > produtoSelecionado.getQuantidade()) {
            JOptionPane.showMessageDialog(this, 
                String.format("Estoque insuficiente! Apenas %d unidades de %s disponíveis.", 
                    produtoSelecionado.getQuantidade(), produtoSelecionado.getProduto()), 
                "Estoque Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Cria uma CÓPIA do produto selecionado, mas com a quantidade desejada
            // (Presumindo que o construtor ou setters do Produto permitem definir a quantidade)
            // OBS: A logica de estoque (diminuir a quantidade do produto original) DEVE ser tratada no Carrinho.adicionarAoCarrinho()
            Produto produtoParaCarrinho = new Produto(
                produtoSelecionado.getProduto(),
                produtoSelecionado.getPreco(),
                produtoSelecionado.getPrecoCompra(),
                quantidade // Apenas a quantidade para o carrinho
            );
            
            carrinho.adicionarAoCarrinho(produtoParaCarrinho);
            
            // Atualiza a exibição do carrinho
            atualizarCarrinho();
            
            // Reajusta a quantidade no spinner para 1
            spinnerQuantidade.setValue(1); 

            JOptionPane.showMessageDialog(this, 
                String.format("%d unidades de %s adicionadas ao carrinho!", quantidade, produtoSelecionado.getProduto()), 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Após adicionar, recarrega a lista de produtos (opcional, dependendo da regra de negócio)
            // carregarProdutos();
            
        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, 
                "Erro ao adicionar produto ao carrinho: " + ex.getMessage(), 
                "Erro Interno", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerDoCarrinho() {
        Produto produtoSelecionado = listaCarrinho.getSelectedValue();
        if (produtoSelecionado != null) {
            
            // A quantidade a remover deve ser a quantidade que está no item do carrinho
            // Se o item for agrupado por nome, a remoção pode ser complexa.
            // Para simplificar, vamos remover apenas 1 unidade por vez, ou perguntar
            
            String qtdParaRemoverStr = JOptionPane.showInputDialog(this, 
                "Quantas unidades de " + produtoSelecionado.getProduto() + " deseja remover? (Max: " + produtoSelecionado.getQuantidade() + ")", 
                "Remover do Carrinho", JOptionPane.PLAIN_MESSAGE);
            
            if (qtdParaRemoverStr == null) {
                return; // Cancelou
            }
            
            int qtdParaRemover;
            try {
                qtdParaRemover = Integer.parseInt(qtdParaRemoverStr);
                if (qtdParaRemover <= 0 || qtdParaRemover > produtoSelecionado.getQuantidade()) {
                    JOptionPane.showMessageDialog(this, "Quantidade inválida para remoção.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Entrada inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Cria um "produto de remoção" para passar ao Carrinho
            Produto produtoParaRemover = new Produto(
                produtoSelecionado.getProduto(),
                produtoSelecionado.getPreco(),
                produtoSelecionado.getPrecoCompra(), // Presume que o construtor aceita
                qtdParaRemover
            );


            carrinho.removerDoCarrinho(produtoParaRemover);
            atualizarCarrinho();
            JOptionPane.showMessageDialog(this, qtdParaRemover + " unidade(s) de " + produtoSelecionado.getProduto() + " removida(s) do carrinho!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            
            // carregarProdutos(); // Atualiza a lista de produtos disponíveis (se a regra for descontar do estoque em tempo real)
            
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto para remover.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarCarrinho() {
        listModelCarrinho.clear();
        for (Produto p : carrinho.getProdutosCarrinho()) {
            // O ProdutoListCellRenderer cuida da formatação
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
            ex.printStackTrace(); // Para depuração
            JOptionPane.showMessageDialog(this, "Erro de banco de dados ao finalizar compra: " + ex.getMessage(), "Erro de Transação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ProdutoListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Produto) {
                Produto produto = (Produto) value;
                String texto;
                
                if (list == listaProdutos) {
                    // Renderização para a lista de Produtos Disponíveis (mostra o estoque)
                    texto = String.format("%s - R$ %.2f [Estoque: %d]", 
                        produto.getProduto(), produto.getPreco(), produto.getQuantidade());
                } else if (list == listaCarrinho) {
                    // Renderização para a lista do Carrinho (mostra a quantidade no carrinho)
                    texto = String.format("%s - R$ %.2f (Qtd: %d)", 
                        produto.getProduto(), produto.getPreco(), produto.getQuantidade());
                } else {
                    texto = produto.toString();
                }
                
                setText(texto);
            }
            return this;
        }
    }
}