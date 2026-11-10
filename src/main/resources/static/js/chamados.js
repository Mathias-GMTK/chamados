// Telas de chamados: lista com filtros e paginação, abertura, detalhe e atendimento.

import * as api from "./api.js";
import {
  esc, data, options, statusTag, prioridadeTag, campo, abrirFormulario,
  toast, toastErro, confirmar, STATUS, PRIORIDADE, PROXIMOS_STATUS, ACAO_STATUS,
} from "./ui.js";

// Clientes, categorias e técnicos, para mostrar nomes no lugar de IDs
async function carregarCadastros() {
  const [clientes, categorias, tecnicos] = await Promise.all([
    api.clientes.listar(), api.categorias.listar(), api.tecnicos.listar(),
  ]);
  const mapa = (lista) => new Map(lista.map((i) => [i.id, i]));
  return {
    clientes, categorias, tecnicos,
    cliente: mapa(clientes), categoria: mapa(categorias), tecnico: mapa(tecnicos),
  };
}

const nome = (mapa, id) => (id == null ? null : mapa.get(id)?.nome ?? `#${id}`);

// ======================================================================
// Lista
// ======================================================================
export async function renderLista(app, params) {
  const filtro = {
    status: params.get("status") || "",
    prioridade: params.get("prioridade") || "",
    clienteId: params.get("clienteId") || "",
    page: Number(params.get("page") || 0),
  };

  app.innerHTML = `
    <header class="page-head">
      <div><h1>Chamados</h1><p>Acompanhe e atenda as solicitações de suporte.</p></div>
      <button class="btn btn-primary" data-novo>Abrir chamado</button>
    </header>
    <div data-aviso></div>
    <section class="statusbar" data-statusbar aria-label="Chamados por status"></section>
    <div class="filters" data-filtros></div>
    <section class="panel" data-lista><div class="loading">Carregando…</div></section>`;

  let cad;
  try {
    cad = await carregarCadastros();
  } catch (err) {
    app.querySelector("[data-lista]").innerHTML = `<div class="empty"><p>${esc(err.message)}</p></div>`;
    return;
  }

  app.querySelector("[data-novo]").onclick = () => formularioChamado(cad);

  if (!cad.clientes.length || !cad.categorias.length) {
    const falta = [
      !cad.clientes.length && `<a href="#/clientes">um cliente</a>`,
      !cad.categorias.length && `<a href="#/categorias">uma categoria</a>`,
    ].filter(Boolean).join(" e ");
    app.querySelector("[data-aviso]").innerHTML =
      `<div class="hint">Para abrir um chamado, cadastre antes ${falta}.</div>`;
  }

  const irPara = (mudancas) => {
    const novo = { ...filtro, page: 0, ...mudancas };
    const q = new URLSearchParams();
    for (const [k, v] of Object.entries(novo)) if (v !== "" && v !== 0) q.set(k, v);
    location.hash = `#/chamados${q.toString() ? "?" + q : ""}`;
  };

  // ---- filtros ----
  const filtros = app.querySelector("[data-filtros]");
  filtros.innerHTML = `
    <label>Prioridade
      <select data-f="prioridade">${options(
        Object.entries(PRIORIDADE).map(([k, v]) => [k, v.label]), filtro.prioridade, { vazio: "Todas" })}</select>
    </label>
    <label>Cliente
      <select data-f="clienteId">${options(
        cad.clientes.map((c) => [c.id, c.nome]), filtro.clienteId, { vazio: "Todos" })}</select>
    </label>
    ${filtro.status || filtro.prioridade || filtro.clienteId
      ? `<button class="btn" data-limpar>Limpar filtros</button>` : ""}`;
  filtros.querySelectorAll("[data-f]").forEach((sel) => {
    sel.onchange = () => irPara({ [sel.dataset.f]: sel.value });
  });
  filtros.querySelector("[data-limpar]")?.addEventListener("click", () =>
    irPara({ status: "", prioridade: "", clienteId: "" }));

  // ---- barra de status: um total por status, respeitando os outros filtros ----
  const statusbar = app.querySelector("[data-statusbar]");
  const base = { prioridade: filtro.prioridade, clienteId: filtro.clienteId, size: 1 };
  Promise.all(Object.keys(STATUS).map((st) =>
    api.chamados.listar({ ...base, status: st }).then((r) => [st, r.page.totalElements])))
    .then((totais) => {
      const soma = totais.reduce((acc, [, n]) => acc + n, 0);
      statusbar.innerHTML = `
        <div class="statusbar-track" aria-hidden="true">
          ${soma === 0 ? "" : totais.map(([st, n]) =>
            `<span style="flex-grow:${n};background:${STATUS[st].cor}"></span>`).join("")}
        </div>
        <div class="statusbar-legend">
          ${totais.map(([st, n]) => `
            <button class="st-chip" style="--c:${STATUS[st].cor}" data-st="${st}"
              aria-pressed="${filtro.status === st}">
              <strong>${n}</strong><span>${STATUS[st].label}</span>
            </button>`).join("")}
        </div>`;
      statusbar.querySelectorAll("[data-st]").forEach((b) => {
        b.onclick = () => irPara({ status: filtro.status === b.dataset.st ? "" : b.dataset.st });
      });
    })
    .catch(() => { statusbar.hidden = true; });

  // ---- tabela ----
  const lista = app.querySelector("[data-lista]");
  let pagina;
  try {
    pagina = await api.chamados.listar(filtro);
  } catch (err) {
    lista.innerHTML = `<div class="empty"><p>${esc(err.message)}</p></div>`;
    return;
  }

  const { content, page } = pagina;
  if (!content.length) {
    const filtrando = filtro.status || filtro.prioridade || filtro.clienteId;
    lista.innerHTML = `<div class="empty"><p>${filtrando
      ? "Nenhum chamado com esses filtros."
      : "Nenhum chamado aberto ainda."}</p>
      ${filtrando ? "" : `<button class="btn btn-primary" data-novo2>Abrir chamado</button>`}</div>`;
    lista.querySelector("[data-novo2]")?.addEventListener("click", () => formularioChamado(cad));
    return;
  }

  lista.innerHTML = `
    <div class="table-wrap"><table>
      <thead><tr>
        <th>#</th><th>Chamado</th><th>Status</th><th>Prioridade</th>
        <th class="hide-sm">Técnico</th><th class="hide-sm">Aberto em</th>
      </tr></thead>
      <tbody>
        ${content.map((c) => `
          <tr class="clickable" data-id="${c.id}" tabindex="0">
            <td class="num">${c.id}</td>
            <td>
              <div class="cell-title">${esc(c.titulo)}</div>
              <div class="cell-sub">${esc(nome(cad.cliente, c.clienteId))} · ${esc(nome(cad.categoria, c.categoriaId))}</div>
            </td>
            <td>${statusTag(c.status)}</td>
            <td>${prioridadeTag(c.prioridade)}</td>
            <td class="hide-sm">${esc(nome(cad.tecnico, c.tecnicoId) ?? "Sem técnico")}</td>
            <td class="hide-sm">${data(c.dataAbertura)}</td>
          </tr>`).join("")}
      </tbody>
    </table></div>
    <div class="pager">
      <span>Página ${page.number + 1} de ${Math.max(page.totalPages, 1)} · ${page.totalElements} chamado(s)</span>
      <div>
        <button class="btn" data-pg="${page.number - 1}" ${page.number === 0 ? "disabled" : ""}>Anterior</button>
        <button class="btn" data-pg="${page.number + 1}" ${page.number + 1 >= page.totalPages ? "disabled" : ""}>Próxima</button>
      </div>
    </div>`;

  lista.querySelectorAll("tr[data-id]").forEach((tr) => {
    const abrir = () => { location.hash = `#/chamados/${tr.dataset.id}`; };
    tr.onclick = abrir;
    tr.onkeydown = (e) => { if (e.key === "Enter") abrir(); };
  });
  lista.querySelectorAll("[data-pg]").forEach((b) => {
    b.onclick = () => irPara({ page: Number(b.dataset.pg) });
  });
}

