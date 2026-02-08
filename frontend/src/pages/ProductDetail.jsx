import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  getProduct,
  getProductRawMaterials,
  addProductRawMaterial,
  updateProductRawMaterial,
  removeProductRawMaterial,
  getRawMaterials,
} from '../api'

const emptyAssoc = { rawMaterialId: '', requiredQuantity: '' }

function ProductDetail() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [associations, setAssociations] = useState([])
  const [allMaterials, setAllMaterials] = useState([])
  const [form, setForm] = useState(emptyAssoc)
  const [editingRmId, setEditingRmId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    Promise.all([getProduct(id), getProductRawMaterials(id), getRawMaterials()])
      .then(([p, assocs, mats]) => {
        setProduct(p)
        setAssociations(assocs)
        setAllMaterials(mats)
      })
      .catch(() => setError('Erro ao carregar dados'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [id])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const data = {
      rawMaterialId: parseInt(form.rawMaterialId),
      requiredQuantity: parseFloat(form.requiredQuantity),
    }
    try {
      if (editingRmId) {
        await updateProductRawMaterial(id, editingRmId, data)
      } else {
        await addProductRawMaterial(id, data)
      }
      setForm(emptyAssoc)
      setEditingRmId(null)
      load()
    } catch (err) {
      if (err.body && typeof err.body === 'object' && !err.body.error) {
        setError(Object.values(err.body).join(', '))
      } else {
        setError(err.message)
      }
    }
  }

  const handleEdit = (assoc) => {
    setEditingRmId(assoc.rawMaterialId)
    setForm({
      rawMaterialId: String(assoc.rawMaterialId),
      requiredQuantity: String(assoc.requiredQuantity),
    })
    setError('')
  }

  const handleRemove = async (rawMaterialId) => {
    if (!window.confirm('Remover esta matéria-prima do produto?')) return
    try {
      await removeProductRawMaterial(id, rawMaterialId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleCancel = () => {
    setEditingRmId(null)
    setForm(emptyAssoc)
    setError('')
  }

  if (loading) return <p>Carregando...</p>
  if (!product) return <p>Produto não encontrado.</p>

  const associatedIds = new Set(associations.map(a => a.rawMaterialId))
  const availableMaterials = allMaterials.filter(m => !associatedIds.has(m.id))

  return (
    <>
      <Link to="/" className="btn btn-outline-secondary btn-sm mb-3">Voltar</Link>

      <h2>{product.name}</h2>
      <p><strong>Código:</strong> {product.code} | <strong>Valor:</strong> R$ {Number(product.value).toFixed(2)}</p>

      <hr />
      <h4>Matérias-Primas do Produto</h4>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit} className="row g-2 mb-4 align-items-end">
        <div className="col-sm">
          <label className="form-label">Matéria-Prima</label>
          {editingRmId ? (
            <input
              className="form-control"
              value={allMaterials.find(m => m.id === editingRmId)?.name || ''}
              disabled
            />
          ) : (
            <select name="rawMaterialId" className="form-select" value={form.rawMaterialId} onChange={handleChange} required>
              <option value="">Selecione...</option>
              {availableMaterials.map(m => (
                <option key={m.id} value={m.id}>{m.code} - {m.name}</option>
              ))}
            </select>
          )}
        </div>
        <div className="col-sm">
          <label className="form-label">Qtd. Necessária</label>
          <input name="requiredQuantity" type="number" step="0.0001" min="0.0001" className="form-control" value={form.requiredQuantity} onChange={handleChange} required />
        </div>
        <div className="col-auto">
          <button type="submit" className="btn btn-primary">{editingRmId ? 'Atualizar' : 'Adicionar'}</button>
          {editingRmId && <button type="button" className="btn btn-secondary ms-2" onClick={handleCancel}>Cancelar</button>}
        </div>
      </form>

      {associations.length === 0 ? (
        <p className="text-muted">Nenhuma matéria-prima associada.</p>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nome</th>
                <th>Qtd. Necessária</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {associations.map(a => (
                <tr key={a.rawMaterialId}>
                  <td>{a.rawMaterialCode}</td>
                  <td>{a.rawMaterialName}</td>
                  <td>{a.requiredQuantity}</td>
                  <td className="action-btns">
                    <button className="btn btn-sm btn-outline-primary me-1" onClick={() => handleEdit(a)}>Editar</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleRemove(a.rawMaterialId)}>Remover</button>
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

export default ProductDetail
