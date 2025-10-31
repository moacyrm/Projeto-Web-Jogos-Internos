# Projeto Web Jogos Internos

Sistema de gerenciamento de jogos internos para instituições de ensino.

## 🧩 Visão Geral

Este projeto tem como objetivo implementar um sistema completo para gerenciamento de jogos internos entre equipes de uma instituição (campi, cursos, atletas, esportes, grupos, eventos, eliminatórias etc).
O sistema foi desenvolvido com

* Backend em Spring Boot,
* Banco de dados PostgreSQL,
* Arquitetura em camadas (controller, service, repository, entities, DTOs).
  Ele contempla funcionalidades como:
* Cadastro de atletas, equipes, cursos, campi e esportes.
* Sorteio de grupos para fases qualificatórias (com 3, 4 ou 5 equipes por grupo).
* Geração de chaves eliminatórias (ex: simples, dupla eliminação) com lógica para evitar confrontos antecipados entre equipes do mesmo grupo.
* Suporte automático à equipe “Bye” quando necessário (número ímpar de equipes).
* DTOs para visualização e testes via Postman.
* Validações como limite mínimo/máximo de atletas por esporte, restrição de uma equipe por curso por esporte, separação de eventos por nível de curso (integrado, técnico, superior).
* API REST para interação com o frontend ou via Postman.

## ✅ Funcionalidades principais

* Cadastro, edição, listagem de atletas, equipes, cursos, campi, esportes, eventos.
* Sorteio automático de grupos com regras predefinidas.
* Geração de chaves eliminatórias (fases, confrontos, “Bye”).
* Validações de negócio:

  * Limite mínimo e máximo de atletas por esporte.
  * Restrição de uma equipe por curso por esporte.
  * Separação de eventos por nível de curso.
  * Impedir rematch de equipes do mesmo grupo antes da final, com lógica de reembaralhamento.
* Endpoints REST com DTOs para facilitar consumo por frontend ou para testes.

## 🛠️ Tecnologias usadas

* Java + Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven
* DTOs para camada de API
* Camadas bem definidas: Controller → Service → Repository
* Lógica de negócios específica para torneios corporativos/internos

## 📋 Requisitos (como descrito no enunciado)

* Criação de banco de dados, filegroups, esquemas, logins, usuários, permissões e índices (geralmente no ambiente do servidor).
* Suporte à equipe “Bye” para número ímpar de equipes.
* Criação automática de confrontos com data/hora.
* Separação dos eventos por tipo de curso (integrado, técnico, superior).
* Uso obrigatório do técnico como criador da equipe.
* Sorteio de grupos e confrontos, com 3, 4 ou 5 equipes por grupo.
* Dois melhores de cada grupo avançam para fase eliminatória.
* Validações de limites de atletas por esporte e fase eliminatória.
* Evitar que equipes do mesmo grupo se enfrentem antes da final, ou reembaralhar quando necessário.
* Geração de DTOs para melhorar visualização no Postman.

## 📌 Em andamento / Próximos passos

* Refatoração de serviços que ainda podem ser otimizados (ex: `EquipeService`, `JogoService`).
* Testes de integração e unitários completos.
* Frontend de consumo da API (se ainda não desenvolvido).
* Documentação Swagger/OpenAPI para exposição dos endpoints.
* Deploy em ambiente de produção ou nuvem.

## 🤝 Como contribuir

1. Faça um fork deste repositório.
2. Crie uma nova branch para sua feature ou correção: `git checkout -b feature/nova-funcionalidade`.
3. Realize suas modificações e commit com mensagens claras: `git commit -m "Descrição da mudança"`.
4. Envie para o repositório remoto: `git push origin feature/nova-funcionalidade`.
5. Crie um Pull Request explicando o que foi modificado e por quê.
