/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entity.clases.EmpresaProveedor;
import com.entity.clases.Compra;
import java.util.ArrayList;
import java.util.Collection;
import com.entity.clases.Producto;
import com.entity.clases.Proveedor;
import com.jpa.controller.exceptions.IllegalOrphanException;
import com.jpa.controller.exceptions.NonexistentEntityException;
import com.jpa.controller.exceptions.RollbackFailureException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author jhonf
 */
public class ProveedorJpaController implements Serializable {

    public ProveedorJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Proveedor proveedor) throws RollbackFailureException, Exception {
        if (proveedor.getCompraCollection() == null) {
            proveedor.setCompraCollection(new ArrayList<Compra>());
        }
        if (proveedor.getProductoCollection() == null) {
            proveedor.setProductoCollection(new ArrayList<Producto>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            EmpresaProveedor idEmpresa = proveedor.getIdEmpresa();
            if (idEmpresa != null) {
                idEmpresa = em.getReference(idEmpresa.getClass(), idEmpresa.getId());
                proveedor.setIdEmpresa(idEmpresa);
            }
            Collection<Compra> attachedCompraCollection = new ArrayList<Compra>();
            for (Compra compraCollectionCompraToAttach : proveedor.getCompraCollection()) {
                compraCollectionCompraToAttach = em.getReference(compraCollectionCompraToAttach.getClass(), compraCollectionCompraToAttach.getId());
                attachedCompraCollection.add(compraCollectionCompraToAttach);
            }
            proveedor.setCompraCollection(attachedCompraCollection);
            Collection<Producto> attachedProductoCollection = new ArrayList<Producto>();
            for (Producto productoCollectionProductoToAttach : proveedor.getProductoCollection()) {
                productoCollectionProductoToAttach = em.getReference(productoCollectionProductoToAttach.getClass(), productoCollectionProductoToAttach.getId());
                attachedProductoCollection.add(productoCollectionProductoToAttach);
            }
            proveedor.setProductoCollection(attachedProductoCollection);
            em.persist(proveedor);
            if (idEmpresa != null) {
                idEmpresa.getProveedorCollection().add(proveedor);
                idEmpresa = em.merge(idEmpresa);
            }
            for (Compra compraCollectionCompra : proveedor.getCompraCollection()) {
                Proveedor oldIdProveedorOfCompraCollectionCompra = compraCollectionCompra.getIdProveedor();
                compraCollectionCompra.setIdProveedor(proveedor);
                compraCollectionCompra = em.merge(compraCollectionCompra);
                if (oldIdProveedorOfCompraCollectionCompra != null) {
                    oldIdProveedorOfCompraCollectionCompra.getCompraCollection().remove(compraCollectionCompra);
                    oldIdProveedorOfCompraCollectionCompra = em.merge(oldIdProveedorOfCompraCollectionCompra);
                }
            }
            for (Producto productoCollectionProducto : proveedor.getProductoCollection()) {
                Proveedor oldIdProveedorOfProductoCollectionProducto = productoCollectionProducto.getIdProveedor();
                productoCollectionProducto.setIdProveedor(proveedor);
                productoCollectionProducto = em.merge(productoCollectionProducto);
                if (oldIdProveedorOfProductoCollectionProducto != null) {
                    oldIdProveedorOfProductoCollectionProducto.getProductoCollection().remove(productoCollectionProducto);
                    oldIdProveedorOfProductoCollectionProducto = em.merge(oldIdProveedorOfProductoCollectionProducto);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Proveedor proveedor) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Proveedor persistentProveedor = em.find(Proveedor.class, proveedor.getId());
            EmpresaProveedor idEmpresaOld = persistentProveedor.getIdEmpresa();
            EmpresaProveedor idEmpresaNew = proveedor.getIdEmpresa();
            Collection<Compra> compraCollectionOld = persistentProveedor.getCompraCollection();
            Collection<Compra> compraCollectionNew = proveedor.getCompraCollection();
            Collection<Producto> productoCollectionOld = persistentProveedor.getProductoCollection();
            Collection<Producto> productoCollectionNew = proveedor.getProductoCollection();
            List<String> illegalOrphanMessages = null;
            for (Compra compraCollectionOldCompra : compraCollectionOld) {
                if (!compraCollectionNew.contains(compraCollectionOldCompra)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Compra " + compraCollectionOldCompra + " since its idProveedor field is not nullable.");
                }
            }
            for (Producto productoCollectionOldProducto : productoCollectionOld) {
                if (!productoCollectionNew.contains(productoCollectionOldProducto)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Producto " + productoCollectionOldProducto + " since its idProveedor field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idEmpresaNew != null) {
                idEmpresaNew = em.getReference(idEmpresaNew.getClass(), idEmpresaNew.getId());
                proveedor.setIdEmpresa(idEmpresaNew);
            }
            Collection<Compra> attachedCompraCollectionNew = new ArrayList<Compra>();
            for (Compra compraCollectionNewCompraToAttach : compraCollectionNew) {
                compraCollectionNewCompraToAttach = em.getReference(compraCollectionNewCompraToAttach.getClass(), compraCollectionNewCompraToAttach.getId());
                attachedCompraCollectionNew.add(compraCollectionNewCompraToAttach);
            }
            compraCollectionNew = attachedCompraCollectionNew;
            proveedor.setCompraCollection(compraCollectionNew);
            Collection<Producto> attachedProductoCollectionNew = new ArrayList<Producto>();
            for (Producto productoCollectionNewProductoToAttach : productoCollectionNew) {
                productoCollectionNewProductoToAttach = em.getReference(productoCollectionNewProductoToAttach.getClass(), productoCollectionNewProductoToAttach.getId());
                attachedProductoCollectionNew.add(productoCollectionNewProductoToAttach);
            }
            productoCollectionNew = attachedProductoCollectionNew;
            proveedor.setProductoCollection(productoCollectionNew);
            proveedor = em.merge(proveedor);
            if (idEmpresaOld != null && !idEmpresaOld.equals(idEmpresaNew)) {
                idEmpresaOld.getProveedorCollection().remove(proveedor);
                idEmpresaOld = em.merge(idEmpresaOld);
            }
            if (idEmpresaNew != null && !idEmpresaNew.equals(idEmpresaOld)) {
                idEmpresaNew.getProveedorCollection().add(proveedor);
                idEmpresaNew = em.merge(idEmpresaNew);
            }
            for (Compra compraCollectionNewCompra : compraCollectionNew) {
                if (!compraCollectionOld.contains(compraCollectionNewCompra)) {
                    Proveedor oldIdProveedorOfCompraCollectionNewCompra = compraCollectionNewCompra.getIdProveedor();
                    compraCollectionNewCompra.setIdProveedor(proveedor);
                    compraCollectionNewCompra = em.merge(compraCollectionNewCompra);
                    if (oldIdProveedorOfCompraCollectionNewCompra != null && !oldIdProveedorOfCompraCollectionNewCompra.equals(proveedor)) {
                        oldIdProveedorOfCompraCollectionNewCompra.getCompraCollection().remove(compraCollectionNewCompra);
                        oldIdProveedorOfCompraCollectionNewCompra = em.merge(oldIdProveedorOfCompraCollectionNewCompra);
                    }
                }
            }
            for (Producto productoCollectionNewProducto : productoCollectionNew) {
                if (!productoCollectionOld.contains(productoCollectionNewProducto)) {
                    Proveedor oldIdProveedorOfProductoCollectionNewProducto = productoCollectionNewProducto.getIdProveedor();
                    productoCollectionNewProducto.setIdProveedor(proveedor);
                    productoCollectionNewProducto = em.merge(productoCollectionNewProducto);
                    if (oldIdProveedorOfProductoCollectionNewProducto != null && !oldIdProveedorOfProductoCollectionNewProducto.equals(proveedor)) {
                        oldIdProveedorOfProductoCollectionNewProducto.getProductoCollection().remove(productoCollectionNewProducto);
                        oldIdProveedorOfProductoCollectionNewProducto = em.merge(oldIdProveedorOfProductoCollectionNewProducto);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = proveedor.getId();
                if (findProveedor(id) == null) {
                    throw new NonexistentEntityException("The proveedor with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Proveedor proveedor;
            try {
                proveedor = em.getReference(Proveedor.class, id);
                proveedor.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The proveedor with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Compra> compraCollectionOrphanCheck = proveedor.getCompraCollection();
            for (Compra compraCollectionOrphanCheckCompra : compraCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Proveedor (" + proveedor + ") cannot be destroyed since the Compra " + compraCollectionOrphanCheckCompra + " in its compraCollection field has a non-nullable idProveedor field.");
            }
            Collection<Producto> productoCollectionOrphanCheck = proveedor.getProductoCollection();
            for (Producto productoCollectionOrphanCheckProducto : productoCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Proveedor (" + proveedor + ") cannot be destroyed since the Producto " + productoCollectionOrphanCheckProducto + " in its productoCollection field has a non-nullable idProveedor field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            EmpresaProveedor idEmpresa = proveedor.getIdEmpresa();
            if (idEmpresa != null) {
                idEmpresa.getProveedorCollection().remove(proveedor);
                idEmpresa = em.merge(idEmpresa);
            }
            em.remove(proveedor);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Proveedor> findProveedorEntities() {
        return findProveedorEntities(true, -1, -1);
    }

    public List<Proveedor> findProveedorEntities(int maxResults, int firstResult) {
        return findProveedorEntities(false, maxResults, firstResult);
    }

    private List<Proveedor> findProveedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Proveedor.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Proveedor findProveedor(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Proveedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getProveedorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Proveedor> rt = cq.from(Proveedor.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
