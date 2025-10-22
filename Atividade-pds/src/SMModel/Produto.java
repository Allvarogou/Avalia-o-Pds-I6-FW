package SMModel;

public class Produto {
    private String produto;
    private float preco;
    private float precoCompra;
    private int quantidade;

    
    public Produto() {
    }

    
    public Produto(String produto, float preco, float precoCompra, int quantidade) {
        this.produto = produto;
        this.preco = preco;
        this.precoCompra = precoCompra;
        this.quantidade = quantidade;
    }

    
    @Override
    public String toString() {
        return String.format("%s (R$ %.2f) - Estoque: %d", 
                             produto, 
                             preco, 
                             quantidade);
    }
    
    
    public String getProduto() {
        return produto;
    }
    
    public void setProduto(String produto) {
        this.produto = produto;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public float getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(float precoCompra) {
        this.precoCompra = precoCompra;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}