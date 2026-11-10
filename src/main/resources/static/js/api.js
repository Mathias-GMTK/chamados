// Cliente da API REST. Todas as chamadas do front passam por aqui,
// seguindo os endpoints documentados no Swagger (/v3/api-docs).

export class ApiError extends Error {
  constructor(status, erro, detalhes = []) {
    super(erro);
    this.status = status;
    this.detalhes = detalhes;
  }
}

async function request(method, url, body) {
  const options = { method, headers: { Accept: "application/json" } };
  if (body !== undefined) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }

  let resp;
  try {
    resp = await fetch(url, options);
  } catch {
    throw new ApiError(0, "Não foi possível conectar à API. Verifique se a aplicação está rodando.");
  }

  if (resp.status === 204) return null;

  const text = await resp.text();
  const data = text ? JSON.parse(text) : null;

  if (!resp.ok) {
    // Formato ErroResposta: { momento, status, erro, detalhes }
    const erro = data?.erro || `Erro ${resp.status} ao acessar ${url}`;
    throw new ApiError(resp.status, erro, data?.detalhes || []);
  }
  return data;
}

const get = (url) => request("GET", url);
const post = (url, body) => request("POST", url, body);
const put = (url, body) => request("PUT", url, body);
const patch = (url, body) => request("PATCH", url, body);
const del = (url) => request("DELETE", url);

function query(params) {
  const q = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== "") q.append(k, v);
  }
  const s = q.toString();
  return s ? `?${s}` : "";
}

// CRUD genérico para clientes, categorias e técnicos
function crud(base) {
  return {
    listar: () => get(base),
    buscar: (id) => get(`${base}/${id}`),
    criar: (dados) => post(base, dados),
    atualizar: (id, dados) => put(`${base}/${id}`, dados),
    excluir: (id) => del(`${base}/${id}`),
  };
}

export const clientes = crud("/clientes");
export const categorias = crud("/categorias");
export const tecnicos = crud("/tecnicos");

export const chamados = {
  // Resposta paginada: { content: [...], page: { size, number, totalElements, totalPages } }
  listar: ({ status, prioridade, clienteId, page = 0, size = 10 } = {}) =>
    get(`/chamados${query({ status, prioridade, clienteId, page, size, sort: "dataAbertura,desc" })}`),
  buscar: (id) => get(`/chamados/${id}`),
  criar: (dados) => post("/chamados", dados),
  atualizar: (id, dados) => put(`/chamados/${id}`, dados),
  atribuir: (id, tecnicoId) => patch(`/chamados/${id}/atribuir`, { tecnicoId }),
  alterarStatus: (id, status) => patch(`/chamados/${id}/status`, { status }),
  comentar: (id, dados) => post(`/chamados/${id}/comentarios`, dados),
  excluir: (id) => del(`/chamados/${id}`),
};
