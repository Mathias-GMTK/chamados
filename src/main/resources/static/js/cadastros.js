// Telas de cadastro (clientes, categorias e técnicos).
// As três seguem o mesmo padrão, então são descritas por configuração.

import * as api from "./api.js";
import { esc, campo, options, abrirFormulario, toast, toastErro, confirmar, PERFIL } from "./ui.js";

const CADASTROS = {
  clientes: {
    api: api.clientes,
    titulo: "Clientes",
    descricao: "Quem abre os chamados.",
    singular: "cliente",
    novo: "Cadastrar cliente",
    vazio: "Nenhum cliente cadastrado. Cadastre o primeiro para poder abrir chamados.",
    colunas: [
      ["Nome", (c) => `<div class="cell-title">${esc(c.nome)}</div>`],
      ["E-mail", (c) => esc(c.email)],
      ["Telefone", (c) => esc(c.telefone || "—"), "hide-sm"],
    ],
    extras: (c) => `<a class="btn-link" href="#/chamados?clienteId=${c.id}">Ver chamados</a>`,
    campos: (c = {}) =>
      campo({ nome: "nome", rotulo: "Nome", valor: c.nome, obrigatorio: true, max: 100 }) +
      campo({ nome: "email", rotulo: "E-mail", tipo: "email", valor: c.email, obrigatorio: true }) +
      campo({ nome: "telefone", rotulo: "Telefone", tipo: "tel", valor: c.telefone, max: 20 }),
    payload: (f) => ({ nome: f.nome.trim(), email: f.email.trim(), telefone: f.telefone.trim() || null }),
  },

  categorias: {
    api: api.categorias,
    titulo: "Categorias",
    descricao: "Tipos de problema usados para classificar os chamados.",
    singular: "categoria",
    feminino: true,
    novo: "Cadastrar categoria",
    vazio: "Nenhuma categoria cadastrada. Crie categorias como Rede, Hardware ou Acesso.",
    colunas: [
      ["Nome", (c) => `<div class="cell-title">${esc(c.nome)}</div>`],
      ["Descrição", (c) => esc(c.descricao || "—")],
    ],
    campos: (c = {}) =>
      campo({ nome: "nome", rotulo: "Nome", valor: c.nome, obrigatorio: true, max: 100 }) +
      campo({ nome: "descricao", rotulo: "Descrição", valor: c.descricao, max: 100 }),
    payload: (f) => ({ nome: f.nome.trim(), descricao: f.descricao.trim() || null }),
  },

  tecnicos: {
    api: api.tecnicos,
    titulo: "Técnicos",
    descricao: "Quem atende os chamados.",
    singular: "técnico",
    novo: "Cadastrar técnico",
    vazio: "Nenhum técnico cadastrado. Cadastre quem vai atender os chamados.",
    colunas: [
      ["Nome", (t) => `<div class="cell-title">${esc(t.nome)}</div>`],
      ["E-mail", (t) => esc(t.email)],
      ["Perfil", (t) => esc(PERFIL[t.perfil] || t.perfil)],
    ],
    campos: (t = {}) =>
      campo({ nome: "nome", rotulo: "Nome", valor: t.nome, obrigatorio: true, max: 100 }) +
      campo({ nome: "email", rotulo: "E-mail", tipo: "email", valor: t.email, obrigatorio: true, max: 150 }) +
      campo({
        nome: "perfil", rotulo: "Perfil", tipo: "select", obrigatorio: true,
        opcoes: options(Object.entries(PERFIL), t.perfil || "TECNICO"),
      }),
    payload: (f) => ({ nome: f.nome.trim(), email: f.email.trim(), perfil: f.perfil }),
  },
};

export async function renderCadastro(app, tipo) {
  const cfg = CADASTROS[tipo];
  app.innerHTML = `
    <header class="page-head">
      <div><h1>${cfg.titulo}</h1><p>${esc(cfg.descricao)}</p></div>
      <button class="btn btn-primary" data-novo>${esc(cfg.novo)}</button>
    </header>
    <section class="panel" data-lista><div class="loading">Carregando…</div></section>`;

  const lista = app.querySelector("[data-lista]");

  async function carregar() {
    let itens;
    try {
      itens = await cfg.api.listar();
    } catch (err) {
      lista.innerHTML = `<div class="empty"><p>${esc(err.message)}</p></div>`;
      return;
    }

    if (!itens.length) {
      lista.innerHTML = `<div class="empty"><p>${esc(cfg.vazio)}</p>
        <button class="btn btn-primary" data-novo>${esc(cfg.novo)}</button></div>`;
      lista.querySelector("[data-novo]").onclick = () => abrir();
      return;
    }

    lista.innerHTML = `
      <div class="table-wrap"><table>
        <thead><tr>
          <th>#</th>
          ${cfg.colunas.map(([t, , cls]) => `<th class="${cls || ""}">${t}</th>`).join("")}
          <th><span class="hide-sm">Ações</span></th>
        </tr></thead>
        <tbody>
          ${itens.map((item) => `
            <tr>
              <td class="num">${item.id}</td>
              ${cfg.colunas.map(([, fn, cls]) => `<td class="${cls || ""}">${fn(item)}</td>`).join("")}
              <td class="actions">
                ${cfg.extras ? cfg.extras(item) : ""}
                <button class="btn-link" data-editar="${item.id}">Editar</button>
                <button class="btn-link danger" data-excluir="${item.id}">Excluir</button>
              </td>
            </tr>`).join("")}
        </tbody>
      </table></div>`;

    const porId = new Map(itens.map((i) => [String(i.id), i]));
    lista.querySelectorAll("[data-editar]").forEach((b) => {
      b.onclick = () => abrir(porId.get(b.dataset.editar));
    });
    lista.querySelectorAll("[data-excluir]").forEach((b) => {
      b.onclick = () => excluir(porId.get(b.dataset.excluir));
    });
  }

  function abrir(item) {
    const editando = Boolean(item);
    abrirFormulario({
      titulo: editando ? `Editar ${cfg.singular}` : cfg.novo,
      campos: cfg.campos(item),
      botao: editando ? "Salvar alterações" : "Cadastrar",
      onSubmit: async (form) => {
        const dados = cfg.payload(form);
        if (editando) {
          await cfg.api.atualizar(item.id, dados);
          toast(msg(cfg, "atualizad"));
        } else {
          await cfg.api.criar(dados);
          toast(msg(cfg, "cadastrad"));
        }
        await carregar();
      },
    });
  }

  async function excluir(item) {
    if (!confirmar(`Excluir ${cfg.singular} "${item.nome}"? Essa ação não pode ser desfeita.`)) return;
    try {
      await cfg.api.excluir(item.id);
      toast(msg(cfg, "excluíd"));
      await carregar();
    } catch (err) {
      toastErro(err);
    }
  }

  app.querySelector("[data-novo]").onclick = () => abrir();
  await carregar();
}

// "Cliente cadastrado." / "Categoria cadastrada."
function msg(cfg, verbo) {
  const nome = cfg.singular.charAt(0).toUpperCase() + cfg.singular.slice(1);
  return `${nome} ${verbo}${cfg.feminino ? "a" : "o"}.`;
}
