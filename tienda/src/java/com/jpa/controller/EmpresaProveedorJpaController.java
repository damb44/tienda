/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.controller;

import com.entity.clases.EmpresaProveedor;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entity.clases.Proveedor;
import com.jpa.controller.exceptions.IllegalOrphanException;
import com.jpa.controller.exceptions.NonexistentEntityException;
import com.jpa.controller.exceptions.RollbackFailureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author jhonf
 */
public class EmpresaProveedorJpaController implements Serializable {

    public EmpresaProveedorJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(EmpresaProveedor empresaProveedor) throws RollbackFailureException, Exception {
        if (empresaProveedor.getProveedorCollection() == null) {
            empresaProveedor.setProveedorCollection(new ArrayList<Proveedor>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Proveedor> attachedProveedorCollection = new ArrayList<Proveedor>();
            for (Proveedor proveedorCollectionProveedorToAttach : empresaProveedor.getProveedorCollection()) {
                proveedorCollectionProveedorToAttach = em.getReference(proveedorCollectionProveedorToAttach.getClass(), proveedorCollectionProveedorToAttach.getId());
                attachedProveedorCollection.add(proveedorCollectionProveedorToAttach);
            }
            empresaProveedor.setProveedorCollection(attachedProveedorCollection);
            em.persist(empresaProveedor);
            for (Proveedor proveedorCollectionProveedor : empresaProveedor.getProveedorCollection()) {
                EmpresaProveedor oldIdEmpresaOfProveedorCollectionProveedor = proveedorCollectionProveedor.getIdEmpresa();
                proveedorCollectionProveedor.setIdEmpresa(empresaProveedor);
                proveedorCollectionProveedor = em.merge(proveedorCollectionProveedor);
                if (oldIdEmpresaOfProveedorCollectionProveedor != null) {
                    oldIdEmpresaOfProveedorCollectionProveedor.getProveedorCollection().remove(proveedorCollectionProveedor);
                    oldIdEmpresaOfProveedorCollectionProveedor = em.merge(oldIdEmpresaOfProveedorCollectionProveedor);
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

    public void edit(EmpresaProveedor empresaProveedor) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            EmpresaProveedor persistentEmpresaProveedor = em.find(EmpresaProveedor.class, empresaProveedor.getId());
            Collection<Proveedor> proveedorCollectionOld = persistentEmpresaProveedor.getProveedorCollection();
            Collection<Proveedor> proveedorCollectionNew = empresaProveedor.getProveedorCollection();
            List<String> illegalOrphanMessages = null;
            for (Proveedor proveedorCollectionOldProveedor : proveedorCollectionOld) {
                if (!proveedorCollectionNew.contains(proveedorCollectionOldProveedor)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Proveedor " + proveedorCollectionOldProveedor + " since its idEmpresa field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Proveedor> attachedProveedorCollectionNew = new ArrayList<Proveedor>();
            for (Proveedor proveedorCollectionNewProveedorToAttach : proveedorCollectionNew) {
                proveedorCollectionNewProveedorToAttach = em.getReference(proveedorCollectionNewProveedorToAttach.getClass(), proveedorCollectionNewProveedorToAttach.getId());
                attachedProveedorCollectionNew.add(proveedorCollectionNewProveedorToAttach);
            }
            proveedorCollectionNew = attachedProveedorCollectionNew;
            empresaProveedor.setProveedorCollection(proveedorCollectionNew);
            empresaProveedor = em.merge(empresaProveedor);
            for (Proveedor proveedorCollectionNewProveedor : proveedorCollectionNew) {
                if (!proveedorCollectionOld.contains(proveedorCollectionNewProveedor)) {
                    EmpresaProveedor oldIdEmpresaOfProveedorCollectionNewProveedor = proveedorCollectionNewProveedor.getIdEmpresa();
                    proveedorCollectionNewProveedor.setIdEmpresa(empresaProveedor);
                    proveedorCollectionNewProveedor = em.merge(proveedorCollectionNewProveedor);
                    if (oldIdEmpresaOfProveedorCollectionNewProveedor != null && !oldIdEmpresaOfProveedorCollectionNewProveedor.equals(empresaProveedor)) {
                        oldIdEmpresaOfProveedorCollectionNewProveedor.getProveedorCollection().remove(proveedorCollectionNewProveedor);
                        oldIdEmpresaOfProveedorCollectionNewProveedor = em.merge(oldIdEmpresaOfProveedorCollectionNewProveedor);
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
                Integer id = empresaProveedor.getId();
                if (findEmpresaProveedor(id) == null) {
                    throw new NonexistentEntityException("The empresaProveedor with id " + id + " no longer exists.");
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
            EmpresaProveedor empresaProveedor;
            try {
                empresaProveedor = em.getReference(EmpresaProveedor.class, id);
                empresaProveedor.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The empresaProveedor with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Proveedor> proveedorCollectionOrphanCheck = empresaProveedor.getProveedorCollection();
            for (Proveedor proveedorCollectionOrphanCheckProveedor : proveedorCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This EmpresaProveedor (" + empresaProveedor + ") cannot be destroyed since the Proveedor " + proveedorCollectionOrphanCheckProveedor + " in its proveedorCollection field has a non-nullable idEmpresa field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(empresaProveedor);
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

    public List<EmpresaProveedor> findEmpresaProveedorEntities() {
        return findEmpresaProveedorEntities(true, -1, -1);
    }

    public List<EmpresaProveedor> findEmpresaProveedorEntities(int maxResults, int firstResult) {
        return findEmpresaProveedorEntities(false, maxResults, firstResult);
    }

    private List<EmpresaProveedor> findEmpresaProveedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(EmpresaProveedor.class));
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

    public EmpresaProveedor findEmpresaProveedor(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(EmpresaProveedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmpresaProveedorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<EmpresaProveedor> rt = cq.from(EmpresaProveedor.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
