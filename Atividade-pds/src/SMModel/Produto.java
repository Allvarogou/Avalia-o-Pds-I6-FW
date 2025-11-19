package SMModel;

import java.util.Objects;

public class Produto {
    // Atributos (baseados no que vimos nas outras telas)
    private String produto; // Nome do produto
    private double preco; // Preço de venda
    private double precoCompra;
    private int quantidade;

    // Construtor completo
    public Produto(String produto, double preco, double precoCompra, int quantidade) {
        this.produto = produto;
        this.preco = preco;
        this.precoCompra = precoCompra;
        this.quantidade = quantidade;
    }

    // Construtor vazio (caso precise)
    public Produto() {
    }

    // --- Getters e Setters ---
    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(double precoCompra) {
        this.precoCompra = precoCompra;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    // --- PARA EXIBIR NA LISTA ---
    @Override
    public String toString() {
        return produto + " - R$ " + String.format("%.2f", preco);
    }

    // --- A CORREÇÃO MÁGICA PARA O REMOVER FUNCIONAR ---
    // Isso ensina ao Java que dois produtos com o mesmo NOME são a mesma coisa

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Produto outroProduto = (Produto) o;
        // Compara se os nomes são iguais (ignorando maiúsculas/minúsculas)
        return Objects.equals(this.produto.toLowerCase(), outroProduto.produto.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.produto.toLowerCase());
    }
}