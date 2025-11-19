package SMModel;

import java.util.ArrayList;

public class Carrinho {
    private ArrayList<Produto> produtosCarrinho = new ArrayList<>();

    public void adicionarAoCarrinho(Produto produto) {
        produtosCarrinho.add(produto);
    }

    public void removerDoCarrinho(Produto produtoParaRemover) {
        for (int i = 0; i < produtosCarrinho.size(); i++) {
            Produto p = produtosCarrinho.get(i);
            if (p.equals(produtoParaRemover)) {

                int novaQuantidade = p.getQuantidade() - produtoParaRemover.getQuantidade();

                if (novaQuantidade > 0) {
                    p.setQuantidade(novaQuantidade); // Apenas diminui
                } else {
                    produtosCarrinho.remove(i); // Remove o item se a qtd zerar
                }
                return; // Sai do método após encontrar
            }
        }
    }

    public void listarProdutosCarrinho() {
        for (Produto produto : produtosCarrinho) {
            System.out.println(
                    "Produto no carrinho: " + produto.getProduto() + ", Preço: " + produto.getPreco() + ", Quantidade: "
                            + produto.getQuantidade());
        }
    }

    public float calcularTotal() {
        float total = 0;
        for (Produto produto : produtosCarrinho) {
            total += produto.getPreco() * produto.getQuantidade();
        }
        return total;
    }

    public ArrayList<Produto> getProdutosCarrinho() {
        return produtosCarrinho;
    }

    public void setProdutosCarrinho(ArrayList<Produto> produtosCarrinho) {
        this.produtosCarrinho = produtosCarrinho;
    }

    public void limparCarrinho() {
        produtosCarrinho.clear();
        System.out.println("Carrinho limpo.");
    }
}