// ======================================================================
// Abrir / editar chamado
// ======================================================================
function formularioChamado(cad, chamado) {
  const editando = Boolean(chamado);
  if (!cad.clientes.length || !cad.categorias.length) {
    toast("Cadastre ao menos um cliente e uma categoria antes de abrir um chamado.", "erro");
    return;
  }
  const c = chamado || { prioridade: "MEDIA" };

  abrirFormulario({
    titulo: editando ? `Editar chamado #${c.id}` : "Abrir chamado",
    botao: editando ? "Salvar alterações" : "Abrir chamado",
    campos:
      campo({ nome: "titulo", rotulo: "Título", valor: c.titulo, obrigatorio: true, max: 150,
        ajuda: "Resuma o problema em uma frase." }) +
      campo({ nome: "descricao", rotulo: "Descrição", tipo: "textarea", valor: c.descricao, max: 2000 }) +
      `<div class="field-row">` +
        campo({ nome: "clienteId", rotulo: "Cliente", tipo: "select", obrigatorio: true,
          opcoes: options(cad.clientes.map((x) => [x.id, x.nome]), c.clienteId, { vazio: "Selecione" }) }) +
        campo({ nome: "categoriaId", rotulo: "Categoria", tipo: "select", obrigatorio: true,
          opcoes: options(cad.categorias.map((x) => [x.id, x.nome]), c.categoriaId, { vazio: "Selecione" }) }) +
      `</div><div class="field-row">` +
        campo({ nome: "prioridade", rotulo: "Prioridade", tipo: "select", obrigatorio: true,
          opcoes: options(Object.entries(PRIORIDADE).map(([k, v]) => [k, v.label]), c.prioridade) }) +
        (editando ? "" : campo({ nome: "tecnicoId", rotulo: "Técnico", tipo: "select",
          opcoes: options(cad.tecnicos.map((x) => [x.id, x.nome]), "", { vazio: "Definir depois" }) })) +
      `</div>`,
    onSubmit: async (f) => {
      const dados = {
        titulo: f.titulo.trim(),
        descricao: f.descricao.trim() || null,
        prioridade: f.prioridade,
        clienteId: f.clienteId ? Number(f.clienteId) : null,
        categoriaId: f.categoriaId ? Number(f.categoriaId) : null,
        tecnicoId: editando ? c.tecnicoId : (f.tecnicoId ? Number(f.tecnicoId) : null),
      };
      if (editando) {
        await api.chamados.atualizar(c.id, dados);
        toast("Chamado atualizado.");
        await renderRotaAtual();
      } else {
        const criado = await api.chamados.criar(dados);
        toast(`Chamado #${criado.id} aberto.`);
        location.hash = `#/chamados/${criado.id}`;
      }
    },
  });
}

