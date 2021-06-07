/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shoppay.shipping.web;

import shoppayentity.entity.CustomerOrder;
import shoppay.shipping.ejb.OrderBrowser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Named
@RequestScoped
public class ShippingBean implements Serializable {
    
    @EJB
    private OrderBrowser orderBrowser;
    
    private static final String SERVICE_ENDPOINT =
            "https://localhost:8181/shoppay/services/orders";
    
    private static final String MEDIA_TYPE = MediaType.APPLICATION_JSON;
    
    private static final long serialVersionUID = -2526289536313985021L;
    
    protected Client client;
    
    private Map<String, CustomerOrder> orders;
    

    @PostConstruct
    private void init() {
        client = ClientBuilder.newClient();
    }

    @PreDestroy
    private void clean() {
        client.close();
    }
    

    /**
     * @return the orders
     */
    public Map<String, CustomerOrder> getOrders() {
        return orders;
    }

    /**
     * @param orders the orders to set
     */
    public void setOrders(Map<String, CustomerOrder> orders) {
        this.orders = orders;
    }

    public enum Status {
        PENDING_PAYMENT(2),
        READY_TO_SHIP(3),
        SHIPPED(4),
        CANCELLED_PAYMENT(5),
        CANCELLED_MANUAL(6);        
        private int status;

        private Status(final int pStatus) {
            status = pStatus;
        }

        public int getStatus() {
            return status;
        }
    }

    
    public String getEndpoint() {
        return SERVICE_ENDPOINT;
    }

    
    
    public List<CustomerOrder> listByStatus(final Status status) {
        System.out.println("ShippingBean, listByStatus, status: "
                + status);
        
        // 'queryParam("orderStatus", "4")' - does NOT work
        List<CustomerOrder> entity = (List<CustomerOrder>) client.target(
                 SERVICE_ENDPOINT)
                .queryParam("orderStatus", "4")
                .request(MEDIA_TYPE)
                .get(new GenericType<List<CustomerOrder>>() {
            });     

        return entity;
    }

    
    public void updateOrderStatus(final String messageID, final Status status) {
        // consume message
        
        System.out.println("ShippingBean, updateOrderStatus Start"
                + "\nmessageID: " + messageID
                + "\nstatus: " + status);
        
        CustomerOrder order = orderBrowser.processOrder(messageID);
        
        Response response = response = client.target(SERVICE_ENDPOINT)
                .path("/" + order.getIdCustomerOrder())
                .request(MEDIA_TYPE)
                .put(Entity.text(String.valueOf(status.getStatus())));
    }

    
    public List<String> getPendingOrders() {
        Map<String, CustomerOrder> pendingOrders = orderBrowser.getOrders(); 
        
        
        //test
//        if(pendingOrders != null){
//            for(Map.Entry<String, CustomerOrder> m : pendingOrders.entrySet()){
//                System.out.println("ShippingBean, getPendingOrders, "
//                        + "key: " + m.getKey()
//                        + " - order: " + m.getValue() + " - status: " + 
//                                m.getValue().getOrderStatus());
//            }
//        }
        
        
        if (pendingOrders == null) {
            return null;
        } else {         
            
            setOrders(pendingOrders);
            return new ArrayList<>(getOrders().keySet());
        }
    }
    

    public List<CustomerOrder> getCompletedOrders() {
        return listByStatus(Status.SHIPPED);
    }
}