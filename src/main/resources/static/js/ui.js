// Utilitários de interface: escape de HTML, rótulos, datas, avisos e modal.

export function esc(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

export const STATUS = {
  ABERTO: { label: "Aberto", cor: "var(--st-aberto)" },
  EM_ATENDIMENTO: { label: "Em atendimento", cor: "var(--st-em-atendimento)" },
  AGUARDANDO_CLIENTE: { label: "Aguardando cliente", cor: "var(--st-aguardando-cliente)" },
  FECHADO: { label: "Fechado", cor: "var(--st-fechado)" },
};

// Mesmas regras do enum StatusChamado.proximosPermitidos() no back-end
export const PROXIMOS_STATUS = {
  ABERTO: ["EM_ATENDIMENTO", "FECHADO"],
  EM_ATENDIMENTO: ["AGUARDANDO_CLIENTE", "FECHADO"],
  AGUARDANDO_CLIENTE: ["EM_ATENDIMENTO", "FECHADO"],
  FECHADO: [],
};

export const ACAO_STATUS = {
  EM_ATENDIMENTO: "Iniciar atendimento",
  AGUARDANDO_CLIENTE: "Aguardar o cliente",
  FECHADO: "Fechar chamado",
};

export const PRIORIDADE = {
  BAIXA: { label: "Baixa", cor: "var(--pr-baixa)" },
  MEDIA: { label: "Média", cor: "var(--pr-media)" },
  ALTA: { label: "Alta", cor: "var(--pr-alta)" },
  CRITICA: { label: "Crítica", cor: "var(--pr-critica)" },
};

export const PERFIL = { ADMIN: "Administrador", TECNICO: "Técnico" };

export function statusTag(status) {
  const s = STATUS[status] || { label: status, cor: "var(--muted)" };
  return `<span class="tag" style="--c:${s.cor}">${esc(s.label)}</span>`;
}

export function prioridadeTag(prioridade) {
  const p = PRIORIDADE[prioridade] || { label: prioridade, cor: "var(--muted)" };
  return `<span class="prio" style="--c:${p.cor}">${esc(p.label)}</span>`;
}

const fmtData = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" });
export function data(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return isNaN(d) ? esc(iso) : fmtData.format(d);
}

export function options(lista, selecionado, { vazio } = {}) {
  const itens = vazio !== undefined ? [`<option value="">${esc(vazio)}</option>`] : [];
  for (const [valor, texto] of lista) {
    const sel = String(valor) === String(selecionado ?? "") ? " selected" : "";
    itens.push(`<option value="${esc(valor)}"${sel}>${esc(texto)}</option>`);
  }
  return itens.join("");
}

// ---------- avisos ----------
export function toast(mensagem, tipo = "ok") {
  const el = document.createElement("div");
  el.className = `toast${tipo === "erro" ? " error" : ""}`;
  el.textContent = mensagem;
  const area = document.getElementById("toasts");
  area.append(el);
  while (area.children.length > 3) area.firstElementChild.remove();
  setTimeout(() => el.remove(), tipo === "erro" ? 6000 : 3500);
}

export function toastErro(err) {
  const extra = err.detalhes?.length ? ` (${err.detalhes.join("; ")})` : "";
  toast(err.message + extra, "erro");
}

// ---------- modal com formulário ----------
const modal = () => document.getElementById("modal");

export function fecharModal() {
  const m = modal();
  if (m.open) m.close();
  m.innerHTML = "";
}

/**
 * Abre um formulário no modal.
 * onSubmit recebe os dados do form; se lançar ApiError, os erros aparecem no próprio modal.
 */
export function abrirFormulario({ titulo, campos, botao, onSubmit }) {
  const m = modal();
  m.innerHTML = `
    <form class="form" novalidate>
      <h2>${esc(titulo)}</h2>
      <div class="form-errors" hidden></div>
      ${campos}
      <div class="form-actions">
        <button type="button" class="btn" data-cancel>Cancelar</button>
        <button type="submit" class="btn btn-primary">${esc(botao)}</button>
      </div>
    </form>`;

  const form = m.querySelector("form");
  const erros = m.querySelector(".form-errors");
  m.querySelector("[data-cancel]").onclick = fecharModal;

  form.onsubmit = async (ev) => {
    ev.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    submit.disabled = true;
    erros.hidden = true;
    try {
      await onSubmit(Object.fromEntries(new FormData(form)));
      fecharModal();
    } catch (err) {
      erros.innerHTML = esc(err.message) +
        (err.detalhes?.length ? `<ul>${err.detalhes.map((d) => `<li>${esc(d)}</li>`).join("")}</ul>` : "");
      erros.hidden = false;
    } finally {
      submit.disabled = false;
    }
  };

  m.showModal();
  form.querySelector("input, select, textarea")?.focus();
}

export function campo({ nome, rotulo, tipo = "text", valor = "", obrigatorio = false, max, ajuda, opcoes }) {
  const req = obrigatorio ? " required" : "";
  const maxAttr = max ? ` maxlength="${max}"` : "";
  let controle;
  if (tipo === "select") {
    controle = `<select name="${nome}"${req}>${opcoes}</select>`;
  } else if (tipo === "textarea") {
    controle = `<textarea name="${nome}"${req}${maxAttr}>${esc(valor)}</textarea>`;
  } else {
    controle = `<input name="${nome}" type="${tipo}" value="${esc(valor)}"${req}${maxAttr}>`;
  }
  return `<label class="field"><span>${esc(rotulo)}${obrigatorio ? "" : " (opcional)"}</span>${controle}${ajuda ? `<small>${esc(ajuda)}</small>` : ""}</label>`;
}

export function confirmar(mensagem) {
  return window.confirm(mensagem);
}
