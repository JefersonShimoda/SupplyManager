import { useState, useEffect } from 'react'
import { getProductionSuggestion } from '../api'

function ProductionSuggestion() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    setError('')
    getProductionSuggestion()
      .then(setData)
      .catch(() => setError('Erro ao carregar sugestão de produção'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Sugestão de Produção</h2>
        <button className="btn btn-outline-primary" onClick={load} disabled={loading}>Atualizar</button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <p>Carregando...</p>
      ) : !data || data.producibleProducts.length === 0 ? (
        <p className="text-muted">Nenhum produto pode ser produzido com o estoque atual.</p>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Produto</th>
                  <th>Valor Unitário</th>
                  <th>Qtd. Produzível</th>
                  <th>Valor Total</th>
                </tr>
              </thead>
              <tbody>
                {data.producibleProducts.map(p => (
                  <tr key={p.productId}>
                    <td>{p.productCode}</td>
                    <td>{p.productName}</td>
                    <td>R$ {Number(p.productValue).toFixed(2)}</td>
                    <td>{p.producibleQuantity}</td>
                    <td>R$ {Number(p.totalValue).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="table-dark">
                  <td colSpan="4"><strong>Valor Total de Produção</strong></td>
                  <td><strong>R$ {Number(data.totalProductionValue).toFixed(2)}</strong></td>
                </tr>
              </tfoot>
            </table>
          </div>
        </>
      )}
    </>
  )
}

export default ProductionSuggestion
