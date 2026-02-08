import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getProducts, createProduct, updateProduct, deleteProduct } from '../api'

const empty = { code: '', name: '', value: '' }

function ProductList() {
  const [items, setItems] = useState([])
  const [form, setForm] = useState(empty)
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    getProducts()
      .then(setItems)
      .catch(() => setError('Erro ao carregar produtos'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const data = { ...form, value: parseFloat(form.value) }
    try {
      if (editingId) {
        await updateProduct(editingId, data)
      } else {
        await createProduct(data)
      }
      setForm(empty)
      setEditingId(null)
      load()
    } catch (err) {
      if (err.body && typeof err.body === 'object' && !err.body.error) {
        setError(Object.values(err.body).join(', '))
      } else {
        setError(err.message)
      }
    }
  }

  const handleEdit = (item) => {
    setEditingId(item.id)
    setForm({ code: item.code, name: item.name, value: String(item.value) })
    setError('')
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Deseja excluir este produto?')) return
    try {
      await deleteProduct(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleCancel = () => {
    setEditingId(null)
    setForm(empty)
    setError('')
  }

  return (
    <>
      <h2>Produtos</h2>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit} className="row g-2 mb-4 align-items-end">
        <div className="col-sm">
          <label className="form-label">Código</label>
          <input name="code" className="form-control" value={form.code} onChange={handleChange} required />
        </div>
        <div className="col-sm">
          <label className="form-label">Nome</label>
          <input name="name" className="form-control" value={form.name} onChange={handleChange} required />
        </div>
        <div className="col-sm">
          <label className="form-label">Valor (R$)</label>
          <input name="value" type="number" step="0.01" min="0.01" className="form-control" value={form.value} onChange={handleChange} required />
        </div>
        <div className="col-auto">
          <button type="submit" className="btn btn-primary">{editingId ? 'Atualizar' : 'Criar'}</button>
          {editingId && <button type="button" className="btn btn-secondary ms-2" onClick={handleCancel}>Cancelar</button>}
        </div>
      </form>

      {loading ? (
        <p>Carregando...</p>
      ) : items.length === 0 ? (
        <p className="text-muted">Nenhum produto cadastrado.</p>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nome</th>
                <th>Valor</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map(item => (
                <tr key={item.id}>
                  <td>{item.code}</td>
                  <td><Link to={`/products/${item.id}`}>{item.name}</Link></td>
                  <td>R$ {Number(item.value).toFixed(2)}</td>
                  <td className="action-btns">
                    <button className="btn btn-sm btn-outline-primary me-1" onClick={() => handleEdit(item)}>Editar</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(item.id)}>Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}

export default ProductList
