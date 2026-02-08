const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    const err = new Error(body?.error || 'Erro na requisição');
    err.status = res.status;
    err.body = body;
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

export const getProducts = () => request('/products');
export const getProduct = (id) => request(`/products/${id}`);
export const createProduct = (data) => request('/products', { method: 'POST', body: JSON.stringify(data) });
export const updateProduct = (id, data) => request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) });
export const deleteProduct = (id) => request(`/products/${id}`, { method: 'DELETE' });

export const getRawMaterials = () => request('/raw-materials');
export const getRawMaterial = (id) => request(`/raw-materials/${id}`);
export const createRawMaterial = (data) => request('/raw-materials', { method: 'POST', body: JSON.stringify(data) });
export const updateRawMaterial = (id, data) => request(`/raw-materials/${id}`, { method: 'PUT', body: JSON.stringify(data) });
export const deleteRawMaterial = (id) => request(`/raw-materials/${id}`, { method: 'DELETE' });

export const getProductRawMaterials = (productId) => request(`/products/${productId}/raw-materials`);
export const addProductRawMaterial = (productId, data) => request(`/products/${productId}/raw-materials`, { method: 'POST', body: JSON.stringify(data) });
export const updateProductRawMaterial = (productId, rawMaterialId, data) => request(`/products/${productId}/raw-materials/${rawMaterialId}`, { method: 'PUT', body: JSON.stringify(data) });
export const removeProductRawMaterial = (productId, rawMaterialId) => request(`/products/${productId}/raw-materials/${rawMaterialId}`, { method: 'DELETE' });

export const getProductionSuggestion = () => request('/production/suggestion');
