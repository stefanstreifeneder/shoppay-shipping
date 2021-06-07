/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shoppay.shipping.web;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import shoppay.shipping.session.UserBean;
import shoppayentity.entity.*;
import shoppay.shipping.qualifiers.LoggedIn;
import shoppay.shipping.web.util.JsfUtil;



@ManagedBean
@SessionScoped
@DeclareRoles({"admin, user"})
@RolesAllowed({"admin, user"})
@ServletSecurity(
 @HttpConstraint(transportGuarantee = TransportGuarantee.CONFIDENTIAL,
    rolesAllowed = {"admin, user"}))
public class UserController implements Serializable{    
    
    private static final long serialVersionUID = 3254181235309041386L;    
    
    @Inject
    private UserBean requestBean;
    
    /**
     * Property of the class.
     */
    private String email;
    
    /**
     * Property of the class.
     */
    private String password;    
    
    private Person user;

    
    @RolesAllowed({"admin, user"})
    @LoggedIn
    public String login(){         
        //String sideToReturn = "";
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = 
                (HttpServletRequest) context.getExternalContext().getRequest();
        
        System.out.println("UserController, login, email: "
                        + email + " - password: " + password);

        try{
            request.login(email, password);
        } catch (ServletException e) {
                context.addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Login failed!", null));
                return "/loginTest.xhtml";
        }
        
        System.out.println("UserController, after login: "
                + "\n - bean: " + this.requestBean
                + "\n - email: " + email
                + "\n - isUserInRol('admin'): " + request.isUserInRole("admin")
                + "\n - isUserInRol('name'): " + request.isUserInRole("user")
                        );
        
        Person p = 
                (Person)this.requestBean.getPersonByRequest(email);

        String s = p.getDtype();

        if(s.equals("admin")){
            user = (Administrator)this.requestBean.getPersonByRequest(email);
        }else if(s.equals("user")){
            user = (Customer)this.requestBean.getPersonByRequest(email);          
        }

        ExternalContext externalContext = 
                FacesContext.getCurrentInstance().getExternalContext();                
            Map<String, Object> sessionMap = externalContext.getSessionMap();  

        if(user.getDtype().equals("admin")){    
            
            sessionMap.put("admin", (Administrator)user);             

            System.out.println("LoginView, ADMIN login, "
                     + "\nisUserInRol('admin'): "            + request.isUserInRole("admin")
                     + "\nisUserInRol('user'): "         + request.isUserInRole("user")
                     + "\ngetUserPrincipal(): "             + request.getUserPrincipal());       

            JsfUtil.addSuccessMessage("Login Success! Welcome back!");
            return "/admin/index";

        }else if(user.getDtype().equals("user")){

            sessionMap.put("user", (Customer)user);

            System.out.println("UserController, login, "
                     + "\nisUserInRol('student'): "         + request.isUserInRole("user")
                     + "\ngetUserPrincipal(): "             + request.getUserPrincipal());
            
            return "/index";

        }
        return "/admin/index";
    }
        
        
    public String logout() {
        System.out.println("LoginView, logout");
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = 
                (HttpServletRequest) context.getExternalContext().getRequest();
        try {
                this.user = null;
                request.logout();
                // clear the session
                ((HttpSession) context.getExternalContext().getSession(false)).invalidate();
        } catch (ServletException e) {
//                log.log(Level.SEVERE, "Failed to logout user!", e);
               // e.printStackTrace();
        }
        
        System.out.println("LoginView, logout - END");
        return "/loginTest.xhtml";
    }

    @Produces
    @LoggedIn
    public Person getAuthenticatedUser() {
            return user;
    }
    
    public boolean isLoggedIn(){
        return user != null;
    }
    
    public String getEmail() {
            return email;
    }
    
    public void setEmail(String email) {
            this.email = email;
    }
    
     public String getPassword() {
            return password;
    }
    
    public void setPassword(String password) {
            this.password = password;
    }
    

    public boolean isAdmin() {
        if(user.getDtype().equals("admin")){
            return true;
        }
        return false;
    }
    
    
    public String goAdmin() {
        if (isAdmin()) {
            return "/admin/index";
        } else {
            return "index";
        }
    }
    
    public Person getUser() {
        return user;
    }
    
   
}