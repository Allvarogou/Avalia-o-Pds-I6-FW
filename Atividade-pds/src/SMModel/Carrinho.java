package SMModel;

import java.util.ArrayList;

public class Carrinho {
    private ArrayList<Produto> produtosCarrinho = new ArrayList<>();

    public void adicionarAoCarrinho(Produto produto) {
        produtosCarrinho.add(produto);
    }

    public void removerDoCarrinho(Produto produto) {
        produtosCarrinho.remove(produto);
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