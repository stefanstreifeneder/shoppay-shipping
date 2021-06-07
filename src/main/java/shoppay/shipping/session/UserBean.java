/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shoppay.shipping.session;

import shoppayentity.entity.*;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author stefan.streifeneder@gmx.de
 */
@Stateless
public class UserBean extends AbstractFacade<Customer> {
    
    @PersistenceContext
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public Person getUserByEmail(String email) {
        Query createNamedQuery = 
                getEntityManager().createNamedQuery("Person.findByEmail");        
        createNamedQuery.setParameter("email", email);        
        return (Person) createNamedQuery.getSingleResult();
    }
    
    public UserBean() {
        super(Customer.class);
    }
    
     public Object getPersonByRequest(String email){ 
        TypedQuery<Person> query = em.createNamedQuery(
                    "Person.findByEmail", Person.class);
        query.setParameter("email", email);
        Person user = null;
        user = query.getSingleResult();
        System.out.println("UserBean, getPersonByRequest, user: " 
                + query.getResultList().size());
        return user;        
    }

}