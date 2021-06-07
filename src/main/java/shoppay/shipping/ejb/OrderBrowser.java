/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shoppay.shipping.ejb;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import shoppayentity.entity.CustomerOrder;

/**
 * 
 * 
 * 
 * The class is regular created. The instance code is pasted.
 * 
 * 
 * @author stefan.streifeneder@gmx.de
 */
@Stateless
public class OrderBrowser {

    private static final Logger logger = Logger.getLogger(OrderBrowser.class.getCanonicalName());
    
    @Inject
    private JMSContext context;
    
    @Resource(mappedName = "jms/myShopOneQueue")
    private Queue queue;
    
    private QueueBrowser browser;
    
    // helper variable to track the number of messages
    private static int i = 0;
    
    
    
    public Map<String, CustomerOrder> getOrders() {  
        System.out.println("ShopOne - OrderBrowser, getOrders, Start, i: " + i++);
        browser = context.createBrowser(queue);
        Enumeration msgs;
        try {
            msgs = browser.getEnumeration();
            if (!msgs.hasMoreElements()) {
                logger.log(Level.INFO, "No messages on the queue!");
            } else {

                Map<String, CustomerOrder> result = new LinkedHashMap<>();
                while (msgs.hasMoreElements()) {
                    Message msg = (Message) msgs.nextElement();     
//                    logger.log(Level.INFO, "Message ID: {0}", msg.getJMSMessageID());
                    Object order = msg.getBody(Object.class);
                    
                    if(order instanceof CustomerOrder){                        
                        if(!result.containsValue((CustomerOrder)order)){                            
                            try{
                                CustomerOrder cTest = (CustomerOrder)order;
                                if(cTest.getOrderStatus().getStatus().equals("3")){
                                    result.put(msg.getJMSMessageID(), (CustomerOrder)order);
                                }
                            }catch(Exception e){
                                System.out.println("ShopPay - OderBrowser, getOrders, "
                                        + "Exc: " + e.getMessage());
                            }
                        } 
                    }
                }
                return result;
            }
        } catch (JMSException ex) {
            Logger.getLogger(OrderBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    
    public CustomerOrder processOrder(String OrderMessageID) {
        //logger.log(Level.INFO, "Processing Order {0}", OrderMessageID);
        System.out.println("ShopPay - OrderBrowser, processOrder, ordermessageID: "
                                + OrderMessageID);        
        JMSConsumer consumer = context.createConsumer(queue, "JMSMessageID='" 
                    + OrderMessageID + "'");
        CustomerOrder order = consumer.receiveBody(CustomerOrder.class, 1);        
        
        System.out.println("ShopPay - OrderBrowser, processOrder, order: " + order);
        return order;
    }
}