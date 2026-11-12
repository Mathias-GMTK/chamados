// Roteamento simples por hash (#/chamados, #/chamados/5, #/clientes ...)

import { renderLista, renderDetalhe } from "./chamados.js";
import { renderCadastro } from "./cadastros.js";
import { fecharModal } from "./ui.js";

const app = document.getElementById("app");

async function rotear() {
  fecharModal();
  const [caminho, busca = ""] = (location.hash.slice(1) || "/chamados").split("?");
  const partes = caminho.split("/").filter(Boolean);
  const secao = partes[0] || "chamados";

  document.querySelectorAll(".nav a").forEach((a) => {
    if (a.dataset.section === secao) a.setAttribute("aria-current", "page");
    else a.removeAttribute("aria-current");
  });

  if (secao === "chamados" && partes[1]) {
    await renderDetalhe(app, partes[1]);
  } else if (secao === "chamados") {
    await renderLista(app, new URLSearchParams(busca));
  } else if (["clientes", "categorias", "tecnicos"].includes(secao)) {
    await renderCadastro(app, secao);
  } else {
    location.hash = "#/chamados";
    return;
  }
  app.focus({ preventScroll: true });
}

window.addEventListener("hashchange", rotear);
rotear();
