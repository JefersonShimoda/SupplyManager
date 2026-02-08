import { useState, useEffect } from 'react'
import { getRawMaterials, createRawMaterial, updateRawMaterial, deleteRawMaterial } from '../api'

const empty = { code: '', name: '', stockQuantity: '' }

function RawMaterialList() {
  const [items, setItems] = useState([])
  const [form, setForm] = useState(empty)
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    getRawMaterials()
      .then(setItems)
      .catch(() => setError('Erro ao carregar matérias-primas'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const data = { ...form, stockQuantity: parseFloat(form.stockQuantity) }
    try {
      if (editingId) {
        await updateRawMaterial(editingId, data)
      } else {
        await createRawMaterial(data)
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
    setForm({ code: item.code, name: item.name, stockQuantity: String(item.stockQuantity) })
    setError('')
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Deseja excluir esta matéria-prima?')) return
    try {
      await deleteRawMaterial(id)
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
      <h2>Matérias-Primas</h2>

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
          <label className="form-label">Estoque</label>
          <input name="stockQuantity" type="number" step="0.0001" min="0" className="form-control" value={form.stockQuantity} onChange={handleChange} required />
        </div>
        <div className="col-auto">
          <button type="submit" className="btn btn-primary">{editingId ? 'Atualizar' : 'Criar'}</button>
          {editingId && <button type="button" className="btn btn-secondary ms-2" onClick={handleCancel}>Cancelar</button>}
        </div>
      </form>

      {loading ? (
        <p>Carregando...</p>
      ) : items.length === 0 ? (
        <p className="text-muted">Nenhuma matéria-prima cadastrada.</p>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nome</th>
                <th>Estoque</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map(item => (
                <tr key={item.id}>
                  <td>{item.code}</td>
                  <td>{item.name}</td>
                  <td>{item.stockQuantity}</td>
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

export default RawMaterialList