// ======================================================================
// Detalhe
// ======================================================================
let renderRotaAtual = async () => {};

export async function renderDetalhe(app, id) {
  renderRotaAtual = () => renderDetalhe(app, id);
  app.innerHTML = `<div class="loading">Carregando…</div>`;

  let c, cad;
  try {
    [c, cad] = await Promise.all([api.chamados.buscar(id), carregarCadastros()]);
  } catch (err) {
    app.innerHTML = `
      <a class="back" href="#/chamados">Voltar para chamados</a>
      <div class="panel empty"><p>${esc(err.message)}</p></div>`;
    return;
  }

  const fechado = c.status === "FECHADO";
  const proximos = PROXIMOS_STATUS[c.status] || [];

  app.innerHTML = `
    <a class="back" href="#/chamados">Voltar para chamados</a>
    <header class="page-head">
      <div><h1>Chamado #${c.id}</h1></div>
      <div style="display:flex;gap:8px">
        ${fechado ? "" : `<button class="btn" data-editar>Editar</button>`}
        <button class="btn btn-danger" data-excluir>Excluir</button>
      </div>
    </header>

    <div class="detail">
      <article class="panel detail-main">
        <h2>${esc(c.titulo)}</h2>
        <p class="detail-desc">${c.descricao ? esc(c.descricao) : `<span class="cell-sub">Sem descrição.</span>`}</p>

        <section class="comments">
          <h3>Comentários (${c.comentarios.length})</h3>
          ${c.comentarios.length ? `
            <ol class="comment-list">
              ${c.comentarios.map((m) => `
                <li class="comment">
                  <header><b>${esc(m.autor)}</b> em ${data(m.data)}</header>
                  <p>${esc(m.texto)}</p>
                </li>`).join("")}
            </ol>` : `<p class="cell-sub">Nenhum comentário ainda. Registre aqui o que foi feito no atendimento.</p>`}
          <form class="comment-form" data-comentar>
            <textarea name="texto" maxlength="1000" required placeholder="Escreva um comentário"
              aria-label="Comentário"></textarea>
            <div class="row">
              <input name="autor" maxlength="100" required placeholder="Seu nome" aria-label="Autor"
                value="${esc(autorSalvo())}">
              <button class="btn btn-primary" type="submit">Comentar</button>
            </div>
          </form>
        </section>
      </article>

      <aside class="panel detail-side">
        <div class="side-block">
          <h3>Status</h3>
          ${statusTag(c.status)}
        </div>

        <div class="side-block">
          <h3>Próximo passo</h3>
          ${proximos.length ? `<div class="next-steps">
            ${proximos.map((st) => `
              <button class="btn" style="--c:${STATUS[st].cor}" data-status="${st}">${esc(ACAO_STATUS[st])}</button>`).join("")}
          </div>` : `<p class="closed-note">Chamado fechado em ${data(c.dataFechamento)}. Não pode mais ser alterado.</p>`}
        </div>

        <div class="side-block">
          <h3>Técnico responsável</h3>
          ${fechado
            ? `<p class="closed-note">${esc(nome(cad.tecnico, c.tecnicoId) ?? "Nenhum")}</p>`
            : cad.tecnicos.length ? `
              <form class="inline-form" data-atribuir>
                <select name="tecnicoId" aria-label="Técnico" required>
                  ${options(cad.tecnicos.map((t) => [t.id, t.nome]), c.tecnicoId, { vazio: "Selecione" })}
                </select>
                <button class="btn" type="submit">Atribuir</button>
              </form>`
            : `<p class="closed-note">Nenhum técnico cadastrado. <a href="#/tecnicos">Cadastrar técnico</a></p>`}
        </div>

        <div class="side-block">
          <h3>Detalhes</h3>
          <dl class="facts">
            <dt>Cliente</dt><dd>${esc(nome(cad.cliente, c.clienteId))}</dd>
            <dt>Categoria</dt><dd>${esc(nome(cad.categoria, c.categoriaId))}</dd>
            <dt>Prioridade</dt><dd>${prioridadeTag(c.prioridade)}</dd>
            <dt>Aberto em</dt><dd>${data(c.dataAbertura)}</dd>
            ${c.dataFechamento ? `<dt>Fechado em</dt><dd>${data(c.dataFechamento)}</dd>` : ""}
          </dl>
        </div>
      </aside>
    </div>`;

  // ---- ações ----
  app.querySelector("[data-editar]")?.addEventListener("click", () => formularioChamado(cad, c));

  app.querySelector("[data-excluir]").onclick = async () => {
    if (!confirmar(`Excluir o chamado #${c.id} e todos os comentários? Essa ação não pode ser desfeita.`)) return;
    try {
      await api.chamados.excluir(c.id);
      toast(`Chamado #${c.id} excluído.`);
      location.hash = "#/chamados";
    } catch (err) { toastErro(err); }
  };

  app.querySelectorAll("[data-status]").forEach((b) => {
    b.onclick = async () => {
      const novo = b.dataset.status;
      if (novo === "FECHADO" && !confirmar("Fechar o chamado? Depois de fechado ele não pode mais ser alterado.")) return;
      b.disabled = true;
      try {
        await api.chamados.alterarStatus(c.id, novo);
        toast(`Status alterado para ${STATUS[novo].label.toLowerCase()}.`);
        await renderDetalhe(app, id);
      } catch (err) { toastErro(err); b.disabled = false; }
    };
  });

  app.querySelector("[data-atribuir]")?.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const tecnicoId = Number(new FormData(ev.target).get("tecnicoId"));
    if (!tecnicoId) return;
    try {
      await api.chamados.atribuir(c.id, tecnicoId);
      toast(`Chamado atribuído a ${nome(cad.tecnico, tecnicoId)}.`);
      await renderDetalhe(app, id);
    } catch (err) { toastErro(err); }
  });

  app.querySelector("[data-comentar]").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const f = Object.fromEntries(new FormData(ev.target));
    if (!f.texto.trim() || !f.autor.trim()) {
      toast("Preencha o comentário e o seu nome.", "erro");
      return;
    }
    try {
      await api.chamados.comentar(c.id, { texto: f.texto.trim(), autor: f.autor.trim() });
      salvarAutor(f.autor.trim());
      toast("Comentário adicionado.");
      await renderDetalhe(app, id);
    } catch (err) { toastErro(err); }
  });
}

// Lembra o nome de quem comenta neste navegador, para não digitar toda vez
function autorSalvo() {
  try { return localStorage.getItem("chamados.autor") || ""; } catch { return ""; }
}
function salvarAutor(nomeAutor) {
  try { localStorage.setItem("chamados.autor", nomeAutor); } catch { /* sem armazenamento */ }
}
