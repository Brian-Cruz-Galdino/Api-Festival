-- Inserir usuários
INSERT INTO usuario (id, nome, email, senha, tipo) VALUES (1, 'Admin Sistema', 'admin@festivais.com', '$2a$10$B/gHc3/pYlTZ8fMTlhnY9./LCG/VAw4FoogTZe0bjYhnoyZiAkaXm', 'ADMIN');
INSERT INTO usuario (id, nome, email, senha, tipo) VALUES (2, 'João Cliente', 'joao@email.com', '$2a$10$xyz456', 'CLIENTE');
INSERT INTO usuario (id, nome, email, senha, tipo) VALUES (3, 'Maria Organizadora', 'maria@eventos.com', '$2a$10$abc789', 'ORGANIZADOR');

-- Inserir artistas
INSERT INTO artista (id, nome, genero_musical, biografia) VALUES (1, 'Coldplay', 'Rock', 'Banda britanica de rock formada em 1996');
INSERT INTO artista (id, nome, genero_musical, biografia) VALUES (2, 'Beyonce', 'Pop/R&B', 'Cantora, compositora e produtora musical americana');
INSERT INTO artista (id, nome, genero_musical, biografia) VALUES (3, 'Drake', 'Hip-Hop/Rap', 'Rapper, cantor e compositor canadense');
INSERT INTO artista (id, nome, genero_musical, biografia) VALUES (4, 'Lady Gaga', 'Pop', 'Cantora, compositora e atriz americana');

-- Inserir eventos
INSERT INTO evento (id, nome, descricao, data_evento, local, capacidade_maxima, preco_ingresso, status) VALUES (1, 'Festival de Verao', 'O maior festival de verao do pais com os melhores artistas', '2024-12-15', 'Praia de Copacabana', 50000, 250.0, 'DISPONIVEL');
INSERT INTO evento (id, nome, descricao, data_evento, local, capacidade_maxima, preco_ingresso, status) VALUES (2, 'Rock in Rio', 'O famoso festival de rock que acontece no Brasil', '2024-09-15', 'Parque Olimpico', 100000, 350.0, 'DISPONIVEL');
INSERT INTO evento (id, nome, descricao, data_evento, local, capacidade_maxima, preco_ingresso, status) VALUES (3, 'Lollapalooza', 'Festival de musica alternativa que reune diversos generos', '2024-03-25', 'Autodromo de Interlagos', 80000, 300.0, 'DISPONIVEL');
INSERT INTO evento (id, nome, descricao, data_evento, local, capacidade_maxima, preco_ingresso, status) VALUES (4, 'Tomorrowland', 'Festival de musica eletronica mais famoso do mundo', '2024-07-20', 'Boom, Belgica', 60000, 500.0, 'DISPONIVEL');

-- Inserir relação entre eventos e artistas
INSERT INTO evento_artista (evento_id, artista_id) VALUES (1, 1);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (1, 2);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (2, 1);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (2, 3);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (3, 2);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (3, 4);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (4, 3);
INSERT INTO evento_artista (evento_id, artista_id) VALUES (4, 4);

-- Inserir ingressos
INSERT INTO ingresso (id, nome_comprador, email_comprador, data_compra, quantidade, preco_total, status, evento_id) VALUES (1, 'Joao Silva', 'joao@email.com', '2023-11-10 10:30:00', 2, 500.0, 'PAGO', 1);
INSERT INTO ingresso (id, nome_comprador, email_comprador, data_compra, quantidade, preco_total, status, evento_id) VALUES (2, 'Maria Santos', 'maria@email.com', '2023-11-11 14:45:00', 1, 250.0, 'RESERVADO', 1);
INSERT INTO ingresso (id, nome_comprador, email_comprador, data_compra, quantidade, preco_total, status, evento_id) VALUES (3, 'Pedro Costa', 'pedro@email.com', '2023-11-12 09:15:00', 4, 1400.0, 'PAGO', 2);
INSERT INTO ingresso (id, nome_comprador, email_comprador, data_compra, quantidade, preco_total, status, evento_id) VALUES (4, 'Ana Oliveira', 'ana@email.com', '2023-11-13 16:20:00', 2, 600.0, 'RESERVADO', 3);

-- Inserir API Keys de exemplo
INSERT INTO api_key (id, chave, usuario_id, data_criacao, data_expiracao, status) VALUES (1, 'demo_key_123456', 1, '2024-01-01 10:00:00', '2024-12-31 23:59:59', 'ATIVA');