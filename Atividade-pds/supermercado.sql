-- Cria o Banco de Dados se ele não existir
CREATE DATABASE IF NOT EXISTS supermercado;

-- Seleciona o Banco de Dados para uso
USE supermercado;

-- 1. Tabela de PRODUTOS
CREATE TABLE IF NOT EXISTS produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    preco_venda FLOAT NOT NULL,      
    preco_compra FLOAT NOT NULL,     
    quantidade INT NOT NULL,         
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 2. Tabela de PESSOAS (USUÁRIOS)
CREATE TABLE IF NOT EXISTS pessoas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL, -- O campo CPF deve ser ajustado para VARCHAR(14) para o CPF sem formatação é suficiente, mas mantemos 14 para segurança.
    isAdm BOOLEAN NOT NULL           
);


-- 3. Tabela de CARRINHOS (REGISTRO DE TRANSAÇÕES)
CREATE TABLE IF NOT EXISTS carrinhos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa INT NOT NULL,
    data_compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_pago FLOAT NOT NULL,
    
    FOREIGN KEY (id_pessoa) REFERENCES pessoas(id)
);


-- 4. Tabela de ITENS DO CARRINHO (DETALHES DA NOTA FISCAL)
CREATE TABLE IF NOT EXISTS itens_carrinho (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_carrinho INT NOT NULL,
    id_produto INT NOT NULL,
    quantidade_comprada INT NOT NULL,
    preco_unitario_na_compra FLOAT NOT NULL, 
    
    FOREIGN KEY (id_carrinho) REFERENCES carrinhos(id),
    FOREIGN KEY (id_produto) REFERENCES produtos(id)
);


-- DADOS INICIAIS CORRIGIDOS
INSERT INTO pessoas (nome, cpf, isAdm) VALUES
('Admin Inicial', '00000000000', TRUE), -- CPF sem formatação
('Cliente Teste', '11111111111', FALSE)
ON DUPLICATE KEY UPDATE nome=VALUES(nome);

INSERT INTO produtos (nome, preco_venda, preco_compra, quantidade) VALUES
('Arroz (5kg)', 25.50, 18.00, 100),
('Feijão Preto (1kg)', 8.90, 6.00, 150),
('Leite Integral (1L)', 4.50, 3.20, 200)
ON DUPLICATE KEY UPDATE quantidade=VALUES(quantidade);