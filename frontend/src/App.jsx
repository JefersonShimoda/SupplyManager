import { Routes, Route, NavLink } from 'react-router-dom'
import ProductList from './pages/ProductList'
import ProductDetail from './pages/ProductDetail'
import RawMaterialList from './pages/RawMaterialList'
import ProductionSuggestion from './pages/ProductionSuggestion'

function App() {
  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
        <div className="container">
          <NavLink className="navbar-brand" to="/">SupplyManager</NavLink>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="nav">
            <ul className="navbar-nav">
              <li className="nav-item">
                <NavLink className="nav-link" to="/">Produtos</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/raw-materials">Matérias-Primas</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/production">Sugestão de Produção</NavLink>
              </li>
            </ul>
          </div>
        </div>
      </nav>
      <div className="container">
        <Routes>
          <Route path="/" element={<ProductList />} />
          <Route path="/products/:id" element={<ProductDetail />} />
          <Route path="/raw-materials" element={<RawMaterialList />} />
          <Route path="/production" element={<ProductionSuggestion />} />
        </Routes>
      </div>
    </>
  )
}

export default App
